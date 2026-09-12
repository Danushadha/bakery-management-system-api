package com.bakery.bakery_management_system_api.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private Long id;

    @Column (name = "user_name" , nullable = false, length = 200)
    private String userName;

    @Column (name = "password" , nullable = false, length = 200)
    private String password;

    @Column (name = "role" , nullable = false, length = 200)
    private String role;


}
