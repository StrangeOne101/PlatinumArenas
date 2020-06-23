package com.strangeone101.platinumarenas;

import org.bukkit.command.CommandSender;

public abstract class ArenaProperty<T> {

    protected T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public abstract T getDefault();

    public abstract String getName();

    public abstract void load(String data);

    public abstract String save();

    public abstract String[] values();

    public abstract T parse(CommandSender sender, String string);

    public static abstract class BooleanProperty extends ArenaProperty<Boolean> {

        @Override
        public void load(String data) {
            this.set(Boolean.valueOf(data));
        }

        @Override
        public String save() {
            return this.value.toString();
        }

        @Override
        public String[] values() {
            return new String[] {"true", "false"};
        }

        @Override
        public Boolean parse(CommandSender sender, String string) {
            if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("true")) {
                return Boolean.parseBoolean(string);
            }
            return null;
        }
    }


}
