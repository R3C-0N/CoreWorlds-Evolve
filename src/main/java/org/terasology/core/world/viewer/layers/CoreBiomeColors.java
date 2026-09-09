// Copyright 2014 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.core.world.viewer.layers;

import com.google.common.collect.Maps;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.nui.Color;

import java.util.Map;
import java.util.function.Function;

/**
 * Maps the core biomes to colors
 */
public class CoreBiomeColors implements Function<Biome, Color> {

    private final Map<Biome, Color> biomeColors = Maps.newHashMap();

    public CoreBiomeColors() {
        biomeColors.put(CoreBiome.DESERT, new Color(0xb0a087ff));
        biomeColors.put(CoreBiome.MOUNTAINS, new Color(0x899a47ff));
        biomeColors.put(CoreBiome.PLAINS, new Color(0x80b068ff));
        biomeColors.put(CoreBiome.SNOW, new Color(0x99ffffff));
        biomeColors.put(CoreBiome.FOREST, new Color(0x439765ff));
        biomeColors.put(CoreBiome.OCEAN, new Color(0x44447aff));
        biomeColors.put(CoreBiome.BEACH, new Color(0xd0c087ff));

        // The ordinary biomes the bands of latitude asked for, kept in the same family of tones as
        // their neighbours: a taiga reads as a colder forest, a savannah as a drier plain.
        biomeColors.put(CoreBiome.TAIGA, new Color(0x3d7a63ff));
        biomeColors.put(CoreBiome.SAVANNA, new Color(0xb8b25bff));
        biomeColors.put(CoreBiome.SWAMP, new Color(0x5a6b46ff));

        // The extreme regions, deliberately outside those families. A region that a whole piece of
        // technology opens has to be visible from the next one over — on the map as in the world.
        biomeColors.put(CoreBiome.ABYSS, new Color(0x1c1c3cff));
        biomeColors.put(CoreBiome.PACK_ICE, new Color(0xdff0f5ff));
        biomeColors.put(CoreBiome.ICE_SHELF, new Color(0xc2e0f0ff));
        biomeColors.put(CoreBiome.VOLCANIC, new Color(0x4a2b26ff));
        biomeColors.put(CoreBiome.MIASMA, new Color(0x7a8f2eff));
        biomeColors.put(CoreBiome.ARCANE, new Color(0x8a5bb8ff));

        // Nothing generates these two, so nothing will ask for their colour. They are here because
        // a null colour out of this map is a crash in the viewer, not a blank pixel.
        biomeColors.put(CoreBiome.SKY, new Color(0x9ecbffff));
        biomeColors.put(CoreBiome.UNDERGROUND, new Color(0x3a3a3aff));
    }

    @Override
    public Color apply(Biome biome) {
        return biomeColors.get(biome);
    }

    /**
     * @param biome the biome
     * @param color the new color
     */
    public void setBiomeColor(Biome biome, Color color) {
        this.biomeColors.put(biome, color);
    }
}
