/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.caiquekola.livechat;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.w3c.dom.events.MouseEvent;

/**
 *
 * @author Caiquekola
 */
public class LiveChatView extends javax.swing.JFrame {

    private Socket socket;
    private String nomeUsuario;
    private String numeroUsuario;
    private BufferedReader entrada;
    private PrintWriter saida;
    private Map<String, JPanel> abasPrivadas = new HashMap<>();
    private Map<String, JTextArea> areasPrivadas = new HashMap<>();
    private DefaultListModel<String> listaUsuariosModel = new DefaultListModel<>();

    public LiveChatView(Socket socket, BufferedReader entrada, PrintWriter saida, String nomeUsuario, String numeroUsuario) {
        initComponents();
        this.socket = socket;
        this.nomeUsuario = nomeUsuario;
        this.numeroUsuario = numeroUsuario;
        this.entrada = entrada;
        this.saida = saida;

        setVisible(true);

        setTitle("Chat - " + nomeUsuario);
        nomeUsuarioLabel.setText(nomeUsuario);
        listaUsuarios.setModel(listaUsuariosModel);

        new Thread(() -> {
            try {
                String msg;

                while ((msg = entrada.readLine()) != null) {
                    //Adição dos usuários na lista lateral
                    if (msg.startsWith("@lista:")) {
                        String lista = msg.substring(7);
                        listaUsuariosModel.clear();
                        for (String user : lista.split(",")) {
                            String u = user.trim();
                            if (!u.isEmpty() && !u.startsWith(numeroUsuario)) {
                                listaUsuariosModel.addElement(u);
                            }
                        }
                        //Mensagens
                    } else if (msg.contains(":")) {
                        String[] partes = msg.split(":", 2);
                        String remetente = partes[0].trim();
                        String corpo = partes[1].trim();

                        boolean privado = remetente.startsWith("[Privado]");
                        if (privado) {
                            String identificador = remetente.replace("[Privado]", "").trim();
                            String[] info = identificador.split(" - ", 2);
                            String numeroOutro = info[0].trim();
                            String nomeOutro = info[1].trim();

                            // Definindo chave da aba com base no OUTRO usuário
                            String chave;
                            if (numeroOutro.equals(numeroUsuario)) {
                                // Se eu estou recebendo uma mensagem de mim mesmo, a aba deve ser do destinatário
                                // então busco pela aba que estou conversando
                                chave = listaUsuarios.getSelectedValue(); // tentativa 1
                                if (chave == null) {
                                    chave = nomeOutro + " - " + numeroOutro; // fallback
                                }
                            } else {
                                chave = numeroOutro + " - " + nomeOutro;
                            }

                            // Abre a aba se não existir
                            if (!abasPrivadas.containsKey(chave)) {
                                abrirConversaPrivada(numeroOutro, nomeOutro);
                            }

                            JTextArea area = areasPrivadas.get(chave);
                            if (area != null) {
                                area.append(remetente + ": " + corpo + "\n");
                            }
                        } else {
                            chatArea.append(remetente + ": " + corpo + "\n");
                        }

                    }

                }
            } catch (IOException e) {
                chatArea.append("Erro ao receber mensagens.\n");
            }
        }).start();

    }

    private void abrirConversaPrivada(String numero, String nome) {
        String chave = numero + " - " + nome;
        if (!abasPrivadas.containsKey(chave)) {
            JPanel painel = new JPanel();
            painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));

            JTextArea area = new JTextArea();
            area.setSize(100, 259);
            area.setEditable(false);
            area.setColumns(20);
            area.setRows(5);
            area.setFocusable(false);
            JScrollPane scroll = new JScrollPane(area);
            areasPrivadas.put(chave, area);

            JTextField campo = new JTextField();
            JButton enviar = new JButton("Enviar");

            JPanel barra = new JPanel();
            barra.setLayout(new BoxLayout(barra, BoxLayout.X_AXIS));
            barra.add(campo);
            barra.add(enviar);

            enviar.addActionListener(e -> {
                String texto = campo.getText().trim();
                if (!texto.isEmpty()) {
                    String numeroDest = chave.split(" - ")[0];
                    saida.println("@" + numeroDest + ":" + texto);
                    campo.setText("");
                }
            });

