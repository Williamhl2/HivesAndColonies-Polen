package com.hivesandcolonies.hccharacters.character.lucy.world;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;

public final class LucyVillageTavernManager {
    private static final int TAVERN_MARKER_SCAN_RADIUS = 160;
    private static final int SAME_VILLAGE_TAVERN_RADIUS = 160;

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
        LucyVillageSceneLocator.SceneLocation savedScene = resolveSavedSite(level, savedSite, bellPos);
        if (savedScene != null) {
            return savedScene;
        }

        LucyVillageTavernSavedData.TavernSite nearbySavedSite = savedData.findNearbyTavern(bellPos, SAME_VILLAGE_TAVERN_RADIUS);
        LucyVillageSceneLocator.SceneLocation nearbySavedScene = resolveSavedSite(level, nearbySavedSite, bellPos);
        if (nearbySavedScene != null) {
            return nearbySavedScene;
        }

        ExistingTavern existingTavern = findExistingTavern(level, bellPos);
        if (existingTavern != null) {
            if (existingTavern.scene() != null) {
                LucyVillageSceneLocator.SceneLocation existingScene = existingTavern.scene();
                savedData.putTavern(new LucyVillageTavernSavedData.TavernSite(
                        bellPos.immutable(),
                        existingScene.anchorPos(),
                        existingScene.lucyPos(),
                        existingScene.soaPos()
                ));
                LucyVillageBoardHelper.ensureBoardNearAnchor(level, existingScene.anchorPos());
                return existingScene;
            }

            HcCharacters.LOGGER.warn(
                    "Found an existing Lucy/Soa tavern marker near village bell {} at {}, but no valid scene positions were found. Skipping fallback placement to avoid duplicate taverns.",
                    bellPos,
                    existingTavern.markerPos()
            );
            return null;
        }

        if (savedSite != null && !isPhysicallyPresent(level, savedSite)) {
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

    private static LucyVillageSceneLocator.SceneLocation resolveSavedSite(
            ServerLevel level,
            LucyVillageTavernSavedData.TavernSite site,
            BlockPos requestedBellPos
    ) {
        if (site == null) {
            return null;
        }
        if (isUsable(level, site)) {
            LucyVillageSceneLocator.SceneLocation scene = site.toSceneLocation();
            LucyVillageBoardHelper.ensureBoardNearAnchor(level, scene.anchorPos());
            return scene;
        }
        if (isPhysicallyPresent(level, site)) {
            HcCharacters.LOGGER.warn(
                    "Lucy/Soa tavern data near village bell {} points to an existing tavern around {}, but its scene positions are blocked. Skipping fallback placement to avoid duplicates.",
                    requestedBellPos,
                    site.anchorPos()
            );
        }
        return null;
    }

    private static ExistingTavern findExistingTavern(ServerLevel level, BlockPos bellPos) {
        BlockPos markerPos = LucyVillageBoardHelper.findMarkerLanternNearby(level, bellPos, TAVERN_MARKER_SCAN_RADIUS);
        if (markerPos == null) {
            return null;
        }

        LucyVillageSceneLocator.SceneLocation tavernScene = LucyVillageSceneLocator.findTavernScene(level, markerPos, bellPos);
        if (tavernScene == null) {
            tavernScene = LucyVillageSceneLocator.findScene(level, markerPos);
        }
        return new ExistingTavern(markerPos.immutable(), tavernScene);
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

    private static boolean isPhysicallyPresent(ServerLevel level, LucyVillageTavernSavedData.TavernSite site) {
        return site != null
                && (LucyVillageBoardHelper.hasMarkerLanternNearby(level, site.anchorPos(), 24)
                || countDomumBlocksNearby(level, site.anchorPos()) >= 16);
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

    private record ExistingTavern(BlockPos markerPos, LucyVillageSceneLocator.SceneLocation scene) {
    }
}

