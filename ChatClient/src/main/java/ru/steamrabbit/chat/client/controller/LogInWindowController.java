package ru.steamrabbit.chat.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import ru.steamrabbit.chat.client.ServerConnection;
import ru.steamrabbit.chat.client.listener.LogInWindowListener;
import ru.steamrabbit.chat.share.Message;

public class LogInWindowController {
    private static final String LOG_NAME = "CLIENT.loginWindow";

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private VBox logInWindowRoot;
    @FXML private Button logInButton;
    @FXML private Button registerButton;
    @FXML private Label warningLabel;

    private LogInWindowListener listener;
    private ServerConnection connection;

    public void setListener(LogInWindowListener listener) {
        this.listener = listener;
    }

    public void setConnection(ServerConnection connection) {
        this.connection = connection;
    }

    public void onWindowKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                if (loginField.isFocused() || passwordField.isFocused() || logInButton.isFocused()) {
                    logIn();
                } else if (registerButton.isFocused()) {
                    toRegistrationWindow();
                }
                break;
            case ESCAPE:
                resetFocus();
                break;
        }
    }

    public void onLogInButtonAction() {
        logIn();
    }

    public void onRegisterButtonAction() {
        toRegistrationWindow();
    }

    private void logIn() {
        log("авторизация...");

        String login = loginField.getText().trim();
        if (login.isEmpty()) {
            log("ошибка авторизации: не введен логин!");
            setWarningText("Ошибка: введите логин!");
            loginField.requestFocus();
            return;
        }

        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            log("ошибка авторизации: не введен пароль!");
            setWarningText("Ошибка: введите пароль!");
            passwordField.requestFocus();
            return;
        }

        if (connection == null) {
            log("ошибка авторизации: не назначено соединение!");
            if (listener != null) listener.onConnectionError("Нет подключения с сервером!");
            return;
        }

        if (connection.sendMessage(Message.authRequest(login, password))) {
            Message authResponse = connection.getMessage();

            switch (authResponse.getType()) {
                case AUTH_SUCCESS:
                    log("авторизация прошла успешно!");
                    if (listener != null) listener.onLogInSuccess();
                    break;
                case AUTH_FAIL:
                    log("ошибка авторизации: " + authResponse.getEntry(Message.Key.FAIL_CAUSE));
                    setWarningText("Ошибка: " + authResponse.getEntry(Message.Key.FAIL_CAUSE));
                    break;
                default:
                    log("ошибка авторизации: получено неожиданное сообщение!");
                    setWarningText("Ошибка: ошибка сервера!");
                    break;
            }
        }

    }

    private void toRegistrationWindow() {
        if (listener != null) listener.onRegistrationRequest();
    }

    private void setWarningText(String text) {
        Platform.runLater(() -> warningLabel.setText(text));
    }

    private void resetFocus() {
        logInWindowRoot.requestFocus();
    }

    private static void log(String msg) {
        System.out.println(LOG_NAME + ": " + msg);
    }
}
