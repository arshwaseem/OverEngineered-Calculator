package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HistoryServiceTests {

    @Mock
    HistoryJPARepository historyJPARepository;

    @InjectMocks
    HistoryService historyService;

    @Test
    void history_ShouldAddHistory(){
        History toSave = new History();
        when(historyJPARepository.save(toSave)).thenReturn(toSave);

        historyService.AddUpdateHistory(toSave);
    }

    @Test
    void history_ShouldGetHistoryById(){
        History toGet = new History();
        toGet.setId(1L);

        when(historyJPARepository.findById(toGet.getId())).thenReturn(Optional.of(toGet));

        Assertions.assertEquals(1L, historyService.GetHistoryByID(1L).getId());
    }

    @Test
    void history_ShouldGetAllHistory(){
        List<History> toGet = new ArrayList<>();
        toGet.add(new History());
        toGet.add(new History());

        when(historyJPARepository.findAll()).thenReturn(toGet);

        Assertions.assertFalse(historyService.GetAllHistory().isEmpty());
    }

    @Test
    void history_ShouldGetAllByServiceName(){
        List<History> toGet = new ArrayList<>();
        History toGet1 = new History();
        toGet1.setServiceName("test");
        toGet.add(toGet1);

        when(historyJPARepository.findAllByServiceName("test")).thenReturn(toGet);
        Assertions.assertFalse(historyService.GetAllByServiceName("test").isEmpty());
        Assertions.assertEquals("test",  historyService.GetAllByServiceName("test").get(0).getServiceName());
    }

    @Test
    void history_ShouldDeleteHistory(){
        doNothing().when(historyJPARepository).deleteById(any());

        historyService.DeleteHistory(1L);
    }
}