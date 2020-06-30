package com.strangeone101.platinumarenas;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class Section {

    private Arena parent;
    private Location start;
    private Location end;
    private short[] blockTypes;
    private short[] blockAmounts;

    private int resetIndex = -1;
    private int resetTypeIndex = 0;
    private int resetAmountIndex = 0;

    private boolean dirty;

    Section(Arena parent, Location start, Location end, short[] blockTypes, short[] blockAmounts) {
        this.blockAmounts = blockAmounts;
        this.blockTypes = blockTypes;
        this.parent = parent;
        this.start = start;
        this.end = end;
    }

    public boolean isResseting() {
        return resetIndex != -1;
    }

    public void cancelReset() {
        resetIndex = -1;
        resetTypeIndex = 0;
        resetAmountIndex = 0;
    }

    /**
     * Resets the section.
     * @param amount How many blocks to reset before pausing
     * @return If the section is finished resetting.
     */
    public boolean reset(int amount) {
        int w = getWidth();
        int h = getHeight();
        int l = getLength();
        for (resetTypeIndex = resetTypeIndex; resetTypeIndex < blockTypes.length; resetTypeIndex++) {
            for (resetAmountIndex = resetAmountIndex; resetAmountIndex < blockAmounts[resetAmountIndex]; resetAmountIndex++) {

                BlockData block = parent.getKeys()[blockTypes[resetTypeIndex]];
                Location loc = getStart().clone().add(Arena.getLocationAtIndex(w, h, l, getStart().getWorld(), resetIndex));
                loc.getBlock().setBlockData(block);

                resetIndex++;

                if (amount > 0 && resetIndex > amount) {
                    resetIndex = 0;
                    return false;
                }
            }
        }

        resetIndex = -1;
        resetTypeIndex = 0;
        resetAmountIndex = 0;

        dirty = false;
        return true;
    }

    public boolean reset() {
        return reset(-1);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * @return The height of the arena on the Y axis
     */
    public int getHeight() {
        return getEnd().getBlockY() - getStart().getBlockY() + 1;
    }

    /**
     * @return The width of the arena on the X axis
     */
    public int getWidth() {
        return getEnd().getBlockX() - getStart().getBlockX() + 1;
    }

    /**
     * @return The length of the arena on the Z axis
     */
    public int getLength() {
        return getEnd().getBlockZ() - getStart().getBlockZ() + 1;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    protected short[] getBlockAmounts() {
        return blockAmounts;
    }

    protected short[] getBlockTypes() {
        return blockTypes;
    }
}
