package com.caiquekola.livechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class Usuario implements Runnable {

    private String nome;
    private String numero;
    private Socket usuarioSocket;
    private PrintWriter saida;
    private BufferedReader entrada;

    public Usuario(String nome, String numero, Socket socket) throws IOException {
        this.nome = nome;
        this.usuarioSocket = socket;
        this.numero = numero;
        saida = new PrintWriter(usuarioSocket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(usuarioSocket.getInputStream()));
    }

    public void enviarMensagem(String mensagem) {
        saida.println(mensagem);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = entrada.readLine()) != null) {
                LiveChatServerSocket.enviarListaUsuariosParaTodos();
                if (msg.startsWith("@todos:")) {
                    LiveChatServerSocket.enviarParaTodos(nome + ": " + msg.substring(7));
                } else if (msg.startsWith("@")) {
                    int sep = msg.indexOf(":");
                    String numDest = msg.substring(1, sep);
                    String conteudo = msg.substring(sep + 1);
                    
                    String nomeDestinatario = LiveChatServerSocket.getNomePorNumero(numDest);
                    String paraDestinatario = "[Privado] " + numero + " - " + nome + ": " + conteudo;
                    String paraRemetente = "[Privado] " + numDest + " - " + nomeDestinatario + ": " + conteudo;

                    LiveChatServerSocket.enviarParaUsuario(numDest, paraDestinatario);
                    this.enviarMensagem(paraRemetente);
                } else {
                    saida.println("Formato inválido. Use @todos: ou @numero:");
                }
            }
        } catch (IOException io) {
            System.err.println("Error");
        } finally {
            try {
                LiveChatServerSocket.removerUsuario(this);
                usuarioSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNome() {
        return nome;
    }

    public String getNumero() {
        return numero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.numero);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) obj;
        return Objects.equals(this.numero, other.numero); // comparação só pelo número
    }

    @Override
    public String toString() {
        return "Usuario{" + "nome=" + nome + ", numero=" + numero + '}';
    }

}
