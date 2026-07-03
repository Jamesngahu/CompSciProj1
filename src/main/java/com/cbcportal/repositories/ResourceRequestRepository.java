package com.cbcportal.repositories;

import com.cbcportal.models.ResourceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
    // NEW: Fetch requests posted by a specific School Admin
    List<ResourceRequest> findBySchoolAdminId(Long adminId);
}