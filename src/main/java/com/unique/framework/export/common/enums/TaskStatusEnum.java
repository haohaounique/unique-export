package com.unique.framework.export.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * description:0-init 1-processing 2-proceed 3-exception 4-failed
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {


    INITIAL("0", "初始"),
    PROCESSING("1", "执行中"),
    COMPLETE("2", "已执行"),
    EXCEPTION("3", "异常"),
    FAILED("4", "失败")
    ;


    private final String code;

    private final String name;

}
