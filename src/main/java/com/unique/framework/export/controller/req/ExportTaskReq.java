package com.unique.framework.export.controller.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
* date:2025/3/26 22:02
* author: haohaounique@163.com
*/
@Data
public class ExportTaskReq {
    /**
     * 模板编码
     */
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 分页参数
     */
    @NotNull(message = "分页参数不能为空")
    private String pageParam;
}
