package com.pot.sparkhub.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 存储文件
     * @param file 上传的文件
     * @return 文件的可访问 URL (例如 /uploads/xxxx.jpg)
     */
    String storeFile(MultipartFile file);

    /**
     * 删除文件
     * @param fileUrl 文件的 URL (例如 /uploads/xxxx.jpg)
     */
    void deleteFile(String fileUrl);
}