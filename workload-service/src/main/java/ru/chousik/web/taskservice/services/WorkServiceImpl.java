package ru.chousik.web.taskservice.services;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.web.dto.StudentDTO;
import ru.chousik.web.dto.TeacherDTO;
import ru.chousik.web.taskservice.clients.StudentClient;
import ru.chousik.web.taskservice.dto.SaveWorkDTO;
import ru.chousik.web.taskservice.dto.WorkDTO;
import ru.chousik.web.taskservice.entity.WorkEntity;
import ru.chousik.web.taskservice.repository.WorkRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    WorkRepository workRepository;
    WorkDocService workDocService;
    StudentClient studentClient;
    ModelMapper modelMapper;

    @Override
    public void saveWork(SaveWorkDTO saveWorkDTO,
                         String key){
        WorkEntity work = fromDTO(saveWorkDTO);
        work.setKey(key);
        Integer unique = workRepository.findFirstUniqueCountByTitle(saveWorkDTO.getTitle())
                .orElseGet(() -> Math.abs(saveWorkDTO.getTitle().hashCode()) % 30 + 70);
        work.setUniqueCount(unique);
        workRepository.save(work);
    }

    @Override
    public void deleteWork(UUID uuid) {
        String key = workRepository.findKeyByUuid(uuid);
        workRepository.deleteById(uuid);
        workDocService.deleteWorkFile(key);
    }

    @Transactional
    @Override
    public void updateWork(UUID uuid, SaveWorkDTO saveWorkDTO, String key) {
        WorkEntity work = workRepository.getWorkEntityByUuid(uuid);
        if (!work.getTitle().equals(saveWorkDTO.getTitle())){
            throw new IllegalArgumentException("Загружен неверный файл.");
        }
        workDocService.deleteWorkFile(work.getKey());
        work.setCompletion(saveWorkDTO.getCompletion());
        work.setKey(key);
    }

    @Override
    public List<WorkDTO> getWorks() {
        List<WorkEntity> works = workRepository.findAll();
        Map<UUID, StudentDTO> studentCache = new HashMap<>();
        Map<UUID, TeacherDTO> teacherCache = new HashMap<>();

        return works.stream()
                .map(work -> mapToDTO(work, studentCache, teacherCache))
                .toList();
    }

    private WorkDTO mapToDTO(WorkEntity work){
        return mapToDTO(work, new HashMap<>(), new HashMap<>());
    }

    private WorkDTO mapToDTO(WorkEntity work,
                             Map<UUID, StudentDTO> studentCache,
                             Map<UUID, TeacherDTO> teacherCache){
        StudentDTO student = studentCache.computeIfAbsent(
                work.getStudentId(),
                id -> studentClient.getStudentById(id));
        TeacherDTO teacher = teacherCache.computeIfAbsent(
                work.getTeacherId(),
                id -> studentClient.getTeacherById(id));
        WorkDTO workDTO = modelMapper.map(work, WorkDTO.class);
        workDTO.setAuthor(String.join(" ",
                List.of(
                        student.getName(),
                        student.getMiddleName(),
                        student.getSurname())));
        workDTO.setSupervisor(String.join(" ",
                List.of(
                        teacher.getName(),
                        teacher.getMiddleName(),
                        teacher.getSurname())));
        return workDTO;
    }

    private WorkEntity fromDTO(SaveWorkDTO saveWorkDTO){
        return modelMapper.map(saveWorkDTO, WorkEntity.class);
    }
}
