package spring.train.spring.Service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring.train.spring.Model.Student;
import spring.train.spring.Service.StudentService;
import spring.train.spring.repository.StudentDao;

import java.util.List;

@Service
@AllArgsConstructor
public class InMemoryStudentService implements StudentService {

    private final StudentDao repository;

    @Override
    public List<Student> findAllStudent() {
        return repository.findAllStudent();
    }

    @Override
    public Student saveStudent(Student student) {
        return repository.saveStudent(student);
    }

    @Override
    public Student findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Student updateStudent(Student student) {
        return repository.updateStudent(student);
    }

    @Override
    public void deleteStudent(String email) {
        repository.deleteStudent(email);
    }
}
