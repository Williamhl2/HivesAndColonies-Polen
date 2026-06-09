package com.hivesandcolonies.hccharacters.character.soa.dialogue;

import java.util.List;

import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRewardPool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public final class SoaMarjorieDialogue {
    public static final String PROFILE_ID = "soa_marjorie_mining_guide";
    public static final String SPEAKER = "SoaMarjorie";

    public static final List<String> TIER_0 = List.of(
            "Nunca bajes sin antorchas. La oscuridad no mata sola, pero invita a quien si lo hace.",
            "El carbon es humilde, pero sin el no hay expedicion larga.",
            "Si encuentras una cueva muy grande, primero ilumina la entrada. Luego exploras.",
            "Un cubo de agua vale mas que media armadura cuando aparece lava.",
            "Marca el camino de vuelta. La mejor minera del mundo tambien se pierde si se confia."
    );

    public static final List<String> TIER_1 = List.of(
            "Para diamantes, baja cerca de Y -59. No es magia: es paciencia y buena capa.",
            "El hierro abunda en montanas altas y tambien aparece bastante en cuevas. Si necesitas mucho, mira hacia arriba.",
            "El oro ama los badlands. En el Nether tambien se deja ver, pero alli todo cobra peaje.",
            "La redstone y el diamante suelen compartir las profundidades. Si ves una, presta atencion alrededor.",
            "El lapislazuli no solo sirve para encantar. Tambien te dice que estas trabajando en una buena franja."
    );

    public static final List<String> TIER_2 = List.of(
            "Las esmeraldas pertenecen a las montanas. Raras, orgullosas y faciles de pasar por alto.",
            "Si el eco vuelve seco, hay una camara grande cerca. Entra con antorchas, no con orgullo.",
            "La lava canta antes de morder. Cuando la oigas, pica con respeto.",
            "Las mejores vetas suelen estar detras de una pared que nadie pensaria romper.",
            "He pasado mas noches bajo tierra que bajo las estrellas. La roca ensena si sabes escuchar."
    );

    public static final List<String> TIER_3 = List.of(
            "Ya reconoces el sonido de una cueva viva. Bien. Eso salva mas vidas que un escudo.",
            "Si buscas netherita, no caves con prisa. La prisa alimenta lagos de lava.",
            "Cuando una mina se vuelve demasiado silenciosa, revisa tus salidas antes de seguir.",
            "Te he visto mejorar. Ya no caminas bajo tierra como turista.",
            "La montana recompensa al paciente, no al codicioso. Recuerdalo cuando veas el primer diamante."
    );

    private SoaMarjorieDialogue() {}

    public static NpcRewardPool rewardPool() {
        return new NpcRewardPool()
                .rare(player -> new ItemStack(Items.DIAMOND_BLOCK),
                        "No es netherita, pero un bloque de diamante nunca estorba en buenas manos.")
                .rare(player -> new ItemStack(Items.EMERALD_BLOCK),
                        "Las montanas me debian esto. Quedatelo.")
                .rare(player -> new ItemStack(Items.DIAMOND, 4 + player.level().random.nextInt(5)),
                        "Diamantes. No suficientes para volverte imprudente, espero.")
                .legendary(player -> new ItemStack(Items.ANCIENT_DEBRIS),
                        "No suelo regalar esto. Me costo bastante encontrarlo.")
                .legendary(player -> new ItemStack(Items.NETHERITE_SCRAP),
                        "Guarda este fragmento. La netherita castiga a quien la presume demasiado pronto.")
                .legendary(player -> new ItemStack(Items.ANCIENT_DEBRIS, player.level().random.nextInt(6) == 0 ? 2 : 1),
                        "Hoy la profundidad fue generosa. No esperes que vuelva a ocurrir pronto.")
                .unique(SoaMarjorieDialogue::legendaryPickaxe,
                        "He llevado esto durante anos. Creo que ya encontré a la persona correcta.")
                .unique(player -> named(new ItemStack(Items.ECHO_SHARD), "Fragmento del Corazon de la Montana", ChatFormatting.AQUA),
                        "Esto no es un mineral cualquiera. Escucha bien cuando lo sostengas.")
                .unique(player -> named(new ItemStack(Items.FILLED_MAP), "Mapa de una veta olvidada", ChatFormatting.GOLD),
                        "Marque algo interesante hace tiempo. Quiza tu puedas llegar mas lejos que yo.");
    }

    private static ItemStack legendaryPickaxe(net.minecraft.world.entity.player.Player player) {
        ItemStack stack = named(new ItemStack(Items.NETHERITE_PICKAXE), "Pico del Primer Minero", ChatFormatting.LIGHT_PURPLE);
        var registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        stack.enchant(registry.getHolderOrThrow(Enchantments.EFFICIENCY), 5);
        stack.enchant(registry.getHolderOrThrow(Enchantments.UNBREAKING), 3);
        return stack;
    }

    private static ItemStack named(ItemStack stack, String name, ChatFormatting color) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(color));
        return stack;
    }
}
