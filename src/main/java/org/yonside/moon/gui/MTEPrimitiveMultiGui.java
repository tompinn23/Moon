package org.yonside.moon.gui;

import org.yonside.moon.metatileentity.base.MTEPrimitiveMultiBase;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTGuis;
import gregtech.api.modularui2.GTWidgetThemes;
import gregtech.common.modularui2.widget.GTProgressWidget;

public class MTEPrimitiveMultiGui {

    private static final int OUTPUT_COLUMNS = 3;

    private final MTEPrimitiveMultiBase<?> base;

    public MTEPrimitiveMultiGui(MTEPrimitiveMultiBase<?> base) {
        this.base = base;
    }

    public ModularPanel build(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
        syncManager.registerSlotGroup("item_inv", 0);

        String[] inputRows = new String[base.inputSlots];
        for (int i = 0; i < inputRows.length; i++) inputRows[i] = "I";

        int outputRowCount = (base.outputSlots + OUTPUT_COLUMNS - 1) / OUTPUT_COLUMNS;
        String[] outputRows = new String[outputRowCount];
        for (int row = 0; row < outputRowCount; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < OUTPUT_COLUMNS; col++) {
                sb.append(row * OUTPUT_COLUMNS + col < base.outputSlots ? 'O' : ' ');
            }
            outputRows[row] = sb.toString();
        }

        int inputTop = 33 - base.inputSlots * 9;
        int outputTop = 33 - outputRowCount * 9;

        return GTGuis.mteTemplatePanelBuilder(base, data, syncManager, uiSettings)
            .build()
            .child(
                SlotGroupWidget.builder()
                    .matrix(inputRows)
                    .key(
                        'I',
                        index -> new ItemSlot()
                            .slot(
                                new ModularSlot(base.inventoryHandler, index).ignoreMaxStackSize(true)
                                    .slotGroup("item_inv"))
                            .widgetTheme(GTWidgetThemes.OVERLAY_ITEM_SLOT_IN))
                    .build()
                    .pos(33, inputTop))
            .child(
                SlotGroupWidget.builder()
                    .matrix(outputRows)
                    .key(
                        'O',
                        index -> new ItemSlot()
                            .slot(
                                new ModularSlot(base.inventoryHandler, base.inputSlots + index)
                                    .accessibility(false, true)
                                    .slotGroup("item_inv"))
                            .widgetTheme(GTWidgetThemes.OVERLAY_ITEM_SLOT_DUST))
                    .build()
                    .pos(85, outputTop))
            .child(
                new GTProgressWidget().neiTransferRect(base.getRecipeMap())
                    .value(
                        new DoubleSyncValue(
                            () -> base.maxProgressTime == 0 ? 0 : (double) base.progressTime / base.maxProgressTime))
                    .texture(GTGuiTextures.PROGRESSBAR_ARROW_STANDARD, 20)
                    .pos(58, 24)
                    .size(20, 18));
    }
}
