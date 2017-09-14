package ru.steamrabbit.chat.client.listener;

public interface ServerConnectionListener {
    void onConnectionSuccess();
    void onConnectionError(String errMsg);
}
