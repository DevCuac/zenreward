package com.cuac_xd.zenrewards.models;

import java.util.List;

public class Reward {

    private final String id;
    private final String type; // REPEATABLE o UNIQUE
    private final long cooldown; // en milisegundos
    private final String permission;
    private final List<String> commands;
    private final List<String> messages;

    public Reward(String id, String type, long cooldown, String permission, List<String> commands, List<String> messages) {
        this.id = id;
        this.type = type;
        this.cooldown = cooldown;
        this.permission = permission;
        this.commands = commands;
        this.messages = messages;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public long getCooldown() { return cooldown; }
    public String getPermission() { return permission; }
    public List<String> getCommands() { return commands; }
    public List<String> getMessages() { return messages; }
}