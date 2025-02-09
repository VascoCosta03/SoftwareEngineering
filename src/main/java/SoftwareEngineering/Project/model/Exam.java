package SoftwareEngineering.Project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "exams")
public class Exam {

    @Id
    private ObjectId id;

    private String subject;
    private Date dateTime;
    private String location;
    private ObjectId professorId;
    private ObjectId studentId;
    private String status;

    public Exam() {
        this.status = "Pending";
    }

    public Exam(String subject, Date dateTime, String location, ObjectId professorId, ObjectId studentId) {
        this.subject = subject;
        this.dateTime = dateTime;
        this.location = location;
        this.professorId = professorId;
        this.studentId = studentId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ObjectId getProfessorId() {
        return professorId;
    }

    public void setProfessorId(ObjectId professorId) {
        this.professorId = professorId;
    }

    public ObjectId getStudentId() {
        return studentId;
    }

    public void setStudentId(ObjectId studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
