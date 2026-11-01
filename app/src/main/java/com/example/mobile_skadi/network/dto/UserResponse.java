package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    private Long id;

    @SerializedName(value = "nome", alternate = {"name"})
    private String nome;

    private String username;
    private String email;

    @SerializedName(value = "nivelAcesso", alternate = {"role", "nivel_acesso"})
    private String role;

    private String status;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
