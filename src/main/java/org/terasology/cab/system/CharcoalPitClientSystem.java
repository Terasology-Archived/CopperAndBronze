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
import org.terasology.cab.ui.UICharcoalPit;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem(value = RegisterMode.CLIENT)
public class CharcoalPitClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private NUIManager nuiManager;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private long lastUpdate;

    @Override
    public void initialise() {
    }

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        if (gameTimeInMs > lastUpdate + 250) {
            for (EntityRef charcoalPit : entityManager.getEntitiesWith(CharcoalPitComponent.class, BlockParticleEffectComponent.class)) {
                BlockParticleEffectComponent particles = charcoalPit.getComponent(BlockParticleEffectComponent.class);
                particles.spawnCount += 5;
                charcoalPit.saveComponent(particles);
            }

            lastUpdate = gameTimeInMs;
        }
    }

    @ReceiveEvent
    public void openCharcoalPitWindow(OpenCharcoalPitRequest event, EntityRef charcoalPit) {
        UICharcoalPit uiCharcoalPit = nuiManager.pushScreen("CopperAndBronze:CharcoalPit", UICharcoalPit.class);
        uiCharcoalPit.setCharcoalPit(charcoalPit);
    }
}
