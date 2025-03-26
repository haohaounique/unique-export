package com.unique.framework.export.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.unique.framework.common.http.exception.GlobalErrorCode;
import com.unique.framework.common.http.exception.GlobalException;
import com.unique.framework.common.http.http.ReqBody;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.controller.req.ExportTaskReq;
import com.unique.framework.export.converter.ExportTaskConverter;
import com.unique.framework.export.entity.ExportConfig;
import com.unique.framework.export.entity.ExportTask;
import com.unique.framework.export.service.IExportConfigService;
import com.unique.framework.export.service.IExportTaskService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * <p>
 * 导出任务 前端控制器
 * </p>
 *
 * @author haohaounique@163.com
 * @since 2025-03-26 21:32:57
 */
@RestController
@RequestMapping("/exportTask")
public class ExportTaskController {


    @Resource
    private IExportTaskService exportTaskService;

    @Resource
    private IExportConfigService exportConfigService;

    @PostMapping(value = "/addExportTask")
    public RespBody<Object> addExportTask(@RequestBody @Valid ReqBody<ExportTaskReq> reqBody) {
        ExportTaskReq exportTaskReq = reqBody.getParam();
        LambdaQueryWrapper<ExportConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExportConfig::getTemplateCode, exportTaskReq.getTemplateCode());
        ExportConfig exportConfig = exportConfigService.getOne(queryWrapper);
        if (Objects.isNull(exportConfig)) {
            throw new GlobalException(GlobalErrorCode.PARAMETER_EXCEPTION, "模板编码不存在");
        }
        ExportTask exportTask = ExportTaskConverter.INSTANCE.toPo(exportTaskReq);
        exportTask.setId(IdWorker.getId());
        exportTask.setTaskCode(System.currentTimeMillis() + "");
        exportTask.setTaskLevel("1");
        exportTask.setTaskName("导出任务");
        exportTask.setTemplateCode("unique_export_order");
        exportTask.setTemplateName(exportConfig.getTemplateName());
        exportTask.setTaskStatus("1");
        exportTask.setTaskLevel("0");
        exportTask.setPageSize(100);
        exportTask.setPageUrl(exportConfig.getPageUrl());
        exportTask.setPageParam(exportTaskReq.getPageParam());
        exportTaskService.save(exportTask);
        return new RespBody<>();
    }
}
