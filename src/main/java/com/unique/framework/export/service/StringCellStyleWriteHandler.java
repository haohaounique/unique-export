package com.unique.framework.export.service;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

public class StringCellStyleWriteHandler implements CellWriteHandler {
    // 用于存储工作簿对应的字符串样式
    private final Map<Workbook, CellStyle> stringStyleCache = new HashMap<>();


    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (!Boolean.TRUE.equals(isHead)) {
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
            // 从缓存中获取样式，如果不存在则创建新样式
            CellStyle cellStyle = stringStyleCache.computeIfAbsent(workbook, wb -> {
                DataFormat dataFormat = wb.createDataFormat();
                CellStyle style = wb.createCellStyle();
                // 设置数据格式为文本
                style.setDataFormat(dataFormat.getFormat("@"));
                return style;
            });
            cell.setCellStyle(cellStyle);
        }
    }

}