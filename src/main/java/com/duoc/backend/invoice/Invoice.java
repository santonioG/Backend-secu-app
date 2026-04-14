package com.duoc.backend.invoice;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.duoc.backend.care.Care;
import com.duoc.backend.medication.Medication;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String patientName;
    private LocalDate date;
    private LocalTime time;

    @ManyToMany
    @JoinTable(
        name = "invoice_cares",
        joinColumns = @JoinColumn(name = "invoice_id"),
        inverseJoinColumns = @JoinColumn(name = "care_id")
    )
    private List<Care> cares;


    @ManyToMany
    @JoinTable(
        name = "invoice_medications",
        joinColumns = @JoinColumn(name = "invoice_id"),
        inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private List<Medication> medications;


    private Double totalCost;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public List<Care> getCares() {
        return cares;
    }

    public void setCares(List<Care> cares) {
        this.cares = cares;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}