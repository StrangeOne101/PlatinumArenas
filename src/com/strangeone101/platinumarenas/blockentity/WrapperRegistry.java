package com.strangeone101.platinumarenas.blockentity;

import org.bukkit.Material;
import org.bukkit.block.TileState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WrapperRegistry {

    private static Map<Class<? extends TileState>, Wrapper> BY_TILE_CLASS = new HashMap<>();
    private static Map<Integer, Wrapper> BY_ID = new HashMap<>();
    private static Map<Material, Wrapper> BY_MATERIAL = new HashMap<>();

    public static void register(Wrapper wrapper, Material... materials) {
        BY_TILE_CLASS.put(wrapper.getTileClass(), wrapper);
        BY_ID.put(BY_ID.size(), wrapper);
        for (Material m : materials) {
            BY_MATERIAL.put(m, wrapper);
        }
    }

    public static Wrapper getFromMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }

    public static Wrapper getFromTile(TileState state) {
        return BY_TILE_CLASS.get(state.getClass());
    }

    public static Wrapper getFromId(int id) {
        return BY_ID.get(id);
    }

    public static void registerAll() {
        register(new SkullWrapper(), Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD);
        register(new BannerWrapper(), Material.BLACK_BANNER, Material.BLACK_WALL_BANNER,
                Material.GRAY_BANNER, Material.LIGHT_GRAY_WALL_BANNER,
                Material.LIGHT_GRAY_BANNER, Material.LIGHT_GRAY_WALL_BANNER,
                Material.WHITE_BANNER, Material.WHITE_WALL_BANNER,
                Material.RED_BANNER, Material.RED_WALL_BANNER,
                Material.ORANGE_BANNER, Material.ORANGE_WALL_BANNER,
                Material.YELLOW_BANNER, Material.YELLOW_WALL_BANNER,
                Material.LIME_BANNER, Material.LIME_WALL_BANNER,
                Material.GREEN_BANNER, Material.GREEN_WALL_BANNER,
                Material.CYAN_BANNER, Material.CYAN_WALL_BANNER,
                Material.LIGHT_BLUE_BANNER, Material.LIGHT_BLUE_WALL_BANNER,
                Material.BLUE_BANNER, Material.BLUE_WALL_BANNER,
                Material.PURPLE_BANNER, Material.PURPLE_WALL_BANNER,
                Material.MAGENTA_BANNER, Material.MAGENTA_WALL_BANNER,
                Material.PINK_BANNER, Material.PINK_WALL_BANNER,
                Material.BROWN_BANNER, Material.BROWN_WALL_BANNER);
        register(new SignWrapper(), getSigns());
    }

    private static Material[] getSigns() {
        List<Material> materials = new ArrayList<>();
        materials.addAll(Arrays.asList(Material.OAK_SIGN, Material.OAK_WALL_SIGN,
                Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
                Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
                Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
                Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
                Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN));

        for (String m : new String[] {"crimson_sign", "crimson_wall_sign", "warped_sign",
                "warped_wall_sign", "mangrove_sign", "mangrove_wall_sign"}) {
            if (Material.getMaterial(m.toUpperCase(Locale.ROOT)) != null) {
                materials.add(Material.getMaterial(m));
            }
        }

        return materials.toArray(new Material[0]);
    }

    public static int getId(Wrapper wrapper) {
        for (int id : BY_ID.keySet()) {
            if (BY_ID.get(id).equals(wrapper)) {
                return id;
            }
        }
        return -1;
    }
}
