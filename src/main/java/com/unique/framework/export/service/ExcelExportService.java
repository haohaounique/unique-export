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
import com.unique.framework.common.http.exception.GlobalErrorCode;
import com.unique.framework.common.http.exception.GlobalException;
import com.unique.framework.common.http.http.PageQuery;
import com.unique.framework.common.http.http.PageResult;
import com.unique.framework.common.http.http.ReqBody;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.common.enums.TaskStatusEnum;
import com.unique.framework.export.entity.ExportConfig;
import com.unique.framework.export.entity.ExportTask;
import com.unique.framework.redis.util.LockUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * date:2025/3/26 22:14
 * author: haohaounique@163.com
 */
@Service
@Slf4j
public class ExcelExportService {
    //system lock
    public static final String LOCK_KEY = "unique-export:lock:%s";

    //template lock
    public static final String TEMPLATE_CODE_LOCK_N = "%s_%s";
    @Resource
    private IExportTaskService exportTaskService;

    @Resource
    private IExportConfigService exportConfigService;

    @Resource
    private RedissonClient redissonClient;

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
        RLock templateLock = getLock(exportConfig.getTemplateCode(), exportConfig.getMaxTaskNum());
        if (Objects.isNull(templateLock)) {
            log.info("task full,{},{}", exportTask.getTaskCode(), exportTask.getTemplateCode());
            return;
        }
//        1. get redis lock
        RLock lock = redissonClient.getLock(String.format(LOCK_KEY, exportTask.getId()));
//        The default maximum time for Excel export execution is 30 minutes. If other threads seize the task generation, it will not affect the final result, but it will call more resources
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(0, 1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.error("get lock exception", e);
        }
        if (!tryLock) {
            LockUtils.unlock(templateLock);
            return;
        }
        if (Objects.equals(exportTask.getTaskStatus(), TaskStatusEnum.COMPLETE.getCode())) {
            LockUtils.unlock(templateLock);
            LockUtils.unlock(lock);
            return;
        }

        try {
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
            List<String> feildHeaderList = OBJECT_MAPPER.readValue(exportConfig.getFieldHeader(), new TypeReference<>() {
            });

            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ) {
                ExportSheetWriterHandler exportSheetWriterHandler = new ExportSheetWriterHandler(0, 2);
                ExcelWriterBuilder writeBuilder = EasyExcelFactory.write(byteArrayOutputStream)
                        .autoCloseStream(true)

                        .registerWriteHandler(exportSheetWriterHandler);
                //write template
                writeBuilder.withTemplate(inputStream);
                // create sheet
                ExcelWriter excelWriter = writeBuilder.build();
                WriteSheet sheet = new ExcelWriterSheetBuilder(excelWriter).build();
                //fill data 或者是直接填充标题头
                List<List<String>> firstRow = new ArrayList<>();
                firstRow.add(feildHeaderList);
                excelWriter.write(firstRow, sheet);
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
        } catch (GlobalException e) {
            throw new GlobalException(GlobalErrorCode.PARAMETER_EXCEPTION, "export task failed", e);
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorCode.PARAMETER_EXCEPTION, "导出失败");
        } finally {
            LockUtils.unlock(templateLock);
            LockUtils.unlock(lock);
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


    private RLock getLock(String templateCode, int taskThreadNum) {
        for (int i = 1; i <= taskThreadNum; i++) {
            String lockName = String.format(LOCK_KEY, String.format(TEMPLATE_CODE_LOCK_N, templateCode, i));
            RLock lock = redissonClient.getLock(lockName);
            boolean tryLock = false;
            try {
                tryLock = lock.tryLock(0, 30, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("thread is interrupted", e);
            }
            if (tryLock) {
                return lock;
            }
        }
        return null;
    }
}
