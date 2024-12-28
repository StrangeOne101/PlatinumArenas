package com.strangeone101.platinumarenas.versioning.v1_21;

import com.google.common.base.Charsets;
import com.strangeone101.platinumarenas.versioning.ISkullInteraction;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkullInteraction1_21 implements ISkullInteraction<PlayerProfile> {

    @Override
    public PlayerProfile cache(Skull baseTileState) {
        return baseTileState.getOwnerProfile();
    }

    @Override
    public Skull set(Skull baseTileState, PlayerProfile data) {
        baseTileState.setOwnerProfile(data);
        return baseTileState;
    }

    @Override
    public String getName(PlayerProfile cache) {
        return cache.getName();
    }

    @Override
    public UUID getUUID(PlayerProfile cache) {
        return cache.getUniqueId();
    }

    @Override
    public String getTexture(PlayerProfile cache) {
        String texture = "{\"textures\":{\"SKIN\":{\"url\":\""+ cache.getTextures().getSkin().toString() + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(texture.getBytes(Charsets.UTF_8));
        String json = "[{\"name\":\"textures\",\"value\":" + base64 + "}]";
        return json;
    }

    public boolean isEnabled() {
        return true;
    }

    @Override
    public PlayerProfile create(UUID uuid, String texture) {
        PlayerProfile profile = Bukkit.createPlayerProfile(uuid);

        String decoded = new String(Base64.getDecoder().decode(texture.substring("[{\"name\":\"textures\",\"value\":".length(), texture.length() - "}]".length())));
        // We simply remove the "beginning" and "ending" part of the JSON, so we're left with only the URL. You could use a proper
        // JSON parser for this, but that's not worth it. The String will always start exactly with this stuff anyway
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length())));

            profile.setTextures(textures);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return profile;
    }
}
