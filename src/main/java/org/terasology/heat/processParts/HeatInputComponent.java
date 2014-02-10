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
package org.terasology.heat.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.HeatUtils;
import org.terasology.machines.processParts.ProcessPart;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;

public class HeatInputComponent implements Component, ProcessPart {
    public float heat;

    @Override
    public void resolve(EntityRef outputEntity) {
    }

    @Override
    public boolean validate(EntityRef entity) {
        return HeatUtils.calculateHeatForConsumer(entity, CoreRegistry.get(BlockEntityRegistry.class)) >= heat;
    }

    @Override
    public boolean isOutput() {
        return false;
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
