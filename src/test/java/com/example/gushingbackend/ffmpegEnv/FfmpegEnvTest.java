package com.example.gushingbackend.ffmpegEnv;

import com.example.gushingbackend.config.WorkflowProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class FfmpegEnvTest {

    @Autowired
    private WorkflowProperties workflowProperties;

    @Test
    void testFfmpegIsCallable() throws Exception {
        String ffmpegBin = workflowProperties.getFfmpegPath();
        System.out.println("ffmpeg路径配置：" + ffmpegBin);

        List<String> command = new ArrayList<>();
        command.add(ffmpegBin);
        command.add("-version");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        System.out.println("ffmpeg输出:\n" + output);
        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg调用失败，exitCode=" + exitCode);
        }
    }
}
