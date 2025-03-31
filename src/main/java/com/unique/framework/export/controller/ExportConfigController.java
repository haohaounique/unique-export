package com.unique.framework.export.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.entity.ExportConfig;
import com.unique.framework.export.service.IExportConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * 导出模板配置 前端控制器
 * </p>
 *
 * @author haohaounique@163.com
 * @since 2025-03-26 21:32:57
 */
@RestController
@RequestMapping("/exportConfig")
public class ExportConfigController {
    @Resource
    private IExportConfigService exportConfigService;

    @PostMapping(value = "/template")
    public RespBody<Object> template(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); //模板名称
        //读取标题和字段
        List<LinkedHashMap<Integer, String>> objects = EasyExcel.read(file.getInputStream()).sheet().headRowNumber(0).doReadSync();
        List<String> headerField = objects.get(0).values().stream().toList();
        List<String> valueField = objects.get(1).values().stream().toList();
        ExportConfig exportConfig = new ExportConfig();
        exportConfig.setId(IdWorker.getId());
        exportConfig.setTemplateCode("unique_export_order");
        exportConfig.setFieldHeader(JSON.toJSONString(headerField));
        exportConfig.setFieldName(JSON.toJSONString(valueField));
        exportConfig.setTemplateName(originalFilename);
        exportConfig.setPageUrl("");
        exportConfigService.save(exportConfig);
        return new RespBody<>(objects);
    }
}
