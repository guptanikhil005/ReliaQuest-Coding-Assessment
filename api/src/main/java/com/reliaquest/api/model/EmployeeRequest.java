package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class EmployeeRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @Min(value = 1, message = "Salary must be greater than 0")
    private Integer salary;
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;
    @NotBlank(message = "Title is required")
    private String title;
}
