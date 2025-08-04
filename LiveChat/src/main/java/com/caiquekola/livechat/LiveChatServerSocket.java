package com.caiquekola.livechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author Caiquekola
 */
public class LiveChatServerSocket {

    private static final int PORTA = 23044;
    private static Set<Usuario> usuarios = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Servidor de Chat iniciado na porta: " + PORTA);
        try {
            ServerSocket serverSocket = new ServerSocket(PORTA);
            while (true) {
                System.out.println("Aguardando por uma nova conexão...");
                Socket socketUsuario = serverSocket.accept();
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socketUsuario.getInputStream()));
                PrintWriter saida = new PrintWriter(new OutputStreamWriter(socketUsuario.getOutputStream()), true); // Autoflush
                String nomeUsuario = entrada.readLine();
                String numeroUsuario = entrada.readLine();
                Usuario novoUsuario = new Usuario(nomeUsuario, numeroUsuario, socketUsuario);
                if(nomeOuNumeroExistem(nomeUsuario,numeroUsuario)) {
                    // Já existe cliente
                    saida.println("COD1");
                    saida.flush();
                    socketUsuario.close();
                } else {
                    usuarios.add(novoUsuario);
                    saida.println("COD0");
                    // Envia lista atualizada para todos
                    enviarListaUsuariosParaTodos();
                    new Thread(novoUsuario).start();
                    System.out.println("Novo cliente conectado: " + socketUsuario.getInetAddress());
                    System.out.println(novoUsuario);

                }

            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    public static synchronized void enviarListaUsuariosParaTodos() {
        StringBuilder lista = new StringBuilder("@lista:");
        for (Usuario u : usuarios) {
            lista.append(u.getNumero()).append(" - ").append(u.getNome()).append(",");
        }
        // Remove última vírgula
        if (lista.length() > 7) {
            lista.setLength(lista.length() - 1);
        }

        for (Usuario u : usuarios) {
            u.enviarMensagem(lista.toString());
        }
    }

    public static synchronized void removerUsuario(Usuario usuario) {
        usuarios.remove(usuario);
        enviarListaUsuariosParaTodos();
    }

    public static synchronized void enviarParaTodos(String mensagem) {
        for (Usuario usuario : usuarios) {
            usuario.enviarMensagem(mensagem);
        }
    }

    public static synchronized void enviarParaUsuario(String numeroDest, String msg) {
        for (Usuario usuario : usuarios) {
            if (usuario.getNumero().equals(numeroDest)) {
                usuario.enviarMensagem(msg);
            }
        }
    }

    public static synchronized String getNomePorNumero(String numero) {
        for (Usuario u : usuarios) {
            if (u.getNumero().equals(numero)) {
                return u.getNome();
            }
        }
        return numero; 
    }
    
    public static synchronized boolean nomeOuNumeroExistem(String nome, String numero) {
    for (Usuario u : usuarios) {
        if (u.getNome().equals(nome) || u.getNumero().equals(numero)) {
            return true;
        }
    }
    return false;
}

}
