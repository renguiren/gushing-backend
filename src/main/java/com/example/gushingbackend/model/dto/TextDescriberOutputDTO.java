package com.example.gushingbackend.model.dto;

import java.util.List;
import lombok.Data;

/**
 * 文生文节点（画面描述专家）输出的结构化结果 DTO。
 * <p>
 * 文生文节点按 skill 提示词输出 JSON，本类用于解析该 JSON，
 * 拆分为图片描述与多段视频描述：
 * <ul>
 *   <li>imageDescription：所有片段共享的参考图描述</li>
 *   <li>videoDescriptions：每个 5 秒片段的视频描述列表</li>
 * </ul>
 */
@Data
public class TextDescriberOutputDTO {

    /** 图片描述（静态画面构成，所有片段共享，输入文生图节点） */
    private String imageDescription;

    /** 视频描述列表（每个元素对应一个 5 秒片段的动态描述，输入图生视频节点） */
    private List<String> videoDescriptions;
}
