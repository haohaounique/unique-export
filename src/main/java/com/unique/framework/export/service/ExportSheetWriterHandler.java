package com.unique.framework.export.service;

import com.alibaba.excel.write.handler.AbstractSheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 当sheet被创建时执行的操作
 */
@Slf4j
public class ExportSheetWriterHandler extends AbstractSheetWriteHandler {

    private Integer headNum;
    private Integer fieldTypeRow;

    public ExportSheetWriterHandler(Integer headNum, Integer fieldTypeRow) {
        this.headNum = headNum;
        this.fieldTypeRow = fieldTypeRow;
    }

    @SneakyThrows
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

//        if (headNum == null || headNum < 1) {
//            return;
//        }
        if (headNum == null) {
            headNum = 0;
            return;
        }

        Sheet sheet = writeSheetHolder.getCachedSheet();
        if (sheet == null) {
            log.warn("未获取到sheet");
            return;
        }

        // 删除字段类型行
        removeFieldTypeRow(sheet, headNum, fieldTypeRow);

        // 删除headRow之后的行
        int lastRowIndex = sheet.getLastRowNum();
        //从header开始删除
//        if (lastRowIndex + 1 <= headNum) {
//            // 没有需要处理的行
//            return;
//        }
        for (int i = headNum; i <= lastRowIndex; i++) {
            removeRow(sheet, i);
        }
    }

    private void removeFieldTypeRow(Sheet sheet, int headRow, Integer fieldTypeRow) {
        if (fieldTypeRow == null || fieldTypeRow > headRow) {
            return;
        }
        removeRow(sheet, fieldTypeRow - 1);
    }

    private void removeRow(Sheet sheet, Integer rowIndex) {
        var row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }
        sheet.removeRow(row);
    }
}