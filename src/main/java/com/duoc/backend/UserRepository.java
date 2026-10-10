package com.duoc.backend;

import org.springframework.data.jpa.repository.JpaRepository;

// Este código será CREADO AUTOMATICAMENTE por Spring en un Bean llamado userRepository
// CRUD significa Create, Read, Update, Delete

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

}
