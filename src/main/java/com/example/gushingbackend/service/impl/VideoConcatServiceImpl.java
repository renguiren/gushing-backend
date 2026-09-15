package com.example.gushingbackend.service.impl;

import com.example.gushingbackend.config.WorkflowProperties;
import com.example.gushingbackend.service.VideoConcatService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * VideoConcatService 实现：通过 FFmpeg 命令行拼接视频片段。
 * <p>
 * 流程：下载所有片段到临时目录 → 生成 concat 列表文件 → 调用 FFmpeg concat 协议拼接 →
 * 输出到配置的视频存储目录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoConcatServiceImpl implements VideoConcatService {

    private final WorkflowProperties workflowProperties;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public String concatenate(List<String> segmentUrls, String outputFilename) {
        if (segmentUrls == null || segmentUrls.isEmpty()) {
            throw new IllegalArgumentException("视频片段列表为空，无法拼接");
        }

        Path tempDir = null;
        try {
            // 1. 创建临时目录存放下载的片段
            tempDir = Files.createTempDirectory("gushing-concat-");
            List<Path> segmentFiles = new ArrayList<>();

            // 2. 下载所有片段
            for (int i = 0; i < segmentUrls.size(); i++) {
                String url = segmentUrls.get(i);
                Path segmentFile = tempDir.resolve("segment_" + i + ".mp4");
                downloadVideo(url, segmentFile);
                segmentFiles.add(segmentFile);
                log.info("下载片段 {}/{} 完成: {}", i + 1, segmentUrls.size(), segmentFile.getFileName());
            }

            // 3. 生成 FFmpeg concat 列表文件
            Path listFile = tempDir.resolve("concat_list.txt");
            StringBuilder listContent = new StringBuilder();
            for (Path seg : segmentFiles) {
                // FFmpeg concat 协议要求使用单引号包裹路径，路径中的单引号需转义
                String escaped = seg.toAbsolutePath().toString().replace("'", "'\\''");
                listContent.append("file '").append(escaped).append("'\n");
            }
            Files.writeString(listFile, listContent.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 4. 确保输出目录存在
            Path outputDir = Path.of(workflowProperties.getVideoOutputDir()).toAbsolutePath();
            Files.createDirectories(outputDir);
            Path outputFile = outputDir.resolve(outputFilename);

            // 5. 调用 FFmpeg 拼接
            runFfmpegConcat(listFile, outputFile);

            log.info("视频拼接完成，输出文件: {}", outputFile);
            return outputFile.toAbsolutePath().toString();

        } catch (IOException | InterruptedException e) {
            log.error("视频拼接失败", e);
            throw new RuntimeException("视频拼接失败: " + e.getMessage(), e);
        } finally {
            // 6. 清理临时目录（下载的片段和列表文件）
            if (tempDir != null) {
                deleteDirectoryQuietly(tempDir);
            }
        }
    }

    /**
     * 下载视频文件到指定路径。
     */
    private void downloadVideo(String url, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(workflowProperties.getDownloadTimeoutSeconds()))
                .GET()
                .build();

        HttpResponse<Path> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofFile(target));

        if (response.statusCode() != 200) {
            throw new IOException("下载视频失败，HTTP 状态码: " + response.statusCode()
                    + ", URL: " + url);
        }
    }

    /**
     * 调用 FFmpeg concat 协议拼接视频。
     * 使用 -c copy 直接拷贝流，速度快且无质量损失（要求所有片段编码参数一致）。
     */
    private void runFfmpegConcat(Path listFile, Path outputFile) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(workflowProperties.getFfmpegPath());
        command.add("-y");                        // 覆盖已存在文件
        command.add("-f");                        // 指定输入格式
        command.add("concat");
        command.add("-safe");
        command.add("0");                         // 允许绝对路径
        command.add("-i");
        command.add(listFile.toAbsolutePath().toString());
        command.add("-c");
        command.add("copy");                      // 直接拷贝流，不重新编码
        command.add(outputFile.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 读取 FFmpeg 输出（合并到日志）
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("FFmpeg 执行失败，退出码: {}，输出: {}", exitCode, output);
            throw new RuntimeException("FFmpeg 拼接失败，退出码: " + exitCode);
        }
        log.debug("FFmpeg 拼接成功: {}", output);
    }

    /**
     * 递归删除目录（静默忽略错误）。
     */
    private void deleteDirectoryQuietly(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("清理临时文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("遍历临时目录失败: {}", dir, e);
        }
    }
}
