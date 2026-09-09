// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.core.world;

import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.naming.Name;

/**
 * The biomes of the world, in two tiers.
 * <p>
 * The first eleven are the ordinary bands of latitude: a temperate middle to each hemisphere,
 * growing colder towards the poles. The last seven are the extreme regions, one per piece of
 * technology the player has to build in order to survive there — they are not elsewhere, they are
 * the far ends of the same map.
 * <p>
 * <b>Two of the seven cannot be a surface biome at all</b>, and they are marked as such below
 * rather than given invented numbers. A biome here is one column of the world; the skies are not a
 * column, and the deep underground is a volume rather than a column.
 * <p>
 * Adding a value here is not free: {@code DefaultFloraProvider} reads a probability for
 * <em>every</em> value at construction and unboxes it, so a value with no entry there is a null
 * pointer at world start, not a missing feature.
 */
public enum CoreBiome implements Biome {
    MOUNTAINS("Mountains"),
    SNOW("Snow"),
    DESERT("Desert"),
    FOREST("Forest"),
    OCEAN("Ocean"),
    BEACH("Beach"),
    PLAINS("Plains"),

    // The ordinary biomes the bands of latitude asked for and the first seven did not cover.

    /** The cold forest between the snow and the temperate woods. */
    TAIGA("Taiga"),
    /**
     * Hot, open, and dry without being a desert.
     * <p>
     * TODO material — a savannah is dry grass, and CoreAssets only has one Grass. It therefore
     * borrows the grassland's block: what is missing is the tint, not the material. Fixing it means
     * either a DryGrass block or a per-biome tint, and the second one is the honest answer.
     */
    SAVANNA("Savannah"),
    /** Low, flat and waterlogged: the ordinary twin of the miasmic swamp, minus the rot. */
    SWAMP("Swamp"),

    // The seven extreme regions. Each one is opened by one piece of technology.

    /** Submarine — the deep sea floor, under the pack ice as much as out in the open. */
    ABYSS("Abyss"),
    /** Ski-train — the northern cap: a frozen ocean, fractured into plates. */
    PACK_ICE("Pack Ice"),
    /** Ski-train — the southern cap: one floating slab, ending in a straight cliff. */
    ICE_SHELF("Ice Shelf"),
    /** Obsidian boat — the seas of lava, in the mountain ranges. */
    VOLCANIC("Volcanic"),
    /** Anti-corrosion suit — the miasmic swamps. */
    MIASMA("Miasma"),
    /** Stasis sphere — the zones of unstable magic, which is to say the lightning. */
    ARCANE("Arcane"),
    /**
     * Airship — the skies.
     * <p>
     * <b>No parameters, on purpose.</b> The skies have no column of their own, so no threshold in
     * {@code CubeBiomeProvider} assigns this value and nothing generates it. It exists so that the
     * seven regions can be enumerated. It has no case in {@link #getTemperature()} or
     * {@link #getHumidity()} either — the fallback answers for it, and giving it numbers would
     * suggest it had been thought about.
     */
    SKY("Sky"),
    /**
     * Drilling rig — the deep underground, unreachable any other way.
     * <p>
     * <b>No parameters, on purpose</b>, for the same reason as {@link #SKY} and one more: what is
     * underground is a volume, not a column, so it wants a field read at every point rather than
     * one name per column. Naming a volume after a column is the mistake this value is here to
     * remember, not to make.
     */
    UNDERGROUND("Underground");

    private final Name id;
    private final String displayName;

    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;
    private Block gravel;
    private Block basalt;
    private Block crystal;
    private Block ash;
    private Block fulgurite;
    private Block peat;
    private Block blight;
    private Block packIce;
    private Block shelfIce;

