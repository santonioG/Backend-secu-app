package com.duoc.backend.patient;

import org.springframework.data.repository.CrudRepository;

import com.duoc.backend.patient.Patient;

public interface PatientRepository extends CrudRepository<Patient, Long> {
}