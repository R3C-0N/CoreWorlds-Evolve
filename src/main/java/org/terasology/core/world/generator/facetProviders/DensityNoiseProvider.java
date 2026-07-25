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

        // Defensive: densityRegion can be wider in X/Z than surfaceRoughnessFacet's area, in which case
        // reading roughness per density column would run off the end of it. That mismatch is an engine
        // bug rather than anything this provider controls - WorldBuilder.determineBorders propagates
        // borders in a single reverse pass over a provider list whose order is not a guaranteed
        // topological order, so a sides border another module asks for on DensityFacet (Caves'
        // CaveToSurfaceProvider requires it with sides=3) can land after this provider has already
        // decided how much SurfaceRoughnessFacet needs. Clamping keeps worldgen alive on engines that
        // still have that bug; it is a no-op once the borders are sized correctly.
        // (BlockArea's 2nd axis, minY()/maxY(), is world Z here - 2D facets are indexed (x, z).)
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
