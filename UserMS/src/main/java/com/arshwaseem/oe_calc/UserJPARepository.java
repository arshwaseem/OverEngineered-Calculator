package com.arshwaseem.oe_calc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<User,Long>{

    Optional<User> findByUserName(String userName);
}
