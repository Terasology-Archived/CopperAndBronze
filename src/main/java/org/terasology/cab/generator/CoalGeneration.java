// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cab.generator;

import org.terasology.anotherWorld.decorator.ore.OreDefinition;
import org.terasology.anotherWorld.decorator.structure.PocketStructureDefinition;
import org.terasology.anotherWorld.decorator.structure.provider.UniformPocketBlockProvider;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
public class CoalGeneration extends PocketStructureDefinition implements OreDefinition {
    public CoalGeneration() {
        super(new UniformPocketBlockProvider(CoreRegistry.get(BlockManager.class).getBlock("Smithing:CoalOre")),
                new PDist(0.9f, 0.15f), new PDist(4f, 1f), new PDist(2f, 1f), new PDist(1800f, 500f), new PDist(0f,
                        0.35f),
                new PDist(1f, 0f), new PDist(0.7f, 0.1f), new PDist(0.2f, 0f), new PDist(0f, 0f));
    }

    @Override
    public String getOreId() {
        return "Smithing:CoalOre";
    }
}
