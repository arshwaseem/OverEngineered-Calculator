package com.arshwaseem.oe_calc;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="username")
    public String userName;
    @Column(name="password")
    public String password;
    @Column(name = "lastresult")
    public double lastResult;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getLastResult() {
        return lastResult;
    }

    public void setLastResult(double lastResult) {
        this.lastResult = lastResult;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long ID) {
        this.id = ID;
    }
}
