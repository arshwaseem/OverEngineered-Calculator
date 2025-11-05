package com.arshwaseem.oe_calc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserJPARepository userJPARepository;

    @InjectMocks
    private UserService userService;

    @Test
    void user_ShouldAddUserWithValidData() {
        User userToSave = new User();
        userToSave.setUsername("test");
        userToSave.setPassword("test");
        userToSave.setId(1L);
        when(userJPARepository.save(userToSave)).thenReturn(userToSave);
        userService.AddUser(userToSave);
    }

    @Test
    void user_ShouldReturnFalseWhenUserAlreadyExists() {
        when(userJPARepository.existsByUsername("test")).thenReturn(true);
        Assertions.assertTrue(userService.userExists("test"));
    }

    @Test
    void user_ShouldGetByName(){
        when(userJPARepository.findByUsername("test")).thenReturn(Optional.of(new User("test","password")));
        Optional<User> result = userService.GetByName("test");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("test",result.get().getUsername());
    }

    @Test
    void user_ShouldThrowExceptionWhenUserDoesNotExist() {
        doNothing().when(userJPARepository).deleteByUsername("test");
        userService.DeleteUser("test");
    }

}