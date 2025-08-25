package com.arshwaseem.oe_calc.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class HistoryService implements HistoryUseCases{

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);
    private final RabbitTemplate rabbitTemplate;

    public HistoryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void PublishHistory(History history) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUENAME, history);
        log.info("Publisher History To Queue");
    }
}
