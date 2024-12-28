package com.strangeone101.platinumarenas.blockentity;

import com.strangeone101.platinumarenas.PlatinumArenas;
import com.strangeone101.platinumarenas.Util;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.TileState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        int mcVersion = PlatinumArenas.getIntVersion(PlatinumArenas.getMCVersion());

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
        register(new SignWrapper(), Tag.ALL_SIGNS.getValues().toArray(new Material[0]));
        register(new BeaconWrapper(), Material.BEACON);

        if (mcVersion >= 1200) {
            register(new PotWrapper(), Material.DECORATED_POT);
        }

        if (Util.isPaperSupported()) { //Only do the following if Paper is being used due to paper API required
            List<Material> chests = new ArrayList<>();
            chests.addAll(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
                    Material.HOPPER, Material.DROPPER, Material.DISPENSER));
            chests.addAll(Tag.SHULKER_BOXES.getValues());
            register(new ChestWrapper(), chests.toArray(new Material[0]));
            register(new FurnaceWrapper(), Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE);
            register(new BrewingStandWrapper(), Material.BREWING_STAND);
            register(new LecternWrapper(), Material.LECTERN);
            register(new CampfireWrapper(), Material.CAMPFIRE, Material.SOUL_CAMPFIRE);
            register(new SuspiciousSandWrapper(), Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL);

            if (mcVersion >= 1193) {
                register(new BookshelfWrapper(), Material.CHISELED_BOOKSHELF);
            }

            if (mcVersion >= 1210) {
                register(new CrafterWrapper(), Material.CRAFTER);
            }
        }
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
