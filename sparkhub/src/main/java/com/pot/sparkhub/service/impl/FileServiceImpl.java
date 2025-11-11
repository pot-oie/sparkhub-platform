package com.pot.sparkhub.service.impl;

import com.pot.sparkhub.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Path rootLocation; // 物理路径
    private final String virtualPath; // URL 路径
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    public FileServiceImpl(@Value("${sparkhub.upload.physical-path}") String physicalPath,
                           @Value("${sparkhub.upload.virtual-path}") String virtualPath) {

        this.rootLocation = Paths.get(physicalPath);
        this.virtualPath = virtualPath;

        // (启动时) 检查并创建物理文件夹
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory!", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                throw new RuntimeException("文件为空或文件名无效。");
            }

            // 1. 生成唯一文件名 (防止覆盖)
            String originalFilename = file.getOriginalFilename();
            // 获取文件扩展名 (e.g., .jpg)
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // (e.g., af21-b3ed-4c1b-a8d9.jpg)
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 2. 确定文件的完整物理保存路径
            Path destinationFile = this.rootLocation.resolve(uniqueFilename);

            // 3. 将文件流复制到目标路径
            Files.copy(file.getInputStream(), destinationFile);

            // 4. 返回可供前端访问的 *URL*
            // (e.g., /uploads/af21-b3ed-4c1b-a8d9.jpg)
            return this.virtualPath + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败。", e);
        }
    }

    /**
     * 根据 URL 删除物理文件
     * (e.g., /uploads/af21-b3ed-4c1b-a8d9.jpg)
     */
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(this.virtualPath)) {
            log.warn("尝试删除无效或非本项目上传的文件: {}", fileUrl);
            return;
        }

        // 1. 从 URL 中解析出唯一的文件名
        String filename = fileUrl.substring(this.virtualPath.length());

        // 2. 确定文件的完整物理路径
        Path filePath = this.rootLocation.resolve(filename);

        try {
            // 3. 检查文件是否存在并删除
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("成功删除文件: {}", filename);
            } else {
                log.warn("尝试删除的文件不存在: {}", filename);
            }
        } catch (IOException e) {
            // 记录错误，但不抛出运行时异常，以免中断主业务事务
            log.error("删除物理文件失败: {}", filename, e);
        }
    }
}