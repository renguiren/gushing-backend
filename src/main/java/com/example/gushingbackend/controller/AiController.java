package com.example.gushingbackend.controller;

import com.example.gushingbackend.model.bo.ImageGenerationBO;
import com.example.gushingbackend.model.bo.TextGenerationBO;
import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.vo.ImageGenerationReqVO;
import com.example.gushingbackend.model.vo.ImageGenerationRespVO;
import com.example.gushingbackend.model.vo.TextGenerationReqVO;
import com.example.gushingbackend.model.vo.TextGenerationRespVO;
import com.example.gushingbackend.model.vo.VideoGenerationReqVO;
import com.example.gushingbackend.model.vo.VideoGenerationRespVO;
import com.example.gushingbackend.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力 RESTful 接口。
 * <p>
 * 仅负责 VO ↔ BO 的转换与参数校验，业务逻辑下沉到 Service。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 文生文接口。
     * <p>
     * POST /api/ai/text-generation
     *
     * @param reqVO 前端入参（prompt 必填，systemPrompt 可选）
     * @return 生成内容、模型与 token 用量
     */
    @PostMapping("/text-generation")
    public TextGenerationRespVO textGeneration(@Valid @RequestBody TextGenerationReqVO reqVO) {
        TextGenerationBO bo = toTextBO(reqVO);
        aiService.textGeneration(bo);
        return toTextVO(bo);
    }

    /**
     * 文生图接口。
     * <p>
     * POST /api/ai/image-generation
     *
     * @param reqVO 前端入参（prompt 必填，negativePrompt/size/n/seed 可选）
     * @return 生成图片 URL、模型与用量
     */
    @PostMapping("/image-generation")
    public ImageGenerationRespVO imageGeneration(@Valid @RequestBody ImageGenerationReqVO reqVO) {
        ImageGenerationBO bo = toImageBO(reqVO);
        aiService.imageGeneration(bo);
        return toImageVO(bo);
    }

    /**
     * 图生视频接口。
     * <p>
     * POST /api/ai/video-generation
     * <p>
     * 由于 MiniMax 视频生成为异步任务，该接口在内部提交后轮询直至任务完成，
     * 对调用方表现为同步返回（受轮询最大等待时长限制）。
     *
     * @param reqVO 前端入参（prompt + firstFrameImage 必填，lastFrameImage/duration/resolution 可选）
     * @return 生成视频 URL、模型、taskId 与状态
     */
    @PostMapping("/video-generation")
    public VideoGenerationRespVO videoGeneration(@Valid @RequestBody VideoGenerationReqVO reqVO) {
        VideoGenerationBO bo = toVideoBO(reqVO);
        aiService.videoGeneration(bo);
        return toVideoVO(bo);
    }

    // ===== 文生文 =====

    private TextGenerationBO toTextBO(TextGenerationReqVO reqVO) {
        TextGenerationBO bo = new TextGenerationBO();
        bo.setPrompt(reqVO.getPrompt());
        bo.setSystemPrompt(reqVO.getSystemPrompt());
        return bo;
    }

    private TextGenerationRespVO toTextVO(TextGenerationBO bo) {
        TextGenerationRespVO vo = new TextGenerationRespVO();
        vo.setContent(bo.getContent());
        vo.setModel(bo.getModel());
        vo.setRole(bo.getRole());
        vo.setPromptTokens(bo.getPromptTokens());
        vo.setCompletionTokens(bo.getCompletionTokens());
        vo.setTotalTokens(bo.getTotalTokens());
        return vo;
    }

    // ===== 文生图 =====

    private ImageGenerationBO toImageBO(ImageGenerationReqVO reqVO) {
        ImageGenerationBO bo = new ImageGenerationBO();
        bo.setPrompt(reqVO.getPrompt());
        bo.setNegativePrompt(reqVO.getNegativePrompt());
        bo.setSize(reqVO.getSize());
        bo.setN(reqVO.getN());
        bo.setPromptExtend(reqVO.getPromptExtend());
        bo.setWatermark(reqVO.getWatermark());
        bo.setSeed(reqVO.getSeed());
        return bo;
    }

    private ImageGenerationRespVO toImageVO(ImageGenerationBO bo) {
        ImageGenerationRespVO vo = new ImageGenerationRespVO();
        vo.setImageUrl(bo.getImageUrl());
        vo.setModel(bo.getModel());
        vo.setImageCount(bo.getImageCount());
        vo.setTotalTokens(bo.getTotalTokens());
        vo.setOutputSize(bo.getOutputSize());
        vo.setRequestId(bo.getRequestId());
        return vo;
    }

    // ===== 图生视频 =====

    private VideoGenerationBO toVideoBO(VideoGenerationReqVO reqVO) {
        VideoGenerationBO bo = new VideoGenerationBO();
        bo.setPrompt(reqVO.getPrompt());
        bo.setFirstFrameImage(reqVO.getFirstFrameImage());
        bo.setLastFrameImage(reqVO.getLastFrameImage());
        bo.setDuration(reqVO.getDuration());
        bo.setResolution(reqVO.getResolution());
        return bo;
    }

    private VideoGenerationRespVO toVideoVO(VideoGenerationBO bo) {
        VideoGenerationRespVO vo = new VideoGenerationRespVO();
        vo.setVideoUrl(bo.getVideoUrl());
        vo.setModel(bo.getModel());
        vo.setTaskId(bo.getTaskId());
        vo.setStatus(bo.getStatus());
        return vo;
    }
}
