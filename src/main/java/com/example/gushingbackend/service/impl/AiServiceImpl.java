package com.example.gushingbackend.service.impl;

import com.example.gushingbackend.client.DashScopeClient;
import com.example.gushingbackend.client.DeepSeekClient;
import com.example.gushingbackend.client.MiniMaxClient;
import com.example.gushingbackend.model.bo.ImageGenerationBO;
import com.example.gushingbackend.model.bo.TextGenerationBO;
import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO.ContentDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO.InputDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO.MessageDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO.ParametersDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO.ChoiceDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO.OutputDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO.RespContentDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO.RespMessageDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO.UsageDTO;
import com.example.gushingbackend.model.dto.DeepSeekMessageDTO;
import com.example.gushingbackend.model.dto.DeepSeekReqDTO;
import com.example.gushingbackend.model.dto.DeepSeekRespDTO;
import com.example.gushingbackend.model.dto.DeepSeekRespDTO.DeepSeekChoiceDTO;
import com.example.gushingbackend.model.dto.DeepSeekRespDTO.DeepSeekUsageDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VQueryRespDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitReqDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitRespDTO;
import com.example.gushingbackend.service.AiService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AiService 实现：编排 BO ↔ DTO 转换并调用各 AI 厂商 Client。
 */
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    /** MiniMax 任务最终成功状态 */
    private static final String STATUS_SUCCESS = "Success";
    /** MiniMax 任务失败状态 */
    private static final String STATUS_FAILED = "Failed";

    private final DeepSeekClient deepSeekClient;
    private final DashScopeClient dashScopeClient;
    private final MiniMaxClient miniMaxClient;

    @Override
    public TextGenerationBO textGeneration(TextGenerationBO bo) {
        DeepSeekReqDTO reqDTO = buildTextRequest(bo);
        DeepSeekRespDTO respDTO = deepSeekClient.chatCompletion(reqDTO);
        fillTextResponse(bo, respDTO);
        return bo;
    }

    @Override
    public ImageGenerationBO imageGeneration(ImageGenerationBO bo) {
        DashScopeT2IReqDTO reqDTO = buildImageRequest(bo);
        DashScopeT2IRespDTO respDTO = dashScopeClient.textToImage(reqDTO);
        fillImageResponse(bo, respDTO);
        return bo;
    }

    @Override
    public VideoGenerationBO videoGeneration(VideoGenerationBO bo) {
        // 同步：提交后轮询直至完成或超时
        submitVideoGeneration(bo);
        pollVideoTask(bo);
        return bo;
    }

    @Override
    public VideoGenerationBO submitVideoGeneration(VideoGenerationBO bo) {
        MiniMaxI2VSubmitReqDTO reqDTO = buildVideoRequest(bo);
        MiniMaxI2VSubmitRespDTO submitResp = miniMaxClient.submitTask(reqDTO);
        if (submitResp == null || !StringUtils.hasText(submitResp.getTaskId())) {
            String msg = (submitResp != null && submitResp.getBaseResp() != null)
                    ? submitResp.getBaseResp().getStatusMsg()
                    : "提交 MiniMax 图生视频任务失败";
            throw new RuntimeException(msg);
        }
        bo.setTaskId(submitResp.getTaskId());
        bo.setModel(reqDTO.getModel());
        // 提交后标记为已提交，等待调用方按 taskId 查询最终结果
        bo.setStatus("Submitted");
        return bo;
    }

    @Override
    public VideoGenerationBO queryVideoTask(VideoGenerationBO bo) {
        MiniMaxI2VQueryRespDTO resp = miniMaxClient.queryTask(bo.getTaskId());
        fillVideoResponse(bo, resp);
        // 任务成功且拿到 file_id → 调用 file retrieve 换取视频下载地址
        // MiniMax 查询任务只返回 file_id，视频地址需二次换取
        if (resp != null
                && STATUS_SUCCESS.equals(resp.getStatus())
                && StringUtils.hasText(resp.getFileId())) {
            String downloadUrl = miniMaxClient.retrieveFileUrl(resp.getFileId());
            if (StringUtils.hasText(downloadUrl)) {
                bo.setVideoUrl(downloadUrl);
            }
        }
        return bo;
    }

    // ===== 文生文 =====

    /** 将 BO 输入转换为 DeepSeek 请求 DTO。 */
    private DeepSeekReqDTO buildTextRequest(TextGenerationBO bo) {
        List<DeepSeekMessageDTO> messages = new ArrayList<>();

        if (StringUtils.hasText(bo.getSystemPrompt())) {
            DeepSeekMessageDTO sys = new DeepSeekMessageDTO();
            sys.setRole("system");
            sys.setContent(bo.getSystemPrompt());
            messages.add(sys);
        }

        DeepSeekMessageDTO user = new DeepSeekMessageDTO();
        user.setRole("user");
        user.setContent(bo.getPrompt());
        messages.add(user);

        DeepSeekReqDTO reqDTO = new DeepSeekReqDTO();
        reqDTO.setModel(deepSeekClient.getDefaultModel());
        reqDTO.setMessages(messages);
        reqDTO.setStream(false);
        return reqDTO;
    }

    /** 将 DeepSeek 响应 DTO 写回 BO。 */
    private void fillTextResponse(TextGenerationBO bo, DeepSeekRespDTO respDTO) {
        if (respDTO == null) {
            return;
        }
        bo.setModel(respDTO.getModel());

        if (respDTO.getChoices() != null && !respDTO.getChoices().isEmpty()) {
            DeepSeekChoiceDTO choice = respDTO.getChoices().get(0);
            DeepSeekMessageDTO message = choice.getMessage();
            if (message != null) {
                bo.setContent(message.getContent());
                bo.setRole(message.getRole());
            }
        }

        DeepSeekUsageDTO usage = respDTO.getUsage();
        if (usage != null) {
            bo.setPromptTokens(usage.getPromptTokens());
            bo.setCompletionTokens(usage.getCompletionTokens());
            bo.setTotalTokens(usage.getTotalTokens());
        }
    }

    // ===== 文生图 =====

    /** 将 BO 输入转换为万相文生图请求 DTO。 */
    private DashScopeT2IReqDTO buildImageRequest(ImageGenerationBO bo) {
        // 组装 content：仅一个 text 项承载 prompt
        ContentDTO content = new ContentDTO();
        content.setType("text");
        content.setText(bo.getPrompt());

        MessageDTO message = new MessageDTO();
        message.setRole("user");
        message.setContent(Collections.singletonList(content));

        InputDTO input = new InputDTO();
        input.setMessages(Collections.singletonList(message));

        ParametersDTO parameters = new ParametersDTO();
        // BO 未指定 size 时使用配置默认值
        parameters.setSize(StringUtils.hasText(bo.getSize())
                ? bo.getSize()
                : dashScopeClient.getDefaultT2ISize());
        parameters.setN(bo.getN() != null ? bo.getN() : 1);
        if (StringUtils.hasText(bo.getNegativePrompt())) {
            parameters.setNegativePrompt(bo.getNegativePrompt());
        }
        if (bo.getPromptExtend() != null) {
            parameters.setPromptExtend(bo.getPromptExtend());
        }
        if (bo.getWatermark() != null) {
            parameters.setWatermark(bo.getWatermark());
        }
        if (bo.getSeed() != null) {
            parameters.setSeed(bo.getSeed());
        }

        DashScopeT2IReqDTO reqDTO = new DashScopeT2IReqDTO();
        reqDTO.setModel(dashScopeClient.getDefaultT2IModel());
        reqDTO.setInput(input);
        reqDTO.setParameters(parameters);
        return reqDTO;
    }

    /** 将万相响应 DTO 写回 BO。 */
    private void fillImageResponse(ImageGenerationBO bo, DashScopeT2IRespDTO respDTO) {
        if (respDTO == null) {
            return;
        }
        bo.setRequestId(respDTO.getRequestId());

        OutputDTO output = respDTO.getOutput();
        if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
            ChoiceDTO choice = output.getChoices().get(0);
            RespMessageDTO message = choice.getMessage();
            if (message != null
                    && message.getContent() != null
                    && !message.getContent().isEmpty()) {
                // 文生图场景下取首个 image 内容
                RespContentDTO imageContent = message.getContent().get(0);
                if (imageContent != null) {
                    bo.setImageUrl(imageContent.getImage());
                }
            }
        }

        UsageDTO usage = respDTO.getUsage();
        if (usage != null) {
            bo.setImageCount(usage.getImageCount());
            bo.setTotalTokens(usage.getTotalTokens());
            bo.setOutputSize(usage.getSize());
        }
    }

    // ===== 图生视频 =====

    /** 将 BO 输入转换为 MiniMax 图生视频提交请求 DTO。 */
    private MiniMaxI2VSubmitReqDTO buildVideoRequest(VideoGenerationBO bo) {
        MiniMaxI2VSubmitReqDTO reqDTO = new MiniMaxI2VSubmitReqDTO();
        reqDTO.setPrompt(bo.getPrompt());
        reqDTO.setFirstFrameImage(bo.getFirstFrameImage());
        if (StringUtils.hasText(bo.getLastFrameImage())) {
            reqDTO.setLastFrameImage(bo.getLastFrameImage());
        }
        reqDTO.setModel(miniMaxClient.getDefaultI2VModel());
        reqDTO.setDuration(bo.getDuration() != null ? bo.getDuration() : miniMaxClient.getDefaultI2VDuration());
        if (StringUtils.hasText(bo.getResolution())) {
            reqDTO.setResolution(bo.getResolution());
        } else {
            reqDTO.setResolution(miniMaxClient.getDefaultI2VResolution());
        }
        return reqDTO;
    }

    /**
     * 轮询 MiniMax 任务直到 Success / Failed / 超时。
     * 超时或失败时抛 RuntimeException，BO 中 status/videoUrl 会被更新。
     * 复用 {@link #queryVideoTask} 进行单次查询。
     */
    private void pollVideoTask(VideoGenerationBO bo) {
        long intervalMs = miniMaxClient.getPollIntervalSeconds() * 1000L;
        long deadline = System.currentTimeMillis() + miniMaxClient.getPollMaxSeconds() * 1000L;

        while (System.currentTimeMillis() < deadline) {
            queryVideoTask(bo);
            String status = bo.getStatus();
            if (STATUS_SUCCESS.equals(status)) {
                return;
            }
            if (STATUS_FAILED.equals(status)) {
                throw new RuntimeException("MiniMax 图生视频任务失败，taskId=" + bo.getTaskId());
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("轮询被中断，taskId=" + bo.getTaskId(), e);
            }
        }
        throw new RuntimeException("MiniMax 图生视频任务轮询超时，taskId=" + bo.getTaskId());
    }

    /** 将 MiniMax 查询响应 DTO 的 status 写回 BO。视频下载地址由 queryVideoTask 单独换取。 */
    private void fillVideoResponse(VideoGenerationBO bo, MiniMaxI2VQueryRespDTO resp) {
        if (resp == null) {
            return;
        }
        bo.setStatus(resp.getStatus());
    }
}