    CoreBiome(String displayName) {
        this.id = new Name("CoreWorlds:" + name());
        this.displayName = displayName;
    }

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:stone");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        gravel = blockManager.getBlock("CoreAssets:Gravel");
        basalt = blockManager.getBlock("CoreAssets:Basalt");
        crystal = blockManager.getBlock("CoreAssets:Crystal");
        ash = blockManager.getBlock("CoreAssets:Ash");
        fulgurite = blockManager.getBlock("CoreAssets:Fulgurite");
        peat = blockManager.getBlock("CoreAssets:Peat");
        blight = blockManager.getBlock("CoreAssets:Blight");
        packIce = blockManager.getBlock("CoreAssets:PackIce");
        shelfIce = blockManager.getBlock("CoreAssets:ShelfIce");
    }

    @Override
    public float getHumidity() {
        switch (this) {
            case MOUNTAINS:
                return 0.3f;
            case SNOW:
                return 0.4f;
            case DESERT:
                return 0.1f;
            case FOREST:
                return 0.7f;
            case OCEAN:
                return 1;
            case BEACH:
                return 0.8f;
            case PLAINS:
                return 0.5f;
            case TAIGA:
                return 0.6f;
            case SAVANNA:
                return 0.3f;
            case SWAMP:
                return 0.95f;
            case ABYSS:
                return 1;
            case PACK_ICE:
                return 0.5f;
            case ICE_SHELF:
                return 0.4f;
            case VOLCANIC:
                return 0.05f;
            case MIASMA:
                return 1;
            case ARCANE:
                return 0.4f;
            default:
                // SKY and UNDERGROUND: no parameters, see their declarations.
                return 0.5f;
        }
    }

    @Override
    public float getTemperature() {
        switch (this) {
            case MOUNTAINS:
                return 0.2f;
            case SNOW:
                return 0;
            case DESERT:
                return 0.9f;
            case FOREST:
                return 0.6f;
            case OCEAN:
                return 0.4f;
            case BEACH:
                return 0.7f;
            case PLAINS:
                return 0.5f;
            case TAIGA:
                return 0.25f;
            case SAVANNA:
                return 0.8f;
            case SWAMP:
                return 0.6f;
            case ABYSS:
                return 0.3f;
            case PACK_ICE:
            case ICE_SHELF:
                return 0;
            case VOLCANIC:
                return 1;
            case MIASMA:
                return 0.65f;
            case ARCANE:
                return 0.5f;
            default:
                // SKY and UNDERGROUND: no parameters, see their declarations.
                return 0.5f;
        }
    }

    @Override
    public Block getSurfaceBlock(Vector3ic pos, int seaLevel) {
        int height = pos.y() - seaLevel;
        switch (this) {
            case FOREST:
            case PLAINS:
            case MOUNTAINS:
                if (height > 96) {
                    return snow;
                } else if (height >= 0) {
                    return grass;
                } else {
                    return dirt;
                }
            case TAIGA:
                // Lower than the temperate tree line, because a taiga is already halfway to the cap.
                if (height > 64) {
                    return snow;
                } else if (height >= 0) {
                    return grass;
                } else {
                    return dirt;
                }
            case SAVANNA:
                // The grassland's block, for want of a dry one — see the declaration.
                return height >= 0 ? grass : dirt;
            case SNOW:
                if (height >= 0) {
                    return snow;
                } else {
                    return dirt;
                }
            case DESERT:
            case OCEAN:
            case BEACH:
                return sand;
            case ABYSS:
                return gravel;
            case PACK_ICE:
                return packIce;
            case ICE_SHELF:
                return shelfIce;
            case VOLCANIC:
                return ash;
            case ARCANE:
                return fulgurite;
            case SWAMP:
                return peat;
            case MIASMA:
                return blight;
            default:
                return dirt;
        }
    }

    @Override
    public Block getBelowSurfaceBlock(Vector3ic pos, float density) {
        switch (this) {
            case DESERT:
                if (density > 8) {
                    return stone;
                } else {
                    return sand;
                }
            case BEACH:
                if (density > 2) {
                    return stone;
                } else {
                    return sand;
                }
            case OCEAN:
            case ABYSS:
                return stone;
            case VOLCANIC:
                return basalt;
            case ARCANE:
                return crystal;
            case SWAMP:
            case MIASMA:
                // Peat is a surface material: a few blocks of it, then the rock underneath.
                if (density > 8) {
                    return stone;
                } else {
                    return peat;
                }
            case PACK_ICE:
                if (density > 8) {
                    return stone;
                } else {
                    return packIce;
                }
            case ICE_SHELF:
                if (density > 8) {
                    return stone;
                } else {
                    return shelfIce;
                }
            default:
                if (density > 32) {
                    return stone;
                } else {
                    return dirt;
                }
        }
    }

    @Override
    public Name getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

}
