package com.example.student_management.services;

import com.example.student_management.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface StudentsRepository extends JpaRepository<Student, Integer> {

    boolean existsByIdno(String idno);
    List<Student> findByNameContainingIgnoreCase(String name);
    List<Student> findByIdnoContainingIgnoreCase(String idno);
}
