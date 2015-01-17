/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.cab.ui;

import org.terasology.cab.CopperAndBronze;
import org.terasology.crafting.ui.workstation.StationAvailableRecipesWidget;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.heat.ui.ThermometerWidget;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILoadBar;
import org.terasology.was.ui.VerticalTextureProgressWidget;
import org.terasology.was.ui.WorkstationScreenUtils;
import org.terasology.workstation.component.WorkstationProcessingComponent;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class MetalStationWindow extends BaseInteractionScreen {

    private InventoryGrid ingredientsInventory;
    private InventoryGrid toolsInventory;
    private ThermometerWidget temperature;
    private VerticalTextureProgressWidget burn;
    private InventoryGrid fuelInput;
    private StationAvailableRecipesWidget availableRecipes;
    private InventoryGrid resultInventory;
    private UILoadBar craftingProgress;
    private InventoryGrid upgrades;
    private UIButton upgradeButton;

    @Override
    public void initialise() {
        ingredientsInventory = find("ingredientsInventory", InventoryGrid.class);
        upgrades = find("upgradesInventory", InventoryGrid.class);
        upgradeButton = find("upgradeButton", UIButton.class);

        upgradeButton.setText("Upgrade");

        toolsInventory = find("toolsInventory", InventoryGrid.class);

        temperature = find("temperature", ThermometerWidget.class);

        burn = find("burn", VerticalTextureProgressWidget.class);
        burn.setMinY(76);
        burn.setMaxY(4);

        fuelInput = find("fuelInput", InventoryGrid.class);

        availableRecipes = find("availableRecipes", StationAvailableRecipesWidget.class);

        craftingProgress = find("craftingProgress", UILoadBar.class);

        resultInventory = find("resultInventory", InventoryGrid.class);

        InventoryGrid playerInventory = find("playerInventory", InventoryGrid.class);

        playerInventory.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        playerInventory.setCellOffset(10);
        playerInventory.setMaxCellCount(30);
    }

    @Override
    protected void initializeWithInteractionTarget(final EntityRef station) {
        WorkstationScreenUtils.setupInventoryGrid(station, ingredientsInventory, "INPUT");
        WorkstationScreenUtils.setupInventoryGrid(station, toolsInventory, "TOOL");
        WorkstationScreenUtils.setupInventoryGrid(station, upgrades, "UPGRADE");
        WorkstationScreenUtils.setupInventoryGrid(station, resultInventory, "OUTPUT");
        WorkstationScreenUtils.setupInventoryGrid(station, fuelInput, "FUEL");

        WorkstationScreenUtils.setupTemperatureWidget(station, temperature, 20f);

        burn.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        HeatProducerComponent heatProducer = station.getComponent(HeatProducerComponent.class);
                        List<HeatProducerComponent.FuelSourceConsume> consumedFuel = heatProducer.fuelConsumed;
                        if (consumedFuel.size() == 0) {
                            return 0f;
                        }
                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        HeatProducerComponent.FuelSourceConsume lastConsumed = consumedFuel.get(consumedFuel.size() - 1);
                        if (gameTime > lastConsumed.startTime + lastConsumed.burnLength) {
                            return 0f;
                        }
                        return 1f - (1f * (gameTime - lastConsumed.startTime) / lastConsumed.burnLength);
                    }

                    @Override
                    public void set(Float value) {
                    }
                });

        availableRecipes.setStation(station);

        craftingProgress.bindVisible(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        WorkstationProcessingComponent processing = station.getComponent(WorkstationProcessingComponent.class);
                        if (processing == null) {
                            return false;
                        }
                        WorkstationProcessingComponent.ProcessDef heatingProcess = processing.processes.get(CopperAndBronze.BASIC_METALCRAFTING_PROCESS_TYPE);
                        return heatingProcess != null;
                    }

                    @Override
                    public void set(Boolean value) {
                    }
                });
        craftingProgress.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        WorkstationProcessingComponent processing = station.getComponent(WorkstationProcessingComponent.class);
                        if (processing == null) {
                            return 1f;
                        }
                        WorkstationProcessingComponent.ProcessDef heatingProcess = processing.processes.get(CopperAndBronze.BASIC_METALCRAFTING_PROCESS_TYPE);
                        if (heatingProcess == null) {
                            return 1f;
                        }

                        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

                        return 1f * (gameTime - heatingProcess.processingStartTime) / (heatingProcess.processingFinishTime - heatingProcess.processingStartTime);
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
