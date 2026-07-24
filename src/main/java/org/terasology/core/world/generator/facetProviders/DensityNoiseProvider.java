// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.core.world.generator.facetProviders;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.core.world.generator.facets.SurfaceRoughnessFacet;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

/**
 * Adds some additional 3D noise to the DensityFacet, so as to introduce cliffs and overhangs and things.
 */
@Requires({
    @Facet(SurfaceRoughnessFacet.class)
})
@Updates({
    @Facet(value = DensityFacet.class, border = @FacetBorder(top = 1)),
    @Facet(SurfacesFacet.class)
})
public class DensityNoiseProvider implements ScalableFacetProvider {
    private SubSampledNoise largeNoise;
    private SubSampledNoise smallNoise;

    @Override
    public void setSeed(long seed) {
        BrownianNoise unscaled = new BrownianNoise(new SimplexNoise(seed), 4);
        unscaled.setPersistence(1);
        smallNoise = new SubSampledNoise(unscaled, new Vector3f(0.015f, 0.02f, 0.015f), 4);
        largeNoise = new SubSampledNoise(unscaled, new Vector3f(0.005f, 0.007f, 0.005f), 4);
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        SurfaceRoughnessFacet surfaceRoughnessFacet = region.getRegionFacet(SurfaceRoughnessFacet.class);
        DensityFacet densityFacet = region.getRegionFacet(DensityFacet.class);
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);

        BlockRegion densityRegion = densityFacet.getWorldRegion();
        float[] smallNoiseValues = smallNoise.noise(densityRegion, scale);
        float[] largeNoiseValues = largeNoise.noise(densityRegion, scale);
        float[] densityValues = densityFacet.getInternal();

        // surfaceRoughnessFacet's region isn't guaranteed to be as large as densityRegion - its border
        // is computed independently, from whatever the largest border any *other* active provider
        // requires on it happens to be, which can be smaller than what densityRegion ends up padded to
        // (e.g. once a provider like Caves' asks for extra border on DensityFacet specifically). Clamp
        // into surfaceRoughnessFacet's own bounds rather than assuming the two regions always match, to
        // avoid reading out of bounds. (BlockArea's 2nd axis, minY()/maxY(), is world Z here - 2D facets
        // are indexed (x, z), not (x, y).)
        BlockAreac roughnessArea = surfaceRoughnessFacet.getWorldArea();

        int x = densityRegion.minX();
        int y = densityRegion.minY();
        int z = densityRegion.minZ();
        for (int i = 0; i < densityValues.length; i++) {
            int roughnessX = Math.max(roughnessArea.minX(), Math.min(roughnessArea.maxX(), x));
            int roughnessZ = Math.max(roughnessArea.minY(), Math.min(roughnessArea.maxY(), z));
            float intensity = Math.max(0f, surfaceRoughnessFacet.getWorld(roughnessX, roughnessZ));
            float smallIntensity = Math.min(intensity, (1 + intensity) / 2);
            float largeIntensity = intensity - smallIntensity;
            densityValues[i] += smallNoiseValues[i] * intensity * 20 + largeNoiseValues[i] * largeIntensity * 60;

            x++;
            if (x > densityRegion.maxX()) {
                x = densityRegion.minX();
                y++;
                if (y > densityRegion.maxY()) {
                    y = densityRegion.minY();
                    z++;
                }
            }
        }

        for (Vector3ic pos : surfacesFacet.getWorldRegion()) {
            if (densityRegion.contains(pos) && densityRegion.contains(pos.x(), pos.y() + 1, pos.z())) {
                surfacesFacet.setWorld(pos, densityFacet.getWorld(pos) > 0 && densityFacet.getWorld(pos.x(), pos.y() + 1, pos.z()) <= 0);
            }
        }
    }
}
