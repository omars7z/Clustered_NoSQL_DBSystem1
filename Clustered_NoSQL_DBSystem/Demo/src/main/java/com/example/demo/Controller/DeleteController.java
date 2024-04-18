package com.example.demo.Controller;

import com.example.demo.Services.StudentServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;


@Controller
public class DeleteController {

    private StudentServices studentServices = StudentServices.getInstance();
    private Logger logger = Logger.getLogger(DeleteController.class.getName());


    @GetMapping("remove-user")
    public String removeUser(Model model, HttpSession httpSession) {
        return "remove-user-form";
    }

    @PostMapping("removeUser")
    public String removeUser(@RequestParam String token, Model model, HttpSession httpSession) {
        logger.info("Received request to remove user with token: " + token);
        studentServices.removeUser(token, httpSession);
        return "admin-system";
    }

    @GetMapping("removeStudent")
    public String removeStudent(Model model, HttpSession httpSession) {
        return "remove-student-form";
    }

    @PostMapping("removeStudent")
    public String removeStudent(@RequestParam String token, Model model, HttpSession httpSession) {
        logger.info("Received request to remove student with token: " + token);
        studentServices.removeStudent(token, httpSession);
        return "class-system";
    }


}
