package com.example.solid.before;

public class Cliente {
    private final String id;
    private final String nome;
    private final String email;
    private final boolean vip;

    public Cliente(String id, String nome, String email, boolean vip) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.vip = vip;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVip() {
        return vip;
    }
}