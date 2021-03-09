/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.cab.generator;

import org.terasology.anotherWorld.decorator.ore.OreDefinition;
import org.terasology.anotherWorld.decorator.structure.PocketStructureDefinition;
import org.terasology.anotherWorld.decorator.structure.provider.UniformPocketBlockProvider;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
public class NativeTinGeneration extends PocketStructureDefinition implements OreDefinition {
    public NativeTinGeneration() {
        super(new UniformPocketBlockProvider(CoreRegistry.get(BlockManager.class).getBlock("CopperAndBronze:NativeTin")),
                new PDist(0.2f, 0.08f), new PDist(3f, 1f), new PDist(2f, 0.5f), new PDist(1700f, 200f), new PDist(0f, 0.35f),
                new PDist(1f, 0f), new PDist(0.7f, 0.1f), new PDist(0.2f, 0f), new PDist(0f, 0f));
    }

    @Override
    public String getOreId() {
        return "CopperAndBronze:NativeTin";
    }
}
