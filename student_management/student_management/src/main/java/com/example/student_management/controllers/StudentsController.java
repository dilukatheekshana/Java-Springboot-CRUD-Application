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
import java.util.Optional;

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

    @GetMapping("/search")
    public String searchStudents(
            @RequestParam("searchCategory") String searchCategory,
            @RequestParam("query") Optional<String> query,
            Model model) {

        if (query.isEmpty() || query.get().isEmpty()) {
            return "redirect:/students";
        }

        List<Student> students;
        switch (searchCategory) {
            case "id":
                students = repo.findById(Integer.parseInt(query.get())).stream().toList();
                break;
            case "name":
                students = repo.findByNameContainingIgnoreCase(query.get());
                break;
            case "idno":
                students = repo.findByIdnoContainingIgnoreCase(query.get());
                break;
            default:
                students = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
                break;
        }
        model.addAttribute("students", students);
        return "students/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        StudentDto studentDto = new StudentDto();
        model.addAttribute("studentDto", studentDto);
        return "students/CreateStudent";
    }

    @GetMapping("/validateIdno")
    @ResponseBody
    public boolean validateIdno(@RequestParam String idno, @RequestParam Optional<Integer> id) {
        if (id.isPresent()) {
            Student student = repo.findById(id.get()).orElse(null);
            if (student != null && student.getIdno().equals(idno)) {
                return false; // The same ID number belongs to the current student
            }
        }
        return repo.existsByIdno(idno);
    }

    @PostMapping("/create")
    public String createStudent(
            @Valid @ModelAttribute StudentDto studentDto,
            BindingResult result
    ) {
        if (repo.existsByIdno(studentDto.getIdno())) {
            result.addError(new FieldError("studentDto", "idno", "This ID number is already used."));
        }

        if (result.hasErrors()) {
            return "students/CreateStudent";
        }

        Student student = new Student();
        student.setName(studentDto.getName());
        student.setIdno(studentDto.getIdno());
        student.setAge(studentDto.getAge());
        student.setGender(studentDto.getGender());
        Date regAt = new Date();
        student.setRegAt(regAt);

        // save image file if present
        MultipartFile image = studentDto.getImageFileName();
        if (image != null && !image.isEmpty()) {
            String storageFileName = regAt.getTime() + "_" + image.getOriginalFilename();
            try {
                String uploadDir = "public/images/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                student.setImageFileName(storageFileName);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
        } else {
            student.setImageFileName(null);
        }

        repo.save(student);

        return "redirect:/students";
    }

    @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id
    ) {
        try {
            Student student = repo.findById(id).get();
            model.addAttribute("student", student);

            StudentDto studentDto = new StudentDto();
            studentDto.setName(student.getName());
            studentDto.setIdno(student.getIdno());
            studentDto.setAge(student.getAge());
            studentDto.setGender(student.getGender());

            model.addAttribute("studentDto", studentDto);

        } catch (Exception ex) {
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
    ) {
        try {
            Student student = repo.findById(id).get();
            model.addAttribute("student", student);

            if (result.hasErrors()) {
                return "students/EditStudent";
            }

            if (repo.existsByIdno(studentDto.getIdno()) && !student.getIdno().equals(studentDto.getIdno())) {
                result.addError(new FieldError("studentDto", "idno", "This ID number is already used."));
                return "students/EditStudent";
            }

            if (!studentDto.getImageFileName().isEmpty()) {
                // delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + student.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // save new image file
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

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/students";
    }

    @GetMapping("/delete")
    public String deleteStudent(@RequestParam int id) {
        try {
            Student student = repo.findById(id).get();

            // delete student image if exists
            if (student.getImageFileName() != null) {
                Path imagePath = Paths.get("public/images/" + student.getImageFileName());
                try {
                    Files.delete(imagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }
            }

            // delete student
            repo.delete(student);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/students";
    }
}
