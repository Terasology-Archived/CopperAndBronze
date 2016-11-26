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
import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.crafting.system.CraftingWorkstationUpgradeProcess;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.crafting.ui.workstation.StationAvailableRecipesWidget;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.heat.ui.ThermometerWidget;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILoadBar;
import org.terasology.was.ui.VerticalTextureProgressWidget;
import org.terasology.was.ui.WorkstationScreenUtils;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Collections;
import java.util.List;

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

    private EntityRef workstation;
    private String upgradeRecipeDisplayed;
    private String matchingUpgradeRecipe;

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
        workstation = station;

        WorkstationScreenUtils.setupInventoryGrid(station, ingredientsInventory, "INPUT");
        WorkstationScreenUtils.setupInventoryGrid(station, toolsInventory, "TOOL");
        WorkstationScreenUtils.setupInventoryGrid(station, upgrades, "UPGRADE");
        WorkstationScreenUtils.setupInventoryGrid(station, resultInventory, "OUTPUT");
        WorkstationScreenUtils.setupInventoryGrid(station, fuelInput, "FUEL");

        WorkstationScreenUtils.setupTemperatureWidget(station, temperature, 20f);

        upgradeButton.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        character.send(new WorkstationProcessRequest(station, matchingUpgradeRecipe));
                    }
                });
        upgradeButton.setVisible(false);

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
                        WorkstationProcessingComponent.ProcessDef heatingProcess = processing.processes.get(CopperAndBronze.BASIC_SMITHING_PROCESS);
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
                        WorkstationProcessingComponent.ProcessDef heatingProcess = processing.processes.get(CopperAndBronze.BASIC_SMITHING_PROCESS);
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
    public void update(float delta) {
        if (!workstation.exists()) {
            CoreRegistry.get(NUIManager.class).closeScreen(this);
            return;
        }
        super.update(delta);

        WorkstationRegistry craftingRegistry = CoreRegistry.get(WorkstationRegistry.class);

        matchingUpgradeRecipe = getMatchingUpgradeRecipe(craftingRegistry);
        if (!isSame(matchingUpgradeRecipe, upgradeRecipeDisplayed)) {
            if (upgradeRecipeDisplayed != null) {
                upgradeButton.setVisible(false);
            }
            if (matchingUpgradeRecipe != null) {
                upgradeButton.setVisible(true);
            }
            upgradeRecipeDisplayed = matchingUpgradeRecipe;
        }

    }

    private boolean isSame(String recipe1, String recipe2) {
        if (recipe1 == null && recipe2 == null) {
            return true;
        }
        if (recipe1 == null || recipe2 == null) {
            return false;
        }
        return recipe1.equals(recipe2);
    }

    private String getMatchingUpgradeRecipe(WorkstationRegistry craftingRegistry) {
        for (WorkstationProcess workstationProcess : craftingRegistry.getWorkstationProcesses(Collections.singleton(CraftingStationUpgradeRecipeComponent.PROCESS_TYPE))) {
            if (workstationProcess instanceof CraftingWorkstationUpgradeProcess) {
                UpgradeRecipe upgradeRecipe = ((CraftingWorkstationUpgradeProcess) workstationProcess).getUpgradeRecipe();
                final UpgradeRecipe.UpgradeResult upgradeResult = upgradeRecipe.getMatchingUpgradeResult(workstation);
                if (upgradeResult != null) {
                    return workstationProcess.getId();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
