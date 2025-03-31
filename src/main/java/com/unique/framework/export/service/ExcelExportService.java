package com.unique.framework.export.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.unique.framework.common.http.http.PageQuery;
import com.unique.framework.common.http.http.PageResult;
import com.unique.framework.common.http.http.ReqBody;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.entity.ExportConfig;
import com.unique.framework.export.entity.ExportTask;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * date:2025/3/26 22:14
 * author: haohaounique@163.com
 */
@Service
@Slf4j
public class ExcelExportService {

    @Resource
    private IExportTaskService exportTaskService;

    @Resource
    private IExportConfigService exportConfigService;

    @Resource(name = "serviceNameRestTemplate")
    private RestTemplate restTemplate;
    private static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(FORMATTER_DATETIME))
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(FORMATTER_DATETIME))
                .serializerByType(LocalDate.class, new LocalDateSerializer(FORMATTER_DATE))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(FORMATTER_DATE))
                .failOnUnknownProperties(false)
                .failOnEmptyBeans(false)
                .build();
    }

    public void exportExcel(String id, InputStream inputStream, HttpServletResponse response) throws Exception {
        ExportTask exportTask = exportTaskService.getById(id);
        if (Objects.isNull(exportTask)) {
            return;
        }
        LambdaQueryWrapper<ExportConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ExportConfig::getTemplateCode, exportTask.getTemplateCode());

        ExportConfig exportConfig = exportConfigService.getOne(lambdaQueryWrapper);
        if (Objects.isNull(exportConfig)) {
            return;
        }

        //系统锁和任务锁

        PageQuery<String> pageQuery = new PageQuery<>();
        pageQuery.setParam(exportTask.getPageParam());
        pageQuery.setSize(exportTask.getPageSize().longValue());
        ReqBody<PageQuery<String>> reqBody = ReqBody.getReqBody(pageQuery);
        RequestEntity<ReqBody> body = RequestEntity.post(new URI(exportTask.getPageUrl())).contentType(MediaType.APPLICATION_JSON).header("Authorization", exportTask.getAuthorizationHeader()).body(reqBody);
        ResponseEntity<RespBody> exchange = restTemplate.exchange(body, RespBody.class);
        PageResult pageResult = JSON.parseObject(JSON.toJSONString(exchange.getBody().getResult()), PageResult.class);
        log.info("result:{}", JSON.toJSONString(pageResult));

        List<String> feildList = OBJECT_MAPPER.readValue(exportConfig.getFieldName(), new TypeReference<>() {
        });

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ) {

            ExcelWriterBuilder writeBuilder = EasyExcelFactory.write(byteArrayOutputStream)
                    .autoCloseStream(true)
                    .registerWriteHandler(new ExportSheetWriterHandler(2, 2));
            //写模板
            writeBuilder.withTemplate(inputStream);

            /**
             * 创建sheet
             */
            ExcelWriter excelWriter = writeBuilder.build();
            /**
             * 此处创建时会执行ExportSheetWriterHandler 的afterSheetCreate 方法
             */
            WriteSheet sheet = new ExcelWriterSheetBuilder(excelWriter).build();
            //预写一行去除填充数据
            excelWriter.write(new ArrayList<>(), sheet);

            List<List<String>> dataList = OBJECT_MAPPER.convertValue(pageResult.getRecords(), new TypeReference<List<Map<String, Object>>>() {
                    }).stream()
                    .map(data -> feildList.stream().map(field -> obtainValue(data, field)).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(dataList)) {
                excelWriter.write(dataList, sheet);
            }

            excelWriter.write(dataList, sheet);

            excelWriter.finish();

            //upload to oss
        }

    }

    private String obtainValue(Map<String, Object> data, String field) {
        if (CharSequenceUtil.isBlank(field)) {
            return "";
        }
        Object value = data.get(field);
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
