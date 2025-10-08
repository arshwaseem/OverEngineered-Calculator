package com.arshwaseem.oe_calc;

import java.util.Optional;

public interface UserUseCases {
    Optional<User> GetByName(String name);
    void AddUser (User user);
    boolean userExists(String name);
    void DeleteUser(String Name);
}
