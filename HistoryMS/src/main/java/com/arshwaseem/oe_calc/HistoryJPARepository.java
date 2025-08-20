package com.arshwaseem.oe_calc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryJPARepository extends JpaRepository<History, Long> {
    List<History> findAllByServiceName(String serviceName);
}
