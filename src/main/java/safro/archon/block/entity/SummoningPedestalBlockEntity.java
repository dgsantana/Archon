package safro.archon.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import safro.archon.registry.BlockRegistry;
import safro.archon.util.SummonUtil;

public class SummoningPedestalBlockEntity extends BlockEntity implements Clearable {
    private final DefaultedList<ItemStack> inventory;
    // 0 - Not processing anything
    // 1 - Summoning Tar boss
    // 2 - Summoning Alya boss
    private int processor = 0;
    private int spawnDelay = 60;

    public SummoningPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.SUMMONING_PEDESTAL_BE, pos, state);
        this.inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        boolean needsUpdate = false;
        ItemStack stack = player.getStackInHand(hand);
        if (this.isIdle() && !world.isClient) {
            if (player.isSneaking()) {
                for (int i = 0; i < inventory.size(); i++) {
                    if (!inventory.get(i).isEmpty()) {
                        Block.dropStack(world, pos, inventory.get(i));
                        inventory.set(i, ItemStack.EMPTY);
                        world.updateListeners(pos, state, state, 3);
                        needsUpdate = true;
                        break;
                    }
                }
                return ActionResult.SUCCESS;
            } else if (!stack.isEmpty() && this.addItem(player.getAbilities().creativeMode ? stack.copy() : stack)) {
                return ActionResult.SUCCESS;
            } else {
                if (SummonUtil.canSummonTar(this)) {
                    checkAndSpawn(player, world, state, pos, 1);
                } else if (SummonUtil.canSummonAlya(this)) {
                    checkAndSpawn(player, world, state, pos, 2);
                }
                return ActionResult.CONSUME;
            }
        }

        if (needsUpdate) {
            markDirty(world, pos, state);
        }
        return ActionResult.PASS;
    }

    public void checkAndSpawn(PlayerEntity player, World world, BlockState state, BlockPos pos, int processor) {
        if (world.getBlockState(pos.up()).isAir() && world.getBlockState(pos.up().up()).isAir()) {
            this.setProcessor(processor);
            this.clear();
            world.updateListeners(pos, state, state, 3);
            markDirty(world, pos, state);
        } else {
            player.sendMessage(new TranslatableText("text.archon.invalid_summon").formatted(Formatting.RED), true);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, SummoningPedestalBlockEntity be) {
        if (be.isProcessing()) {
            --be.spawnDelay;
            if (world.random.nextFloat() <= 0.10) {
                SummonUtil.addLightning(world, pos);
            }
            if (be.spawnDelay <= 0) {
                if (be.getProcessor() == 1) {
                    SummonUtil.summonTar(world, pos.up());
                    be.setProcessor(0);
                } else if (be.getProcessor() == 2) {
                    SummonUtil.summonAlya(world, pos.up());
                    be.setProcessor(0);
                }
            }
        }
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        if (nbt.contains("processor")) {
            processor = nbt.getInt("processing");
        }
    }

    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory, true);
        nbt.putInt("processor", processor);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        Inventories.writeNbt(nbtCompound, this.inventory, true);
        return nbtCompound;
    }

    public boolean addItem(ItemStack item) {
        for(int i = 0; i < this.inventory.size(); ++i) {
            ItemStack itemStack = this.inventory.get(i);
            if (itemStack.isEmpty()) {
                this.inventory.set(i, item.split(1));
                this.updateListeners();
                return true;
            }
        }
        return false;
    }

    public boolean hasItem(Item item) {
        for (ItemStack itemStack : this.inventory) {
            if (item == itemStack.getItem()) {
                return true;
            }
        }
        return false;
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    private void updateListeners() {
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    public void updateItems() {
        if (this.world != null) {
            this.updateListeners();
        }
    }

    public boolean isProcessing() {
        return processor > 0;
    }

    public boolean isIdle() {
        return processor == 0;
    }

    public int getProcessor() {
        return processor;
    }

    public void setProcessor(int type) {
        this.processor = type;
    }

    public void clear() {
        this.inventory.clear();
    }
}
