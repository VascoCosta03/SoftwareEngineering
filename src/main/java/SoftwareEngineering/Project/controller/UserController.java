package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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


        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User currentUser = user.get();
            model.addAttribute("fullName", currentUser.getFirstName() + " " + currentUser.getLastName());

            // Handle null or empty pictureUrl
            String pictureUrl = currentUser.getPictureUrl();
            if (pictureUrl == null || pictureUrl.isEmpty()) {
                pictureUrl = "/images/images/default-profile.png";
            }
            model.addAttribute("pictureUrl", pictureUrl);
        } else {
            model.addAttribute("pictureUrl", "/images/images/default-profile.png");
        }

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

            // Handle null or empty pictureUrl
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
