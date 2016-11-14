package uk.co.mysterymayhem.gravitymod.common.items.tools;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import uk.co.mysterymayhem.gravitymod.api.IWeakGravityEnabler;
import uk.co.mysterymayhem.gravitymod.client.listeners.ItemTooltipListener;
import uk.co.mysterymayhem.gravitymod.common.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.config.ConfigHandler;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.modsupport.ModSupport;

import java.util.List;

/**
 * Created by Mysteryem on 2016-11-11.
 */
public class ItemWeakGravityController extends ItemAbstractGravityController {
    @Override
    public String getName() {
        return "weakgravitycontroller";
    }

    @Override
    public boolean affectsPlayer(EntityPlayerMP player) {
        return GravityManagerCommon.playerIsAffectedByWeakGravity(player);
    }

    @Override
    public void postInitRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, ItemAbstractGravityController.DEFAULT_META),
                "ILI",
                "LAL",
                "ILI",
                'I', ModItems.gravityIngot,
                'L', Blocks.LEVER,
                'A', new ItemStack(ModItems.gravityAnchor, 1, OreDictionary.WILDCARD_VALUE)));
    }

    @Override
    public int getPriority(Entity target) {
        return GravityPriorityRegistry.WEAK_GRAVITY_CONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.weakgravitycontroller"));
        ItemTooltipListener.addWeakGravityTooltip(tooltip, playerIn);
        super.addInformation(stack, playerIn, tooltip, advanced);
    }
}
