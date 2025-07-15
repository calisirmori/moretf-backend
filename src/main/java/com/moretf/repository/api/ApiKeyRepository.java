package com.moretf.repository.api;

import com.moretf.model.ApiKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE ApiKey k SET k.active = false WHERE k.userId = :userId AND k.active = true")
    void deactivateAllByUserId(@Param("userId") String userId);
}


