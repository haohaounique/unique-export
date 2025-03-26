package com.unique.framework.export.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 导出任务
 * </p>
 *
 * @author haohaounique@163.com
 * @since 2025-03-26 21:32:57
 */
@Getter
@Setter
@ToString
@TableName("export_task")
public class ExportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 0-初始 1-执行中 2-已执行 3-执行异常任务状态
     */
    private String taskStatus;

    /**
     * 任务类型0-普通 1-紧急 2-非常紧急
     */
    private String taskLevel;

    /**
     * 分页请求地址
     */
    private String pageUrl;

    /**
     * 分页参数
     */
    private String pageParam;

    /**
     * 认证头
     */
    private String authorizationHeader;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 最大导出数量
     */
    private Integer maxSize;

    /**
     * 任务描述
     */
    private String taskDescription;

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
