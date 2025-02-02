package com.reliaquest.api.model;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T data;
    private String status;
}
