package ru.steamrabbit.chat.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ErrorWindowController {
    @FXML private Label errorLabel;

    public void setErrorLabel(String str) {
        Platform.runLater(() -> errorLabel.setText(str));
    }

    public void onCloseButtonAction() {
        Platform.exit();
    }

    public void onWindowKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Platform.exit();
        }
    }
}
