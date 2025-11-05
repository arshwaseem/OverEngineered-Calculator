package com.arshwaseem.oe_calc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdapterREST.class)
public class UserRESTAdapterTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void userRestController_ShouldRegisterUser() throws Exception {

        doNothing().when(userService).AddUser(new User());
        when(userService.userExists(any())).thenReturn(false);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username" : "test",
                                  "password" : "pass"
                                }""")
                ).andExpect(status().isCreated());
    }

    @Test
    void userRestController_ShouldGetUserByUsername() throws Exception {

        User toFind = new User("test", "pass");

        when(userService.GetByName(toFind.getUsername())).thenReturn(Optional.of(toFind));

        mockMvc.perform(
                        get("/user/username")
                                .param("username", toFind.getUsername())
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    void userRestController_ShouldDeleteUser() throws Exception {

        String toDelete = "test";
        doNothing().when(userService).DeleteUser(toDelete);
        when(userService.userExists(toDelete)).thenReturn(true);

        mockMvc.perform(
                delete("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username",toDelete)
        ).andExpect(status().isNoContent());
    }
}
