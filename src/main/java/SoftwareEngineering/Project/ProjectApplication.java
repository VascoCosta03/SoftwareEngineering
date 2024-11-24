package SoftwareEngineering.Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication
@Controller
public class ProjectApplication {

	@GetMapping("/student-login")
	public String studentLogin() {
		return "student-login";
	}

	@GetMapping("/")
	public String home() {
		return "index";
	}

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}
