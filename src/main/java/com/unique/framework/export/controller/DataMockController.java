package com.unique.framework.export.controller;

import com.unique.framework.common.http.http.PageQuery;
import com.unique.framework.common.http.http.PageResult;
import com.unique.framework.common.http.http.ReqBody;
import com.unique.framework.common.http.http.RespBody;
import com.unique.framework.export.service.ExcelExportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date:2025/3/26 20:30
 * author: haohaounique@163.com
 */
@RestController
@RequestMapping(value = "/data/mock")
public class DataMockController {

    @Resource
    private ExcelExportService excelExportService;

    @RequestMapping(value = "/pageSearch")
    public RespBody<Object> pageSearch(@RequestBody ReqBody<PageQuery<String>> reqBody) {
        PageResult<Map<String, String>> objectPageResult = new PageResult<>();
        objectPageResult.setTotal(1L);
        objectPageResult.setPages(1L);
        objectPageResult.setCurrent(1L);
        objectPageResult.setSize(10L);
        List<Map<String, String>> objects = new ArrayList<>();
        HashMap<String, String> orderMap = new HashMap<>();
        orderMap.put("orderNo", "NO202503260001");
        orderMap.put("orderNum", "5");
        objects.add(orderMap);
        objectPageResult.setRecords(objects);
        return new RespBody<>(objectPageResult);
    }


    @PostMapping(value = "/export/{id}")
    public void export(@PathVariable("id") String id, @RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {
        excelExportService.exportExcel(id, file.getInputStream(),response);
    }

}
