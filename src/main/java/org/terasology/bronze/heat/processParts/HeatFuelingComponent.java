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
package org.terasology.bronze.heat.processParts;

import org.terasology.bronze.heat.HeatProducerComponent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.machines.processParts.ProcessPart;
import org.terasology.registry.CoreRegistry;

public class HeatFuelingComponent implements Component, ProcessPart {
    public long burnLength;
    public float energyProvided;

    @Override
    public void resolve(EntityRef outputEntity) {
        HeatProducerComponent heatProducer = outputEntity.getComponent(HeatProducerComponent.class);
        if (heatProducer != null) {
            long time = CoreRegistry.get(Time.class).getGameTimeInMs();

            HeatProducerComponent.FuelSourceConsume consume = new HeatProducerComponent.FuelSourceConsume();
            consume.startTime = time;
            consume.burnLength = burnLength;
            consume.energyProvided = energyProvided;

            heatProducer.fuelConsumed.add(consume);

            outputEntity.saveComponent(heatProducer);
        }
    }

    @Override
    public boolean validate(EntityRef entity) {
        return true;
    }

    @Override
    public boolean isOutput() {
        return true;
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
