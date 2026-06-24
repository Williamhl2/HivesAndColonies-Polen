package com.hivesandcolonies.hccharacters.character.lucy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;

public final class LucyVillageTavernManager {
    private static final int TAVERN_MARKER_SCAN_RADIUS = 64;

    private LucyVillageTavernManager() {
    }

    public static void ensureTavernGenerated(ServerLevel level, BlockPos origin) {
        if (level == null || origin == null) {
            return;
        }
        ensureSceneLocation(level, origin);
    }

    public static LucyVillageSceneLocator.SceneLocation ensureSceneLocation(ServerLevel level, BlockPos origin) {
        if (level == null || origin == null) {
            return null;
        }

        BlockPos bellPos = LucyVillageSceneLocator.findNearestBell(level, origin);
        if (bellPos == null) {
            return null;
        }

        LucyVillageTavernSavedData savedData = LucyVillageTavernSavedData.get(level);
        LucyVillageTavernSavedData.TavernSite savedSite = savedData.getTavern(bellPos);
        if (savedSite != null && isUsable(level, savedSite)) {
            LucyVillageSceneLocator.SceneLocation scene = savedSite.toSceneLocation();
            LucyVillageBoardHelper.ensureBoardNearAnchor(level, scene.anchorPos());
            return scene;
        }

        LucyVillageSceneLocator.SceneLocation existingScene = findExistingTavernScene(level, bellPos);
        if (existingScene != null) {
            savedData.putTavern(new LucyVillageTavernSavedData.TavernSite(
                    bellPos.immutable(),
                    existingScene.anchorPos(),
                    existingScene.lucyPos(),
                    existingScene.soaPos()
            ));
            LucyVillageBoardHelper.ensureBoardNearAnchor(level, existingScene.anchorPos());
            return existingScene;
        }

        if (savedSite != null) {
            savedData.removeTavern(bellPos);
        }

        LucyVillageSceneLocator.SceneLocation generatedScene = LucyVillageTavernStructurePlacer.tryPlaceNearVillage(level, bellPos);
        if (generatedScene != null) {
            savedData.putTavern(new LucyVillageTavernSavedData.TavernSite(
                    bellPos.immutable(),
                    generatedScene.anchorPos(),
                    generatedScene.lucyPos(),
                    generatedScene.soaPos()
            ));
            LucyVillageBoardHelper.ensureBoardNearAnchor(level, generatedScene.anchorPos());
            return generatedScene;
        }

        return null;
    }

    private static LucyVillageSceneLocator.SceneLocation findExistingTavernScene(ServerLevel level, BlockPos bellPos) {
        BlockPos markerPos = LucyVillageBoardHelper.findMarkerLanternNearby(level, bellPos, TAVERN_MARKER_SCAN_RADIUS);
        if (markerPos == null) {
            return null;
        }

        LucyVillageSceneLocator.SceneLocation tavernScene = LucyVillageSceneLocator.findTavernScene(level, markerPos, bellPos);
        if (tavernScene != null) {
            return tavernScene;
        }

        return LucyVillageSceneLocator.findScene(level, markerPos);
    }

    private static boolean isUsable(ServerLevel level, LucyVillageTavernSavedData.TavernSite site) {
        return canStandAt(level, site.anchorPos())
                && canStandAt(level, site.lucyPos())
                && canStandAt(level, site.soaPos())
                && countDomumBlocksNearby(level, site.anchorPos()) >= 16;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    private static int countDomumBlocksNearby(ServerLevel level, BlockPos center) {
        int count = 0;
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 5; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getNamespace().equals("domum_ornamentum")) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
