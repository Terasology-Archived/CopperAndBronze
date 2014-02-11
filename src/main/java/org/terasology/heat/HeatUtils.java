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
package org.terasology.heat;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

public final class HeatUtils {
    private HeatUtils() {

    }

    public static float doCalculationForOneFuelSourceConsume(float heatStorageEfficiency, long gameTime,
                                                             HeatProducerComponent.FuelSourceConsume fuelSourceConsume) {
        float secondsSinceEnd = (gameTime - fuelSourceConsume.startTime - fuelSourceConsume.burnLength) / 1000f;

        if (secondsSinceEnd > 0) {
            // Finished burning - utilise formula for continuous compounding to calculate cumulative loss of heat - (e^(-(1/efficiency)*time))
            return fuelSourceConsume.energyProvided * (float) Math.pow(Math.E, -(1 / heatStorageEfficiency) * secondsSinceEnd);
        } else {
            return fuelSourceConsume.energyProvided * (gameTime - fuelSourceConsume.startTime) / 1000f / fuelSourceConsume.burnLength;
        }
    }

    public static float calculateHeatForProducer(EntityRef entityRef) {
        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        HeatProducerComponent producer = entityRef.getComponent(HeatProducerComponent.class);
        if (producer == null) {
            return 0;
        }

        float heat = 0;
        for (HeatProducerComponent.FuelSourceConsume fuelSourceConsume : producer.fuelConsumed) {
            heat += doCalculationForOneFuelSourceConsume(producer.heatStorageEfficiency, gameTime, fuelSourceConsume);
        }

        return heat;
    }

    public static float calculateHeatForConsumer(EntityRef entityRef, BlockEntityRegistry blockEntityRegistry) {
        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        HeatConsumerComponent heatConsumer = entityRef.getComponent(HeatConsumerComponent.class);
        if (heatConsumer == null) {
            return -1;
        }

        float result = 0;

        Region3i entityBlocks = getEntityBlocks(entityRef);

        for (Vector3i entityBlock : entityBlocks) {
            for (Side heatDirection : heatConsumer.heatDirections) {
                Vector3i heatProducerPosition = entityBlock.clone();
                heatProducerPosition.add(heatDirection.getVector3i());

                EntityRef potentialHeatProducer = blockEntityRegistry.getEntityAt(heatProducerPosition);
                HeatProducerComponent heatProducerComponent = potentialHeatProducer.getComponent(HeatProducerComponent.class);

                if (heatProducerComponent.heatDirections.contains(heatDirection.reverse())) {
                    result += calculateHeatForProducer(potentialHeatProducer);
                }
            }
        }

        for (HeatConsumerComponent.ResidualHeat residualHeat : heatConsumer.residualHeat) {
            float timeSinceHeatWasEstablished = (gameTime - residualHeat.time) / 1000f;
            result += residualHeat.baseHeat * Math.pow(Math.E, -1 * timeSinceHeatWasEstablished);
        }

        return result * heatConsumer.heatConsumptionEfficiency;
    }

    public static Region3i getEntityBlocks(EntityRef entityRef) {
        BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i blockPosition = blockComponent.getPosition();
            return Region3i.createBounded(blockPosition, blockPosition);
        }
        BlockRegionComponent blockRegionComponent = entityRef.getComponent(BlockRegionComponent.class);
        return blockRegionComponent.region;
    }
}
