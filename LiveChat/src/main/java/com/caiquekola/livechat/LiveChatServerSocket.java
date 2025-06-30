/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.caiquekola.livechat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Caiquekola
 */
public class LiveChatServerSocket {

    public static void main(String[] args) {
        final int PORTA = 23044;

        Set<Usuario> usuarios = new HashSet<>();

        System.out.println("Servidor de Chat iniciado na porta: " + PORTA);
        try {
            ServerSocket serverSocket = new ServerSocket(PORTA);

            while (true) {
                System.out.println("Aguardando por uma nova conexão...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                new Thread().start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }

        
    }

}
}
