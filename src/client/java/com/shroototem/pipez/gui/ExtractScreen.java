package com.shroototem.pipez.gui;

import com.shroototem.pipez.*;
import com.shroototem.pipez.blocks.tileentity.PipeLogicTileEntity;
import com.shroototem.pipez.blocks.tileentity.types.PipeType;
import com.shroototem.pipez.corelib.tag.SingleElementTag;
import com.shroototem.pipez.net.*;
import com.shroototem.pipez.utils.NbtUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ExtractScreen extends AbstractContainerScreen<ExtractContainer> {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "textures/gui/container/extract.png");

    protected static final int FONT_COLOR = 0xFF404040;

    private CycleIconButton redstoneButton;
    private CycleIconButton sortButton;
    private CycleIconButton filterButton;

    private Button addFilterButton;
    private Button editFilterButton;
    private Button removeFilterButton;

    private HoverArea redstoneArea;
    private HoverArea sortArea;
    private HoverArea filterArea;

    private HoverArea[] tabs;
    private PipeType<?, ?>[] pipeTypes;
    private int currentindex;

    private FilterList filterList;

    protected List<HoverArea> hoverAreas = new ArrayList<>();

    public ExtractScreen(ExtractContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        imageWidth = 176;
        imageHeight = 196;

        pipeTypes = container.getPipe().getPipeTypes();
        if (pipeTypes.length > 1) {
            tabs = new HoverArea[pipeTypes.length];
        }
        currentindex = container.getIndex();
        if (currentindex < 0) {
            currentindex = getMenu().getPipe().getPreferredPipeIndex(getMenu().getSide());
        }
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        PipeLogicTileEntity pipe = getMenu().getPipe();
        Direction side = getMenu().getSide();

        filterList = new FilterList(this, 32, 8, 136, 66, () -> pipe.getFilters(side, pipeTypes[currentindex]));

        Supplier<Integer> redstoneModeIndex = () -> pipe.getRedstoneMode(getMenu().getSide(), pipeTypes[currentindex]).ordinal();
        List<CycleIconButton.Icon> redstoneModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16), new CycleIconButton.Icon(BACKGROUND, 208, 16), new CycleIconButton.Icon(BACKGROUND, 224, 16));
        redstoneButton = new CycleIconButton(leftPos + 7, topPos + 7, redstoneModeIcons, redstoneModeIndex, button -> {
            ClientPlayNetworking.send(new CycleRedstoneModePayload(currentindex));
        });
        Supplier<Integer> distributionIndex = () -> pipe.getDistribution(getMenu().getSide(), pipeTypes[currentindex]).ordinal();
        List<CycleIconButton.Icon> distributionIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 0), new CycleIconButton.Icon(BACKGROUND, 192, 0), new CycleIconButton.Icon(BACKGROUND, 208, 0), new CycleIconButton.Icon(BACKGROUND, 224, 0));
        sortButton = new CycleIconButton(leftPos + 7, topPos + 31, distributionIcons, distributionIndex, button -> {
            ClientPlayNetworking.send(new CycleDistributionPayload(currentindex));
        });
        Supplier<Integer> filterModeIndex = () -> pipeTypes[currentindex].hasFilter() ? pipe.getFilterMode(getMenu().getSide(), pipeTypes[currentindex]).ordinal() : 0;
        List<CycleIconButton.Icon> filterModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        filterButton = new CycleIconButton(leftPos + 7, topPos + 55, filterModeIcons, filterModeIndex, button -> {
            ClientPlayNetworking.send(new CycleFilterModePayload(currentindex));
        });
        addFilterButton = Button.builder(Component.translatable("message.pipez.filter.add"), button -> {
            ClientPlayNetworking.send(EditFilterPayload.create(pipeTypes[currentindex].createFilter(), currentindex));
        }).bounds(leftPos + 31, topPos + 79, 40, 20).build();
        editFilterButton = Button.builder(Component.translatable("message.pipez.filter.edit"), button -> {
            if (filterList.getSelected() >= 0) {
                ClientPlayNetworking.send(EditFilterPayload.create(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()), currentindex));
            }
        }).bounds(leftPos + 80, topPos + 79, 40, 20).build();
        removeFilterButton = Button.builder(Component.translatable("message.pipez.filter.remove"), button -> {
            if (filterList.getSelected() >= 0) {
                ClientPlayNetworking.send(new RemoveFilterPayload(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()).getId(), currentindex));
            }
        }).bounds(leftPos + 129, topPos + 79, 40, 20).build();

        addRenderableWidget(redstoneButton);
        addRenderableWidget(sortButton);
        addRenderableWidget(filterButton);
        addRenderableWidget(addFilterButton);
        addRenderableWidget(editFilterButton);
        addRenderableWidget(removeFilterButton);

        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                int tabIndex = i;
                tabs[i] = new HoverArea(-26 + 3, 5 + 25 * i, 24, 24, () -> {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable(pipeTypes[tabIndex].getTranslationKey()));
                    return tooltip;
                });
                hoverAreas.add(tabs[i]);
            }
        }

        redstoneArea = new HoverArea(7, 7, 20, 20, () -> {
            if (redstoneButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.redstone_mode", Component.translatable("tooltip.pipez.redstone_mode." + pipe.getRedstoneMode(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        sortArea = new HoverArea(7, 31, 20, 20, () -> {
            if (sortButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.distribution", Component.translatable("tooltip.pipez.distribution." + pipe.getDistribution(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        filterArea = new HoverArea(7, 55, 20, 20, () -> {
            if (filterButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.filter_mode", Component.translatable("tooltip.pipez.filter_mode." + pipe.getFilterMode(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        hoverAreas.add(redstoneArea);
        hoverAreas.add(sortArea);
        hoverAreas.add(filterArea);

        checkButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        checkButtons();
        filterList.tick();
    }

    private void checkButtons() {
        Upgrade upgrade = getMenu().getPipe().getUpgrade(getMenu().getSide());
        redstoneButton.active = Upgrade.canChangeRedstoneMode(upgrade);
        sortButton.active = Upgrade.canChangeDistributionMode(upgrade);
        filterButton.active = Upgrade.canChangeFilter(upgrade) && pipeTypes[currentindex].hasFilter();
        addFilterButton.active = filterButton.active;
        editFilterButton.active = Upgrade.canChangeFilter(upgrade) && filterList.getSelected() >= 0;
        removeFilterButton.active = editFilterButton.active;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Do not call super to avoid default title rendering
        guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 3, FONT_COLOR, false);

        filterList.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        filterList.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);

        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                if (i == currentindex) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos - 26 + 3, topPos + 5 + 25 * i, 176, 48, 26, 24, 256, 256);
                } else {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos - 26 + 3, topPos + 5 + 25 * i, 176, 72, 26, 24, 256, 256);
                }
            }
            for (int i = 0; i < pipeTypes.length; i++) {
                if (i == currentindex) {
                    guiGraphics.renderItem(pipeTypes[i].getIcon(), leftPos - 26 + 3 + 4, topPos + 5 + 25 * i + 4, 0);
                    guiGraphics.renderItemDecorations(font, pipeTypes[i].getIcon(), leftPos - 26 + 3 + 4, topPos + 5 + 25 * i + 4);
                } else {
                    guiGraphics.renderItem(pipeTypes[i].getIcon(), leftPos - 26 + 3 + 4 + 2, topPos + 5 + 25 * i + 4, 0);
                    guiGraphics.renderItemDecorations(font, pipeTypes[i].getIcon(), leftPos - 26 + 3 + 4 + 2, topPos + 5 + 25 * i + 4);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);
        renderTooltip(guiGraphics, x, y);
        drawHoverAreas(guiGraphics, x, y);
    }

    protected void drawHoverAreas(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (HoverArea area : hoverAreas) {
            if (area.isHovered(leftPos, topPos, mouseX, mouseY)) {
                List<Component> tooltip = area.getTooltip();
                if (!tooltip.isEmpty()) {
                    guiGraphics.setTooltipForNextFrame(font, tooltip.stream().map(Component::getVisualOrderText).collect(java.util.stream.Collectors.toList()), mouseX, mouseY);
                }
            }
        }
    }

    public int getTabsX() {
        return leftPos - getTabsWidth();
    }

    public int getTabsY() {
        return topPos + 5;
    }

    public int getTabsHeight() {
        if (hasTabs()) {
            return 25 * tabs.length;
        } else {
            return 0;
        }
    }

    public int getTabsWidth() {
        return 26 - 3;
    }

    public boolean hasTabs() {
        return tabs != null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean passToChild) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (filterList.mouseClicked(mouseX, mouseY, event.button())) {
            return true;
        }
        if (hasTabs()) {
            for (int i = 0; i < tabs.length; i++) {
                HoverArea hoverArea = tabs[i];
                if (currentindex != i && hoverArea.isHovered(leftPos, topPos, (int) mouseX, (int) mouseY)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                    currentindex = i;
                    init();
                    return true;
                }
            }
        }

        if (event.hasShiftDown()) {
            Slot sl = this.hoveredSlot;
            if (sl != null && !(sl instanceof UpgradeSlot)) {
                addQuickFilter(sl.getItem());
            }
        }
        return super.mouseClicked(event, passToChild);
    }

    public void addQuickFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (!filterButton.active) {
            return;
        }

        Filter<?, ?> filter = pipeTypes[currentindex].createFilter();
        if (filter == null) {
            return;
        }
        filter.setExactMetadata(true);

        if (filter instanceof ItemFilter) {
            filter.setTag(new SingleElementTag(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getItem()));
            filter.setMetadata(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).orElse(null));
            ClientPlayNetworking.send(UpdateFilterPayload.create(filter, currentindex));
        } else if (filter instanceof FluidFilter) {
            // On Fabric, there's no direct FluidUtil.getFirstStackContained equivalent.
            // Quick filter for fluids from item stacks would require fluid storage lookup,
            // which is not readily available on the client. Skip for now.
        }
        // Gas filters are not supported on Fabric
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (filterList.mouseReleased(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (filterList.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
            return true;
        }
        return false;
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    public void addHoverArea(HoverArea hoverArea) {
        hoverAreas.add(hoverArea);
    }

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }

    /**
     * Simple hover area class, replaces corelib's ScreenBase.HoverArea.
     */
    public static class HoverArea {
        private final int x, y, width, height;
        private final Supplier<List<Component>> tooltipSupplier;

        public HoverArea(int x, int y, int width, int height) {
            this(x, y, width, height, Collections::emptyList);
        }

        public HoverArea(int x, int y, int width, int height, Supplier<List<Component>> tooltipSupplier) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tooltipSupplier = tooltipSupplier;
        }

        public boolean isHovered(int guiLeft, int guiTop, int mouseX, int mouseY) {
            int absX = guiLeft + x;
            int absY = guiTop + y;
            return mouseX >= absX && mouseX < absX + width && mouseY >= absY && mouseY < absY + height;
        }

        public List<Component> getTooltip() {
            return tooltipSupplier.get();
        }
    }

}
