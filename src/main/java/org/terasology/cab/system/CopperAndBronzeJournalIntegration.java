// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cab.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.utilities.Assets;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalEntryProducer;
import org.terasology.journal.JournalManager;
import org.terasology.journal.TimestampResolver;
import org.terasology.nui.HorizontalAlign;
import org.terasology.workstationCrafting.component.CraftingStationIngredientComponent;

import java.util.Arrays;
import java.util.Collection;

@RegisterSystem
public class CopperAndBronzeJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;

    private String chapterId = "CopperAndBronze";

    private ParagraphRenderStyle centerRenderStyle = new ParagraphRenderStyle() {
        @Override
        public HorizontalAlign getHorizontalAlignment() {
            return HorizontalAlign.CENTER;
        }
    };

    @Override
    public void initialise() {
        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        chapterHandler.registerJournalEntry("chalcopyriteCrystalAndStation",
                createTimestampEntryProducer("I found Chalcopyrite Crystal, it is a great source of copper. I should be able crush it in metal station into " +
                        "Chalcopyrite Crystal Dust. Once in that form, I will be able to extract copper out of it to build stronger " +
                        "tools.<l><l>To build the station, I need two Cobblestone blocks side-by-side and use my hammer on them " +
                        "as with any previous stations."));

        chapterHandler.registerJournalEntry("chalcopyriteCrystal",
                createTimestampEntryProducer("I found Chalcopyrite Crystal, it is a great source of copper. I should be able crush it in metal station into " +
                        "Chalcopyrite Crystal Dust. Once in that form, I will be able to extract copper out of it to build stronger " +
                        "tools."));

        chapterHandler.registerJournalEntry("nativeCopperAndStation",
                createTimestampEntryProducer("I found Native Copper. Boy, am I lucky! This is a very rare find and is a pure source of copper. I should be able " +
                        "to build stronger tools using it in metal station. To build the station, I need two " +
                        "Cobblestone blocks side-by-side and use my hammer on them as with any previous stations."));

        chapterHandler.registerJournalEntry("nativeCopper",
                createTimestampEntryProducer("I found Native Copper. Boy, am I lucky! This is a very rare find and is a pure source of copper. I should be able " +
                        "to build stronger tools using it in metal station."));

        journalManager.registerJournalChapter(chapterId, Assets.getTexture("CopperAndBronze:CopperAndBronzeJournal").get(), "Copper and Bronze",
                chapterHandler);
    }

    private JournalEntryProducer createTimestampEntryProducer(String text) {
        return new JournalEntryProducer() {
            @Override
            public Collection<ParagraphData> produceParagraph(long date) {
                return Arrays.asList(
                        HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, TimestampResolver.getJournalEntryDate(date)),
                        HTMLLikeParser.parseHTMLLikeParagraph(null,
                                text));
            }
        };
    }

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character, CharacterComponent characterComponent) {
        CraftingStationIngredientComponent ingredientComponent = event.getNewItem().getComponent(CraftingStationIngredientComponent.class);
        if (ingredientComponent != null) {
            String ingredientType = ingredientComponent.type;
            if (ingredientType.equals("CopperAndBronze:chalcopyriteCrystal")
                    && !(journalManager.hasEntry(character, chapterId, "chalcopyriteCrystalAndStation")
                            || journalManager.hasEntry(character, chapterId, "chalcopyriteCrystal"))) {
                if (journalManager.hasEntry(character, chapterId, "nativeCopperAndStation")) {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "chalcopyriteCrystal"));
                } else {
                    character.send(new DiscoveredNewJournalEntry(chapterId, "chalcopyriteCrystalAndStation"));
                }
            } else if (ingredientType.equals("CopperAndBronze:copperNugget")
                    && !(journalManager.hasEntry(character, chapterId, "nativeCopperAndStation")
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
