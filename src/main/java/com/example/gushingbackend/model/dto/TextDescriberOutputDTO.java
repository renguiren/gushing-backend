package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * 文生文节点（画面描述专家）输出的结构化结果 DTO。
 * <p>
 * 文生文节点按 skill 提示词输出 JSON，本类用于解析该 JSON，
 * 拆分为图片描述与视频描述，分别供下游文生图、图生视频节点使用。
 */
@Data
public class TextDescriberOutputDTO {

    /** 图片描述（静态画面构成，输入文生图节点） */
    private String imageDescription;

    /** 视频描述（动态演进，输入图生视频节点） */
    private String videoDescription;
}
