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
        return new InMemoryUserDetailsManager(User.withUsername("test").password("{noop}password").roles("ADMIN").build());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/", "/index" , "/css/**" , "/images/**", "/login").permitAll()
                        .anyRequest().authenticated()
        )
                        .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/index", true)
                        .permitAll());
                return http.build();
    }
}
