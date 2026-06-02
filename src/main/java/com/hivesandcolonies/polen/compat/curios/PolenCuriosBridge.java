package com.hivesandcolonies.polen.compat.curios;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.polen.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Runtime bridge for Curios.
 *
 * Curios is a required gameplay dependency for Polen, but this bridge keeps all direct Curios interaction in one
 * compatibility boundary. The rest of Polen talks in terms of affinity/equipment and never reaches into Curios APIs.
 */
public final class PolenCuriosBridge {
    public static final String CHARM_SLOT = "charm";

    private PolenCuriosBridge() {
    }

    public static boolean isCuriosAvailable() {
        try {
            Class.forName("top.theillusivec4.curios.api.CuriosApi");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static ItemStack stackForAffinity(PolenWorldAffinity affinity) {
        Item item = switch (affinity == null ? PolenWorldAffinity.NONE : affinity) {
            case APIARIST -> ModItems.APIARIST_CHARM.get();
            case ARCANE -> ModItems.ARCANE_CHARM.get();
            case COLONIAL -> ModItems.COLONIAL_CHARM.get();
            case HARVEST -> ModItems.HARVEST_CHARM.get();
            case ARTISAN -> ModItems.ARTISAN_CHARM.get();
            case WAYFARER -> ModItems.WAYFARER_CHARM.get();
            case NONE -> null;
        };
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static void syncAffinityCharmToCurios(PolenEntity polen) {
        if (polen == null || polen.level().isClientSide) {
            return;
        }

        ItemStack charmStack = stackForAffinity(polen.getPolenEquipmentInventory().getAffinityCharm());
        if (charmStack.isEmpty()) {
            return;
        }

        ItemStack current = getCuriosStack(polen, CHARM_SLOT, 0);
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, charmStack)) {
            return;
        }

        setCuriosStack(polen, CHARM_SLOT, 0, charmStack);
    }

    public static ItemStack getCuriosStack(LivingEntity entity, String slot, int index) {
        Object stacks = resolveStacks(entity, slot);
        if (stacks == null) {
            return ItemStack.EMPTY;
        }

        try {
            Method getSlots = stacks.getClass().getMethod("getSlots");
            int slots = ((Number) getSlots.invoke(stacks)).intValue();
            if (index < 0 || index >= slots) {
                return ItemStack.EMPTY;
            }

            Method getStackInSlot = stacks.getClass().getMethod("getStackInSlot", int.class);
            Object value = getStackInSlot.invoke(stacks, index);
            return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean setCuriosStack(LivingEntity entity, String slot, int index, ItemStack stack) {
        Object stacks = resolveStacks(entity, slot);
        if (stacks == null || stack == null) {
            return false;
        }

        try {
            Method getSlots = stacks.getClass().getMethod("getSlots");
            int slots = ((Number) getSlots.invoke(stacks)).intValue();
            if (index < 0 || index >= slots) {
                return false;
            }

            Method setStackInSlot = stacks.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            setStackInSlot.invoke(stacks, index, stack.copy());
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static Object resolveStacks(LivingEntity entity, String slot) {
        Object inventory = resolveCuriosInventory(entity);
        if (inventory == null) {
            return null;
        }

        Object stackHandler = invokeOptional(inventory, "getStacksHandler", new Class<?>[]{String.class}, slot);
        if (stackHandler == null) {
            return null;
        }

        try {
            Method getStacks = stackHandler.getClass().getMethod("getStacks");
            return getStacks.invoke(stackHandler);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Object resolveCuriosInventory(LivingEntity entity) {
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getCuriosInventory = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
            Object raw = getCuriosInventory.invoke(null, entity);
            return unwrapOptionalLike(raw);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Object invokeOptional(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            Object raw = method.invoke(target, args);
            return unwrapOptionalLike(raw);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Object unwrapOptionalLike(Object raw) {
        if (raw == null) {
            return null;
        }

        if (raw instanceof Optional<?> optional) {
            return optional.orElse(null);
        }

        try {
            Method resolve = raw.getClass().getMethod("resolve");
            Object resolved = resolve.invoke(raw);
            if (resolved instanceof Optional<?> optional) {
                return optional.orElse(null);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Not a LazyOptional-like object.
        }

        return raw;
    }
}
