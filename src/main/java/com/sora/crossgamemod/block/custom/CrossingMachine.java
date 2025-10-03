package com.sora.crossgamemod.block.custom;

import com.sora.crossgamemod.CrossGameMod;
import com.sora.crossgamemod.block.entity.CrossingMachineEntity;
import com.sora.crossgamemod.block.entity.ModBlockEntities;
import com.sora.crossgamemod.lib.net.MachineIOType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CrossingMachine extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;


    public CrossingMachine(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRIGGERED, Boolean.FALSE));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.FALSE));

    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (world.isClientSide) return;
        if(!isInput(world,pos)) return;
        boolean hasSignal = world.hasNeighborSignal(pos);
        boolean isTriggered = state.getValue(TRIGGERED);
        if (hasSignal && !isTriggered) {
            CrossGameMod.LOGGER.info("Machine activated by redstone at {}", pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CrossingMachineEntity) {
                ((CrossingMachineEntity) blockEntity).sendSignal();
            }
            world.setBlock(pos, state.setValue(TRIGGERED, true), 3);
        }
        else if (!hasSignal && isTriggered) {
            world.setBlock(pos, state.setValue(TRIGGERED, false), 3);
        }
    }


    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }



    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof CrossingMachineEntity) {
                popResource(pLevel, pPos, new ItemStack(this));
                ((CrossingMachineEntity) blockEntity).onRemove();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    private boolean isInput(Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof CrossingMachineEntity){
            return ((CrossingMachineEntity) entity).ioType == MachineIOType.Input;
        }
        return false;
    }
    private boolean isOutput(Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
        if(entity instanceof CrossingMachineEntity){
            return ((CrossingMachineEntity) entity).ioType == MachineIOType.Output;
        }
        return false;
    }
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide) {
            if(isOutput(world,pos)) {
                boolean isPowered = state.getValue(POWERED);
                if (!isPowered) {
                    world.setBlock(pos, state.setValue(POWERED, true), 3);
                    world.scheduleTick(pos, this, 2);
                } else {
                    world.setBlock(pos, state.setValue(POWERED, false), 3);
                }
                // 通知周围方块更新（触发红石信号）
                world.updateNeighborsAt(pos, this);
            }
        }
    }

    // 声明方块是红石信号源
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    // 输出红石信号强度（激活时输出15）
    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof CrossingMachineEntity) {
                NetworkHooks.openScreen(((ServerPlayer)pPlayer), (CrossingMachineEntity)entity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CrossingMachineEntity(pPos, pState);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_52719_) {
        p_52719_.add(TRIGGERED,POWERED);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.CROSSING_MACHINE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}
