package com.strangeone101.platinumarenas.blockentity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.Util;
import org.bukkit.block.Skull;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SkullWrapper implements Wrapper<Skull, GameProfile> {

    private Field profileField;
    private boolean enabled;

    public SkullWrapper() {

        try {
            Class craftSkull = Class.forName(Util.getCraftbukkitClass("block.CraftSkull"));
            profileField = craftSkull.getDeclaredField("profile");
            profileField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
            enabled = false;
        }

    }

    @Override
    public byte[] write(GameProfile gameProfile) {
        //try {
            //GameProfile gameProfile = (GameProfile) profileField.get(tileState);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Name", gameProfile.getName());
            jsonObject.addProperty("Id", gameProfile.getId().toString());
            jsonObject.add("Properties", new PropertyMap.Serializer().serialize(gameProfile.getProperties(), null, null));
            String json = jsonObject.toString();
            return json.getBytes(StandardCharsets.US_ASCII);
        //} catch (IllegalAccessException e) {
           // e.printStackTrace();
        //}
        //return new byte[0];
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
        String json = new String(bytes, StandardCharsets.US_ASCII);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        String name = jsonObject.get("Name").getAsString();
        UUID id = UUID.fromString(jsonObject.get("Id").getAsString());
        PropertyMap properties = new PropertyMap.Serializer().deserialize(jsonObject.get("Properties"), null, null);
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

    /*@Override
    public Skull read(Skull baseTileState, byte[] bytes) {
        String json = new String(bytes, StandardCharsets.US_ASCII);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        String name = jsonObject.get("Name").getAsString();
        UUID id = UUID.fromString(jsonObject.get("Id").getAsString());
        PropertyMap properties = new PropertyMap.Serializer().deserialize(jsonObject.get("Properties"), null, null);
        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);

        try {
            profileField.set(baseTileState, profile);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return baseTileState;
    }*/

    @Override
    public Class<Skull> getTileClass() {
        return Skull.class;
    }

    @Override
    public boolean isBlank(Skull tileState) {
        return enabled && tileState.hasOwner();
    }
}
