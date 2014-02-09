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
package org.terasology.bronze.system;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.registry.In;
import org.terasology.workstation.component.CraftingStationIngredientComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class CopperAndBronzeJournalIntegration implements ComponentSystem {
    @In
    private JournalManager journalManager;

    private String chapterId = "CopperAndBronze";

    @Override
    public void initialise() {
        journalManager.registerJournalChapter(chapterId, Assets.getTexture("CopperAndBronze:CopperAndBronzeJournal"), "Copper and Bronze");

        journalManager.registerJournalEntry(chapterId, "chalcopyriteCrystalAndStation",
                "I found Chalcopyrite Crystal, it is a great source of copper. I should be able crush it in metal station into " +
                        "Chalcopyrite Crystal Dust. Once in that form, I will be able to extract copper out of it to build stronger " +
                        "tools.\n\nTo build the station, I need two Cobblestone blocks side-by-side and use my hammer on them " +
                        "as with any previous stations.");

        journalManager.registerJournalEntry(chapterId, "chalcopyriteCrystal",
                "I found Chalcopyrite Crystal, it is a great source of copper. I should be able crush it in metal station into " +
                        "Chalcopyrite Crystal Dust. Once in that form, I will be able to extract copper out of it to build stronger " +
                        "tools.");

        journalManager.registerJournalEntry(chapterId, "nativeCopperAndStation",
                "I found Native Copper. Boy, am I lucky! This is a very rare find and is a pure source of copper. I should be able " +
                        "to build stronger tools using it in metal station.\n\nTo build the station, I need two " +
                        "Cobblestone blocks side-by-side and use my hammer on them as with any previous stations.");

        journalManager.registerJournalEntry(chapterId, "nativeCopper",
                "I found Native Copper. Boy, am I lucky! This is a very rare find and is a pure source of copper. I should be able " +
                        "to build stronger tools using it in metal station.");

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character) {
        CraftingStationIngredientComponent ingredientComponent = event.getNewItem().getComponent(CraftingStationIngredientComponent.class);
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            if (ingredientType.equals("CopperAndBronze:chalcopyriteCrystal") &&
                    !(journalManager.hasEntry(character, chapterId, "chalcopyriteCrystalAndStation")
                            || journalManager.hasEntry(character, chapterId, "chalcopyriteCrystal"))) {
                if (journalManager.hasEntry(character, chapterId, "nativeCopperAndStation")) {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "chalcopyriteCrystal"));
                } else {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "chalcopyriteCrystalAndStation"));
                }
            } else if (ingredientType.equals("CopperAndBronze:copperNugget") &&
                    !(journalManager.hasEntry(character, chapterId, "nativeCopperAndStation")
                            || journalManager.hasEntry(character, chapterId, "nativeCopper"))) {
                if (journalManager.hasEntry(character, chapterId, "chalcopyriteCrystalAndStation")) {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "nativeCopper"));
                } else {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "nativeCopperAndStation"));
                }
            }
        }
    }
}
