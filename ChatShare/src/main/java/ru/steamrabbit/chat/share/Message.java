package ru.steamrabbit.chat.share;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    private final Type type;
    private HashMap<Key, String> content;

    private Message(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getEntry (Key key) {
        if (content == null) return null;
        return content.get(key);
    }

    public static Message authRequest(String login, String password) {
        Message message = new Message(Type.AUTH_REQUEST);

        message.content = new HashMap<>(2);
        message.content.put(Key.LOGIN, login);
        message.content.put(Key.PASSWORD, password);

        return message;
    }

    public static Message authSuccess() {
        return new Message(Type.AUTH_SUCCESS);
    }

    public static Message authFail(String failCause) {
        Message message = new Message(Type.AUTH_FAIL);

        message.content = new HashMap<>(1);
        message.content.put(Key.FAIL_CAUSE, failCause);

        return message;
    }

    public static Message regRequest(String name, String login, String password) {
        Message message = new Message(Type.REG_REQUEST);

        message.content = new HashMap<>(3);
        message.content.put(Key.USERNAME, name);
        message.content.put(Key.LOGIN, login);
        message.content.put(Key.PASSWORD, password);

        return message;
    }

    public static Message regSuccess() {
        return new Message(Type.REG_SUCCESS);
    }

    public static Message regFail(String failCause) {
        Message message = new Message(Type.REG_FAIL);

        message.content = new HashMap<>(1);
        message.content.put(Key.FAIL_CAUSE, failCause);

        return message;
    }

    public static Message postText(String text) {
        Message message = new Message(Type.TEXT_POST);

        message.content = new HashMap<>(1);
        message.content.put(Key.TEXT, text);

        return message;
    }

    public static Message receiveText(String username, String text) {
        Message message = new Message(Type.TEXT_RECEIVE);

        message.content = new HashMap<>(2);
        message.content.put(Key.USERNAME, username);
        message.content.put(Key.TEXT, text);

        return message;
    }


    public enum Type {
        // аутентификация
        AUTH_REQUEST,
        AUTH_SUCCESS,
        AUTH_FAIL,

        // регистрация
        REG_REQUEST,
        REG_SUCCESS,
        REG_FAIL,

        // обмен сообщениями
        TEXT_POST,
        TEXT_RECEIVE,

        // специальные
        LOGOUT

    }

    public enum Key {
        LOGIN,
        PASSWORD,
        USERNAME,
        FAIL_CAUSE,
        TEXT
    }
}
