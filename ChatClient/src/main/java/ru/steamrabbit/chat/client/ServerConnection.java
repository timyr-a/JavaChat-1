package ru.steamrabbit.chat.client;

import ru.steamrabbit.chat.client.listener.ServerConnectionListener;
import ru.steamrabbit.chat.client.listener.TextMessageListener;
import ru.steamrabbit.chat.share.Connection;
import ru.steamrabbit.chat.share.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

public final class ServerConnection extends Connection {
    // TODO: вынести настройки соединения клиента с сервером в ini-файл
    private String ip;
    private int port;

    private volatile ServerConnectionListener listener;
    private volatile TextMessageListener textMessageListener;
    private State state;

    static {
        LOG_NAME = "CLIENT.connection";
    }

    public ServerConnection() {
        try (InputStream input = this.getClass().getResourceAsStream("/ini/connection.ini")) {
            Properties props = new Properties();
            props.load(input);

            ip   = (String) props.remove("ip");
            port = Integer.parseInt((String) props.remove("port"));
        } catch (IOException e) {
            // TODO: обработать ошибку
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log("запуск соединения с сервером...");

        try {
            clientSocket = new Socket(ip, port);

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in  = new ObjectInputStream(clientSocket.getInputStream());

            log("соединение с сервером: " + clientSocket);
            if (listener != null) listener.onConnectionSuccess();

            state = State.IDLE;

            while (true) {
                switch (state) {
                    case IDLE:
                        synchronized (this) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                log("соединение прервано!");
                                return;
                            }
                        }
                        break;
                    case WORK:
                        Message message = getMessage();

                        if (message == null) return;

                        log(message.getType().toString());

                        textMessageListener.onTextMessageReceive(message.getEntry(Message.Key.USERNAME),
                                                                 message.getEntry(Message.Key.TEXT));
                        break;
                }
            }

        } catch (IOException e) {
            log("ошибка при соединении: " + e);
            close();

            if (listener != null) listener.onConnectionError("Нет подключения с сервером!");
        }

        log("соединение с сервером завершено.");
    }

    public void setTextMessageListener(TextMessageListener textMessageListener) {
        this.textMessageListener = textMessageListener;
    }

    public synchronized void setToWork() {
        state = State.WORK;
        notify();
    }

    void setListener(ServerConnectionListener listener) {
        this.listener = listener;
    }

    private enum State {
        IDLE,
        WORK
    }
}
