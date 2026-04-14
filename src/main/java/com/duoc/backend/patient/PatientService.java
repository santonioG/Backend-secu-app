package com.duoc.backend.patient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return StreamSupport.stream(patientRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @SuppressWarnings("null")
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
