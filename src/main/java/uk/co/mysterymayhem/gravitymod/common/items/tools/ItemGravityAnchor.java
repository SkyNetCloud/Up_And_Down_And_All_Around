package uk.co.mysterymayhem.gravitymod.common.items.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.lwjgl.input.Keyboard;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.client.renderers.RenderGravityEntityItem;
import uk.co.mysterymayhem.gravitymod.common.GravityPriorityRegistry;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;
import uk.co.mysterymayhem.gravitymod.common.util.boundingboxes.GravityAxisAlignedBB;
import uk.co.mysterymayhem.gravitymod.common.util.item.ITickOnMouseCursor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Mysteryem on 2016-11-03.
 */
public class ItemGravityAnchor extends Item implements ITickOnMouseCursor, IModItem {

    public enum ItemFacing {
        FORWARDS,
        BACKWARDS,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    @Override
    public void preInit() {
        this.setHasSubtypes(true);
        this.addPropertyOverride(new ResourceLocation("facing"), new FacingPropertyGetter());
        IModItem.super.preInit();
    }

    @Override
    public void postInitRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.DOWN.ordinal()),
                "  C",
                "GI ",
                "GG ",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.DOWN.ordinal()),
                "C  ",
                " IG",
                " GG",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.UP.ordinal()),
                "GG ",
                "GI ",
                "  C",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.UP.ordinal()),
                " GG",
                " IG",
                "C  ",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.NORTH.ordinal()),
                "GGG",
                " I ",
                " C ",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.SOUTH.ordinal()),
                " C ",
                " I ",
                "GGG",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.EAST.ordinal()),
                "  G",
                "CIG",
                "  G",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(this, 1, EnumGravityDirection.WEST.ordinal()),
                "G  ",
                "GIC",
                "G  ",
                'C', Items.COMPASS,
                'G', ModItems.gravityIngot,
                'I', "ingotIron"));
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(stack.getItemDamage());
        return direction != EnumGravityDirection.DOWN;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(itemstack.getItemDamage());
        if (direction == EnumGravityDirection.DOWN) {
            return null;
        }
        if (location instanceof EntityItem) {
            return new EntityGravityItem(direction, (EntityItem) location);
        }
        else {
            //what
            FMLLog.bigWarning(
                    "Entity argument should always be an EntityItem, it was "
                            + (location == null ? "null" : "something else (not null)"));
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        KeyBinding keyBindSneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (Keyboard.isKeyDown(keyBindSneak.getKeyCode())) {
//            tooltip.add("Affects gravity in inventory or on mouse cursor");
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.sneak.line1"));
//            tooltip.add("Take care when crafting");
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.sneak.line2"));
        }
        else {
            tooltip.add(I18n.format("mouseovertext.mysttmtgravitymod.gravityanchor.line1"));
            tooltip.add(keyBindSneak.getDisplayName() + I18n.format("mouseovertext.mysttmtgravitymod.presskeyfordetails"));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int i = stack.getItemDamage();
        return super.getUnlocalizedName() + "." + EnumGravityDirection.getSafeDirectionFromOrdinal(i).getName();
    }

    // From EntityItem::onUpdate
    private static final double GRAVITY_DOWNWARDS_MOTION = 0.03999999910593033D;

    // Counteracts vanilla gravity and applies motion with the same strength in the gravity direction of the item
    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        entityItem.hoverStart = 0;
        if (!entityItem.hasNoGravity()) {
            final EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(entityItem.getEntityItem().getItemDamage());
            if (direction == EnumGravityDirection.DOWN) {
                return false;
            }

            //TODO: Find the correct block and apply its slipperiness instead?
            // The item entities tend to slide all over the place otherwise
            if (entityItem.isCollided) {
                entityItem.motionX *= 0.8d;
                entityItem.motionY *= 0.8d;
                entityItem.motionZ *= 0.8d;
            }

            // Undo usual vanilla gravity
//            entityItem.motionY += GRAVITY_DOWNWARDS_MOTION;

            // Apply the correct change to motion
            double[] d = direction.adjustXYZValues(0, GRAVITY_DOWNWARDS_MOTION, 0);
            entityItem.motionX -= d[0];
            entityItem.motionY -= d[1];
            entityItem.motionZ -= d[2];

            // TODO: Determine how much of an effect (if any) this has
            switch (direction) {
                case UP:
                    entityItem.onGround = entityItem.isCollidedVertically && entityItem.motionY > 0;
                    break;
                case NORTH:
                    entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionZ < 0;
                    break;
                case EAST:
                    entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionX > 0;
                    break;
                case SOUTH:
                    entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionZ > 0;
                    break;
                case WEST:
                    entityItem.onGround = entityItem.isCollidedHorizontally && entityItem.motionX < 0;
                    break;
            }

            // Not sure if it's needed
            entityItem.isAirBorne = !entityItem.onGround;
        }
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayerMP) {
            int meta = stack.getItemDamage();
            API.setPlayerGravity(EnumGravityDirection.getSafeDirectionFromOrdinal(meta), (EntityPlayerMP) entityIn, GravityPriorityRegistry.GRAVITY_ANCHOR);
        }
    }

    @Override
    public String getName() {
        return "gravityanchor";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int damage = 0; damage < EnumGravityDirection.values().length; damage++) {
            subItems.add(new ItemStack(itemIn, 1, damage));
        }
    }

    @SideOnly(Side.CLIENT)
    private static class MeshDefinitions implements ItemMeshDefinition {

        final ArrayList<ModelResourceLocation> list;
        final ModelResourceLocation generalResource;

        MeshDefinitions(ItemGravityAnchor item) {
            generalResource = new ModelResourceLocation(item.getRegistryName(), "inventory");

            list = new ArrayList<>();
            for (ItemFacing direction : ItemFacing.values()) {
                list.add(new ModelResourceLocation(item.getRegistryName() + "_" + direction.name().toLowerCase(Locale.ENGLISH), "inventory"));
            }
        }

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack) {
//            int metadata = stack.getMetadata();
//            EnumGravityDirection direction = EnumGravityDirection.getSafeDirectionFromOrdinal(metadata);
//            return list.get(direction.ordinal());
            return generalResource;
        }
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    @SideOnly(Side.CLIENT)
    public void preInitModel() {
        ItemGravityAnchor.MeshDefinitions meshDefinitions = new ItemGravityAnchor.MeshDefinitions(this);
        ModelBakery.registerItemVariants(this, meshDefinitions.generalResource);
        ModelBakery.registerItemVariants(this, meshDefinitions.list.toArray(new ModelResourceLocation[meshDefinitions.list.size()]));
        ModelLoader.setCustomMeshDefinition(this, meshDefinitions);
    }

    // Most of the ItemGravityAnchor class is this ItemPropertyGetter :\, why did I do this?
    private static class FacingPropertyGetter implements IItemPropertyGetter {
        @Override
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            if (entityIn != null/* && stack != null*/) {
                AxisAlignedBB bb = entityIn.getEntityBoundingBox();
                EnumGravityDirection entityGravityDirection;
                if (bb instanceof GravityAxisAlignedBB) {
                    entityGravityDirection = ((GravityAxisAlignedBB) bb).getDirection();
                }
                else {
                    entityGravityDirection = EnumGravityDirection.DOWN;
                }
//                        entityGravityDirection =
//                        EnumFacing entityFacingDirection;

                final double entityPitch = entityIn.rotationPitch;
                final double entityYaw;
                if (entityIn instanceof EntityPlayer) {
                    entityYaw = MathHelper.wrapDegrees(entityIn.rotationYaw);
                }
                else {
                    entityYaw = MathHelper.wrapDegrees(entityIn.rotationYawHead);
                }
//
                double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(entityYaw, entityPitch, entityIn);
                final double relativeYaw = relativeYawAndPitch[Hooks.YAW];
                final double relativePitch = relativeYawAndPitch[Hooks.PITCH];
//
//
//                        if (entityIn.rotationPitch >= 45) {
//                            entityFacingDirection = EnumFacing.DOWN;
//                        }
//                        else if (entityIn.rotationPitch <= 45) {
//                            entityFacingDirection = EnumFacing.UP;
//                        }
//                        else {
//                            entityFacingDirection = entityIn.getHorizontalFacing();
//                        }
                EnumGravityDirection itemDirection = EnumGravityDirection.getSafeDirectionFromOrdinal(stack.getItemDamage());

//                        EnumGravityDirection facingDirection = EnumGravityDirection.fromEnumFacing(facing);
                if (entityIn instanceof EntityZombie) {
                    FMLLog.info("z " + entityYaw);
                }

                ItemFacing itemFacing = null;

//                        if (facingDirection == direction) {
//                            itemFacing = ItemFacing.FORWARDS;
//                        }
//                        else if (facingDirection == direction.getOpposite()) {
//                            itemFacing = ItemFacing.BACKWARDS;
//                        }
//                        else {
//                            return -1;
//                        }

                if (itemDirection == entityGravityDirection) {
                    if (relativePitch >= 45) {
                        itemFacing = ItemFacing.FORWARDS;
                    }
                    else if (relativePitch <= -45) {
                        itemFacing = ItemFacing.BACKWARDS;
                    }
                    else {
                        itemFacing = ItemFacing.DOWN;
                    }
                }
                else if (itemDirection.getOpposite() == entityGravityDirection) {
                    if (relativePitch >= 45) {
                        itemFacing = ItemFacing.BACKWARDS;
                    }
                    else if (relativePitch <= -45) {
                        itemFacing = ItemFacing.FORWARDS;
                    }
                    else {
                        itemFacing = ItemFacing.UP;
                    }
                }
                else {
                    EnumFacing relativeHorizontalFacing = EnumFacing.getHorizontal(MathHelper.floor_double((relativeYaw * 4.0F / 360.0F) + 0.5D) & 3);
                    EnumFacing absoluteHorizontalFacing;

                    switch (entityGravityDirection.getInverseAdjustmentFromDOWNDirection()) {
                        case UP:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z);
                            break;
                        case DOWN:
                            absoluteHorizontalFacing = relativeHorizontalFacing;
                            break;
                        case NORTH:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.X);
                            break;
                        case EAST:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z);
                            break;
                        case SOUTH:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X);
                            break;
                        default://case WEST:
                            absoluteHorizontalFacing = relativeHorizontalFacing.rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z).rotateAround(EnumFacing.Axis.Z);
                            break;
                    }

                    EnumGravityDirection absoluteHorizontalFacingDirection = EnumGravityDirection.fromEnumFacing(absoluteHorizontalFacing);

                    if (absoluteHorizontalFacingDirection == itemDirection) {
                        if (relativePitch >= 45) {
                            itemFacing = ItemFacing.UP;
                        }
                        else if (relativePitch <= -45) {
                            itemFacing = ItemFacing.DOWN;
                        }
                        else {
                            itemFacing = ItemFacing.FORWARDS;
                        }
                    }
                    else if (absoluteHorizontalFacingDirection == itemDirection.getOpposite()) {
                        if (relativePitch >= 45) {
                            itemFacing = ItemFacing.DOWN;
                        }
                        else if (relativePitch <= -45) {
                            itemFacing = ItemFacing.UP;
                        }
                        else {
                            itemFacing = ItemFacing.BACKWARDS;
                        }
                    }
                    // I'm not sure of a better way to handle the rest of the cases
                    // This certainly is pretty horrible, not sure how it is for speed
                    else {
                        ItemFacing option1 = null;
                        ItemFacing option2 = null;
                        switch (itemDirection) {
                            case UP:
                                switch (entityGravityDirection) {
                                    case SOUTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case NORTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case WEST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case EAST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                }
                                break;
                            case DOWN:
                                switch (entityGravityDirection) {
                                    case SOUTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case NORTH:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case WEST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case EAST:
                                        if (relativeYaw < -90 || relativeYaw > 90) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                }
                                break;
                            case NORTH:
                                switch (entityGravityDirection) {
                                    case UP:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case DOWN:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case EAST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case WEST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                }
                                break;
                            case EAST:
                                switch (entityGravityDirection) {
                                    case DOWN:
                                    case NORTH:
                                    case SOUTH:
                                        option1 = ItemFacing.RIGHT;
                                        option2 = ItemFacing.LEFT;
                                        break;
                                    case UP:
                                        option1 = ItemFacing.LEFT;
                                        option2 = ItemFacing.RIGHT;
                                }
                                if (relativeYaw < -90 || relativeYaw > 90) {
                                    itemFacing = option1;
                                }
                                else {
                                    itemFacing = option2;
                                }
                                break;
                            case SOUTH:
                                switch (entityGravityDirection) {
                                    case UP:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                    case DOWN:
                                        if (entityYaw > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case EAST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        break;
                                    case WEST:
                                        if (entityPitch > 0) {
                                            itemFacing = ItemFacing.RIGHT;
                                        }
                                        else {
                                            itemFacing = ItemFacing.LEFT;
                                        }
                                        break;
                                }
                                break;
                            case WEST:
                                switch (entityGravityDirection) {
                                    case DOWN:
                                    case NORTH:
                                    case SOUTH:
                                        option1 = ItemFacing.LEFT;
                                        option2 = ItemFacing.RIGHT;
                                        break;
                                    case UP:
                                        option1 = ItemFacing.RIGHT;
                                        option2 = ItemFacing.LEFT;
                                }
                                if (relativeYaw < -90 || relativeYaw > 90) {
                                    itemFacing = option1;
                                }
                                else {
                                    itemFacing = option2;
                                }
                                break;
                        }
                        if (itemFacing == null) {
                            return -1;
//                                    return stack.getItemDamage();
                        }
                        //relative yaw
                        //Umm
//                                return -1;
//                                itemFacing = ItemFacing.LEFT;
                    }
                }

