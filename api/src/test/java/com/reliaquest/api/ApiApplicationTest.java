package com.reliaquest.api;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiApplicationTest {

    @Mock
    private RestTemplate restTemplate;

    private EmployeeService employeeService;

    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate);
        ReflectionTestUtils.setField(employeeService, "baseUrl", BASE_URL);
    }

    @Test
    void getAllEmployees_Success() {
        List<Employee> expectedEmployees = createSampleEmployees();
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(expectedEmployees);

        ResponseEntity<ApiResponse<List<Employee>>> responseEntity =
                ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(BASE_URL),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        
        List<Employee> actualEmployees = employeeService.getAllEmployees();

        
        assertNotNull(actualEmployees);
        assertEquals(expectedEmployees.size(), actualEmployees.size());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void getEmployeeById_Success() {
        
        Employee expectedEmployee = createSampleEmployee();
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(expectedEmployee);

        ResponseEntity<ApiResponse<Employee>> responseEntity =
                ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(BASE_URL + "/1"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        
        Employee actualEmployee = employeeService.getEmployeeById("1");

        
        assertNotNull(actualEmployee);
        assertEquals(expectedEmployee.getId(), actualEmployee.getId());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void createEmployee_Success() {
        
        EmployeeRequest request = createSampleEmployeeRequest();
        Employee expectedEmployee = createSampleEmployee();
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(expectedEmployee);

        ResponseEntity<ApiResponse<Employee>> responseEntity =
                ResponseEntity.ok(apiResponse);

        when(restTemplate.exchange(
                eq(BASE_URL),
                eq(HttpMethod.POST),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        
        Employee actualEmployee = employeeService.createEmployee(request);

        
        assertNotNull(actualEmployee);
        assertEquals(expectedEmployee.getId(), actualEmployee.getId());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void deleteEmployeeById_Success() {
        
        Employee employee = createSampleEmployee();
        ApiResponse<Employee> getResponse = new ApiResponse<>();
        getResponse.setData(employee);

        ApiResponse<Boolean> deleteResponse = new ApiResponse<>();
        deleteResponse.setData(true);

        when(restTemplate.exchange(
                eq(BASE_URL + "/1"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(getResponse));

        when(restTemplate.exchange(
                eq(BASE_URL),
                eq(HttpMethod.DELETE),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        
        String result = employeeService.deleteEmployeeById("1");

        
        assertEquals(employee.getEmployeeName(), result);
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void handleHttpClientException_NotFound() {
        
        HttpClientErrorException exception = HttpClientErrorException
                .create(HttpStatus.NOT_FOUND, "Not found",
                        new HttpHeaders(), null, null);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(exception);

         
        assertThrows(EmployeeNotFoundException.class, () ->
                employeeService.getEmployeeById("1"));
    }

    // Helper methods
    private List<Employee> createSampleEmployees() {
        Employee emp1 = new Employee();
        emp1.setId("1");
        emp1.setEmployeeName("Nikhil");
        emp1.setEmployeeSalary(50000);
        emp1.setEmployeeAge(30);

        Employee emp2 = new Employee();
        emp2.setId("2");
        emp2.setEmployeeName("Rajat");
        emp2.setEmployeeSalary(60000);
        emp2.setEmployeeAge(35);

        return Arrays.asList(emp1, emp2);
    }

    private Employee createSampleEmployee() {
        Employee employee = new Employee();
        employee.setId("1");
        employee.setEmployeeName("Nikhil");
        employee.setEmployeeSalary(50000);
        employee.setEmployeeAge(30);
        return employee;
    }

    private EmployeeRequest createSampleEmployeeRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Nikhil");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");
        return request;
    }
}