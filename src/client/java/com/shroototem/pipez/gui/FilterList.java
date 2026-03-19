package com.shroototem.pipez.gui;

import com.shroototem.pipez.DirectionalPosition;
import com.shroototem.pipez.Filter;
import com.shroototem.pipez.PipezMod;
import com.shroototem.pipez.corelib.Pair;
import com.shroototem.pipez.corelib.tag.SingleElementTag;
import com.shroototem.pipez.corelib.tag.Tag;
import com.shroototem.pipez.utils.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FilterList extends WidgetBase {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "textures/gui/container/extract.png");

    protected Supplier<List<Filter<?, ?>>> filters;
    protected int offset;
    protected int selected;
    private ExtractScreen.HoverArea[] hoverAreas;
    private ExtractScreen.HoverArea[] itemHoverAreas;
    private ExtractScreen.HoverArea[] blockHoverAreas;
    private int columnHeight;
    private int columnCount;
    private Map<DirectionalPosition, Pair<BlockState, ItemStack>> filterPosCache;

    public FilterList(ExtractScreen screen, int posX, int posY, int xSize, int ySize, Supplier<List<Filter<?, ?>>> filters) {
        super(screen, posX, posY, xSize, ySize);
        this.filters = filters;
        columnHeight = 22;
        columnCount = 3;
        selected = -1;

        hoverAreas = new ExtractScreen.HoverArea[columnCount];
        itemHoverAreas = new ExtractScreen.HoverArea[columnCount];
        blockHoverAreas = new ExtractScreen.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new ExtractScreen.HoverArea(0, i * columnHeight, xSize, columnHeight);
            itemHoverAreas[i] = new ExtractScreen.HoverArea(3, 3 + i * columnHeight, 16, 16);
            blockHoverAreas[i] = new ExtractScreen.HoverArea(xSize - 3 - 16 - 11, 3 + i * columnHeight, 16, 16);
        }

        filterPosCache = new HashMap<>();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        List<Filter<?, ?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            Filter<?, ?> filter = f.get(getOffset() + i);
            if (itemHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                StackInfo stack = getStackInfo(filter);
                if (stack != null && !stack.isEmpty()) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(stack.displayName());
                    if (filter.isInvert()) {
                        tooltip.set(0, Component.translatable("tooltip.pipez.filter.not").withStyle(ChatFormatting.DARK_RED).append(" ").append(tooltip.get(0)));
                    }
                    if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                        tooltip.add(Component.translatable("tooltip.pipez.filter.accepts_tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                    }
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(java.util.stream.Collectors.toList()), mouseX, mouseY);
                }
            } else if (blockHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                if (filter.getDestination() != null) {
                    List<Component> tooltip = new ArrayList<>();
                    Pair<BlockState, ItemStack> destPair = getBlockAt(filter.getDestination());
                    if (destPair.getKey() == null) {
                        tooltip.add(net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(Component.translatable("tooltip.pipez.filter.unknown_block")).withStyle(ChatFormatting.DARK_RED));
                    } else {
                        tooltip.add(destPair.getKey().getBlock().getName().withStyle(ChatFormatting.BLUE));
                    }
                    BlockPos pos = filter.getDestination().getPos();
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_location", number(pos.getX()), number(pos.getY()), number(pos.getZ())));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_distance", number(pos.distManhattan(getContainer().getPipe().getBlockPos()))));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_side", Component.translatable("message.pipez.direction." + filter.getDestination().getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(java.util.stream.Collectors.toList()), mouseX, mouseY);
                }
            }
        }
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    /**
     * Simplified stack info that replaces the corelib AbstractStack.
     * Holds an ItemStack for rendering and a display name.
     */
    public static class StackInfo {
        private final ItemStack renderStack;
        private final Component displayName;

        public StackInfo(ItemStack renderStack, Component displayName) {
            this.renderStack = renderStack;
            this.displayName = displayName;
        }

        public boolean isEmpty() {
            return renderStack.isEmpty();
        }

        public Component displayName() {
            return displayName;
        }

        public void render(GuiGraphics guiGraphics, int x, int y) {
            guiGraphics.renderItem(renderStack, x, y);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, renderStack, x, y);
        }
    }

    /**
     * Creates a StackInfo from a filter for rendering purposes.
     * Replaces the corelib AbstractStack / WrappedItemStack / WrappedFluidStack approach.
     */
    @Nullable
    public static StackInfo getStackInfo(Filter<?, ?> filter) {
        Object o = null;

        if (filter.getTag() != null) {
            o = get(filter.getTag());
        }

        if (o instanceof Item) {
            ItemStack stack = new ItemStack((Item) o);
            if (filter.getMetadata() != null && Minecraft.getInstance().level != null) {
                stack.applyComponents(ComponentUtils.getPatch(Minecraft.getInstance().level.registryAccess(), filter.getMetadata().copy()));
            }
            return new StackInfo(stack, stack.getHoverName());
        } else if (o instanceof Fluid) {
            // For fluids, create a bucket item representation for display
            Fluid fluid = (Fluid) o;
            ItemStack bucketStack = new ItemStack(fluid.getBucket());
            if (bucketStack.isEmpty()) {
                bucketStack = new ItemStack(Items.BUCKET);
            }
            ResourceLocation fluidId = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);
            Component fluidName = Component.translatable("block." + fluidId.getNamespace() + "." + fluidId.getPath());
            return new StackInfo(bucketStack, fluidName);
        }

        return null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);

        List<Filter<?, ?>> f = filters.get();
        for (int i = getOffset(); i < f.size() && i < getOffset() + columnCount; i++) {
            int pos = i - getOffset();
            int startY = guiTop + pos * columnHeight;
            Filter<?, ?> filter = f.get(i);
            if (i == getSelected()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft, startY, 0, 218, 125, columnHeight, 256, 256);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft, startY, 0, 196, 125, columnHeight, 256, 256);
            }

            StackInfo stack = getStackInfo(filter);
            if (stack != null && !stack.isEmpty()) {
                stack.render(guiGraphics, guiLeft + 3, startY + 3);
                if (filter.getTag() != null) {
                    if (filter.getTag() instanceof SingleElementTag) {
                        drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.item", Component.literal(stack.displayName().getString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    } else {
                        drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    }
                }
            } else {
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.any_item").withStyle(ChatFormatting.WHITE));
            }
            if (filter.getMetadata() != null && filter.getMetadata().size() > 0) {
                MutableComponent tags = Component.translatable("message.pipez.filter.nbt.tag" + (filter.getMetadata().size() != 1 ? "s" : ""), filter.getMetadata().size()).withStyle(ChatFormatting.DARK_PURPLE);
                MutableComponent nbtStr = Component.translatable("message.pipez.filter.nbt", tags).withStyle(ChatFormatting.WHITE);
                if (filter.isExactMetadata()) {
                    nbtStr.append(" ").append(Component.translatable("message.pipez.filter.nbt.exact"));
                }
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 10, nbtStr);
            }

            if (filter.isInvert()) {
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 15, Component.translatable("message.pipez.filter.inverted").withStyle(ChatFormatting.DARK_RED));
            }

            if (filter.getDestination() != null) {
                Pair<BlockState, ItemStack> dstPair = getBlockAt(filter.getDestination());
                guiGraphics.renderItem(dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, 0);
                guiGraphics.renderItemDecorations(mc.font, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, String.valueOf(filter.getDestination().getDirection().name().charAt(0)));
            }
        }

        if (f.size() > columnCount) {
            float h = 66 - 17;
            float perc = (float) getOffset() / (float) (f.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft + xSize - 10, posY, 125, 196, 10, 17, 256, 256);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft + xSize - 10, guiTop, 135, 196, 10, 17, 256, 256);
        }
    }

    private Pair<BlockState, ItemStack> getBlockAt(DirectionalPosition destination) {
        Pair<BlockState, ItemStack> cached = filterPosCache.get(destination);
        if (cached != null) {
            return cached;
        }
        ItemStack stack = new ItemStack(Items.WHITE_CONCRETE);
        BlockState state = null;
        if (mc.level != null && mc.level.hasChunkAt(destination.getPos())) {
            state = mc.level.getBlockState(destination.getPos());
            try {
                ItemStack pickBlock = new ItemStack(state.getBlock().asItem());
                if (!pickBlock.isEmpty()) {
                    stack = pickBlock;
                }
            } catch (Exception ignored) {
            }
        }
        Pair<BlockState, ItemStack> result = new Pair<>(state, stack);
        filterPosCache.put(destination, result);
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        filterPosCache.clear();
    }

    public int getOffset() {
        List<Filter<?, ?>> f = filters.get();
        if (f.size() <= columnCount) {
            offset = 0;
        } else if (offset > f.size() - columnCount) {
            offset = f.size() - columnCount;
        }
        return offset;
    }

    public int getSelected() {
        if (selected >= filters.get().size()) {
            selected = -1;
        }
        return selected;
    }

    private void drawStringSmall(GuiGraphics guiGraphics, int x, int y, Component text) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.5F, 0.5F);
        guiGraphics.drawString(mc.font, text, 0, 0, 0xFF000000, false);
        guiGraphics.pose().popMatrix();
    }

    @Nullable
    public static <T> T get(Tag<T> tag) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        long time = Minecraft.getInstance().level.getGameTime();
        List<T> allElements = tag.getAll().stream().toList();
        if (allElements.isEmpty()) {
            return null;
        }
        return allElements.get((int) (time / 20L % allElements.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        List<Filter<?, ?>> f = filters.get();
        if (f.size() > columnCount) {
            if (deltaY < 0D) {
                offset = Math.min(getOffset() + 1, f.size() - columnCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Filter<?, ?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            selected = getOffset() + i;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
