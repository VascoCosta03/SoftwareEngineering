package SoftwareEngineering.Project.repository;

import SoftwareEngineering.Project.model.Exam;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExamRepository extends MongoRepository<Exam, ObjectId> {
    List<Exam> findByStudentId(ObjectId studentId);
    List<Exam> findByProfessorId(ObjectId professorId);
}


