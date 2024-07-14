package com.diplom.faces_recognition.dao.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users", schema = "test_schema")
public class ModelMapper {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    int id;

    @Column(name="login", length=50, nullable=false, unique=false)
    String name;

    @Column(name="password", length=50, nullable=false, unique=false)
    String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
