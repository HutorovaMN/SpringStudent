package spring.train.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.train.spring.Model.Student;


public interface StudentRepository extends JpaRepository<Student, Long> {
    void deleteByEmail(String email);
    Student findStudentByEmail(String email);
}
