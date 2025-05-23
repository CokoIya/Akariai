package com.moyz.adi.common.workflow.data;

import lombok.Data;

@Data
public abstract class NodeIODataContent<T> {

    private String title;

    private Integer type;

    private T value;
}
