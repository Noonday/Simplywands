package net.bennysmith.simplywands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bennysmith.simplywands.blockentity.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

//  Draws the item stored in a pedestal, floating and slowly spinning above it
public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    //  Item centre, in blocks above the pedestal's own block origin. The model's
    //  topmost geometry (floating_ring) reaches 15.75/16 = 0.984, so this clears it.
    private static final float HEIGHT = 1.22F;
    //  Item sprites are ONE WHOLE BLOCK across and centred on the pose origin:
    //    - ItemModelGenerator.processFrames builds the quad at (0,0,7.5)-(16,16,8.5)
    //    - ItemRenderer.render then does poseStack.translate(-0.5,-0.5,-0.5)
    //  so without this scale the wand renders at block size, centred at HEIGHT and
    //  buried in the pedestal with only a sliver showing.
    private static final float SCALE = 0.45F;
    private static final float SPIN_SPEED = 2.0F;

    private final Minecraft minecraft = Minecraft.getInstance();

    //  TEMPORARY DIAGNOSTIC - logs once, then stays quiet. Delete this field and
    //  the block below it once the floating wand is confirmed working.
    private static boolean loggedOnce = false;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PedestalBlockEntity pedestal, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!loggedOnce) {
            loggedOnce = true;
            System.out.println("[SimplyWands] PedestalRenderer.render REACHED, pos="
                    + pedestal.getBlockPos() + " wand=" + pedestal.getWand());
        }

        ItemStack wand = pedestal.getWand();
        if (wand.isEmpty()) {
            return;
        }

        //  Use the block entity's own level rather than the global one - the BER
        //  is always invoked with a level attached
        Level level = pedestal.getLevel();
        if (level == null) {
            return;
        }
        double time = level.getGameTime() + partialTick;

        poseStack.pushPose();
        //  Centre of the block, floating clear of the cap
        poseStack.translate(0.5, HEIGHT, 0.5);
        //  NOTE: % binds tighter than *, so this needs its own brackets or the
        //  angle grows without bound and loses precision over a long session
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((time * SPIN_SPEED) % 360.0)));
        //  Tip it over so it reads like a wand being held out rather than lying flat
        poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F));
        poseStack.scale(SCALE, SCALE, SCALE);

        //  ItemDisplayContext.NONE resolves to ItemTransform.NO_TRANSFORM (identity) via
        //  ItemTransforms#getTransform's `default ->` branch. GROUND does too today,
        //  but NONE stays identity even if a model later defines a "ground" display.
        minecraft.getItemRenderer().renderStatic(
                wand,
                ItemDisplayContext.NONE,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                level,
                (int) time
        );

        poseStack.popPose();
    }

    //  The item floats above the block. Without this the renderer can be culled
    //  when the pedestal is near the edge of the screen.
    @Override
    public AABB getRenderBoundingBox(PedestalBlockEntity pedestal) {
        return new AABB(pedestal.getBlockPos()).inflate(1.0, 2.0, 1.0);
    }

    @Override
    public boolean shouldRenderOffScreen(PedestalBlockEntity pedestal) {
        return false;
    }
}