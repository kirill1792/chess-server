package ru.kirill.chess.model;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PlayServer {
    private ServerSocket serverSocket;
    private Map<Long, ClientSocket> playerSockets = new ConcurrentHashMap<>();
    private ExecutorService acceptConnectionsExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean started;

    public PlayServer() {
        start(9075);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server socket started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        started = true;
        acceptConnectionsExecutor.submit(this::acceptConnections);
    }

    public void stop() {
        try {
            started = false;
            serverSocket.close();
            playerSockets.values().forEach(this::closeClientSocket);
            playerSockets.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removePlayer(long playerId) {
        ClientSocket removedSocket = playerSockets.remove(playerId);
        if (removedSocket != null) {
            try {
                System.out.printf("Closed socket for playerId: %s\n", playerId);
                closeClientSocket(removedSocket);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        return removedSocket != null;
    }

    public String sendDataToPlayer(long playerId, String data) {
        ClientSocket socket = playerSockets.get(playerId);
        sendData(socket, data);
        return receiveData(socket);
    }

    public Set<Long> getPlayerIds() {
        return playerSockets.keySet();
    }

    private void acceptConnections() {
        System.out.println("Accepting connections on port " + serverSocket.getLocalPort() + "...");
        while (started) {
            try {
                Socket socket = serverSocket.accept();
                System.out.printf("Accept connection to client %s\n", socket.getRemoteSocketAddress());
                handleClientSocket(socket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendData(ClientSocket socket, String data) {
        var out = new DataOutputStream(socket.getOut());
        try {
            out.writeUTF(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String receiveData(ClientSocket socket) {
        var in = new DataInputStream(socket.getIn());
        try {
            return in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeClientSocket(ClientSocket socket) {
        try {
            socket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClientSocket(Socket socket) {
        ClientSocket clientSocket = new ClientSocket(socket);
        String playerData = receiveData(clientSocket);
        long playerId = Long.parseLong(playerData);
        System.out.printf("Received playerId: %s\n", playerId);
        removePlayer(playerId);
        playerSockets.put(playerId, clientSocket);
    }

    private static class ClientSocket implements AutoCloseable {
        private Socket socket;
        private InputStream in;
        private OutputStream out;

        public ClientSocket(Socket socket) {
            this.socket = socket;
            try {
                this.in = socket.getInputStream();
                this.out = socket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            in.close();
            out.close();
            socket.close();
        }

        public Socket getSocket() {
            return socket;
        }

        public InputStream getIn() {
            return in;
        }

        public OutputStream getOut() {
            return out;
        }
    }
}