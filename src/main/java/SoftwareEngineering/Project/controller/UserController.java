package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.Exam;
import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.ExamRepository;
import SoftwareEngineering.Project.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    public UserController(UserRepository userRepository, ExamRepository examRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been successfully logged out.");
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/students")
    public String student(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the user details from the database
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User currentUser = userOptional.get();

            // Pass user details to the model for rendering in the view
            model.addAttribute("fullName", currentUser.getFirstName() + " " + currentUser.getLastName());

            String pictureUrl = currentUser.getPictureUrl();
            if (pictureUrl == null || pictureUrl.isEmpty()) {
                pictureUrl = "/images/images/default-profile.png"; // Default picture if none is set
            }
            model.addAttribute("pictureUrl", pictureUrl);

            // You could also pass additional user-related data if necessary
        } else {
            model.addAttribute("fullName", "Guest");
            model.addAttribute("pictureUrl", "/images/images/default-profile.png");
        }

        // Return the view for the student page (students.html)
        return "students";
    }



    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping("/professors")
    public String professor(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User currentUser = user.get();
            model.addAttribute("fullName", currentUser.getFirstName() + " " + currentUser.getLastName());

            String pictureUrl = currentUser.getPictureUrl();
            if (pictureUrl == null || pictureUrl.isEmpty()) {
                pictureUrl = "/images/images/default-profile.png";
            }
            model.addAttribute("pictureUrl", pictureUrl);
        } else {
            model.addAttribute("pictureUrl", "/images/images/default-profile.png");
        }

        return "professors";
    }

}
