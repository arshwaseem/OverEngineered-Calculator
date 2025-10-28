package com.arshwaseem.oe_calc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="servicename")
    private String serviceName;
    @Column(name="numa")
    private double numA;
    @Column(name="numb")
    private double numB;
    @Column(name="result")
    private double result;

    @Column(name="timestamp")
    private Timestamp timeStamp;
}
