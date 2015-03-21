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
package org.terasology.cab.system;

import org.terasology.cab.component.CharcoalPitComponent;
import org.terasology.cab.event.OpenCharcoalPitRequest;
import org.terasology.cab.event.ProduceCharcoalRequest;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.block.regions.BlockRegionComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CharcoalPitAuthoritySystem extends BaseComponentSystem {
    public static final String PRODUCE_CHARCOAL_ACTION_PREFIX = "CopperAndBronze:ProduceCharcoal|";
    @In
    private Time time;
    @In
    private PrefabManager prefabManager;
    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;
    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent
    public void userActivatesCharcoalPit(ActivateEvent event, EntityRef entity, CharcoalPitComponent charcoalPit) {
        entity.send(new OpenCharcoalPitRequest());
    }

    @ReceiveEvent
    public void startBurningCharcoal(ProduceCharcoalRequest event, EntityRef entity,
                                     CharcoalPitComponent charcoalPit, InventoryComponent inventoryComponent) {
        int logCount = CharcoalPitUtils.getLogCount(entity);

        if (CharcoalPitUtils.canBurnCharcoal(logCount, entity)) {
            // Remove logs from inventory
            for (int i = 0; i < charcoalPit.inputSlotCount; i++) {
                EntityRef itemInSlot = InventoryUtils.getItemAt(entity, i);
                if (itemInSlot.exists()) {
                    inventoryManager.removeItem(entity, entity, itemInSlot, true);
                }
            }

            int charcoalCount = CharcoalPitUtils.getResultCharcoalCount(logCount, entity);
            int burnLength = 5 * 60 * 1000;

            // Set burn length
            charcoalPit.burnStartWorldTime = time.getGameTimeInMs();
            charcoalPit.burnFinishWorldTime = charcoalPit.burnStartWorldTime + burnLength;
            entity.saveComponent(charcoalPit);

            Prefab prefab = prefabManager.getPrefab("CopperAndBronze:CharcoalPitSmoke");
            BlockParticleEffectComponent particles = prefab.getComponent(BlockParticleEffectComponent.class);
            entity.addComponent(particles);

            BlockRegionComponent region = entity.getComponent(BlockRegionComponent.class);
            if (region != null) {
                Vector3f center = region.region.center();
                Vector3i max = region.region.max();

                LocationComponent location = entity.getComponent(LocationComponent.class);
                location.setWorldPosition(new Vector3f(center.x - 0.5f, max.y + 1, center.z - 0.5f));
                entity.saveComponent(location);
            }

            delayManager.addDelayedAction(entity, PRODUCE_CHARCOAL_ACTION_PREFIX + charcoalCount, burnLength);
        }
    }

    @ReceiveEvent
    public void charcoalBurningFinished(DelayedActionTriggeredEvent event, EntityRef entity,
                                        CharcoalPitComponent charcoalPit, InventoryComponent inventoryComponent) {
        String actionId = event.getActionId();
        if (actionId.startsWith(PRODUCE_CHARCOAL_ACTION_PREFIX)) {

            entity.removeComponent(BlockParticleEffectComponent.class);

            int count = Integer.parseInt(actionId.substring(PRODUCE_CHARCOAL_ACTION_PREFIX.length()));
            for (int i = charcoalPit.inputSlotCount; i < charcoalPit.inputSlotCount + charcoalPit.outputSlotCount; i++) {
                EntityRef itemInSlot = InventoryUtils.getItemAt(entity, i);
                if (!itemInSlot.exists()) {
                    int toAdd = Math.min(count, 99);
                    EntityRef charcoalItem = entityManager.create("CopperAndBronze:Charcoal");
                    ItemComponent item = charcoalItem.getComponent(ItemComponent.class);
                    item.stackCount = (byte) toAdd;
                    charcoalItem.saveComponent(item);
                    if (!inventoryManager.giveItem(entity, entity, charcoalItem, i)) {
                        charcoalItem.destroy();
                    } else {
                        count -= toAdd;
                    }
                }
                if (count == 0) {
                    break;
                }
            }
        }
    }
}
