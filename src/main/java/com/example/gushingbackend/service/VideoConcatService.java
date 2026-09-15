package com.example.gushingbackend.service;

import java.util.List;

/**
 * 视频拼接服务接口。
 * <p>
 * 将多个视频片段按顺序拼接为一个完整视频。
 */
public interface VideoConcatService {

    /**
     * 下载多个视频片段并按顺序拼接为一个完整视频。
     *
     * @param segmentUrls 视频片段 URL 列表（按拼接顺序排列）
     * @param outputFilename 输出文件名（不含路径，如 "workflow_xxx.mp4"）
     * @return 拼接后视频的本地文件绝对路径
     */
    String concatenate(List<String> segmentUrls, String outputFilename);
}
