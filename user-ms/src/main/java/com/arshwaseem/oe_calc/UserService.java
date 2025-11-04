package com.arshwaseem.oe_calc;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserUseCases{
    private final UserJPARepository userJPARepository;

    public UserService(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Override
    public Optional<User> GetByName(String name) {
        return userJPARepository.findByusername(name);
    }

    @Override
    public User AddUser(User user) {
        return userJPARepository.save(user);
    }

    @Override
    public boolean userExists(String userName) {
        Optional<User> user = userJPARepository.findByusername(userName);
        return user.isPresent();
    }

    @Override
    @Transactional
    public void DeleteUser(String name){
        userJPARepository.deleteByusername(name);
    }

}
