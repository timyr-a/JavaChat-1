package ru.steamrabbit.chat.share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Connection extends Thread {
    protected static String LOG_NAME = "SHARE.connection";

    protected Socket clientSocket;
    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public Message getMessage() {
        Message message = null;

        try {
            message = (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log("получить сообщение не удалось! Ошибка: " + e.toString());

            if (e.getClass() == SocketException.class) interrupt();
        }

        return message;
    }

    public boolean sendMessage(Message message) {
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            log("отправить сообщение не удалось! Ошибка: " + e.toString());
            return false;
        }
    }

    public boolean isOpened() {
        return !clientSocket.isClosed() && in != null && out != null;
    }

    public void close() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                log("произошла ошибка при закрытии выходного потока!");
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log("произошла ошибка при закрытии входного потока!");
            }
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log("произошла ошибка при закрытии сокета!");
            }
        }

        log("соединение закрыто.");
    }

    protected static void log(String msg) {
        System.out.println(LOG_NAME + ": " + msg);
    }
}
