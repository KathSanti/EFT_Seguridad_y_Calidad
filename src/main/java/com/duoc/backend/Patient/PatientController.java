package com.duoc.backend.Patient;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.Patient.dto.PatientCreateDto;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/patient")
public class PatientController {

    

    // Eliminamos Autowired e inyectamos por constructor no por atributo (Recomendación Sonar)
    
    private final PatientService patientService;

    //iyectamos dependencia mediante constructor
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/register")
    public String greetings(@RequestParam(value="name", defaultValue="World") String name) {
        return "Hello {" + name + "}";
    }

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ASISTENTE')")
    public Patient savePatient(@Valid @RequestBody PatientCreateDto dto) {
        
        Patient safePatient = new Patient();
        
        // Solo permitimos que se escriban los datos del formulario (DTO)
        safePatient.setName(dto.name());
        safePatient.setSpecies(dto.species());
        safePatient.setBreed(dto.breed());
        safePatient.setAge(dto.age());
        safePatient.setOwner(dto.owner());
        
        // No tocamos ni el id y tampoco el active se asignan de manera interna reponsabilidad exclusiva del servidor

        return patientService.savePatient(safePatient);
    }

    //Metodo para desactivar paciente consulta put 
    @PutMapping("/disable/{id}")
    public void disablePatient(@PathVariable Long id) {
        patientService.disablePatient(id);
    }


    
}