            campo.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        enviar.doClick();
                    }
                }
            });

            painel.add(scroll);
            painel.add(barra);

            abasPrivadas.put(chave, painel);
            jTab.addTab(chave, painel);
        }
        jTab.setSelectedComponent(abasPrivadas.get(chave));
    }

    private void enviarMensagem() {
        String texto = mensagemCampo.getText().trim();
        if (texto.isEmpty()) {
            return;
        }

        String abaAtual = jTab.getTitleAt(jTab.getSelectedIndex());
        String mensagem;
        if (abaAtual.equals("Geral")) {
            mensagem = "@todos:" + texto;
        } else {
            String numeroDest = abaAtual.split(" - ")[0];
            mensagem = "@" + numeroDest + ":" + texto;
        }

        saida.println(mensagem);
        mensagemCampo.setText("");
    }

    private JPanel criarPainelPrivado(String chave, JTextArea areaMensagens) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));

        JTextArea chatPrivado = areaMensagens;
        chatPrivado.setEditable(false);
        JScrollPane scrollChat = new JScrollPane(chatPrivado);

        JTextField campoMensagem = new JTextField();
        JButton botaoEnviar = new JButton("Enviar");

        // Ação do botão
        botaoEnviar.addActionListener(e -> {
            String texto = campoMensagem.getText().trim();
            if (!texto.isEmpty()) {
                String numeroDest = chave.split(" - ")[0];
                saida.println("@" + numeroDest + ":" + texto);
                campoMensagem.setText("");
            }
        });

        // Tecla Enter no campo de mensagem
        campoMensagem.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    botaoEnviar.doClick();
                }
            }
        });

        painel.add(scrollChat);
        painel.add(campoMensagem);
        painel.add(botaoEnviar);
        return painel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTab = new javax.swing.JTabbedPane();
        jP1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        mensagemCampo = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nomeUsuarioLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaUsuarios = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Enviar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        mensagemCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mensagemCampoActionPerformed(evt);
            }
        });
        mensagemCampo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mensagemCampoKeyPressed(evt);
            }
        });

        chatArea.setColumns(20);
        chatArea.setRows(5);
        chatArea.setFocusable(false);
        jScrollPane2.setViewportView(chatArea);

        jLabel1.setText("Mensagens");

        jLabel3.setText("Usuários");

        nomeUsuarioLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nomeUsuarioLabel.setText("Nickname");
        nomeUsuarioLabel.setToolTipText("");

        listaUsuarios.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

            public int getSize() {
                return strings.length;
            }

            public String getElementAt(int i) {
                return strings[i];
            }
        });
        listaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listaUsuariosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listaUsuarios);

        javax.swing.GroupLayout jP1Layout = new javax.swing.GroupLayout(jP1);
        jP1.setLayout(jP1Layout);
        jP1Layout.setHorizontalGroup(
                jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jP1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jP1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(mensagemCampo)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 418,
                                                Short.MAX_VALUE)
                                        .addGroup(jP1Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(nomeUsuarioLabel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jP1Layout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane3)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 119,
                                                Short.MAX_VALUE))
                                .addGap(6, 6, 6)));
        jP1Layout.setVerticalGroup(
                jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jP1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel1)
                                        .addComponent(nomeUsuarioLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 202,
                                                Short.MAX_VALUE)
                                        .addComponent(jScrollPane3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jP1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(mensagemCampo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        jTab.addTab("Geral", jP1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTab));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTab));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void mensagemCampoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mensagemCampoActionPerformed
    }// GEN-LAST:event_mensagemCampoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
    }// GEN-LAST:event_jButton1ActionPerformed

    private void mensagemCampoKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_mensagemCampoKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            enviarMensagem();

        }
    }// GEN-LAST:event_mensagemCampoKeyPressed

    private void listaUsuariosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_listaUsuariosMouseClicked
        if (evt.getClickCount() == 2) {
            String selecionado = listaUsuarios.getSelectedValue();
            if (selecionado != null && selecionado.contains(" - ")) {
                String numero = selecionado.split(" - ")[0];
                String nome = selecionado.split(" - ")[1];
                abrirConversaPrivada(numero, nome);
            }
        }
    }// GEN-LAST:event_listaUsuariosMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LiveChatView.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LiveChatView.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LiveChatView.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LiveChatView.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chatArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jP1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTab;
    private javax.swing.JList<String> listaUsuarios;
    private javax.swing.JTextField mensagemCampo;
    private javax.swing.JLabel nomeUsuarioLabel;
    // End of variables declaration//GEN-END:variables
}
