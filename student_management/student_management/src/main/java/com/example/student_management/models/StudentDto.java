package com.example.student_management.models;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class StudentDto {
    @NotEmpty(message = "The Name is required")
    private String name;

    @NotEmpty(message = "The ID card Number is required")
    private String idno;

    @NotEmpty(message = "Gender is required")
    private String gender;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;

    private MultipartFile imageFileName;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public MultipartFile getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(MultipartFile imageFileName) {
        this.imageFileName = imageFileName;
    }
}
