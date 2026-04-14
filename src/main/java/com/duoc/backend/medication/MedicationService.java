package com.duoc.backend.medication;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    public List<Medication> getAllMedications() {
        return (List<Medication>) medicationRepository.findAll();
    }

    @SuppressWarnings("null")
    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    public Medication saveMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @SuppressWarnings("null")
    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}