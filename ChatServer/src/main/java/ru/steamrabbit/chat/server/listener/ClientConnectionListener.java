package ru.steamrabbit.chat.server.listener;

import ru.steamrabbit.chat.server.ClientConnection;
import ru.steamrabbit.chat.share.Message;

public interface ClientConnectionListener {
    void onTextMessageReceived(Message message);
    void onConnectionClosed(ClientConnection connection);
}
