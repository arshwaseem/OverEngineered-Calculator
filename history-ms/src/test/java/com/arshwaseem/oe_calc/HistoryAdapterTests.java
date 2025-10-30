package com.arshwaseem.oe_calc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryAdapterREST.class)
public class HistoryAdapterTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void history_shouldGetALl() throws Exception {

        List<History> expected = new ArrayList<>();

        expected.add(new History());
        expected.add(new History());

        when(historyService.GetAllHistory()).thenReturn(expected);

        MvcResult res = mockMvc.perform(
                get("/history/all")
        ).andExpect(status().isOk()).andReturn();

        List<History> history = objectMapper.readValue(res.getResponse().getContentAsString(), new TypeReference<List<History>>(){});

        Assertions.assertNotNull(history);
        Assertions.assertEquals(expected.size(), history.size());

    }

    @Test
    void history_shouldGetByID() throws Exception {
        History toFind = new History();
        toFind.setId(1L);

        when(historyService.GetHistoryByID(1L)).thenReturn(toFind);

        mockMvc.perform(
                get("/history/" + toFind.getId())
        ).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }
}
