package com.astarhub.astarsquad.Bai9;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public class Assignment {
    
}

class Employee {

}

// Lấy danh sách nhân viên thuộc phòng ban IT.
class bai1 {
    // public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    //     Page<Employee> findByDeparment(String department, Pageable pageable);
    // }
    // public class EmployeeService {
    //     private final EmployeeRepository employeeRepository;
        
    //     public List<Employee> getEmployeesByDepartment(String department, Pageable pageable) {
    //         return employeeRepository.findByDeparment(department, pageable);
    //     }
    // }

    // public class EmployeeController {
    //     private final EmployeeService employeeService;
    //     @GetMapping
    //     public ResponseEntity<ApiResponse<List<Employee>>> getEmployees() {
    //         Pageable pageable = PageRequest.of(2, 10, Sort.by("salary").descending());
    //         return employeeService.getEmployeesByDepartment("IT", pageable);
    //     }
    // }
}
