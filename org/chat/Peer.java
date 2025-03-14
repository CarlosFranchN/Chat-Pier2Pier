package org.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author londres-27
 */
public class Peer {
    private String username;
    private ServerSocket serverSocket;
    private HashMap<String,Socket> connections = new HashMap<>();
    // private List<Socket> connections = new ArrayList<>();

    public Peer(String username, int port){
        this.username = username;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Peer " + username + "está ouvindo a porta " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        new Thread(this::listenForConnections).start();
        new Thread(this::listenForUserInput).start();
    }

    private void listenForConnections(){
        while (true) { 
         try {
             Socket socket = serverSocket.accept();
            //  connections.add(socket);
             new Thread(() -> handleConnection(socket)).start();
         } catch (IOException e) {
            e.printStackTrace();
         }   
        }
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
        while (true) {
        String message = userInput.readLine();
        broadcastMessage(message);
        }
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

    private void broadcastMessage(String message) {
        for (Socket socket : connections) {
            try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username + ": " + message);
            } catch (IOException e) {
            e.printStackTrace();
            }
            } 
    }

    public void connectToPeer(String host, int porta) {
        try {
            Socket socket = new Socket(host, porta);
            connections.add(socket);
            new Thread(() -> handleConnection(socket)).start();
            System.out.println("Conectado com sucesso! Em peer " + host + ":" + porta);
        } catch (IOException e) {
            System.out.println("Falha em conectar! Em peer " + host + ":" + porta);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o seu nome do usuario: ");
        String userName = scanner.nextLine();

        System.out.println("Digite a porta para escutar: ");
        int porta = scanner.nextInt();
        scanner.nextLine();

        Peer peer = new Peer(userName, porta);
        peer.start();

        System.out.println("Deseja conectar com outra pessoa? (s/n)");
        String resposta = scanner.nextLine();

        if (resposta.equalsIgnoreCase("s")) {
            System.out.println("Digite o endereço do peer (host): ");
            String peerHost = scanner.nextLine();

            System.out.println("Digite a porta do peer: ");
            int portaPeer = scanner.nextInt();
            scanner.nextLine();

            peer.connectToPeer(peerHost, portaPeer);
        }
        scanner.close();
    }
}
