package com.arshwaseem.oe_calc;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_username", columnList = "users"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString(exclude = "password")
@EqualsAndHashCode(of="id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message="Username cannot be Empty")
    @Column(name="username", nullable=false, unique=true)
    private String username;
    @NotBlank(message="Password Cannot Be Empty")
    @Column(name="password")
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
