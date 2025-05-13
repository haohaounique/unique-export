package com.unique.framework.export.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.unique.framework.export.service.StringCellStyleWriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * date:2025/5/12 21:19
 * author: haohaounique@163.com
 */
@RestController
@RequestMapping("/excelFill")
public class ExcelFillController {

    /**
     * 测试导出普通方式
     * @param response
     * @throws Exception
     */
    @RequestMapping("/test")
    public void test(HttpServletResponse response) throws Exception {
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("orderNo", "202505120001");
        map.put("orderNum", 101);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\haoha\\Desktop\\订单导出模板.xlsx"));
             ExcelWriter excelWriter = EasyExcel.write(byteArrayOutputStream).withTemplate(fileInputStream).build();
        ) {
            WriteSheet writeSheet = EasyExcel.writerSheet().sheetNo(0).build();
            excelWriter.fill(map, writeSheet);
            excelWriter.finish();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("测试.xlsx", StandardCharsets.UTF_8));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentLength(byteArray.length);
            response.getOutputStream().write(byteArray);
        }
    }

    /**
     * 测试导出普通方式
     * @param response
     * @throws Exception
     */
    @RequestMapping("/moreSheet")
    public void moreSheet(HttpServletResponse response) throws Exception {
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("orderNo", "202505120001");
        map.put("orderNum", 103);
        Map<String, Object> map2 = MapUtils.newHashMap();
        map2.put("orderNo", "202505120001");
        map2.put("orderNum", 104);
        Map<String, Object> map4 = MapUtils.newHashMap();
        map4.put("orderDate", "20250513");
        map4.put("total", "2021");

        Map<String, Object> map3 = MapUtils.newHashMap();
        map3.put("orderNo", "202505120001");
        map3.put("orderNum", 104);
        Map<String, Object> map5 = MapUtils.newHashMap();
        map5.put("orderNo1", "202505120001");
        map5.put("orderNum1", 104);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\haoha\\Desktop\\订单导出模板.xlsx"));
             ExcelWriter excelWriter = EasyExcel.write(byteArrayOutputStream).autoCloseStream(Boolean.TRUE).withTemplate(fileInputStream).build();
        ) {
            WriteSheet writeSheet = EasyExcel.writerSheet().sheetNo(0).registerWriteHandler(new StringCellStyleWriteHandler()).build();
            WriteSheet writeSheet1 = EasyExcel.writerSheet().sheetNo(1).registerWriteHandler(new StringCellStyleWriteHandler()).build();
            excelWriter.fill(map, writeSheet);
            //横向填充集合值
            excelWriter.fill(List.of(map2,map3), writeSheet1);
            //纵向填充 固定值
            excelWriter.fill(map4, writeSheet1);
            //横向填充
            FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
            excelWriter.fill(List.of(map5,map5),fillConfig, writeSheet1);
            //自定义输入参数
            List<List<String>> totalListList = ListUtils.newArrayList();
            List<String> totalList = ListUtils.newArrayList();
            totalListList.add(totalList);
            totalList.add(null);
            totalList.add(null);
            totalList.add(null);
            // 第四列
            totalList.add("统计:1000");
            // 这里是write 别和fill 搞错了
            excelWriter.write(totalListList, writeSheet);
            excelWriter.finish();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("测试.xlsx", StandardCharsets.UTF_8));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentLength(byteArray.length);
            response.getOutputStream().write(byteArray);
        }
    }




}
