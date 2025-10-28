package com.arshwaseem.oe_calc;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/user")
@AllArgsConstructor
public class UserAdapterREST {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> RegisterUser(@Valid @RequestBody UserRequest userRequest) {
        try{

            if(userService.userExists(userRequest.getUsername())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
            }

            User res = new User();
            res.setUsername(userRequest.getUsername());
            res.setPassword(userRequest.getPassword());

            userService.AddUser(res);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error Registering User: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> DeleteUser(@RequestParam String username) {
        try{
            if(!userService.userExists(username)){
                log.error("Cannot Delete User {} : DOES NOT EXIST",username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
            }
            userService.DeleteUser(username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }catch (Exception e) {
            log.error("Error Deleting User: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/username")
    public ResponseEntity<?> GetUserByName(@RequestParam String username) {
        try{
            Optional<User> result = userService.GetByName(username);
            if(result.isPresent()){
                return ResponseEntity.status(HttpStatus.OK).body(new UserResponse(result.get().getUsername(), result.get().getPassword()));
            }
            else {
                log.error("Cannot Find User {}",username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error Getting User: {}" , e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
