package com.kln.lms.api.repository;

import com.kln.lms.api.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findStudentByEmail(String email);
}
