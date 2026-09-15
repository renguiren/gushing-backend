package com.example.gushingbackend.model.bo;

import java.util.List;
import lombok.Data;

/**
 * 主体参考视频生成业务对象 BO。
 * 后端逻辑处理的中间实体：既承载输入（prompt/subjectReference/duration/resolution），
 * 也承载调用结果（videoUrl/model/taskId/status）。
 * <p>
 * 该对象也是工作流节点间串行/并行传递数据的载体：
 * 例如上游文生图节点输出的 imageUrl 可作为本节点的 subjectReference 输入；
 * 本节点的 videoUrl 又可作为下游视频处理节点的输入。
 */
@Data
public class VideoGenerationBO {

    // ===== 输入 =====

    /** 视频文本描述 */
    private String prompt;

    /** 主体参考图片 URL 列表，必填 */
    private List<String> subjectReference;

    /** 视频时长（秒），6 或 10 */
    private Integer duration;

    /** 视频分辨率，如 1080P / 768P */
    private String resolution;

    // ===== 输出 =====

    /** 异步任务 ID */
    private String taskId;

    /** 任务状态：Submitted / Processing / Success / Failed */
    private String status;

    /** 生成视频的下载 URL */
    private String videoUrl;

    /** 实际使用的模型名称 */
    private String model;
}
