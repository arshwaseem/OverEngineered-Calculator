package com.arshwaseem.oe_calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserAdapterREST {
    private static final Logger log = LoggerFactory.getLogger(UserAdapterREST.class);
    private final UserService userService;

    public UserAdapterREST(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> RegisterUser(@RequestBody UserDTO userDto) {
        try{
            if(userService.userExists(userDto.getUsername())){
                throw new RuntimeException("User already exists");
            }
            User res = new User();
            res.setUsername(userDto.getUsername());
            res.setPassword(userDto.getPassword());

            userService.AddUser(res);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            log.error("Error Registering User: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<User> DeleteUser(@RequestBody UserDTO user) {
        try{
            if(!userService.userExists(user.getUsername())){
                throw new RuntimeException("User does not exist");
            }
            userService.DeleteUser(user.getUsername());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }catch (Exception e) {
            log.error("Error Deleting User: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/username")
    public ResponseEntity<User> GetUserByName(@RequestParam String username) {
        try{
            Optional<User> result = userService.GetByName(username);
            if(result.isPresent()){
                return ResponseEntity.status(HttpStatus.OK).body(result.get());
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error Getting User: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
