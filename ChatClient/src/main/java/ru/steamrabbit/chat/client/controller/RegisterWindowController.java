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
import ru.steamrabbit.chat.client.listener.RegisterWindowListener;
import ru.steamrabbit.chat.share.Message;

public class RegisterWindowController {
    private static final String LOG_NAME = "CLIENT.registerWindow";

    @FXML
    private VBox registerWindowRoot;
    @FXML private TextField nameField;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Label warningLabel;

    private RegisterWindowListener listener;
    private ServerConnection connection;

    public void setListener(RegisterWindowListener listener) {
        this.listener = listener;
    }

    public void setConnection(ServerConnection connection) {
        this.connection = connection;
    }

    public void onWindowKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                if (nameField.isFocused() || loginField.isFocused() || passwordField.isFocused() ||
                        confirmPasswordField.isFocused() || registerButton.isFocused()) {
                    registerNewUser();
                } else if (cancelButton.isFocused()) {
                    toLoginWindow();
                }
                break;
            case ESCAPE:
                resetFocus();
                break;
        }
    }

    public void onRegisterButtonAction() {
        registerNewUser();
    }

    public void onCancelButtonAction() {
        toLoginWindow();
    }

    private void registerNewUser() {
        log("регистрация нового пользователя...");

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            log("ошибка регистрации: не введено имя пользователя!");
            setWarningText("Ошибка: введите имя пользователя!");
            nameField.requestFocus();
            return;
        }

        String login = loginField.getText().trim();
        if (login.isEmpty()) {
            log("ошибка регистрации: не введен логин!");
            setWarningText("Ошибка: введите логин!");
            loginField.requestFocus();
            return;
        }

        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            log("ошибка регистрации: не введен пароль!");
            setWarningText("Ошибка: введите пароль!");
            passwordField.requestFocus();
            return;
        }

        String confirmPassword = confirmPasswordField.getText().trim();
        if (confirmPassword.isEmpty()) {
            log("ошибка регистрации: не введено подтверждение пароля!");
            setWarningText("Ошибка: подтвердите пароль!");
            confirmPasswordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            log("ошибка регистрации: пароли не совпадают");
            setWarningText("Ошибка: пароли не совпадают!");
            confirmPasswordField.clear();
            confirmPasswordField.requestFocus();
            return;
        }

        if (connection == null) {
            log("ошибка авторизации: не назначено соединение!");
            if (listener != null) listener.onConnectionError("Нет подключения с сервером!");
            return;
        }

        if (connection.sendMessage(Message.regRequest(name, login, password))) {
            Message regResponse = connection.getMessage();

            switch (regResponse.getType()) {
                case REG_SUCCESS:
                    log("регистрация прошла успешно!");
                    if (listener != null) listener.onLoginRequest();
                    break;
                case REG_FAIL:
                    log("ошибка регистрация: " + regResponse.getEntry(Message.Key.FAIL_CAUSE));
                    setWarningText("Ошибка: " + regResponse.getEntry(Message.Key.FAIL_CAUSE));
                    break;
                default:
                    log("ошибка авторизации: получено неожиданное сообщение!");
                    setWarningText("Ошибка: ошибка сервера!");
                    break;
            }
        }
    }

    private void toLoginWindow() {
        if (listener != null) listener.onLoginRequest();
    }

    public void setWarningText(String text) {
        Platform.runLater(() -> warningLabel.setText(text));
    }

    private void resetFocus() {
        registerWindowRoot.requestFocus();
    }

    private static void log(String msg) {
        System.out.println(LOG_NAME + ": " + msg);
    }
}
