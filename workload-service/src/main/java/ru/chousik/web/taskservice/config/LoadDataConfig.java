//package ru.chousik.web.taskservice.config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.stereotype.Service;
//import ru.chousik.web.taskservice.entity.TeacherEntity;
//import ru.chousik.web.taskservice.entity.StudentEntity;
//import ru.chousik.web.taskservice.repository.StudentRepository;
//import ru.chousik.web.taskservice.repository.TeacherRepository;
//
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class LoadDataConfig {
//    StudentRepository studentRepository;
//    TeacherRepository teacherRepository;
//
//    //#todo тут Логику добавления Маш
//    @PostConstruct
//    public void initial(){
//        studentRepository.save(new StudentEntity("Черемисова",
//                "Мария",
//                "Александровна"));
//        Optional<TeacherEntity> teacher = teacherRepository.getTeacherEntityByNameAndSurnameAndMiddleName("Захар",
//                "Силаев",
//                "Алексеевич");
//    }
//}