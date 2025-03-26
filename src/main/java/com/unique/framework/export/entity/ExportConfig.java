package com.unique.framework.export.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 导出模板配置
 * </p>
 *
 * @author haohaounique@163.com
 * @since 2025-03-26 21:32:57
 */
@Getter
@Setter
@ToString
@TableName("export_config")
public class ExportConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型import-导入 export-导出
     */
    private String templateType;

    /**
     * 导出数量限制
     */
    private Integer maxSize;

    /**
     * 最大任务数
     */
    private Integer maxTaskNum;

    /**
     * 请求地址
     */
    private String pageUrl;

    /**
     * 表头
     */
    private String fieldHeader;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 删除表示0-未删除 1-已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String updateUser;
}
