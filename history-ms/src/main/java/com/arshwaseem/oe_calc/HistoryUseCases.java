package com.arshwaseem.oe_calc;

import java.util.List;

public interface HistoryUseCases {
    void AddUpdateHistory(History history);
    History GetHistoryByID(Long id);
    List<History> GetAllHistory();
    List<History> GetAllByServiceName(String serviceName);
    void DeleteHistory(Long id);
}
