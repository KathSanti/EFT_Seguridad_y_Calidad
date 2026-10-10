CREATE DATABASE IF NOT EXISTS mydatabase;
USE mydatabase;

CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(255),
    email VARCHAR(100),
    enabled INT,
    role VARCHAR(50)
);

DELETE FROM user WHERE username = 'admin';

-- Las contraseñas están hasheadas con BCrypt. El valor en texto plano de las 3 es "123".
INSERT INTO user (username, password, email, enabled, role)
VALUES ('admin', '$2a$10$yU9AaX/Nc4qalt9yeBEjPumRJ06mntrgFAWXudLSy6EOBR9YRDkyG', 'admin@veterinaria.cl', 1, 'ROLE_ADMIN');

INSERT INTO user (username, password, email, enabled, role)
VALUES ('asistente', '$2a$10$yU9AaX/Nc4qalt9yeBEjPumRJ06mntrgFAWXudLSy6EOBR9YRDkyG', 'asistente@veterinaria.cl', 1, 'ROLE_ASISTENTE');

INSERT INTO user (username, password, email, enabled, role)
VALUES ('cliente', '$2a$10$yU9AaX/Nc4qalt9yeBEjPumRJ06mntrgFAWXudLSy6EOBR9YRDkyG', 'cliente@veterinaria.cl', 1, 'ROLE_USER');

INSERT INTO user (username, password, email, enabled, role)
VALUES ('doctor', '$2a$10$yU9AaX/Nc4qalt9yeBEjPumRJ06mntrgFAWXudLSy6EOBR9YRDkyG', 'doctor@veterinaria.cl', 1, 'ROLE_DOCTOR');


-- 2. Crear tabla e insertar Medicamentos

CREATE TABLE IF NOT EXISTS medication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    cost DOUBLE
);

INSERT INTO medication (name, cost) VALUES ('Antiparasitario Interno', 15000.0);
INSERT INTO medication (name, cost) VALUES ('Antibiotico (Amoxicilina)', 22500.0);
INSERT INTO medication (name, cost) VALUES ('Antiflamatorio (Meloxicam)', 12000.0);
INSERT INTO medication (name, cost) VALUES ('Vacuna octuple', 25000.0);


-- 3. Crear tabla e insertar Servicios Care

CREATE TABLE IF NOT EXISTS care (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    cost DOUBLE
);

INSERT INTO care (name, cost) VALUES ('Consulta General', 30000.0);
INSERT INTO care (name, cost) VALUES ('Consulta Especialidad', 45000.0);
INSERT INTO care (name, cost) VALUES ('Ecografía Abdominal', 40000.0);
INSERT INTO care (name, cost) VALUES ('Limpieza Dental', 35000.0);


-- 4. Crear tabla e insertar Pacientes

CREATE TABLE IF NOT EXISTS patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    species VARCHAR(100),
    breed VARCHAR(100),
    age INT,
    owner VARCHAR(255),
    active INT
);

INSERT INTO patient (name, species, breed, age, owner, active) VALUES ('Firulais', 'Perro', 'Labrador', 3, 'Juan Pérez', 1);
INSERT INTO patient (name, species, breed, age, owner, active) VALUES ('Michi', 'Gato', 'Siamés', 2, 'María López', 1);
INSERT INTO patient (name, species, breed, age, owner, active) VALUES ('Rocky', 'Perro', 'Bulldog', 5, 'Pedro Ramírez', 1);


-- 5. Crear tabla e insertar Citas

CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE,
    time TIME,
    reason VARCHAR(255),
    veterinarian VARCHAR(255)
);

INSERT INTO appointment (date, time, reason, veterinarian) VALUES ('2026-10-20', '10:30:00', 'Control anual', 'Dra. Soto');
INSERT INTO appointment (date, time, reason, veterinarian) VALUES ('2026-10-21', '09:00:00', 'Vacunación', 'Dr. Fuentes');
INSERT INTO appointment (date, time, reason, veterinarian) VALUES ('2026-10-22', '15:45:00', 'Revisión post operatoria', 'Dra. Soto');


-- 6. Crear tabla e insertar Facturas (Invoice) y sus relaciones ManyToMany con care y medication

CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(255),
    date DATE,
    time TIME,
    total_cost DOUBLE
);

CREATE TABLE IF NOT EXISTS invoice_cares (
    invoice_id BIGINT,
    care_id BIGINT,
    PRIMARY KEY (invoice_id, care_id),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id),
    FOREIGN KEY (care_id) REFERENCES care(id)
);

CREATE TABLE IF NOT EXISTS invoice_medications (
    invoice_id BIGINT,
    medication_id BIGINT,
    PRIMARY KEY (invoice_id, medication_id),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id),
    FOREIGN KEY (medication_id) REFERENCES medication(id)
);

-- Factura 1: Firulais -> Consulta General (id 1, 30000) + Antiparasitario Interno (id 1, 15000) = 45000
INSERT INTO invoice (patient_name, date, time, total_cost) VALUES ('Firulais', '2026-10-20', '11:00:00', 45000.0);
INSERT INTO invoice_cares (invoice_id, care_id) VALUES (1, 1);
INSERT INTO invoice_medications (invoice_id, medication_id) VALUES (1, 1);

-- Factura 2: Michi -> Limpieza Dental (id 4, 35000)
INSERT INTO invoice (patient_name, date, time, total_cost) VALUES ('Michi', '2026-10-21', '09:30:00', 35000.0);
INSERT INTO invoice_cares (invoice_id, care_id) VALUES (2, 4);

-- Factura 3: Rocky -> Consulta Especialidad (id 2, 45000) + Ecografía Abdominal (id 3, 40000) + Antibiotico (id 2, 22500) = 107500
INSERT INTO invoice (patient_name, date, time, total_cost) VALUES ('Rocky', '2026-10-22', '16:15:00', 107500.0);
INSERT INTO invoice_cares (invoice_id, care_id) VALUES (3, 2);
INSERT INTO invoice_cares (invoice_id, care_id) VALUES (3, 3);
INSERT INTO invoice_medications (invoice_id, medication_id) VALUES (3, 2);