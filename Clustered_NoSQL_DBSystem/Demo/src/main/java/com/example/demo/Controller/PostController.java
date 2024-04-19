package com.example.demo.Controller;

import com.example.demo.Model.ApiResponse;
import com.example.demo.Services.GradeServices;
import com.example.demo.Services.StudentServices;
import com.example.demo.Services.WorkerService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;


@Controller
public class PostController {

    private StudentServices studentServices = StudentServices.getInstance();
    private GradeServices gradeServices = GradeServices.getInstance();
    private WorkerService workerService = WorkerService.getInstance();

    private final ApiResponse apiResponse = new ApiResponse();
    private Logger logger = Logger.getLogger(PostController.class.getName());

    @PostMapping("registerNewUser")
    public String registerNewUser(@RequestParam String username, Model model) {
        logger.info("Received request to register new user " + username);

        JSONObject jsonObject = new JSONObject(studentServices.registerANewUser(username));
        logger.info("User credentials json object from the database: " + jsonObject.toString());
        String token = jsonObject.getString("token");
        int workerPort = jsonObject.getInt("workerPort");

        model.addAttribute("username", username);
        model.addAttribute("token", token);
        model.addAttribute("workerName", workerService.getWorkerName(workerPort));

        workerService.addWorkerPortToToken(token, workerPort);
        return "show-user-credentials";
    }

    @GetMapping("CreateNewAccount")
    public String createNewAccount(HttpSession httpSession, Model model) {
        return "create-account-form";
    }


    @PostMapping("storeNewAccountData")
    public String storeNewAccountData(@RequestParam String studentName, @RequestParam String studentPlace,
                                      @RequestParam String studentPhone, @RequestParam int GPA,
                                      Model model, HttpSession httpSession) {

        logger.info("Received request to store new account data");
        logger.info("Student name: " + studentName);
        logger.info("Place: " + studentPlace);
        logger.info("Phone: " + studentPhone);
        logger.info("GPA: " + GPA);

        JSONObject student = new JSONObject();
        student.put("name", studentName);
        student.put("place", studentPlace);
        student.put("phone", studentPhone);
        student.put("GPA", GPA);

        logger.info("Student data JSON: " + student.toString());

        studentServices.addNewStudent(student, httpSession);
        return "class-system";
    }


    @GetMapping("addNewGrades")
    public String addNewGrades(HttpSession httpSession, Model model) {
        return "add-new-grades";
    }

    @PostMapping("storeNewGrades")
    public String storeNewGrades(@RequestParam String studentAccountID, @RequestParam String gradeAmount,
                                      Model model, HttpSession httpSession) {

        logger.info("Storing new grade");
        logger.info("Receiver name: " + studentAccountID);
        logger.info("Added Grade to cumulative GPA: " + gradeAmount);

        gradeServices.changeGrade(studentAccountID, gradeAmount, httpSession);
        return "class-system";
    }

    @PostMapping("check-user-credentials")
    public String checkUserCredentials(@RequestParam String username, @RequestParam String token,
                                       HttpSession httpSession, Model model) {


        model.addAttribute("username", username);
        model.addAttribute("token", token);

        if (studentServices.isAuthorizedUser(username, token)) {

            logger.info("User credentials are correct for " + username);
            int workerPort = workerService.getWorkerPort(token);
            model.addAttribute("username", username);
            model.addAttribute("token", token);
            model.addAttribute("workerPort", workerPort);
            httpSession.setAttribute("username", username);
            httpSession.setAttribute("token", token);
            httpSession.setAttribute("workerPort", workerPort);
            return "class-system";
        } else {
            model.addAttribute("response", apiResponse.generateErrorResponse("User authorization failed"));
            return "registration-failed";
        }
    }

    @PostMapping("check-admin-credentials")
    public String checkAdminCredentials(@RequestParam String username, @RequestParam String token,
                                        HttpSession httpSession, Model model) {

        model.addAttribute("username", username);
        model.addAttribute("token", token);

        if (studentServices.isAuthorizedAdmin(username, token)) {

            logger.info("Admin credentials are correct for " + username);
            model.addAttribute("username", username);
            model.addAttribute("token", token);
            httpSession.setAttribute("username", username);
            httpSession.setAttribute("token", token);
            return "admin-system";
        } else {
            model.addAttribute("response", apiResponse.generateErrorResponse("Admin authorization failed"));
            return "registration-failed";
        }
    }


}
