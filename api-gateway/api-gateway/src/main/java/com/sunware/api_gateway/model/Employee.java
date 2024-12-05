package com.sunware.api_gateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.util.Set;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "temporary_otp")
    private String temporaryOtp;

    @ManyToMany(fetch = FetchType.EAGER)    
    @JoinTable(
        name = "employee_permission",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    // Default constructor
    public Employee() {
    }

    // Parameterized constructor
    public Employee(Long id, String email, String temporaryOtp, Set<Permission> permissions) {
        this.id = id;
        this.email = email;
        this.temporaryOtp = temporaryOtp;
        this.permissions = permissions;
    }

    // Getter and setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for temporaryOtp
    public String getTemporaryOtp() {
        return temporaryOtp;
    }

    public void setTemporaryOtp(String temporaryOtp) {
        this.temporaryOtp = temporaryOtp;
    }

    // Getter and setter for permissions
    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", temporaryOtp='" + temporaryOtp + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
