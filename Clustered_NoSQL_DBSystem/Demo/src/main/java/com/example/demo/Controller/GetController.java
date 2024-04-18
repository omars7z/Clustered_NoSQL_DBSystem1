package com.example.demo.Controller;

import com.example.demo.Services.GradeServices;
import com.example.demo.Services.StudentServices;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;

@Controller
public class GetController {

    private StudentServices studentServices = StudentServices.getInstance();
    private GradeServices gradeServices = GradeServices.getInstance();
    private Logger logger = Logger.getLogger(GetController.class.getName());


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("classSystem")
    public String classSystem(HttpSession httpSession, Model model) {
        return "class-system";
    }

    @GetMapping("newUser")
    public String newUser() {
        logger.info("Received request to login new user");
        return "new-user";
    }

    @GetMapping("all-users")
    public String allUsers(Model model, HttpSession httpSession) {
        logger.info("Received request to show all users");
        JSONArray users = studentServices.allActiveUsers(httpSession);
        logger.info("Users: " + users.toString());
        model.addAttribute("users", users);
        return "show-all-users";
    }


    @GetMapping("userRegistration")
    public String userRegistration() {
        logger.info("Received request to register new user");
        return "user-registration";
    }

    @GetMapping("adminRegistration")
    public String adminRegistration() {
        logger.info("Received request to register new admin");
        return "admin-registration";
    }


    @GetMapping("getStudent")
    public String getStudent(HttpSession httpSession, Model model) {
        return "get-student-id";
    }

    @GetMapping("getStudent-grades")
    public String showStudentGrades(Model model, HttpSession httpSession) {
        return "get-student-id-grades-form";
    }


    @GetMapping("showStudentsAccounts")
    public String showStudentsAccounts(HttpSession httpSession, Model model) {

        String students = studentServices.allStudents(httpSession);
        JSONObject response = new JSONObject(students);
        JSONArray studentsArray = new JSONArray(response.get("message").toString());
        logger.info("Students: " + studentsArray.toString());

        model.addAttribute("students", studentsArray);
        return "show-students-accounts";
    }

    @PostMapping("showStudent")
    public String showStudent(@RequestParam String studentAccountID, Model model, HttpSession httpSession) {

        JSONObject student = studentServices.getStudentByID(studentAccountID, httpSession);
        logger.info("Student: " + student.toString());
        model.addAttribute("student", student);
        return "show-student";
    }

    @PostMapping("showGrades")
    public String showGrades(@RequestParam String studentAccountID, Model model, HttpSession httpSession) {
        logger.info("Received request to show grades for student with ID: " + studentAccountID);
        JSONArray grades = gradeServices.getStudentGrades(studentAccountID, httpSession);
        logger.info("Grades: " + grades.toString());
        model.addAttribute("grades", grades);
        return "show-grades";
    }

}
