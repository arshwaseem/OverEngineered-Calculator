package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService implements HistoryUseCases{
    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);
    public HistoryJPARepository historyJPARepository;

    public HistoryService(HistoryJPARepository historyJPARepository) {
        this.historyJPARepository = historyJPARepository;
    }

    @Override
    public void AddUpdateHistory(History history) {
        try{
            historyJPARepository.save(history);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Override
    public void DeleteHistory(Long id) {
        try{
            historyJPARepository.deleteById(id);
        } catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public History GetHistoryByID(Long id) {
        try {
            return historyJPARepository.findById(id).get();
        } catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public List<History> GetAllHistory() {
        try {
            return historyJPARepository.findAll();
        } catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public List<History> GetAllByServiceName(String serviceName) {
        try {
            return historyJPARepository.findAllByServiceName(serviceName);
        } catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

}
