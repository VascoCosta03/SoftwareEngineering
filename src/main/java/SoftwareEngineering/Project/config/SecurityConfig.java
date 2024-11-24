package SoftwareEngineering.Project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    InMemoryUserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager(User.withUsername("test@gmail.com").password("{noop}password").roles("ADMIN").build());

    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http .authorizeRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/", "index" , "/css/**" , "/images/**").permitAll()
                        .anyRequest().authenticated()
        )
                .formLogin(form -> form.loginPage("/student-login").permitAll())
                .build();
    }
}
