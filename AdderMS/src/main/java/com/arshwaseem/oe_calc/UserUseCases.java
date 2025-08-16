package com.arshwaseem.oe_calc;

import java.util.Optional;

public interface UserUseCases {
    Optional<User> GetByID(Long id);
    Optional<User> GetByName(String name);
    void AddUser (User user);
}
