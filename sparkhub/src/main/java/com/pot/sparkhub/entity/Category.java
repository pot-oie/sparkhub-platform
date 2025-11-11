package com.pot.sparkhub.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Category implements Serializable {
    // 缓存序列化需要一个 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
}