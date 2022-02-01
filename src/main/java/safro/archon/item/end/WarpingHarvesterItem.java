package safro.archon.item.end;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Pair;
import safro.archon.item.HarvesterItem;

public class WarpingHarvesterItem extends HarvesterItem {

    public WarpingHarvesterItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public Pair<EntityType<? extends Entity>, Item> getGroup1() {
        return new Pair<>(EntityType.ENDERMAN, Items.ENDER_PEARL);
    }

    @Override
    public Pair<EntityType<? extends Entity>, Item> getGroup2() {
        return new Pair<>(EntityType.SHULKER, Items.SHULKER_SHELL);
    }

    @Override
    public Pair<EntityType<? extends Entity>, Item> getGroup3() {
        return new Pair<>(EntityType.ENDERMITE, Items.OBSIDIAN);
    }
}
