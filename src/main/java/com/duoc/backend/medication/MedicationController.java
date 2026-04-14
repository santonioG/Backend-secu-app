package com.duoc.backend.medication;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medications")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping
    public List<Medication> getAllMedications() {
        return medicationService.getAllMedications();
    }

    @GetMapping("/{id}")
    public Medication getMedicationById(@PathVariable Long id) {
        return medicationService.getMedicationById(id);
    }

    @PostMapping
    public Medication saveMedication(@RequestBody Medication medication) {
        return medicationService.saveMedication(medication);
    }

    @DeleteMapping("/{id}")
    public void deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
    }
}
