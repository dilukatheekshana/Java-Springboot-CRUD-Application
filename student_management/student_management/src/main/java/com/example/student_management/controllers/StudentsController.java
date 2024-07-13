package com.example.student_management.controllers;

import com.example.student_management.models.Student;
import com.example.student_management.models.StudentDto;
import com.example.student_management.services.StudentsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentsController {

    @Autowired
    private StudentsRepository repo;

    @GetMapping({"", "/"})
    public String showStudentList(Model model) {
        List<Student> students = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("students", students);
        return "students/index";
    }

    @GetMapping({"/create"})
    public String showCreatePage(Model model) {
        StudentDto studentDto = new StudentDto();
        model.addAttribute("studentDto", studentDto);
        return "students/CreateStudent";
    }

    @PostMapping("/create")
    public String CreateStudent(
            @Valid @ModelAttribute StudentDto studentDto,
            BindingResult result
    ){
        if  (studentDto.getImageFileName().isEmpty()){
            result.addError(new FieldError("studentDto","ImageFileName","The image file is required"));
        }

        if (result.hasErrors()){
            return "students/CreateStudent";
        }

        //save image file
        MultipartFile image = studentDto.getImageFileName();
        Date regAt = new Date();
        String storageFileName = regAt.getTime() + "_" + image.getOriginalFilename();

        try{
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        Student student = new Student();
        student.setName(studentDto.getName());
        student.setIdno(studentDto.getIdno());
        student.setAge(studentDto.getAge());
        student.setGender(studentDto.getGender());
        student.setImageFileName(storageFileName);
        student.setRegAt(regAt);

        repo.save(student);

        return "redirect:/students";
    }

    @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id
    ){

        try {
            Student student = repo.findById(id).get();
            model.addAttribute("student",student);

            StudentDto studentDto = new StudentDto();
            studentDto.setName(student.getName());
            studentDto.setIdno(student.getIdno());
            studentDto.setAge(student.getAge());
            studentDto.setGender(student.getGender());

            model.addAttribute("studentDto", studentDto);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return "redirect:/students";
        }
        return "students/EditStudent";
    }

    @PostMapping("/edit")
    public String updateStudent(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute StudentDto studentDto,
            BindingResult result
    ){
        try {
            Student student = repo.findById(id).get();
            model.addAttribute("student", student);

            if (result.hasErrors()) {
                return "students/EditStudent";
            }

            if (!studentDto.getImageFileName().isEmpty()){
                //delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + student.getImageFileName());

                try{
                    Files.delete(oldImagePath);
                }
                catch (Exception ex){
                    System.out.println("Exception: " + ex.getMessage());
                }

                //save new image file
                MultipartFile image = studentDto.getImageFileName();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                student.setImageFileName(storageFileName);
            }

            student.setName(studentDto.getName());
            student.setIdno(studentDto.getIdno());
            student.setAge(studentDto.getAge());
            student.setGender(studentDto.getGender());

            repo.save(student);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/students";
    }

    @GetMapping("/delete")
    public String deleteStudent(@RequestParam int id){
        try{
            Student student = repo.findById(id).get();

            //delete student image
            Path imagePath = Paths.get("public/images/" + student.getImageFileName());

            try{
                Files.delete(imagePath);
            }
            catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }

            //delete student
            repo.delete(student);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/students";
    }


}
