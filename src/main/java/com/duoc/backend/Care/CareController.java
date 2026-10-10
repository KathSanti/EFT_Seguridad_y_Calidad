package com.duoc.backend.Care;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.Care.dto.CareCreateDto;
import com.duoc.backend.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/care")
public class CareController {

    private final CareRepository careRepository;

    public CareController(CareRepository careRepository) {
        this.careRepository = careRepository;
    }


    @GetMapping
    public List<Care> getAllCares() {
        return careRepository.findAll();
    }

    @GetMapping("/{id}")
    public Care getCareById(@PathVariable Long id) {
        return careRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidado no encontrado con id: " + id));
    }


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ASISTENTE')")
    public Care saveCare(@Valid @RequestBody CareCreateDto dto) {
        
        Care safeCare = new Care();
        
        // Solo permitimos que se escriban los datos del formulario (DTO)
        safeCare.setName(dto.name());
        safeCare.setCost(dto.cost());
        
        // No tocamos ni el id reponsabilidad exclusiva del servidor

        return careRepository.save(safeCare);
    }

    @DeleteMapping("/{id}")
    public void deleteCare(@PathVariable Long id) {
        if (!careRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cuidado no encontrado con id: " + id);
        }
        careRepository.deleteById(id);
    }
}