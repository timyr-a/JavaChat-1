package ru.steamrabbit.chat.share;

public class User {
    private final int id;
    private final String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        return (obj instanceof User) &&
               ((User) obj).id == this.id;
    }

    public String getName() {
        return name;
    }
}
