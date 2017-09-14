package ru.steamrabbit.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.steamrabbit.chat.client.controller.ChatWindowController;
import ru.steamrabbit.chat.client.controller.LogInWindowController;
import ru.steamrabbit.chat.client.controller.RegisterWindowController;
import ru.steamrabbit.chat.client.listener.ChatWindowListener;
import ru.steamrabbit.chat.client.listener.LogInWindowListener;
import ru.steamrabbit.chat.client.listener.RegisterWindowListener;
import ru.steamrabbit.chat.client.listener.ServerConnectionListener;
import ru.steamrabbit.chat.client.controller.ErrorWindowController;

import java.io.IOException;

public class Client extends Application implements ChatWindowListener,
                                                   LogInWindowListener,
                                                   RegisterWindowListener,
                                                   ServerConnectionListener {
    private Stage stage;
    private ServerConnection connection;

    @Override
    public void start(Stage stage) throws Exception {
        log("запуск клиента...");
        this.stage = stage;
        connectToServer();
    }

    /**
     * Создать соединения с сервером
     */
    private void connectToServer() {
        connection = new ServerConnection();
        connection.setListener(this);
        connection.start();
    }

    /**
     * Создать окно об ошибке и завершить приложение
     * @param errMsg    сообщение об ошибке
     */
    private void createErrorWindow(String errMsg) throws IOException {
        log("создание окна ошибки...");

        FXMLLoader loader  = new FXMLLoader(getClass().getResource("/fxml/errorWindow.fxml"));
        Parent loginWindow = loader.load();

        ErrorWindowController errorWindowController = loader.getController();
        errorWindowController.setErrorLabel(errMsg);

        stage.setTitle("Ошибка");
        stage.setScene(new Scene(loginWindow));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Создать окно логина, из него возможно перейти в окно регистрации
     */
    private void createLoginWindow() throws IOException {
        log("создание окна входа...");

        FXMLLoader loader  = new FXMLLoader(getClass().getResource("/fxml/logInWindow.fxml"));
        Parent loginWindow = loader.load();

        LogInWindowController logInWindowController = loader.getController();
        logInWindowController.setConnection(connection);
        logInWindowController.setListener(this);

        stage.setTitle("Вход в чат");
        stage.setScene(new Scene(loginWindow));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Создать окно регистрации нового пользователя, из него можно вернуться в окно логина
     */
    private void createRegisterWindow() throws IOException {
        log("создание окна регистрации нового пользователя...");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registerWindow.fxml"));
        Parent registerWindow = loader.load();

        RegisterWindowController registerWindowController = loader.getController();
        registerWindowController.setConnection(connection);
        registerWindowController.setListener(this);

        stage.setTitle("Регистрация пользователя");
        stage.setScene(new Scene(registerWindow));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Создать основное окно чата
     */
    private void createChatWindow() throws IOException {
        log("создание окна чата...");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chatWindow.fxml"));
        Parent chatWindow = loader.load();

        ChatWindowController chatWindowController = loader.getController();
        chatWindowController.setConnection(connection);
        chatWindowController.setListener(this);

        stage.setTitle("Чат (пользователь: %USER_NAME%)");
        stage.setScene(new Scene(chatWindow));
        stage.setResizable(true);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    private static void log(String msg) {
        System.out.println("CLIENT: " + msg);
    }

    @Override
    public void stop() throws Exception {
        if (connection.isOpened()) {
            connection.close();
            connection.interrupt();
        }
        log("приложение завершено.");
    }

    /*
     * ОБРАБОТКА СОБЫТИЙ
     */

    @Override
    public void onConnectionSuccess() {
        Platform.runLater(() -> {
            try {
                createLoginWindow();
            } catch (IOException e) {
                onConnectionError("Ошибка: " + e);
            }
        });
    }

    @Override
    public void onLogInSuccess() {
        Platform.runLater(() -> {
            try {
                createChatWindow();
            } catch (IOException e) {
                onConnectionError("Ошибка: " + e);
            }
        });
    }

    @Override
    public void onConnectionError(String errMsg) {
        Platform.runLater(() -> {
            try {
                createErrorWindow(errMsg);
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        });
    }

    @Override
    public void onRegistrationRequest() {
        Platform.runLater(() -> {
            try {
                createRegisterWindow();
            } catch (IOException e) {
                onConnectionError("Ошибка: " + e);
            }
        });
    }

    @Override
    public void onLoginRequest() {
        Platform.runLater(() -> {
            try {
                createLoginWindow();
            } catch (IOException e) {
                onConnectionError("Ошибка: " + e);
            }
        });
    }


    /*
     * ЗАПУСК
     */
    public static void main(String[] args) {
        launch(args);
    }
}
