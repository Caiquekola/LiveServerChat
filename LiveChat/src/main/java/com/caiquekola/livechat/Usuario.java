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

    // Importante para comparações em listas e mapas no servidor!
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(nome, usuario.nome); 
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}

