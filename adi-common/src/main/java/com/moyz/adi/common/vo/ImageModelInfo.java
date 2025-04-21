package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片模型信息 VO，支持 Builder 模式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageModelInfo {
    private Long modelId;
    private String modelName;
    private String modelTitle;
    private String modelPlatform;
    private Boolean enable;
    private Boolean isFree;
}