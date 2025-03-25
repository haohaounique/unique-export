package com.unique.framework.export.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.entity.ExcelTemplate;
import com.unique.framework.export.service.IExcelTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * date:2025/3/25 21:57
 * author: haohaounique@163.com
 */
@RestController
@RequestMapping(value = "/excel/upload")
@Slf4j
public class ExcelUploadController {

    @Resource
    private IExcelTemplateService excelTemplateService;

    @PostMapping(value = "/template")
    public RespBody<Object> template(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); //模板名称
        List<LinkedHashMap<Integer, String>> objects = EasyExcel.read(file.getInputStream()).sheet().headRowNumber(0).doReadSync();

        List<String> headerField = objects.get(0).values().stream().toList();
        List<String> valueField = objects.get(1).values().stream().toList();
        ExcelTemplate excelTemplate = new ExcelTemplate();
        excelTemplate.setId(IdWorker.getId());
        excelTemplate.setTemplateCode("unique_export_order");
        excelTemplate.setFieldHeader(headerField.toString());
        excelTemplate.setFieldName(valueField.toString());
        excelTemplate.setTemplateName(originalFilename);
        excelTemplate.setReqUrl("");
        excelTemplateService.save(excelTemplate);
        return new RespBody<>(objects);
    }
}
