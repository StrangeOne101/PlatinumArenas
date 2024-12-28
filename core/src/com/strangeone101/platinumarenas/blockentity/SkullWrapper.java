package com.strangeone101.platinumarenas.blockentity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Util;
import com.strangeone101.platinumarenas.buffers.SmartReader;
import com.strangeone101.platinumarenas.buffers.SmartWriter;
import com.strangeone101.platinumarenas.versioning.ISkullInteraction;
import com.strangeone101.platinumarenas.versioning.v1_17.SkullInteraction1_17;
import com.strangeone101.platinumarenas.versioning.v1_21.SkullInteraction1_21;
import org.bukkit.block.Skull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

public class SkullWrapper implements Wrapper<Skull, SkullWrapper.ProfilePlusData> {

    private ISkullInteraction interaction;

    public SkullWrapper() {

        if (PlatinumArenas.getMCVersionInt() >= 1121) {
            interaction = new SkullInteraction1_21();
        } else {
            interaction = new SkullInteraction1_17();
        }

    }

    @Override
    public byte[] write(ProfilePlusData data) {
        SmartWriter out = new SmartWriter();

        Object gameProfile = data.profile;

        //Write UUID
        out.writeUUID(interaction.getUUID(gameProfile));

        //Write the name length + name
        out.writeString(interaction.getName(gameProfile));

        //Write properties
        String propertyString = interaction.getTexture(gameProfile);
        out.writeString(propertyString);

        return out.toByteArray();
    }

    @Override
    public ProfilePlusData cache(Skull baseTileState) {
        ProfilePlusData data = new ProfilePlusData();
        try {
            Object profile = interaction.cache(baseTileState); //GameProfile (or PlayerProfile in 1.21)
            data.profile = profile;
            data.persistentData = baseTileState.getPersistentDataContainer().serializeToBytes();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public ProfilePlusData read(byte[] bytes) {
        SmartReader buffer = new SmartReader(bytes);

        UUID id = buffer.getUUID();

        String name = buffer.getString();

        String propertyString = buffer.getString();

        Object profile = interaction.create(id, propertyString);

        ProfilePlusData data = new ProfilePlusData();
        data.profile = profile;
        data.persistentData = buffer.getByteArray();
        return data;
    }

    @Override
    public Skull place(Skull baseTileState, ProfilePlusData cache) {
        try {
            interaction.set(baseTileState, cache.profile);
            baseTileState.getPersistentDataContainer().readFromBytes(cache.persistentData);
        } catch (IOException e) {
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
        return !(interaction.isEnabled() && tileState.hasOwner()); //Don't let any skull be saved if we cant save the texture OR if it isnt owned
    }

    public static class ProfilePlusData {
        public Object profile;
        public byte[] persistentData;
    }
}
