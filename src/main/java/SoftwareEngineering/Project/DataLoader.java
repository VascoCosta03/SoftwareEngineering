package SoftwareEngineering.Project;

import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    @Bean
    CommandLineRunner loadData(UserRepository userRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            User user1 = new User("test", encoder.encode("password"), "ADMIN");
            User user2 = new User("user", encoder.encode("password"), "USER");



            userRepository.save(user1);
            userRepository.save(user2);
        };
    }
}
