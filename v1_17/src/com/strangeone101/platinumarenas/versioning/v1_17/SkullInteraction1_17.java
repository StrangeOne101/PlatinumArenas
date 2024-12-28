package com.strangeone101.platinumarenas.versioning.v1_17;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.versioning.ISkullInteraction;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;

import java.lang.reflect.Field;
import java.util.UUID;

public class SkullInteraction1_17 implements ISkullInteraction<GameProfile> {

    private Field profileField;
    private boolean enabled;

    public SkullInteraction1_17() {
        try {
            Class craftSkull = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".block.CraftSkull");
            profileField = craftSkull.getDeclaredField("profile");
            profileField.setAccessible(true);
            enabled = true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
            enabled = false;
        }
    }
    
    @Override
    public GameProfile cache(Skull baseTileState) {
        try {
            GameProfile profile = (GameProfile) profileField.get(baseTileState);
            return profile;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Skull set(Skull baseTileState, GameProfile data) {
        try {
            profileField.set(baseTileState, data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return baseTileState;
    }

    @Override
    public String getName(GameProfile cache) {
        return cache.getName();
    }

    @Override
    public UUID getUUID(GameProfile cache) {
        return cache.getId();
    }

    @Override
    public String getTexture(GameProfile cache) {
        return new PropertyMap.Serializer().serialize(cache.getProperties(), null, null).toString();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public GameProfile create(UUID uuid, String texture) {
        GameProfile profile = new GameProfile(uuid, null);

        JsonElement element = new JsonParser().parse(texture);
        PropertyMap propertyMap = new PropertyMap.Serializer().deserialize(element, null, null);

        profile.getProperties().putAll(propertyMap);
        return profile;
    }
}
