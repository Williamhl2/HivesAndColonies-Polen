package com.hivesandcolonies.hccharacters.character.soa.dialogue;

import java.util.List;

import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRewardPool;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                .rare(player -> new ItemStack(Items.TORCH, 24 + player.level().random.nextInt(17)),
                        "Provisiones. No brillan, pero evitan que termines negociando con un zombi.")
                .rare(player -> new ItemStack(Items.COAL, 8 + player.level().random.nextInt(9)),
                        "Carbón. Humilde, útil y menos peligroso que tu entusiasmo.")
                .rare(player -> new ItemStack(Items.RAW_IRON, 2 + player.level().random.nextInt(4)),
                        "Hierro decente. No lo desperdicies en una pala heroica.")
                .rare(player -> new ItemStack(Items.LAPIS_LAZULI, 4 + player.level().random.nextInt(6)),
                        "Lapis. Sirve más cuando sabes encantar que cuando sabes presumir.")
                .legendary(player -> new ItemStack(Items.DIAMOND),
                        "Uno. No una fortuna. Si eso te vuelve imprudente, te lo quito de vuelta.")
                .legendary(player -> new ItemStack(Items.EMERALD, 2 + player.level().random.nextInt(3)),
                        "Las montañas pagaron poco, pero pagaron.");
    }
}
