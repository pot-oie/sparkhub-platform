package com.pot.sparkhub.controller;

import com.pot.sparkhub.common.Result;
import com.pot.sparkhub.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 专用的文件上传接口
     * (需要登录权限, 任何人登录了都能上传)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_CREATOR', 'ROLE_ADMIN')")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 存储文件并获取 URL
            String fileUrl = fileService.storeFile(file);

            // 2. 将 URL 包装在 Map 中返回
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl); // (前端通过 data.url 获取)

            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}