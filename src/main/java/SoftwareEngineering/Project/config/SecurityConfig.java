package SoftwareEngineering.Project.config;

import SoftwareEngineering.Project.service.MongoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final MongoUserDetailsService mongoUserDetailsService;

    public SecurityConfig(MongoUserDetailsService mongoUserDetailsService) {
        this.mongoUserDetailsService = mongoUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index", "/css/**", "/images/**", "/login").permitAll()
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/students/**").authenticated()
                        .requestMatchers("/professors/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/students/exams").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }



    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = determineRedirectUrl(authentication);
            response.sendRedirect(redirectUrl);
        };
    }

    private String determineRedirectUrl(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return "/admin";
            } else if (authority.getAuthority().equals("ROLE_STUDENT")) {
                return "/students";
            } else if (authority.getAuthority().equals("ROLE_PROFESSOR")) {
                return "/professors";
            }
        }

        return "/index";
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
