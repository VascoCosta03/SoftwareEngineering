package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.Exam;
import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.ExamRepository;
import SoftwareEngineering.Project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class ExamController {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public ExamController(ExamRepository examRepository, UserRepository userRepository) {
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/students/exams")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Map<String, Object>> getStudentExams(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User student = userOptional.get();
            ObjectId studentObjectId = student.getId();

            List<Exam> exams = examRepository.findByStudentId(studentObjectId);

            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            return exams.stream().map(exam -> {
                Map<String, Object> event = new HashMap<>();
                event.put("title", exam.getSubject());

                ZonedDateTime utcDateTime = exam.getDateTime()
                        .toInstant()
                        .atZone(ZoneId.of("UTC"));
                event.put("start", formatter.format(utcDateTime));
                event.put("location", exam.getLocation());

                Optional<User> professor = userRepository.findById(exam.getProfessorId().toHexString());
                String professorName = professor.map(User::getName).orElse("Unknown Professor");
                event.put("professor", professorName);

                String formattedTime = formatter.format(utcDateTime);
                event.put("time", formattedTime);

                return event;
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }



    @GetMapping("/professors/exams")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<Map<String, Object>> getProfessorsExams(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User professor = userOptional.get();
            ObjectId professorObjectId = professor.getId();

            List<Exam> exams = examRepository.findByProfessorId(professorObjectId);

            if (exams.isEmpty()) {
                System.out.println("No exams found for professor: " + professorObjectId);
            } else {
                exams.forEach(exam -> System.out.println("Found exam: " + exam.getSubject()));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            return exams.stream().map(exam -> {
                Map<String, Object> event = new HashMap<>();
                event.put("title", exam.getSubject());

                Date examDate = exam.getDateTime();
                Instant instant = examDate.toInstant();
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
                event.put("start", formatter.format(zonedDateTime));

                event.put("location", exam.getLocation());

                event.put("professor", professor.getName());

                String formattedTime = formatter.format(zonedDateTime);
                event.put("time", formattedTime);

                return event;
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


}
