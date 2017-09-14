package ru.steamrabbit.chat.server;

import ru.steamrabbit.chat.server.listener.ClientConnectionListener;
import ru.steamrabbit.chat.share.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements ClientConnectionListener {
    // TODO: вынести настройки соединения в ini-файл
    private int port = -1;

    private boolean isActive = true;
    private ServerSocket serverSocket;
    private BlockingQueue<ClientConnection> connections = new LinkedBlockingQueue<>(50);

    private Server() {
        log("создание сервера...");
        try (InputStream input = this.getClass().getResourceAsStream("/ini/connection.ini")) {
            Properties props = new Properties();
            props.load(input);

            port = Integer.parseInt((String) props.remove("port"));
        } catch (IOException e) {
            // TODO: обработать ошибку
            e.printStackTrace();
        }

        if (port > -1) {
            // подключение к базе данных
            DBService dbService = new DBService();
            dbService.open();
            if (!dbService.isOpened()) {
                log("ошибка при создании сервера: не удалось подключиться к базе данных.");
                System.exit(1);
            }

            try {
                serverSocket = new ServerSocket(port);

                while (isActive) {
                    log("ожидание соединение клиентов...");
                    Socket clientSocket = serverSocket.accept();

                    ClientConnection connection = new ClientConnection(clientSocket, connections, dbService);
                    connections.put(connection);
                    log("добавлено новое соединение. Всего соединений: " + connections.size() + ".");

                    connection.setListener(this);
                    connection.start();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        } else {
            log("ошибка порт " + port);
        }
    }

    private void close() {
        // TODO: закрытие сервера
        connections.forEach(connection -> close());
        connections.clear();
    }

    private static void log(String msg) {
        System.out.println("SERVER: " + msg);
    }

    @Override
    public void onTextMessageReceived(Message message) {

    }

    @Override
    public void onConnectionClosed(ClientConnection connection) {
        connections.remove(connection);
        log("соединение удалено. Всего соединений: " + connections.size() + ".");
    }

    public static void main(String[] args) {
        new Server();
    }
}
