package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_version")
public class ReportVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String reportId;

    private String designContent;

    private Integer versionNum;

    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
