package com.shroototem.pipez.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.shroototem.pipez.*;
import com.shroototem.pipez.corelib.tag.SingleElementTag;
import com.shroototem.pipez.corelib.tag.Tag;
import com.shroototem.pipez.corelib.tag.TagUtils;
import com.shroototem.pipez.items.FilterDestinationToolItem;
import com.shroototem.pipez.net.OpenExtractPayload;
import com.shroototem.pipez.net.UpdateFilterPayload;
import com.shroototem.pipez.utils.NbtUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FilterScreen extends AbstractContainerScreen<FilterContainer> {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "textures/gui/container/filter.png");

    protected static final int FONT_COLOR = 0xFF404040;
    protected static final int WHITE = 0xFFFFFFFF;
    protected static final int DARK_RED = ChatFormatting.DARK_RED.getColor() != null ? (0xFF000000 | ChatFormatting.DARK_RED.getColor()) : 0xFFAA0000;
    protected static final int slotColor = -2130706433;

    private EditBox item;
    private EditBox nbt;

    private CycleIconButton nbtButton;
    private CycleIconButton invertButton;

    private Button submitButton;
    private Button cancelButton;

    private ExtractScreen.HoverArea itemHoverArea;
    private ExtractScreen.HoverArea itemTextHoverArea;
    private ExtractScreen.HoverArea nbtTextHoverArea;
    private ExtractScreen.HoverArea exactNBTHoverArea;
    private ExtractScreen.HoverArea invertHoverArea;
    private ExtractScreen.HoverArea destinationHoverArea;
    private ExtractScreen.HoverArea destinationTextHoverArea;

    private Filter<?, ?> filter;

    protected List<ExtractScreen.HoverArea> hoverAreas = new ArrayList<>();

    public FilterScreen(FilterContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        imageWidth = 176;
        imageHeight = 222;

        filter = getMenu().getFilter();
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        List<CycleIconButton.Icon> nbtIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16));
        nbtButton = new CycleIconButton(leftPos + 125, topPos + 81, nbtIcons, () -> filter.isExactMetadata() ? 1 : 0, button -> {
            filter.setExactMetadata(!filter.isExactMetadata());
        });
        addRenderableWidget(nbtButton);
        List<CycleIconButton.Icon> invertIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        invertButton = new CycleIconButton(leftPos + 149, topPos + 81, invertIcons, () -> filter.isInvert() ? 1 : 0, button -> {
            filter.setInvert(!filter.isInvert());
        });
        addRenderableWidget(invertButton);

        cancelButton = Button.builder(Component.translatable("message.pipez.filter.cancel"), button -> {
            ClientPlayNetworking.send(new OpenExtractPayload(getMenu().getIndex()));
        }).bounds(leftPos + 25, topPos + 105, 60, 20).build();
        addRenderableWidget(cancelButton);

        submitButton = Button.builder(Component.translatable("message.pipez.filter.submit"), button -> {
            ClientPlayNetworking.send(UpdateFilterPayload.create(filter, menu.getIndex()));
        }).bounds(leftPos + 91, topPos + 105, 60, 20).build();
        addRenderableWidget(submitButton);

        item = new EditBox(font, leftPos + 29, topPos + 17, 140, 18, Component.empty());
        item.setTextColor(WHITE);
        item.setBordered(true);
        item.setMaxLength(Integer.MAX_VALUE);
        if (filter.getTag() != null) {
            if (filter.getTag() instanceof SingleElementTag) {
                item.setValue(filter.getTag().getName().toString());
            } else {
                item.setValue("#" + filter.getTag().getName().toString());
            }
        }
        item.setResponder(this::onItemTextChanged);
        item.setFilter(s -> {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            return ResourceLocation.tryParse(s) != null;
        });
        addRenderableWidget(item);

        nbt = new EditBox(font, leftPos + 7, topPos + 49, 162, 18, Component.empty());
        nbt.setTextColor(WHITE);
        nbt.setBordered(true);
        nbt.setMaxLength(Integer.MAX_VALUE);
        nbt.setValue(filter.getMetadata() != null ? filter.getMetadata().toString() : "");
        nbt.setResponder(this::onNbtTextChanged);
        nbt.visible = hasNBT();

        addRenderableWidget(nbt);

        nbtButton.active = filter.getMetadata() != null;

        itemHoverArea = new ExtractScreen.HoverArea(8, 18, 16, 16, () -> {
            List<Component> tooltip = new ArrayList<>();
            FilterList.StackInfo stack = FilterList.getStackInfo(filter);
            if (stack != null) {
                tooltip.add(stack.displayName());
                if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                    tooltip.add(Component.translatable("tooltip.pipez.filter.accepts_tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                }
            }
            return tooltip;
        });
        hoverAreas.add(itemHoverArea);
        itemTextHoverArea = new ExtractScreen.HoverArea(29, 17, 140, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("tooltip.pipez.filter.item_tag.description"));
            return tooltip;
        });
        hoverAreas.add(itemTextHoverArea);
        nbtTextHoverArea = new ExtractScreen.HoverArea(7, 49, 162, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (hasNBT()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt_string.description"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt_string.no_nbt"));
            }
            return tooltip;
        });
        hoverAreas.add(nbtTextHoverArea);
        exactNBTHoverArea = new ExtractScreen.HoverArea(126, 82, 20, 20, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.isExactMetadata()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt.exact"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt.not_exact"));
            }
            return tooltip;
        });
        hoverAreas.add(exactNBTHoverArea);
        invertHoverArea = new ExtractScreen.HoverArea(150, 82, 20, 20, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.isInvert()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.inverted"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.not_inverted"));
            }
            return tooltip;
        });
        hoverAreas.add(invertHoverArea);
        destinationHoverArea = new ExtractScreen.HoverArea(8, 83, 16, 16, () -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("tooltip.pipez.filter.destination.description"));
            if (filter.getDestination() != null) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.destination.click_to_remove").withStyle(ChatFormatting.GRAY));
            }
            return tooltip;
        });
        hoverAreas.add(destinationHoverArea);

        destinationTextHoverArea = new ExtractScreen.HoverArea(25, 82, 96, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.getDestination() != null) {
                DirectionalPosition dst = filter.getDestination();
                tooltip.add(Component.translatable("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), Component.translatable("message.pipez.direction." + dst.getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
            }
            return tooltip;
        });
        hoverAreas.add(destinationTextHoverArea);
    }

    private boolean hasNBT() {
        return !(filter instanceof GasFilter);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        submitButton.active = filter.getTag() != null || filter.getMetadata() != null;
    }

    public void onItemTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(WHITE);
            filter.setTag(null);
            return;
        }

        if (filter instanceof ItemFilter) {
            Tag tag = TagUtils.getItem(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(DARK_RED);
            } else {
                item.setTextColor(WHITE);
            }
        } else if (filter instanceof FluidFilter) {
            Tag tag = TagUtils.getFluid(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(DARK_RED);
            } else {
                item.setTextColor(WHITE);
            }
        }
        // Gas filters not supported on Fabric
    }

    public void onNbtTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(WHITE);
            nbtButton.active = false;
            filter.setExactMetadata(false);
            filter.setMetadata(null);
            return;
        }
        nbtButton.active = true;
        try {
            filter.setMetadata(TagParser.parseCompoundFully(text));
            nbt.setTextColor(WHITE);
        } catch (CommandSyntaxException e) {
            nbt.setTextColor(DARK_RED);
            filter.setMetadata(null);
        }
    }

    public void onInsertStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (filter instanceof ItemFilter) {
            item.setValue(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            nbt.setValue(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).map(CompoundTag::toString).orElse(""));
        }
        // Fluid and Gas filter insertion from ItemStack not readily available on Fabric client
    }

    public void onInsertDestination(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof FilterDestinationToolItem)) {
            filter.setDestination(null);
            return;
        }
        DirectionalPosition dst = FilterDestinationToolItem.getDestination(stack);
        filter.setDestination(dst);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Do not call super to avoid default title rendering
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.item_tag"), 8, 7, FONT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.nbt_string"), 8, 39, FONT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.destination"), 8, 71, FONT_COLOR, false);
        guiGraphics.drawString(font, playerInventoryTitle.getVisualOrderText(), 8, imageHeight - 96 + 3, FONT_COLOR, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        FilterList.StackInfo stack = FilterList.getStackInfo(filter);
        if (stack != null) {
            stack.render(guiGraphics, leftPos + 8, topPos + 18);
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos + 31, topPos + 89);
        guiGraphics.pose().scale(0.5F, 0.5F);
        if (filter.getDestination() != null) {
            DirectionalPosition dst = filter.getDestination();
            guiGraphics.drawString(font, Component.translatable("message.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), Component.literal(String.valueOf(dst.getDirection().name().charAt(0))).withStyle(ChatFormatting.DARK_GREEN)), 0, 0, WHITE, false);
        } else {
            guiGraphics.drawString(font, Component.translatable("message.pipez.filter_destination_tool.destination.any"), 0, 0, WHITE, false);
        }
        guiGraphics.pose().popMatrix();

        if (itemHoverArea.isHovered(leftPos, topPos, mouseX, mouseY)) {
            drawHoverSlot(guiGraphics, leftPos + 8, topPos + 18);
        }
        if (destinationHoverArea.isHovered(leftPos, topPos, mouseX, mouseY)) {
            drawHoverSlot(guiGraphics, leftPos + 8, topPos + 83);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);
        renderTooltip(guiGraphics, x, y);
        drawHoverAreas(guiGraphics, x, y);
    }

    protected void drawHoverAreas(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (ExtractScreen.HoverArea area : hoverAreas) {
            if (area.isHovered(leftPos, topPos, mouseX, mouseY)) {
                List<Component> tooltip = area.getTooltip();
                if (!tooltip.isEmpty()) {
                    guiGraphics.setTooltipForNextFrame(font, tooltip.stream().map(Component::getVisualOrderText).collect(java.util.stream.Collectors.toList()), mouseX, mouseY);
                }
            }
        }
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    private void drawHoverSlot(GuiGraphics guiGraphics, int posX, int posY) {
        guiGraphics.fillGradient(posX, posY, posX + 16, posY + 16, slotColor, -2130706433);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean passToChild) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (itemHoverArea.isHovered(leftPos, topPos, (int) mouseX, (int) mouseY)) {
            if (event.hasShiftDown()) {
                item.setValue("");
                filter.setTag(null);
            } else {
                onInsertStack(getMenu().getCarried());
            }
            return true;
        }
        if (destinationHoverArea.isHovered(leftPos, topPos, (int) mouseX, (int) mouseY)) {
            onInsertDestination(getMenu().getCarried());
            return true;
        }

        if (event.hasShiftDown()) {
            Slot sl = this.hoveredSlot;
            if (sl != null) {
                onInsertStack(sl.getItem());
            }
        }

        return super.mouseClicked(event, passToChild);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // Escape
            minecraft.player.closeContainer();
            return true;
        }

        return item.keyPressed(event) ||
                item.canConsumeInput() ||
                nbt.keyPressed(event) ||
                nbt.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String itemTxt = item.getValue();
        String nbtTxt = nbt.getValue();
        init(mc, x, y);
        item.setValue(itemTxt);
        nbt.setValue(nbtTxt);
    }

}
