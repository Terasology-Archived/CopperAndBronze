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
        float secondsSinceStart = (gameTime - fuelSourceConsume.startTime) / 1000f;
        float secondsSinceEnd = (gameTime - fuelSourceConsume.startTime - fuelSourceConsume.burnLength) / 1000f;

        if (secondsSinceEnd > 0) {
            // Finished burning
            return fuelSourceConsume.energyProvided * (fuelSourceConsume.burnLength / secondsSinceEnd) * heatStorageEfficiency;
        } else {
            return fuelSourceConsume.energyProvided * secondsSinceStart / fuelSourceConsume.burnLength;
        }
    }

    public static int calculateHeatForProducer(EntityRef entityRef) {
        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        HeatProducerComponent producer = entityRef.getComponent(HeatProducerComponent.class);
        if (producer == null) {
            return -1;
        }

        float heat = 0;
        for (HeatProducerComponent.FuelSourceConsume fuelSourceConsume : producer.fuelConsumed) {
            heat += doCalculationForOneFuelSourceConsume(producer.heatStorageEfficiency, gameTime, fuelSourceConsume);
        }

        return (int) heat;
    }

    public static int calculateHeatForConsumer(EntityRef entityRef, BlockEntityRegistry blockEntityRegistry) {
        HeatConsumerComponent heatConsumer = entityRef.getComponent(HeatConsumerComponent.class);
        if (heatConsumer == null) {
            return -1;
        }

        int result = 0;

        Region3i entityBlocks = getEntityBlocks(entityRef);

        for (Vector3i entityBlock : entityBlocks) {
            for (Side heatDirection : heatConsumer.heatDirections) {
                Vector3i heatProducerPosition = entityBlock.clone();
                heatProducerPosition.add(heatDirection.getVector3i());

                EntityRef potentialHeatProducer = blockEntityRegistry.getEntityAt(heatProducerPosition);
                HeatProducerComponent heatProducerComponent = potentialHeatProducer.getComponent(HeatProducerComponent.class);

                if (heatProducerComponent.heatDirections.contains(heatDirection.reverse())) {
                    int producerResult = calculateHeatForProducer(potentialHeatProducer);
                    if (producerResult > -1) {
                        result += producerResult * heatConsumer.heatConsumptionEfficiency;
                    }
                }
            }
        }

        return result;
    }

    private static Region3i getEntityBlocks(EntityRef entityRef) {
        BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i blockPosition = blockComponent.getPosition();
            return Region3i.createBounded(blockPosition, blockPosition);
        }
        BlockRegionComponent blockRegionComponent = entityRef.getComponent(BlockRegionComponent.class);
        return blockRegionComponent.region;
    }
}
