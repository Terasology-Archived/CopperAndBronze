/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.cab.ui;

import org.terasology.cab.component.CharcoalPitComponent;
import org.terasology.cab.event.ProduceCharcoalRequest;
import org.terasology.cab.system.CharcoalPitUtils;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILoadBar;

public class UICharcoalPit extends CoreScreenLayer {
    private EntityRef charcoalPitEntity;
    private InventoryGrid input;
    private InventoryGrid output;
    private UIButton process;
    private UILoadBar burningProgress;

    @Override
    public void initialise() {
        input = find("input", InventoryGrid.class);
        output = find("output", InventoryGrid.class);

        InventoryGrid player = find("player", InventoryGrid.class);
        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        process = find("process", UIButton.class);
        burningProgress = find("burningProgress", UILoadBar.class);
    }

    public void setCharcoalPit(final EntityRef entity) {
        this.charcoalPitEntity = entity;

        CharcoalPitComponent charcoalPit = entity.getComponent(CharcoalPitComponent.class);

        input.setTargetEntity(entity);
        input.setCellOffset(0);
        input.setMaxCellCount(charcoalPit.inputSlotCount);

        output.setTargetEntity(entity);
        output.setCellOffset(charcoalPit.inputSlotCount);
        output.setMaxCellCount(charcoalPit.outputSlotCount);

        process.setText("To Charcoal");
        process.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        entity.send(new ProduceCharcoalRequest());
                    }
                });
    }

    @Override
    public void update(float delta) {
        if (!charcoalPitEntity.exists()) {
            CoreRegistry.get(NUIManager.class).closeScreen(this);
            return;
        }

        super.update(delta);

        long worldTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);
        if (charcoalPit.burnFinishWorldTime > worldTime) {
            // It's burning wood now
            input.setVisible(false);
            process.setVisible(false);
            output.setVisible(false);
            burningProgress.setVisible(true);
            burningProgress.setValue(1f * (worldTime - charcoalPit.burnStartWorldTime) / (charcoalPit.burnFinishWorldTime - charcoalPit.burnStartWorldTime));
        } else {
            // It's not burning wood
            input.setVisible(true);
            output.setVisible(true);
            burningProgress.setVisible(false);

            int logCount = CharcoalPitUtils.getLogCount(charcoalPitEntity);

            process.setVisible(CharcoalPitUtils.canBurnCharcoal(logCount, charcoalPitEntity));
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
