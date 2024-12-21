package com.strangeone101.platinumarenas.blockentity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.Util;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import org.bukkit.block.Skull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

public class SkullWrapper implements Wrapper<Skull, SkullWrapper.ProfilePlusData> {

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
    public byte[] write(ProfilePlusData data) {
        SmartWriter out = new SmartWriter();

        GameProfile gameProfile = data.profile;

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
    public ProfilePlusData cache(Skull baseTileState) {
        try {
            GameProfile profile = (GameProfile) profileField.get(baseTileState);
            ProfilePlusData data = new ProfilePlusData();
            data.profile = profile;
            data.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
            return data;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public ProfilePlusData read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        UUID id = buffer.getUUID();

        String name = buffer.getString();

        String propertyString = buffer.getString();
        JsonElement element = new JsonParser().parse(propertyString); //Parse as JSON
        PropertyMap properties = new PropertyMap.Serializer().deserialize(element, null, null); //To object

        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);

        ProfilePlusData data = new ProfilePlusData();
        data.profile = profile;
        data.persistentData = buffer.getByteArray();
        return data;
    }

    @Override
    public Skull place(Skull baseTileState, ProfilePlusData cache) {
        try {
            profileField.set(baseTileState, cache.profile);
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static class ProfilePlusData {
        public GameProfile profile;
        public byte[] persistentData;
    }
}
