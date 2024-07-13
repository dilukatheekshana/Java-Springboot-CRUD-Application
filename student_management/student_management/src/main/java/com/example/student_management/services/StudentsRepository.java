package com.example.student_management.services;

import com.example.student_management.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentsRepository extends JpaRepository<Student, Integer> {
}
