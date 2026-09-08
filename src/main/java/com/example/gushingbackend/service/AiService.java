package com.example.gushingbackend.service;

import com.example.gushingbackend.model.bo.ImageGenerationBO;
import com.example.gushingbackend.model.bo.TextGenerationBO;
import com.example.gushingbackend.model.bo.VideoGenerationBO;

/**
 * AI 能力服务接口。
 * <p>
 * 当前定义了文生文、文生图、图生视频能力。该接口的方法既是 Controller 的底层支撑，
 * 也可被后续工作流节点内部直接调用（串行/并行），因此入参与返回
 * 统一使用 BO，避免与前端 VO 耦合。
 */
public interface AiService {

    /**
     * 调用 DeepSeek 进行文生文。
     *
     * @param bo 业务对象，至少需提供 prompt（可选 systemPrompt）
     * @return 写入生成结果的业务对象（content/model/usage 等已填充）
     */
    TextGenerationBO textGeneration(TextGenerationBO bo);

    /**
     * 调用阿里云万相进行文生图。
     *
     * @param bo 业务对象，至少需提供 prompt（可选 negativePrompt/size/n 等）
     * @return 写入生成结果的业务对象（imageUrl/model/usage 等已填充）
     */
    ImageGenerationBO imageGeneration(ImageGenerationBO bo);

    /**
     * 调用 MiniMax 进行图生视频。
     * <p>
     * MiniMax 视频生成是异步任务，内部会提交任务并轮询直到完成（或超时），
     * 对调用方表现为同步返回。BO 需提供 firstFrameImage（必填）与 prompt（必填）。
     *
     * @param bo 业务对象，至少需提供 prompt + firstFrameImage
     * @return 写入生成结果的业务对象（videoUrl/model/taskId/status 等已填充）
     */
    VideoGenerationBO videoGeneration(VideoGenerationBO bo);
}
