package com.pot.sparkhub.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
}