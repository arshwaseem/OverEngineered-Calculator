package com.arshwaseem.oe_calc;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="servicename")
    String serviceName;
    @Column(name="numa")
    double numA;
    @Column(name="numb")
    double numB;
    @Column(name="result")
    double result;

    @Column(name="userid")
    private long userId;

    @Column(name="timestamp")
    Timestamp timeStamp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getNumA() {
        return numA;
    }

    public void setNumA(double numA) {
        this.numA = numA;
    }

    public double getNumB() {
        return numB;
    }

    public void setNumB(double numB) {
        this.numB = numB;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }
}
