package ru.steamrabbit.chat.client.listener;

public interface LogInWindowListener {
    void onConnectionError(String errMsg);
    void onLogInSuccess();
    void onRegistrationRequest();
}
