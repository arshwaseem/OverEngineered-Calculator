package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController("/user")
public class UserAdapterREST {
    private static final Logger log = LoggerFactory.getLogger(UserAdapterREST.class);
    private final UserService userService;

    public UserAdapterREST(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> RegisterUser(User user) {
        try{
            userService.AddUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error("Error Registering User: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<User> GetById(@PathVariable String id) {
        try{
            Optional<User> login = userService.GetByID(Long.parseLong(id));
            if (login.isPresent()) {
                return ResponseEntity.of(login);
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error while trying to login user " + "\n" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/name")
    public ResponseEntity<User> GetByName(@RequestBody String UserName) {
        try{
            Optional<User> login = userService.GetByName(UserName);
            if (login.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(login.get());
            }
            else  {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
