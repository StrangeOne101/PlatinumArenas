package com.strangeone101.platinumarenas.blockentity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.Util;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Skull;

import java.lang.reflect.Field;
import java.util.UUID;

public class SkullWrapper implements Wrapper<Skull, GameProfile> {

    private Field profileField;
    private boolean enabled;

    public SkullWrapper() {

        try {
            Class craftSkull = Class.forName(Util.getCraftbukkitClass("block.CraftSkull"));
            profileField = craftSkull.getDeclaredField("profile");
            profileField.setAccessible(true);
            enabled = true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
            enabled = false;
        }

    }

    @Override
    public byte[] write(GameProfile gameProfile) {
        SmartWriter out = new SmartWriter();

        //Write UUID
        out.writeUUID(gameProfile.getId());

        //Write the name length + name
        out.writeString(gameProfile.getName());

        //Write properties
        String propertyString = new PropertyMap.Serializer().serialize(gameProfile.getProperties(), null, null).toString();
        out.writeString(propertyString);

        return out.toByteArray();
    }

    @Override
    public GameProfile cache(Skull baseTileState) {
        try {
            return (GameProfile) profileField.get(baseTileState);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public GameProfile read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        UUID id = buffer.getUUID();

        String name = buffer.getString();

        String propertyString = buffer.getString();
        JsonElement element = new JsonParser().parse(propertyString); //Parse as JSON
        PropertyMap properties = new PropertyMap.Serializer().deserialize(element, null, null); //To object

        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);
        return profile;
    }

    @Override
    public Skull place(Skull baseTileState, GameProfile cache) {
        try {
            profileField.set(baseTileState, cache);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return baseTileState;
    }

    @Override
    public Class<Skull> getTileClass() {
        return Skull.class;
    }

    @Override
    public boolean isBlank(Skull tileState) {
        return !(enabled && tileState.hasOwner()); //Don't let any skull be saved if we cant save the texture OR if it isnt owned
    }
}
