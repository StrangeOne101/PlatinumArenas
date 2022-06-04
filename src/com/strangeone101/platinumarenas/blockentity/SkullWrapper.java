package com.strangeone101.platinumarenas.blockentity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.Util;
import org.bukkit.block.Skull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
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
            enabled = true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
            enabled = false;
        }

    }

    @Override
    public byte[] write(GameProfile gameProfile) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        //Write UUID
        out.writeLong(gameProfile.getId().getLeastSignificantBits());
        out.writeLong(gameProfile.getId().getMostSignificantBits());

        //Write the name length + name
        byte[] nameBytes = gameProfile.getName() == null ? new byte[0] : gameProfile.getName().getBytes(StandardCharsets.US_ASCII);
        out.writeByte(nameBytes.length);
        out.write(nameBytes);

        //Write properties
        String propertyString = new PropertyMap.Serializer().serialize(gameProfile.getProperties(), null, null).toString();
        byte[] propertyBytes = propertyString.getBytes(StandardCharsets.US_ASCII);
        out.writeInt(propertyBytes.length);
        out.write(propertyBytes);

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
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.position(0);

        long least = buffer.getLong(); //Get UUID
        long most = buffer.getLong();
        UUID id = new UUID(most, least);

        byte[] nameBytes = new byte[buffer.get()]; //Get name length
        for (int i = 0; i < nameBytes.length; i++) nameBytes[i] = buffer.get();
        String name = new String(nameBytes, StandardCharsets.US_ASCII); //Get name from bytes

        byte[] propertyBytes = new byte[buffer.getInt()]; //Get properties length
        for (int i = 0; i < propertyBytes.length; i++) propertyBytes[i] = buffer.get();

        String propertyString = new String(propertyBytes, StandardCharsets.US_ASCII); //Get property bytes to string
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
