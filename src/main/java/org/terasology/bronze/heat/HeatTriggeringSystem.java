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
package org.terasology.bronze.heat;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.machines.events.ProcessingMachineChanged;
import org.terasology.registry.In;

import java.util.Iterator;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HeatTriggeringSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private static final float REMOVE_FUEL_THRESHOLD = 1f;
    private static final long TRIGGER_INTERVAL = 100;
    private long lastChecked;

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (currentTime > lastChecked + TRIGGER_INTERVAL) {
            lastChecked = currentTime;

            for (EntityRef entityRef : entityManager.getEntitiesWith(HeatConsumerComponent.class)) {
                entityRef.send(new ProcessingMachineChanged());
            }

            for (EntityRef entityRef : entityManager.getEntitiesWith(HeatProducerComponent.class)) {
                HeatProducerComponent producer = entityRef.getComponent(HeatProducerComponent.class);

                boolean changed = false;
                Iterator<HeatProducerComponent.FuelSourceConsume> fuelConsumedIterator = producer.fuelConsumed.iterator();
                while (fuelConsumedIterator.hasNext()) {
                    HeatProducerComponent.FuelSourceConsume fuelSourceConsume = fuelConsumedIterator.next();
                    if (HeatUtils.doCalculationForOneFuelSourceConsume(producer.heatStorageEfficiency, currentTime, fuelSourceConsume) < REMOVE_FUEL_THRESHOLD) {
                        fuelConsumedIterator.remove();
                        changed = true;
                    } else {
                        break;
                    }
                }
                if (changed) {
                    entityRef.saveComponent(producer);
                }
            }
        }
    }
}
