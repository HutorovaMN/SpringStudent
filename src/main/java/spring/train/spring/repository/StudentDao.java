package spring.train.spring.repository;

import org.springframework.stereotype.Repository;
import spring.train.spring.Model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Repository
public class StudentDao {
    private final List<Student> students = new ArrayList<>();

    public List<Student> findAllStudent() {
        return students;
    }

    public Student saveStudent(Student student) {
        students.add(student);
        return student;
    }

    public Student findByEmail(String email) {
        return students.stream().filter(x -> x.getEmail().equals(email))
                .findFirst().orElse(null);
    }

    public Student updateStudent(Student student) {
        var studentIndex = IntStream.range(0, students.size())
                .filter(i -> students.get(i).getEmail().equals(student.getEmail()))
                .findFirst()
                .orElse(-1);

        if (studentIndex > -1) {
            students.set(studentIndex, student);
            return student;
        }
        return null;
    }

    public void deleteStudent(String email) {
        var student = findByEmail(email);
        if (student != null) {
            students.remove(student);
        }
    }
}
