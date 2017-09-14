package ru.steamrabbit.chat.client.listener;

public interface RegisterWindowListener {
    void onConnectionError(String errMsg);
    void onLoginRequest();
}
