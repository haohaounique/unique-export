package com.unique.framework.export.converter;

import com.unique.framework.export.controller.req.ExportTaskReq;
import com.unique.framework.export.entity.ExportTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * date:2025/3/26 21:51
 * author: haohaounique@163.com
 */
@Mapper
public interface ExportTaskConverter {
    ExportTaskConverter INSTANCE = Mappers.getMapper(ExportTaskConverter.class);

    ExportTask toPo(ExportTaskReq param);
}
