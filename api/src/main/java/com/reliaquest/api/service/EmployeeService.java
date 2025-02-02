package com.reliaquest.api.service;

import com.reliaquest.api.exceptions.ApiException;
import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.RateLimitExceededException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.DeleteEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmployeeService {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY = 1000L;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    @Value("${employee.api.url}")
    private String baseUrl;

    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Retryable(
            value = { RateLimitExceededException.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = BACKOFF_MULTIPLIER)
    )
    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees");
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody().getData();
        } catch (HttpClientErrorException e) {
            handleHttpClientException(e, "Error fetching all employees");
            return null;
        }
    }

    @Retryable(
            value = { RateLimitExceededException.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = BACKOFF_MULTIPLIER)
    )
    public Employee getEmployeeById(String id) {
        log.debug("Fetching employee with id: {}", id);
        try {
            String url = baseUrl + "/" + id;
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody().getData();
        } catch (HttpClientErrorException e) {
            handleHttpClientException(e, "Error fetching employee by id: " + id);
            return null;
        }
    }

    @Retryable(
            value = { RateLimitExceededException.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = BACKOFF_MULTIPLIER)
    )
    public Employee createEmployee(EmployeeRequest employeeRequest) {
        log.debug("Creating new employee: {}", employeeRequest);
        try {
            HttpEntity<EmployeeRequest> request = new HttpEntity<>(employeeRequest, headers);

            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {}
            );

            return response.getBody().getData();
        } catch (HttpClientErrorException e) {
            handleHttpClientException(e, "Error creating employee");
            return null;
        }
    }

    @Retryable(
            value = { RateLimitExceededException.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = BACKOFF_MULTIPLIER)
    )
    public String deleteEmployeeById(String id) {
        log.debug("Deleting employee with id: {}", id);
        try {
            Employee employee = getEmployeeById(id);
            String name = employee.getEmployeeName();
            HttpEntity<DeleteEmployeeRequest> requestEntity = new HttpEntity<>(
                    new DeleteEmployeeRequest(name), headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            boolean deleted = Optional.ofNullable(response.getBody())
                    .map(ApiResponse::getData)
                    .orElse(false);

            if (!deleted) {
                throw new EmployeeNotFoundException("Deletion failed for employee ID: " + id);
            }

            log.info("Successfully deleted employee with id: {}", id);
            return name;
        } catch (HttpClientErrorException e) {
            handleHttpClientException(e, "Error deleting employee with id: " + id);
            return null;
        }
    }

    private void handleHttpClientException(HttpClientErrorException e, String errorMessage) {
        log.error(errorMessage, e);
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new RateLimitExceededException("Rate limit exceeded");
        } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new EmployeeNotFoundException("Employee not found");
        } else {
            throw new ApiException("Unexpected error: " + e.getMessage());
        }
    }
}
