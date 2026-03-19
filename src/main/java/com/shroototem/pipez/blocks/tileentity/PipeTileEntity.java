package com.shroototem.pipez.blocks.tileentity;

import com.shroototem.pipez.corelib.ITickableBlockEntity;
import com.shroototem.pipez.DirectionalPosition;
import com.shroototem.pipez.blocks.PipeBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;

import org.jetbrains.annotations.Nullable;
import java.util.*;

public abstract class PipeTileEntity extends BlockEntity implements ITickableBlockEntity {

    @Nullable
    protected List<Connection> connectionCache;
    @Nullable
    protected Connection[] extractingConnectionCache;
    protected boolean[] extractingSides;
    protected boolean[] disconnectedSides;

    /**
     * Invalidating the cache five ticks after load, because some mods may not be ready immediately.
     */
    private int invalidateCountdown;

    public PipeTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        extractingSides = new boolean[Direction.values().length];
        disconnectedSides = new boolean[Direction.values().length];
    }

    public List<Connection> getConnections() {
        if (level == null) {
            return new ArrayList<>();
        }
        if (connectionCache == null) {
            updateConnectionCache();
            if (connectionCache == null) {
                return new ArrayList<>();
            }
        }
        return connectionCache;
    }

    @Nullable
    public Connection getExtractingConnection(Direction side) {
        if (level == null) {
            return null;
        }
        if (extractingConnectionCache == null) {
            updateExtractingConnectionCache();
            if (extractingConnectionCache == null) {
                return null;
            }
        }
        return extractingConnectionCache[side.get3DDataValue()];
    }

    public static void markPipesDirty(Level world, BlockPos pos) {
        List<BlockPos> travelPositions = new ArrayList<>();
        LinkedList<BlockPos> queue = new LinkedList<>();
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipeBlock = (PipeBlock) block;

        PipeTileEntity pipeTe = pipeBlock.getTileEntity(world, pos);
        if (pipeTe != null) {
            for (Direction side : Direction.values()) {
                if (pipeTe.isExtracting(side)) {
                    if (!pipeBlock.canConnectTo(world, pos, side)) {
                        pipeTe.setExtracting(side, false);
                        if (!pipeTe.hasReasonToStay()) {
                            pipeBlock.setHasData(world, pos, false);
                        }
                        pipeTe.syncData();
                    }
                }
            }
        }

        travelPositions.add(pos);
        addToDirtyList(world, pos, pipeBlock, travelPositions, queue);
        while (queue.size() > 0) {
            BlockPos blockPos = queue.removeFirst();
            block = world.getBlockState(blockPos).getBlock();
            if (block instanceof PipeBlock) {
                addToDirtyList(world, blockPos, (PipeBlock) block, travelPositions, queue);
            }
        }
        for (BlockPos p : travelPositions) {
            BlockEntity te = world.getBlockEntity(p);
            if (!(te instanceof PipeTileEntity)) {
                continue;
            }
            PipeTileEntity pipe = (PipeTileEntity) te;
            pipe.connectionCache = null;
        }
    }

    private static void addToDirtyList(Level world, BlockPos pos, PipeBlock pipeBlock, List<BlockPos> travelPositions, LinkedList<BlockPos> queue) {
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, pos, direction)) {
                BlockPos p = pos.relative(direction);
                if (!travelPositions.contains(p) && !queue.contains(p)) {
                    travelPositions.add(p);
                    queue.add(p);
                }
            }
        }
    }

    private void updateConnectionCache() {
        if (!(level instanceof ServerLevel serverLevel)) {
            connectionCache = null;
            return;
        }
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof PipeBlock)) {
            connectionCache = null;
            return;
        }
        if (!isExtracting()) {
            connectionCache = null;
            return;
        }

        Map<DirectionalPosition, Connection> connections = new HashMap<>();

        Map<BlockPos, Integer> queue = new HashMap<>();
        List<BlockPos> travelPositions = new ArrayList<>();

        addToQueue(serverLevel, worldPosition, queue, travelPositions, connections, 1);

        while (queue.size() > 0) {
            Map.Entry<BlockPos, Integer> blockPosIntegerEntry = queue.entrySet().stream().findAny().get();
            addToQueue(serverLevel, blockPosIntegerEntry.getKey(), queue, travelPositions, connections, blockPosIntegerEntry.getValue());
            travelPositions.add(blockPosIntegerEntry.getKey());
            queue.remove(blockPosIntegerEntry.getKey());
        }

        connectionCache = new ArrayList<>(connections.values());
    }

    private void updateExtractingConnectionCache() {
        if (!(level instanceof ServerLevel serverLevel)) {
            connectionCache = null;
            return;
        }
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof PipeBlock)) {
            extractingConnectionCache = null;
            return;
        }

        extractingConnectionCache = new Connection[Direction.values().length];

        for (Direction direction : Direction.values()) {
            if (!isExtracting(direction)) {
                extractingConnectionCache[direction.get3DDataValue()] = null;
                continue;
            }
            extractingConnectionCache[direction.get3DDataValue()] = new Connection(serverLevel, getBlockPos().relative(direction), direction.getOpposite(), 1);
        }
    }

    public void addToQueue(ServerLevel world, BlockPos position, Map<BlockPos, Integer> queue, List<BlockPos> travelPositions, Map<DirectionalPosition, Connection> insertPositions, int distance) {
        Block block = world.getBlockState(position).getBlock();
        if (!(block instanceof PipeBlock)) {
            return;
        }
        PipeBlock pipeBlock = (PipeBlock) block;
        for (Direction direction : Direction.values()) {
            if (pipeBlock.isConnected(world, position, direction)) {
                BlockPos p = position.relative(direction);
                DirectionalPosition dp = new DirectionalPosition(p, direction.getOpposite());
                Connection connection = new Connection(world, dp.getPos(), dp.getDirection(), distance);
                if (!isExtracting(level, position, direction) && canInsert(level, connection)) {
                    if (!insertPositions.containsKey(dp)) {
                        insertPositions.put(dp, connection);
                    } else {
                        if (insertPositions.get(dp).getDistance() > distance) {
                            insertPositions.put(dp, connection);
                        }
                    }
                } else {
                    if (!travelPositions.contains(p) && !queue.containsKey(p)) {
                        queue.put(p, distance + 1);
                    }
                }
            }
        }
    }

    private boolean isExtracting(Level level, BlockPos pos, Direction direction) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof PipeTileEntity pipe) {
            if (pipe.isExtracting(direction)) {
                return true;
            }
        }
        return false;
    }

    public abstract boolean canInsert(Level level, Connection connection);

    @Override
    public void tick() {
        if (invalidateCountdown >= 0) {
            invalidateCountdown--;
            if (invalidateCountdown <= 0) {
                connectionCache = null;
            }
        }
    }

    public boolean isExtracting(Direction side) {
        return extractingSides[side.get3DDataValue()];
    }

    public boolean isExtracting() {
        for (boolean extract : extractingSides) {
            if (extract) {
                return true;
            }
        }
        return false;
    }

    public boolean hasReasonToStay() {
        if (isExtracting()) {
            return true;
        }
        for (boolean disconnected : disconnectedSides) {
            if (disconnected) {
                return true;
            }
        }
        return false;
    }

    public void setExtracting(Direction side, boolean extracting) {
        extractingSides[side.get3DDataValue()] = extracting;
        extractingConnectionCache = null;
        setChanged();
    }

    public boolean isDisconnected(Direction side) {
        return disconnectedSides[side.get3DDataValue()];
    }

    public void setDisconnected(Direction side, boolean disconnected) {
        disconnectedSides[side.get3DDataValue()] = disconnected;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        extractingSides = new boolean[Direction.values().length];
        input.getIntArray("ExtractingSides").ifPresent(arr -> {
            for (int i = 0; i < Math.min(arr.length, extractingSides.length); i++) {
                extractingSides[i] = arr[i] != 0;
            }
        });

        disconnectedSides = new boolean[Direction.values().length];
        input.getIntArray("DisconnectedSides").ifPresent(arr -> {
            for (int i = 0; i < Math.min(arr.length, disconnectedSides.length); i++) {
                disconnectedSides[i] = arr[i] != 0;
            }
        });
        invalidateCountdown = 10;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        int[] extractingArr = new int[extractingSides.length];
        for (int i = 0; i < extractingSides.length; i++) {
            extractingArr[i] = extractingSides[i] ? 1 : 0;
        }
        output.putIntArray("ExtractingSides", extractingArr);

        int[] disconnectedArr = new int[disconnectedSides.length];
        for (int i = 0; i < disconnectedSides.length; i++) {
            disconnectedArr[i] = disconnectedSides[i] ? 1 : 0;
        }
        output.putIntArray("DisconnectedSides", disconnectedArr);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncData(ServerPlayer player) {
        player.connection.send(getUpdatePacket());
    }

    public void syncData() {
        if (level == null || level.isClientSide()) {
            return;
        }
        LevelChunk chunk = level.getChunkAt(getBlockPos());
        ((ServerChunkCache) level.getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(e -> e.connection.send(getUpdatePacket()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getUpdateTag(provider);
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(valueOutput);
        updateTag.merge(valueOutput.buildResult());
        return updateTag;
    }

    public static class Connection {
        private final BlockPos pos;
        private final Direction direction;
        private final int distance;
        private BlockApiCache<Storage<ItemVariant>, Direction> itemCache;
        private BlockApiCache<EnergyStorage, Direction> energyCache;
        private BlockApiCache<Storage<FluidVariant>, Direction> fluidCache;

        public Connection(ServerLevel level, BlockPos pos, Direction direction, int distance) {
            this.pos = pos;
            this.direction = direction;
            this.distance = distance;

            itemCache = BlockApiCache.create(ItemStorage.SIDED, level, pos);
            energyCache = BlockApiCache.create(EnergyStorage.SIDED, level, pos);
            fluidCache = BlockApiCache.create(FluidStorage.SIDED, level, pos);
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return "Connection{" +
                    "pos=" + pos +
                    ", direction=" + direction +
                    ", distance=" + distance +
                    '}';
        }

        @Nullable
        public Storage<ItemVariant> getItemHandler() {
            return itemCache.find(direction);
        }

        /**
         * Alias for getItemHandler() - used by ItemPipeType.
         */
        @Nullable
        public Storage<ItemVariant> getItemStorage() {
            return getItemHandler();
        }

        @Nullable
        public EnergyStorage getEnergyHandler() {
            return energyCache.find(direction);
        }

        /**
         * Alias for getEnergyHandler() - used by EnergyPipeType.
         */
        @Nullable
        public EnergyStorage getEnergyStorage() {
            return getEnergyHandler();
        }

        @Nullable
        public Storage<FluidVariant> getFluidHandler() {
            return fluidCache.find(direction);
        }

        /**
         * Alias for getFluidHandler() - used by FluidPipeType.
         */
        @Nullable
        public Storage<FluidVariant> getFluidStorage() {
            return getFluidHandler();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T> T getCapability(BlockApiLookup<T, Direction> lookup) {
            if (lookup == ItemStorage.SIDED) {
                return (T) getItemHandler();
            } else if (lookup == EnergyStorage.SIDED) {
                return (T) getEnergyHandler();
            } else if (lookup == FluidStorage.SIDED) {
                return (T) getFluidHandler();
            }
            return null;
        }
    }

}
