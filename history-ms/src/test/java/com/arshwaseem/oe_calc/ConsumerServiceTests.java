package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsumerServiceTests {

    @Mock
    private HistoryService historyService;
    @MockitoBean
    private HistoryJPARepository historyJPARepository;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    void history_ShouldConsume(){
        History history = new History();
        doNothing().when(historyService).AddUpdateHistory(history);

        consumerService.consumeHistory(history);
    }


}
