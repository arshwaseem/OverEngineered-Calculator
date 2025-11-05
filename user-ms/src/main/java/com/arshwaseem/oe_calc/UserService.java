package com.arshwaseem.oe_calc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UserService implements UserUseCases{
    private final UserJPARepository userJPARepository;

    public UserService(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Override
    public Optional<User> GetByName(String name) {
        try{
            return userJPARepository.findByusername(name);
        } catch (Exception e){
            log.error(e.getMessage());
            return Optional.empty();
        }

    }

    @Override
    public void AddUser(User user) {
        try{
            userJPARepository.save(user);
        } catch (Exception e){
            log.error(e.getMessage());
        }

    }

    @Override
    public boolean userExists(String userName) {
        try{
            Optional<User> user = userJPARepository.findByusername(userName);
            return user.isPresent();
        } catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

    }

    @Override
    @Transactional
    public void DeleteUser(String name){
        try{
            userJPARepository.deleteByusername(name);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

}
