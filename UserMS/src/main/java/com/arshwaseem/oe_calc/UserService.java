package com.arshwaseem.oe_calc;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserUseCases{
    private final UserJPARepository userJPARepository;

    public UserService(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }


    @Override
    public Optional<User> GetByID(Long id) {
        return userJPARepository.findById(id);
    }

    @Override
    public Optional<User> GetByName(String name) {
        return userJPARepository.findByUserName(name);
    }

    @Override
    public void AddUser(User user) {
        userJPARepository.save(user);
    }

}
