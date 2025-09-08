package com.forwardapp.forward.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coaches")
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;

    private String password;

    private String bio;

    private String specialty;

    @Enumerated(EnumType.STRING)
    private Role role = Role.COACH;

    // Constructors
    public Coach() {
    }

    public Coach(String email, String username, String password, String bio, String specialty) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.specialty = specialty;
        this.role = Role.COACH;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Coach{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                ", specialty='" + specialty + '\'' +
                ", role=" + role +
                '}';
    }
}
