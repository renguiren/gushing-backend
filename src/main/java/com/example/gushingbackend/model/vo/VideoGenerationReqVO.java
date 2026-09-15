package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 主体参考视频生成请求 VO（面向前端）。
 */
@Data
public class VideoGenerationReqVO {

    /** 视频文本描述，必填 */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 2000, message = "提示词长度超出限制")
    private String prompt;

    /** 主体参考图片 URL 列表，必填（至少 1 张） */
    @NotEmpty(message = "主体参考图片不能为空")
    private List<String> subjectReference;

    /** 视频时长（秒），6 或 10；不传使用默认配置 */
    private Integer duration;

    /** 视频分辨率，如 1080P / 768P；不传使用默认配置 */
    private String resolution;
}
