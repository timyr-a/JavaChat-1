package ru.steamrabbit.chat.server;

import ru.steamrabbit.chat.server.exception.AuthenticationException;
import ru.steamrabbit.chat.server.exception.RegisterException;
import ru.steamrabbit.chat.share.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

public class DBService implements AutoCloseable {
    private java.sql.Connection connection;
    private PreparedStatement getUser;
    private PreparedStatement registerUser;
    private PreparedStatement saveMessage;

    private Properties props = new Properties();
    private String url = "";
    private String schema = "";

    public DBService() {
        try (InputStream input = this.getClass().getResourceAsStream("/ini/db.ini")) {
            props.load(input);
            url = (String) props.remove("url");
            schema = (String) props.remove("schema");
        } catch (IOException e) {
            // TODO: обработать ошибку
            e.printStackTrace();
        }
    }

    public User authentication(String login, String password) throws AuthenticationException {
        log("аутентификация пользователя (login:" + login + " password:" + password + ")...");
        User user;

        if (isOpened()) {
            try {
                getUser.setString(1, login);
            } catch (SQLException e) {
                log("произошла ошибка при атунтификации пользователя: " + e.toString());
                throw new AuthenticationException("ошибка БД!", e);
            }

            try {
                ResultSet result = getUser.executeQuery();

                if (result.next()) {
                    if (password.equals(result.getString("password"))) {
                        user = new User(result.getInt("id"), result.getString("name"));
                    } else {
                        log("произошла ошибка при атунтификации пользователя: неправильный пароль!");
                        throw new AuthenticationException("неверная пара логин/пароль!");
                    }
                } else {
                    log("произошла ошибка при атунтификации пользователя: нет такого пользователя");
                    throw new AuthenticationException("неверная пара логин/пароль!");
                }
            } catch (SQLException e) {
                log("произошла ошибка при атунтификации пользователя: " + e.toString());
                throw new AuthenticationException("ошибка БД!", e);
            }

        } else {
            log("произошла ошибка при атунтификации пользователя: нет соединения с базой данных!");
            throw new AuthenticationException("нет соединения с БД!");
        }

        return user;
    }

    public void registerNewUser(String name, String login, String password) throws RegisterException {
        log("регистрация нового пользователя...");

        if (isOpened()) {
            try {
                registerUser.setString(1, name);
                registerUser.setString(2, login);
                registerUser.setString(3, password);
                if (registerUser.executeUpdate() == 0) {
                    log("произошла ошибка при регистрации нового пользователя: пользователь не добавлен!");
                    throw new RegisterException("ошибка базы данных!");
                }
            } catch (SQLException e) {
                log("произошла ошибка при регистрации нового пользователя: " + e.toString());
                if (e.getErrorCode() == 1062) throw new RegisterException("такой пользователь уже существует!", e);
                else                          throw new RegisterException("ошибка базы данных!", e);
            }
        } else {
            log("произошла ошибка при регистрации нового пользователя: нет соединения с базой данных!");
            throw new RegisterException("нет соединения с базой данных!");
        }
    }

    public boolean saveMessage(User user, String message) {
        if (isOpened()) {
            try {
                saveMessage.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                saveMessage.setInt(2, user.getId());
                saveMessage.setString(3, message);

                if (saveMessage.executeUpdate() > 0) {
                    return true;
                } else {
                    log("произошла ошибка при сохранении сообщения в базу данных: сообщение не сохранено!");
                }
            } catch (SQLException e) {
                log("произошла ошибка при сохранении сообщения в базу данных: " + e);
            }
        }

        return false;
    }

    public boolean open() {
        log("открытие соединения с базой данных...");

        try {
            connection = DriverManager.getConnection(url + schema, props);

            // создание шаблонов запроса
            try {
                getUser      = connection.prepareStatement("SELECT * FROM users WHERE login = ?;");
                registerUser = connection.prepareStatement("INSERT INTO users(name, login, password) VALUES(?, ?, ?);");
                saveMessage  = connection.prepareStatement("INSERT INTO messages(date, userID, message) VALUES(?, ?, ?);");
            } catch (SQLException e) {
                log("произошла ошибка при создании шаблонов запроса: " + e.toString());
                throw e;
            }
        } catch (SQLException e) {
            log("произошла ошибка при попытке соединении: " + e.toString());

            // Не найдена база с таким именем
            if (e.getErrorCode() == 1049) {
                try {
                    createNewSchema();
                    open();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    close();
                    return false;
                }
            } else {
                close();
                return false;
            }
        }

        log("соединение с базой данных открыто.");
        return true;
    }

    private void createNewSchema() throws SQLException {
        log("создание новой схемы...");

        connection = DriverManager.getConnection(url, props);

        String createSchemaQuery =
                "CREATE SCHEMA `" + schema + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci ;";

        String createUsersTableQuery =
                "CREATE TABLE `" + schema + "`.`users` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT, " +
                        "`name` VARCHAR(45) NOT NULL, " +
                        "`login` VARCHAR(45) NOT NULL, " +
                        "`password` VARCHAR(45) NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE INDEX `id_UNIQUE` (`id` ASC), " +
                "UNIQUE INDEX `login_UNIQUE` (`login` ASC));";

        String createMessagesTableQuery =
                "CREATE TABLE `" + schema + "`.`messages` (" +
                        "`id` INT NOT NULL AUTO_INCREMENT, " +
                        "`date` DATETIME NOT NULL, " +
                        "`userId` INT NOT NULL, " +
                        "`message` LONGTEXT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE INDEX `id_UNIQUE` (`id` ASC));";

        PreparedStatement createSchema = connection.prepareStatement(createSchemaQuery);
        PreparedStatement createUsersTable = connection.prepareStatement(createUsersTableQuery);
        PreparedStatement createMessagesTable = connection.prepareStatement(createMessagesTableQuery);

        createSchema.executeUpdate();
        createUsersTable.executeUpdate();
        createMessagesTable.executeUpdate();
    }

    public void close() {
        log("закрытие соединения с базой данных...");

        if (getUser != null) {
            try {
                getUser.close();
            } catch (SQLException e) {
                log("произошла ошибка при попытке закрыть шаблон запроса getUser: " + e.toString());
            }
        }

        if (registerUser != null) {
            try {
                registerUser.close();
            } catch (SQLException e) {
                log("произошла ошибка при попытке закрыть шаблон запроса registerUser: " + e.toString());
            }
        }

        if (saveMessage != null) {
            try {
                saveMessage.close();
            } catch (SQLException e) {
                log("произошла ошибка при попытке закрыть шаблон запроса saveMessage: " + e.toString());
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log("произошла ошибка при попытке закрыть соединение: " + e.toString());
            }
        }

        log("соединение с базой данных закрыто.");
    }

    public boolean isOpened() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void log(String msg) {
        System.out.println("SERVER.database: " + msg);
    }

}
