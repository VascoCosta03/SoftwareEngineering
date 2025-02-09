package SoftwareEngineering.Project.controller;

import SoftwareEngineering.Project.model.Exam;
import SoftwareEngineering.Project.model.User;
import SoftwareEngineering.Project.repository.ExamRepository;
import SoftwareEngineering.Project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

                event.put("status", exam.getStatus());

                event.put("id", exam.getId().toHexString());

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
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            return exams.stream().map(exam -> {
                Map<String, Object> event = new HashMap<>();
                event.put("id", exam.getId().toHexString());
                event.put("title", exam.getSubject());

                Date examDate = exam.getDateTime();
                Instant instant = examDate.toInstant();
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
                event.put("start", formatter.format(zonedDateTime));

                event.put("location", exam.getLocation());

                event.put("professor", professor.getName());

                String formattedTime = formatter.format(zonedDateTime);
                event.put("time", formattedTime);

                event.put("status", exam.getStatus());

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
            exam.setStatus("Pending");

            examRepository.save(exam);

            Optional<User> professor = userRepository.findById(professorId.toHexString());
            String professorName = professor.map(User::getName).orElse("Unknown Professor");

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

    @PatchMapping("/professors/exams/{examId}/status")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> updateExamStatus(@PathVariable String examId, @RequestBody Map<String, String> request) {
        Optional<Exam> examOptional = examRepository.findById(new ObjectId(examId));

        if (examOptional.isPresent()) {
            Exam exam = examOptional.get();
            String newStatus = request.get("status");

            if (!newStatus.equals("Accepted") && !newStatus.equals("Rejected")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));
            }

            exam.setStatus(newStatus);
            examRepository.save(exam);

            return ResponseEntity.ok(Map.of("message", "Exam status updated successfully", "exam", exam));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Exam not found"));
    }

    @PutMapping("/students/exams/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateExam(@PathVariable String id, @RequestBody Exam updatedExam) {
        Optional<Exam> existingExamOpt = examRepository.findById(new ObjectId(id));

        if (!existingExamOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Exam not found"));
        }

        Exam existingExam = existingExamOpt.get();
        existingExam.setSubject(updatedExam.getSubject());
        existingExam.setDateTime(updatedExam.getDateTime());
        existingExam.setLocation(updatedExam.getLocation());
        existingExam.setStatus("Pending");

        examRepository.save(existingExam);

        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

}
