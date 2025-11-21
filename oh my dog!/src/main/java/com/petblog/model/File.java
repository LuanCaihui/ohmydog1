package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 文件（博客附件）实体类（对应表：files）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    /**
     * 附件ID（自增主键）
     */
    private Integer fileId;
    /**
     * 附件名称
     */
    private String fileName;
    /**
     * 附件路径（服务器存储路径）
     */
    private String filePath;
    /**
     * 附件类型（如：image/jpeg）
     */
    private String fileType;
    /**
     * 附件大小（单位：字节）
     */
    private Float fileSize;
    /**
     * 附件上传时间
     */
    private Date fileUploadedTime;
    /**
     * 博客ID（外键，关联blogs表，附件所属博客）
     */
    private Integer blogId;
}