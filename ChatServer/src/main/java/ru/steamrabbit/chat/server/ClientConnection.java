package ru.steamrabbit.chat.server;

import ru.steamrabbit.chat.server.exception.AuthenticationException;
import ru.steamrabbit.chat.server.exception.RegisterException;
import ru.steamrabbit.chat.server.listener.ClientConnectionListener;
import ru.steamrabbit.chat.share.Connection;
import ru.steamrabbit.chat.share.Message;
import ru.steamrabbit.chat.share.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Date;
import java.util.concurrent.BlockingQueue;

public class ClientConnection extends Connection {
    private DBService dbService;

    private ClientConnectionListener listener;
    private BlockingQueue<ClientConnection> connections;
    private User user;

    static {
        LOG_NAME = "SERVER.connection";
    }

    ClientConnection(Socket clientSocket, BlockingQueue<ClientConnection> connections, DBService dbService) {
        if (clientSocket == null) throw new NullPointerException();
        if (connections  == null) throw new NullPointerException();
        if (dbService    == null) throw new NullPointerException();

        this.clientSocket = clientSocket;
        this.connections  = connections;
        this.dbService    = dbService;
    }

    void setListener(ClientConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in  = new ObjectInputStream(clientSocket.getInputStream());

            listenConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    private void listenConnection() {
        while (true) {
            Message message = getMessage();

            // Если получено сообщение null, то канал связи разорван и следует закрыть соединение
            if (message == null) return;

            log("получено сообщение: " + message.getType());

            switch (message.getType()) {
                case AUTH_REQUEST:
                    user = null;

                    try {
                        user = dbService.authentication(message.getEntry(Message.Key.LOGIN),
                                message.getEntry(Message.Key.PASSWORD));

                        // проверка на повторную авторизацию одно и того же пользователя
                        for (ClientConnection connection : connections) {
                            if (connection != this) {
                                if (connection.user.equals(this.user)) {
                                    this.user = null;
                                    throw new AuthenticationException("пользователь уже в сети!");
                                }
                            }
                        }
                    } catch (AuthenticationException e) {
                        sendMessage(Message.authFail(e.getMessage()));
                    }

                    if (user != null) sendMessage(Message.authSuccess());
                    break;
                case REG_REQUEST:
                    try {
                        dbService.registerNewUser(message.getEntry(Message.Key.USERNAME),
                                message.getEntry(Message.Key.LOGIN),
                                message.getEntry(Message.Key.PASSWORD));
                        log("новый пользователь зарегестрирован успешно.");
                        sendMessage(Message.regSuccess());
                    } catch (RegisterException e) {
                        log("ошибка при регистрации нового пользователя: " + e.getMessage());
                        sendMessage(Message.regFail(e.getMessage()));
                    }
                    break;
                case TEXT_POST:
                    Message dispatch = Message.receiveText(user.getName(), message.getEntry(Message.Key.TEXT));

                    // сохранить сообщение в БД
                    dbService.saveMessage(user, message.getEntry(Message.Key.TEXT));

                    connections.forEach(connection -> {
                        if (isOpened()) {
                            if (connection.isAuthorized()) {
                                connection.sendMessage(dispatch);
                            }
                        } else {
                            connection.close();
                        }
                    });
                    break;
            }
        }
    }

    public boolean isAuthorized() {
        return user != null;
    }

    @Override
    public void close() {
        super.close();
        if (listener != null) listener.onConnectionClosed(this);
    }
}
