package org.example.dao;

import org.example.models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, String> {
    Zone findByEngName(String engName);
}
