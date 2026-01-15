package com.hth.udecareer.repository;

import com.hth.udecareer.entities.PieregisterCustomUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PieregisterCustomUserRoleRepository extends JpaRepository<PieregisterCustomUserRole, Long> {
    Optional<PieregisterCustomUserRole> findByWpRoleName(String wpRoleName);
}
