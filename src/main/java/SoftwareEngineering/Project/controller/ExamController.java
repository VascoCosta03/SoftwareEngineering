package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.Exam;
import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.ExamRepository;
import SoftwareEngineering.Project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/students/exams")
    public Map<String, Object> addExam(@RequestBody Map<String, Object> examData, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User student = userOptional.get();
            ObjectId studentObjectId = student.getId();

            String title = (String) examData.get("title");
            String dateTimeStr = (String) examData.get("start");
            String location = (String) examData.get("location");
            String professorIdStr = (String) examData.get("professorId");

            ObjectId professorId = new ObjectId(professorIdStr);

            // Parse the exam date
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            ZonedDateTime examDateTime;
            try {
                examDateTime = ZonedDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                return Map.of("success", false, "message", "Invalid date format: " + dateTimeStr);
            }
            Instant instant = examDateTime.toInstant();

            Exam exam = new Exam();
            exam.setSubject(title);
            exam.setDateTime(Date.from(instant));
            exam.setLocation(location);
            exam.setStudentId(studentObjectId);
            exam.setProfessorId(professorId);

            examRepository.save(exam);

            Optional<User> professor = userRepository.findById(professorId.toHexString());
            String professorName = professor.map(User::getName).orElse("Unknown Professor");

            // Prepare a response consistent with the frontend expectation
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("title", exam.getSubject());
            response.put("start", formatter.format(examDateTime));
            response.put("location", exam.getLocation());
            response.put("professor", professorName);

            return response;
        }

        return Map.of("success", false, "message", "Student not found");
    }

    @GetMapping("/professors")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Map<String, Object>> getProfessors() {
        List<User> professors = userRepository.findByRole("PROFESSOR");

        return professors.stream().map(professor -> {
            Map<String, Object> professorInfo = new HashMap<>();
            professorInfo.put("id", professor.getId().toHexString());
            professorInfo.put("name", professor.getName());
            return professorInfo;
        }).collect(Collectors.toList());
    }




}
