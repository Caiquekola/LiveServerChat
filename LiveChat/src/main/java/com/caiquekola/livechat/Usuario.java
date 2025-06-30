package com.caiquekola.livechat;

import java.util.Objects;

public class Usuario {
    private String nome;
    private String numero; //

    public Usuario(String nome, String numero) {
        this.nome = nome;
        this.numero = numero;
    }

    public String getNome() {
        return nome;
    }

}

