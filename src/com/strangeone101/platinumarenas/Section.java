package com.strangeone101.platinumarenas;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.strangeone101.platinumarenas.blockentity.Wrapper;
import com.strangeone101.platinumarenas.blockentity.WrapperRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Section {

    private Arena parent;         //The arena housing this section
    private Location start;       //Corner 1
    private Location end;         //Corner 2
    private short[] blockTypes;   //An array of shorts (that are block key indexes)
    private short[] blockAmounts; //An array of how many of that block there are in a line.

    private int ID;

    private Map<Integer, Pair<Wrapper, Object>> NBT_CACHE = new HashMap<>();

    /**
     * The index of where the reset is currently up to in the blockTypes variable
     */
    private int resetTypeIndex;
    /**
     * The index of where the reset is currently up to in the total amount of blocks placed
     * in this section.
     */
    private int resetLocationIndex;
    /**
     * The index of where the reset is currently up to for the current block type. This resets
     * to 0 every time the block type being placed changes
     */
    private int resetCurrentTypeIndex;

    /**
     * How many blocks reset this tick. Just because there is no easy way to call this back in the
     * reset method
     */
    private int blocksResetThisTick = 0;

    private boolean dirty;

    Section(Arena parent, int ID, Location start, Location end, short[] blockTypes, short[] blockAmounts) {
        this(parent, ID, start, end, blockTypes, blockAmounts, Maps.newHashMapWithExpectedSize(0));
    }

    Section(Arena parent, int ID, Location start, Location end, short[] blockTypes, short[] blockAmounts, Map<Integer, Pair<Wrapper, Object>> nbt) {
        this.blockAmounts = blockAmounts;
        this.blockTypes = blockTypes;
        this.parent = parent;
        this.start = start;
        this.end = end;
        this.ID = ID;
        this.NBT_CACHE = nbt;
    }

    /**
     * Whether the section is currently being reset or not
     * @return
     */
    public boolean isResetting() {
        return resetLocationIndex != -1;
    }

    /**
     * Cancel an ongoing reset
     */
    public void cancelReset() {
        resetLocationIndex = -1;
        resetCurrentTypeIndex = 0;
        resetTypeIndex = 0;
    }

    /**
     * Resets the section. This will continue off where it last left off, will iterate
     * over the provided amount of blocks before halting again. This allows us to
     * reset the section/arena over multiple ticks to prevent large lag spikes -
     * especially for large arenas.
     *
     * @param max How many blocks to reset before pausing
     * @return True if the section is finished resetting, or false if it has more to do later
     */
    public boolean reset(int max) {
        int w = getWidth();
        int h = getHeight();
        int l = getLength();

        // resetIndex should remain at 0, and only be reset when the entire section is complete
        // This way we can continue iterating from where we left off
        if (resetTypeIndex < 0)
        {
            resetTypeIndex = 0;
        }

        if (resetLocationIndex < 0)
        {
            resetLocationIndex = 0;
        }

        int count = 0;
        blocksResetThisTick = 0;

        while (resetTypeIndex < blockTypes.length)
        {
            short type = this.blockTypes[this.resetTypeIndex];
            short amount = this.blockAmounts[this.resetTypeIndex];

            BlockData data = this.parent.getKeys()[type];

            while (resetCurrentTypeIndex < amount)
            {
                Location offset = Arena.getLocationAtIndex(w, h, l, getStart().getWorld(), resetLocationIndex);

                Block block = getStart().add(offset).getBlock();
                getStart().subtract(offset);

                block.setBlockData(data, false);

                //Custom NBT on blocks
                if (NBT_CACHE.containsKey(resetLocationIndex)) { //If the NBT cache has this location
                    if (!(block.getState() instanceof TileState)) { //If the placed block doesn't have NBT somehow???
                        PlatinumArenas.INSTANCE.getLogger().warning("Tried to place NBT at block " + block.toString() + " but can't (no TileState)");
                    } else {
                        Pair<Wrapper, Object> nbtPair = NBT_CACHE.get(resetLocationIndex);
                        Wrapper wrapper = nbtPair.getKey();
                        Object cache = nbtPair.getRight();

                        if (!wrapper.getTileClass().isAssignableFrom(block.getState().getClass())) { //If the blocktype doesnt match
                            PlatinumArenas.INSTANCE.getLogger().warning("Tried to place NBT at block " + block.toString() + " but can't (TileState type mismatch)");
                            PlatinumArenas.INSTANCE.getLogger().warning("Block '" + block.getState().getClass().getName() + "', wrapper '" + wrapper.getTileClass().getName() + "'");
                        } else {
                            try {
                                wrapper.place((TileState) block.getState(), cache).update(); //The update part is important or else the tilestate won't change
                            } catch (Exception e) {
                                PlatinumArenas.INSTANCE.getLogger().warning("Tried to place NBT at block " + block.toString() + " and failed");
                                e.printStackTrace();
                            }
                        }
                    }
                }

                count++;
                resetCurrentTypeIndex++;
                blocksResetThisTick++;
                resetLocationIndex++;

                if (max > 0 && count > max)
                {
                    return false;
                }
            }

            resetCurrentTypeIndex = 0;
            resetTypeIndex++;
        }

        resetTypeIndex = 0;
        resetLocationIndex = -1;
        resetCurrentTypeIndex = 0;

        dirty = false;
        return true;
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

    /**
     * @return The total number of blocks in the arena
     */
    public int getTotalBlocks() {
        return getWidth() * getHeight() * getLength();
    }

    /**
     * @return The amount of blocks reset in the current tick
     */
    protected int getBlocksResetThisTick() {
        return blocksResetThisTick;
    }

    /**
     * @return The start location of this section. This is where the reset
     * will start from
     */
    public Location getStart() {
        return start;
    }

    /**
     * @return The end location of this section. This is the last block
     * location that will be reset
     */
    public Location getEnd() {
        return end;
    }

    /**
     * @return The amounts of each type of block
     */
    protected short[] getBlockAmounts() {
        return blockAmounts;
    }

    /**
     * @return An array of all the block types to reset in the arena. The
     * short is the index key from the parent arena's block keyset
     */
    protected short[] getBlockTypes() {
        return blockTypes;
    }

    /**
     * Converts all the NBT data in this section into a byte array
     * ready for writing to file
     * @return The byte array of NBT
     */
    protected byte[] getNBTData() {
        Map<Wrapper, Map<Object, List<Integer>>> wrapperTypes = new HashMap<>();

        for (Integer index : NBT_CACHE.keySet()) {
            Pair<Wrapper, Object> pair = NBT_CACHE.get(index);

            if (!wrapperTypes.containsKey(pair.getKey())) {
                wrapperTypes.put(pair.getKey(), new HashMap<>());
            }

            if (!wrapperTypes.get(pair.getLeft()).containsKey(pair.getRight())) {
                wrapperTypes.get(pair.getLeft()).put(pair.getRight(), new ArrayList<>());
            }

            wrapperTypes.get(pair.getLeft()).get(pair.getRight()).add(index);
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeByte(wrapperTypes.size()); //First byte is how many wrapper types

        for (Wrapper wrapper : wrapperTypes.keySet()) {
            out.writeByte(WrapperRegistry.getId(wrapper));   //Write the ID
            out.writeShort(wrapperTypes.get(wrapper).size()); //The amount of NBT blocks to save for this type

            for (Object cache : wrapperTypes.get(wrapper).keySet()) {
                List<Integer> indexes = wrapperTypes.get(wrapper).get(cache);
                int amountOfIndexes = indexes.size();

                //We do this in 127 lots because we write as a byte how many indexes use this object.
                //If there is more than 127 indexes for this object, we will just repeat the entire thing
                //again.
                do {
                    byte amountOfIndexesAsByte = (byte) (amountOfIndexes % 127);

                    out.writeByte(amountOfIndexesAsByte); //How many indexes use this object (in this iteration, at least)

                    do {
                        out.writeInt(indexes.get(0)); //Write an index
                        indexes.remove(0);      //Remove it from the list
                        amountOfIndexesAsByte--;
                    } while (amountOfIndexesAsByte > 0);

                    byte[] cacheByes = wrapper.write(cache); //Convert the cache object to bytes after we have written all the indexes
                    out.writeInt(cacheByes.length); //Write the length of the data, so we know how far to read it later on
                    out.write(cacheByes);

                    amountOfIndexes -= 127; //It's fine if it goes negative. Negative will end the loop
                } while (amountOfIndexes > 127);
            }
        }

        return out.toByteArray();
    }

    public Map<Integer, Pair<Wrapper, Object>> getNBTCache() {
        return NBT_CACHE;
    }

    @Override
    public String toString()
    {
        return "Section{" + "start=" + start + ", end=" + end + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return parent.equals(section.parent) &&
                start.equals(section.start) &&
                end.equals(section.end);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(parent, start, end);
        return result;
    }

    public int getID() {
        return ID;
    }
}