//                        switch(entityGravityDirection) {
//                            case UP:
//                                switch (itemDirection) {
//                                    case UP:
//                                        if (entityPitch > 0) {
//                                            itemFacing =
//                                        }
//                                        break;
//                                    case DOWN:
//                                        switch (entityFacingDirection) {
//                                            case DOWN:
//                                                itemFacing = ItemFacing.FORWARDS;
//                                                break;
//                                            case UP:
//                                                itemFacing = ItemFacing.BACKWARDS;
//                                                break;
//                                            default:
//                                                itemFacing = ItemFacing.UP;
//                                                break;
//                                        }
//                                        break;
//                                    case NORTH:
//                                        switch (entityFacingDirection) {
//                                            case DOWN:
//                                                break;
//                                            case UP:
//                                                break;
//                                            case NORTH:
//                                                break;
//                                            case SOUTH:
//                                                break;
//                                            case WEST:
//                                                break;
//                                            case EAST:
//                                                break;
//                                        }
//                                        break;
//                                    case EAST:
//                                        break;
//                                    case SOUTH:
//                                        break;
//                                    case WEST:
//                                        break;
//                                }
//                                break;
//                            case DOWN:
//                                switch (entityFacingDirection) {
//                                    case DOWN:
//                                        itemFacing = ItemFacing.FORWARDS;
//                                        break;
//                                    case UP:
//                                        itemFacing = ItemFacing.BACKWARDS;
//                                        break;
//                                    default:
//                                        break;
//                                }
//                                break;
//                            case NORTH:
//                                break;
//                            case EAST:
//                                break;
//                            case SOUTH:
//                                break;
//                            case WEST:
//                                break;
//                        }
                return itemFacing.ordinal();
            }
            // Item frames and other things
            return ItemFacing.DOWN.ordinal();
        }
    }

//
//    public void onModelBake(ModelBakeEvent event) {
//        event.getModelRegistry().getObject(null).getOverrides().getOverrides()
//    }
}
