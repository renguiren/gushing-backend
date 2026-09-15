package com.example.gushingbackend.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * MiniMax V2 多模态视频生成提交请求 DTO。
 * 接口：POST /v2/video_generation
 * <p>
 * 使用 content[] 数组传多模态输入：1 个 text 元素描述视频 + N 个 image_url 元素作参考图。
 * 字段映射遵循 MiniMax V2 接口的 snake_case 约定。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniMaxI2VSubmitReqDTO {

    /** 模型名称，如 MiniMax-H3 */
    private String model;

    /** 多模态输入数组：1 个 text + 参考图 image_url */
    private List<ContentElementDTO> content;

    /** 视频时长（秒），4~15 整数 */
    private Integer duration;

    /** 视频分辨率，如 768P / 2K */
    private String resolution;

    /** 画面宽高比，默认 adaptive；可显式指定 16:9 / 9:16 / 1:1 等 */
    private String ratio;

    /**
     * content 数组元素 DTO。
     * type=text 时填 text；type=image_url 时填 image_url + role。
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentElementDTO {
        /** 元素类型：text / image_url */
        private String type;
        /** 文本描述（type=text 时使用） */
        private String text;
        /** 图片信息（type=image_url 时使用） */
        @JsonProperty("image_url")
        private ImageUrlDTO imageUrl;
        /** 素材用途：reference_image / first_frame / last_frame（type=image_url 时使用） */
        private String role;
    }

    /**
     * 图片 URL 包装 DTO。
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageUrlDTO {
        /** 图片 URL */
        private String url;
    }
}
