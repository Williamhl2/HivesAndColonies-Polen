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
            "Si vas a tocar una roca, al menos asegurate de que no sea la que sostiene el techo.",
            "No te pongas delante del pico. Parece obvio, pero he conocido gente menos lista.",
            "Camina detras de mi. No por confianza, por seguridad.",
            "Si escuchas lava, no corras hacia ella. Si, he tenido que decirlo antes.",
            "Un cubo de agua vale mas que media armadura cuando aparece lava. Aprende eso antes de presumir."
    );

    public static final List<String> TIER_1 = List.of(
            "Te reconozco. Sigues vivo, lo que en mineria ya es curriculum.",
            "Marca el camino de vuelta. Una mina no se pierde: te convence de que tu sabes mas.",
            "El carbon humilde salva mas viajes que el diamante orgulloso. Aprende eso primero.",
            "Pica con ritmo, no con panico. La piedra nota la diferencia.",
            "Para diamantes, baja cerca de Y -59. Para sobrevivir, baja con cerebro."
    );

    public static final List<String> TIER_2 = List.of(
            "Vas mejorando. Hoy casi no pareces una tragedia con botas.",
            "Si vas a seguirme, pisa donde piso. Y si sobrevives, quizá te deje presumirlo.",
            "No esta mal. No bien, pero no esta mal.",
            "Te daria una pala, pero temo que la uses para cavarte una excusa.",
            "Te guardé una veta decente. No digas que nunca hago cosas bonitas."
    );

    public static final List<String> TIER_3 = List.of(
            "Bien. Contigo cerca al menos tengo a quien culpar si algo explota.",
            "Me caes bien. No lo arruines haciendo algo heroico.",
            "Buen ojo. No tan bueno como el mio, claro. Pero buen ojo.",
            "Si alguien pregunta, tu ayudaste. Si encontramos diamantes, yo los encontre.",
            "Te confiaria una ruta de mina. No mi pico. No exageremos."
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
