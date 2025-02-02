package com.reliaquest.api.model;

public class DeleteEmployeeRequest {
    private String name;

    public DeleteEmployeeRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
