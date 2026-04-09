package com.duoc.backend.appointment;

import org.springframework.data.repository.CrudRepository;

import com.duoc.backend.appointment.Appointment;

public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
}
