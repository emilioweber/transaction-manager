package com.wex.purchasetransaction.auth.repository;

import com.wex.purchasetransaction.auth.repository.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {
    void deleteByUserId(Integer id);
}
