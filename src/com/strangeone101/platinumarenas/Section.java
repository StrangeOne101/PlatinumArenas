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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Section {

    private Arena parent;         //The arena housing this section
    private Location start;       //Corner 1
    private Location end;         //Corner 2
    private short[] blockTypes;   //An array of shorts (that are block key indexes)
    private short[] blockAmounts; //An array of how many of that block there are in a line.

    private int ID;

    private Map<Integer, Pair<Wrapper, Object>> NBT_CACHE = new HashMap<>();

    /**
     * The total block index of where the reset is up to. This variable is set
     * back to 0 after the reset pauses (this allows us to reset the arena over
     * time instead of in a single tick).
     */
    private int resetIndex = -1;

    private int resetTypeIndex = 0;
    /**
     * The block number the reset is currently on in the blockAmounts variable. Will be between 0 and
     * blockAmounts[resetTypeIndex] (short)
     */
    private int resetAmountIndex = 0;

    private int maxResetPerTick = 0;

    /**
     * The index of where the reset is currently up to in the blockTypes variable
     */
    private int index;
    private int locationIndex;
    private int positionIndex;

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
     * @param max How many blocks to reset before pausing
     * @return True if the section is finished resetting, or false if it has more to do later
     */
    public boolean reset(int max) {
        int w = getWidth();
        int h = getHeight();
        int l = getLength();

        // resetIndex should remain at 0, and only be reset when the entire section is complete
        // This way we can continue iterating from where we left off
        if (index < 0)
        {
            index = 0;
        }

        if (locationIndex < 0)
        {
            locationIndex = 0;
        }

        int count = 0;
        blocksResetThisTick = 0;

        while (index < blockTypes.length)
        {
            short type = this.blockTypes[this.index];
            short amount = this.blockAmounts[this.index];

            BlockData data = this.parent.getKeys()[type];

            while (positionIndex < amount)
            {
                Location offset = Arena.getLocationAtIndex(w, h, l, getStart().getWorld(), locationIndex);

                Block block = getStart().add(offset).getBlock();
                getStart().subtract(offset);

                block.setBlockData(data, false);

                //Custom NBT on blocks
                if (NBT_CACHE.containsKey(locationIndex)) { //If the NBT cache has this location
                    if (!(block.getState() instanceof TileState)) { //If the placed block doesn't have NBT somehow???
                        PlatinumArenas.INSTANCE.getLogger().warning("Tried to place NBT at block " + block.toString() + " but can't (no TileState)");
                    } else {
                        Pair<Wrapper, Object> nbtPair = NBT_CACHE.get(locationIndex);
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
                positionIndex++;
                blocksResetThisTick++;
                locationIndex++;

                if (max > 0 && count > max)
                {
                    return false;
                }
            }

            positionIndex = 0;
            index++;
        }

        index = 0;
        locationIndex = 0;
        positionIndex = 0;

//        for (resetTypeIndex = resetTypeIndex; resetTypeIndex < blockTypes.length; resetTypeIndex++) {
//            for (resetAmountIndex = resetAmountIndex; resetAmountIndex < blockAmounts[resetAmountIndex]; resetAmountIndex++) {
//
//                BlockData block = parent.getKeys()[blockTypes[resetTypeIndex]];
//                Location loc = getStart().clone().add(Arena.getLocationAtIndex(w, h, l, getStart().getWorld(), resetIndex));
//
//                System.out.println("resetTypeIndex: " + resetAmountIndex + ", resetAmountIndex: " + resetAmountIndex + ", resetIndex: " + resetIndex + ", amount: " + amount);
//                System.out.println("Updating " + loc + " to " + block.getMaterial());
//
//                loc.getBlock().setBlockData(block);
//
//                resetIndex++;
//
//                if (amount > 0 && resetIndex > amount) {
//                    resetIndex = 0;
//                    return false;
//                }
//            }
//        }

        resetIndex = 0;
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

    /**
     * @return The total number of blocks in the arena
     */
    public int getTotalBlocks() {
        return getWidth() * getHeight() * getLength();
    }

    protected int getBlocksResetThisTick() {
        return blocksResetThisTick;
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

    protected byte[] getNBTData() {
        Map<Wrapper, Map<Integer, Object>> wrapperTypes = new HashMap<>();

        for (Integer index : NBT_CACHE.keySet()) {
            Pair<Wrapper, Object> pair = NBT_CACHE.get(index);

            if (!wrapperTypes.containsKey(pair.getKey())) {
                wrapperTypes.put(pair.getKey(), new HashMap<>());
            }

            wrapperTypes.get(pair.getKey()).put(index, pair.getRight());
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeByte(wrapperTypes.size()); //First byte is how many wrapper types

        for (Wrapper wrapper : wrapperTypes.keySet()) {
            out.writeInt(WrapperRegistry.getId(wrapper)); //Write the ID
            out.writeInt(wrapperTypes.get(wrapper).size()); //The amount of NBT blocks to save for this type

            for (Integer index : wrapperTypes.get(wrapper).keySet()) {
                Object cache = wrapperTypes.get(wrapper).get(index);

                byte[] bytes = wrapper.write(cache);

                out.writeInt(index); //Write the index for this object
                out.writeInt(bytes.length); //Write the length of the cache bytes
                out.write(bytes); //Write all the bytes
            }
        }

        return out.toByteArray();
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
