package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_template")
public class ReportTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String reportName;

    private String designContent;

    private String datasetIds;

    private String category;

    private Integer status;

    private Integer isPublic;

    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
