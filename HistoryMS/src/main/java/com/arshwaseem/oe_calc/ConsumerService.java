package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    private final HistoryService historyService;

    public ConsumerService(HistoryService historyService) {
        this.historyService = historyService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUENAME)
    public void consumeHistory(History history){
        try{
            historyService.AddUpdateHistory(history);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
