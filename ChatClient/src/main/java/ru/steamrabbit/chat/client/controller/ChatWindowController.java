package ru.steamrabbit.chat.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import ru.steamrabbit.chat.client.ServerConnection;
import ru.steamrabbit.chat.client.listener.ChatWindowListener;
import ru.steamrabbit.chat.client.listener.TextMessageListener;
import ru.steamrabbit.chat.share.Message;

public class ChatWindowController implements TextMessageListener {
    private static final String LOG_NAME = "CLIENT.chatWindow";

    @FXML private VBox chatWindowRoot;
    @FXML private TextArea messageArea;
    @FXML private TextArea chatArea;
    @FXML private Button sendMessageButton;

    private boolean isReadyToSend;
    private ChatWindowListener listener;
    private ServerConnection connection;

    @FXML
    private void initialize() {
        setIsReadyToSend(false);

        // Смена логики работы клавиши ENTER в поле ввода сообщения:
        // ENTER - отправить сообщение, ALT+ENTER - на новую строку
        messageArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();

            if (keyCode == KeyCode.ENTER) {
                if (event.isAltDown()) messageArea.appendText("\n");
                else                   sendMessageToServer();
                event.consume();
            }
        });

        System.out.println("ChatWindowController: инициализованно...");
    }

    public void setConnection(ServerConnection connection) {
        this.connection = connection;
        connection.setTextMessageListener(this);
        connection.setToWork();
    }

    public void onWindowKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) resetFocus();
    }

    public void onMessageAreaKeyReleased() {
        String text = messageArea.getText();
        setIsReadyToSend((text != null && text.trim().length() > 0));
    }

    public void onSendMessageButtonAction() {
        sendMessageToServer();
    }

    private void sendMessageToServer() {
        System.out.println("ChatWindowController: sendMessageToServer()");

        if (isReadyToSend) {
            // TODO: логика отправки сообщения серверу
            connection.sendMessage(Message.postText(messageArea.getText().trim()));

            messageArea.clear();
            messageArea.requestFocus();
            setIsReadyToSend(false);
        }
    }

    private void setIsReadyToSend(boolean value) {
        isReadyToSend = value;
        sendMessageButton.setDisable(!isReadyToSend);
    }

    private void resetFocus() {
        chatWindowRoot.requestFocus();
    }

    public void setListener(ChatWindowListener listener) {
        this.listener = listener;
    }

    private void log(String msg) {
        System.out.println(LOG_NAME + ": " + msg);
    }

    @Override
    public void onTextMessageReceive(String name, String text) {
        chatArea.appendText(name + ": " + text + "\n\n");
    }
}
