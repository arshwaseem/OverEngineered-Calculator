package com.arshwaseem.oe_calc;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name="history", indexes = {
@Index(name = "idx_history_servicename", columnList = "servicename")
, @Index(name = "idx_history_timestamp", columnList = "timestamp")})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of="id")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
