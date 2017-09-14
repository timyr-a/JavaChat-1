package ru.steamrabbit.chat.client.listener;

public interface TextMessageListener {
    void onTextMessageReceive(String name, String text);
}
