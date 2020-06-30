package com.strangeone101.platinumarenas;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class Section {

    private Arena parent;         //The arena housing this section
    private Location start;       //Corner 1
    private Location end;         //Corner 2
    private short[] blockTypes;   //An array of shorts (that are block key indexes)
    private short[] blockAmounts; //An array of how many of that block there are in a line.

    /**
     * The total block index of where the reset is up to. This variable is set
     * back to 0 after the reset pauses (this allows us to reset the arena over
     * time instead of in a single tick).
     */
    private int resetIndex = -1;
    /**
     * The index of where the reset is currently up to in the blockTypes variable
     */
    private int resetTypeIndex = 0;
    /**
     * The block number the reset is currently on in the blockAmounts variable. Will be between 0 and
     * blockAmounts[resetTypeIndex] (short)
     */
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
     * Resets the section. This will continue off where it last left off, will iterate
     * over the provided amount of blocks before halting again. This allows us to
     * reset the section/arena over multiple ticks to prevent large lag spikes -
     * especially for large arenas.
     *
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

    /**
     * Resets the section. This will continue off where it last left off, and
     * will not stop until the entire section is reset. This may cause a lot
     * of lag. Use `reset(amount)` instead.
     *
     * @return If the section is finished resetting.
     */
    public boolean reset() {
        return reset(-1);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return If the section has been modified by players
     */
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
