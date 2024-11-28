package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.Exam;
import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.ExamRepository;
import SoftwareEngineering.Project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    @Autowired
    private MongoTemplate mongoTemplate;

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
            ObjectId studentObjectId = student.getId();  // Directly use the ObjectId

            List<Exam> exams = examRepository.findByStudentId(studentObjectId);

            if (exams.isEmpty()) {
                System.out.println("No exams found for student: " + studentObjectId);
            } else {
                exams.forEach(exam -> System.out.println("Found exam: " + exam.getSubject()));
            }

            // Format the date using ZonedDateTime, without the UTC suffix
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Custom pattern to remove [UTC]

            // Map the exams to the format required by FullCalendar
            return exams.stream().map(exam -> {
                Map<String, Object> event = new HashMap<>();
                event.put("title", exam.getSubject());

                // Ensure that exam.getDateTime() is a java.util.Date
                Date examDate = exam.getDateTime();  // Assuming getDateTime() returns java.util.Date

                // Convert java.util.Date to java.time.Instant
                Instant instant = examDate.toInstant();

                // Convert Instant to ZonedDateTime using UTC
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC")); // Adjust timezone if needed

                // Put the formatted start time into the event
                event.put("start", formatter.format(zonedDateTime));

                event.put("location", exam.getLocation());
                return event;
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


}
