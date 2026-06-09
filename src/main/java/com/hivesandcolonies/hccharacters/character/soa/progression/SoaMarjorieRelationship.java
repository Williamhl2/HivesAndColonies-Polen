package com.hivesandcolonies.hccharacters.character.soa.progression;

import java.util.function.IntFunction;

import com.hivesandcolonies.hccharacters.character.soa.dialogue.SoaMarjorieDialogue;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipLevels;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipManager;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipRecord;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class SoaMarjorieRelationship {
    public static final String CHARACTER_ID = SoaMarjorieDialogue.PROFILE_ID;
    public static final String DISPLAY_NAME = SoaMarjorieDialogue.SPEAKER;
    public static final IntFunction<String> RANK_RESOLVER = SoaMarjorieRelationship::rankName;

    private static final String FLAG_MET_AT_BOARD = "met_at_board";
    private static final String FLAG_RECEIVED_BOARD_GIFT = "received_board_gift";
    private static final String FLAG_FIRST_CAVE_MINING = "first_cave_mining";
    private static final String FLAG_FIRST_SHARED_ORE = "first_shared_ore";
    private static final String FLAG_ATTACKED_SOA = "attacked_soa";

    private static final String COUNTER_BOARD_VISITS = "board_visits";
    private static final String COUNTER_CAVE_ENCOUNTERS = "cave_encounters";
    private static final String COUNTER_SHARED_ORE = "shared_ore";

    private static final String COOLDOWN_ORE_SHARE = "soa_ore_share_affinity";
    private static final String COOLDOWN_ATTACK_WARNING = "soa_attack_warning_affinity";

    private static final long ORE_SHARE_AFFINITY_COOLDOWN = 20L * 90L;
    private static final long ATTACK_AFFINITY_COOLDOWN = 20L * 300L;

    private SoaMarjorieRelationship() {
    }

    public static NpcRelationshipRecord get(ServerPlayer player) {
        return NpcRelationshipManager.get(player, CHARACTER_ID);
    }

    public static int affinity(ServerPlayer player) {
        return get(player).affinity();
    }

    public static String rankName(int affinity) {
        return switch (NpcRelationshipLevels.rankIndex(affinity)) {
            case 4 -> "Companero de mina";
            case 3 -> "Mano confiable";
            case 2 -> "Aprendiz tolerable";
            case 1 -> "Aprendiz reciente";
            default -> "Novato peligroso";
        };
    }

    public static void recordBoardVisit(ServerPlayer player) {
        boolean firstMeeting = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_MET_AT_BOARD);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_MET_AT_BOARD);
        int visits = NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_BOARD_VISITS);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstMeeting ? 2 : 1,
                "soa_board_visit",
                firstMeeting
                        ? "Soa decidio que podias escuchar el tablón sin romper nada."
                        : "Soa noto que vuelves al tablón con menos cara de perderte.",
                RANK_RESOLVER
        );
        if (visits == 3) {
            NpcRelationshipManager.addAffinity(
                    player,
                    CHARACTER_ID,
                    DISPLAY_NAME,
                    1,
                    "soa_repeat_board_visits",
                    "Soa empieza a reconocer tu costumbre de aparecer donde hay trabajo.",
                    RANK_RESOLVER
            );
        }
    }

    public static void recordBoardGift(ServerPlayer player) {
        boolean firstGift = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_RECEIVED_BOARD_GIFT);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_RECEIVED_BOARD_GIFT);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstGift ? 2 : 1,
                "soa_board_gift",
                firstGift
                        ? "Aceptaste una herramienta util sin preguntar si era tesoro. Bien."
                        : "Soa aprobo que aun recuerdes el valor de las provisiones.",
                RANK_RESOLVER
        );
    }

    public static void recordCaveEncounter(ServerPlayer player) {
        boolean firstCave = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_FIRST_CAVE_MINING);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_FIRST_CAVE_MINING);
        NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_CAVE_ENCOUNTERS);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstCave ? 3 : 2,
                "soa_cave_encounter",
                firstCave
                        ? "Seguiste a Soa bajo tierra y no corriste en la primera sombra. Eso cuenta."
                        : "Soa noto que tus pasos bajo piedra ya no suenan tan perdidos.",
                RANK_RESOLVER
        );
    }

    public static void recordOreShared(ServerPlayer player, int totalItemsShared) {
        if (totalItemsShared <= 0) {
            return;
        }
        boolean firstShare = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_FIRST_SHARED_ORE);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_FIRST_SHARED_ORE);
        NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_SHARED_ORE);
        NpcRelationshipManager.addAffinityWithCooldown(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstShare ? 2 : 1,
                COOLDOWN_ORE_SHARE,
                firstShare
                        ? "Soa compartio parte de lo minado. No lo llames generosidad en voz alta."
                        : "Soa aprobo que acompanaras sin estorbar demasiado.",
                ORE_SHARE_AFFINITY_COOLDOWN,
                RANK_RESOLVER
        );
    }

    public static void recordAttack(ServerPlayer player) {
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_ATTACKED_SOA);
        NpcRelationshipManager.addAffinityWithCooldown(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                -8,
                COOLDOWN_ATTACK_WARNING,
                "Soa recordara ese golpe. La piedra perdona mas rapido que ella.",
                ATTACK_AFFINITY_COOLDOWN,
                RANK_RESOLVER
        );
    }

    public static String arrivalBoardLine(ServerPlayer player) {
        return switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> "Mira quien llego. Justo necesitaba a alguien que no gritara al ver grava caer.";
            case 3 -> "Bien. Contigo cerca al menos tengo a quien culpar si algo explota.";
            case 2 -> "Si vas a quedarte cerca del tablón, intenta parecer aprendiz y no accidente anunciado.";
            case 1 -> "Te reconozco. Sigues vivo, lo que en mineria ya es curriculum.";
            default -> "Tu. Novato. Si el tablón te manda bajo tierra, lee dos veces antes de cavar una tumba elegante.";
        };
    }

    public static String arrivalCaveLine(ServerPlayer player) {
        return switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> "Vamos, companero. Si encontramos diamantes, yo los encontre. Tu estabas presente.";
            case 3 -> "Si vas a seguirme, pisa donde piso. Y si sobrevives, quizá te deje presumirlo.";
            case 2 -> "Te guardé una veta decente. No digas que nunca hago cosas bonitas.";
            case 1 -> "Camina detras de mi. No por confianza, por seguridad.";
            default -> "No te pongas delante del pico. Parece obvio, pero he conocido gente menos lista.";
        };
    }

    public static String idleBoardLine(ServerPlayer player, RandomSource random) {
        String[] lines = switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> new String[] {
                    "Me caes bien. No lo arruines haciendo algo heroico.",
                    "Si alguien pregunta, tu ayudaste. Si encontramos diamantes, yo los encontre.",
                    "Te confiaria una ruta de mina. No mi pico. No exageremos."
            };
            case 3 -> new String[] {
                    "Bien. Contigo cerca al menos tengo a quien culpar si algo explota.",
                    "Buen ojo. No tan bueno como el mio, claro. Pero buen ojo.",
                    "Trajiste suerte o trajiste problemas. En las minas suele ser lo mismo."
            };
            case 2 -> new String[] {
                    "Vas mejorando. Hoy casi no pareces una tragedia con botas.",
                    "No esta mal. No bien, pero no esta mal.",
                    "Si sobrevives a dos encargos seguidos, quiza deje de llamarlo accidente."
            };
            case 1 -> new String[] {
                    "Marca el camino de vuelta. Una mina no se pierde: te convence de que tu sabes mas.",
                    "Si escuchas lava, no corras hacia ella. Si, he tenido que decirlo antes.",
                    "El carbon humilde salva mas viajes que el diamante orgulloso. Aprende eso primero."
            };
            default -> new String[] {
                    "Si vas a tocar una roca, al menos asegúrate de que no sea la que sostiene el techo.",
                    "Camina detras de mi. No por confianza, por seguridad.",
                    "Un tablón da encargos. Yo doy advertencias. Las mias suelen doler menos si obedeces."
            };
        };
        return lines[random.nextInt(lines.length)];
    }

    public static String idleCaveLine(ServerPlayer player, RandomSource random) {
        String[] lines = switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> new String[] {
                    "Te guardé una veta decente. No digas que nunca hago cosas bonitas.",
                    "Me caes bien. No lo arruines haciendo algo heroico.",
                    "Si encuentras lava, era tu turno. Si encuentras diamante, claramente seguiste mi ruta."
            };
            case 3 -> new String[] {
                    "Si vas a seguirme, pisa donde piso. Y si sobrevives, quizá te deje presumirlo.",
                    "Bien. Contigo cerca al menos tengo a quien culpar si algo explota.",
                    "Buen ritmo. Casi parece que las botas ya saben de que lado queda el suelo."
            };
            case 2 -> new String[] {
                    "No esta mal. No bien, pero no esta mal.",
                    "Antorcha cada pocos pasos. La oscuridad cobra intereses.",
                    "Si el eco vuelve seco, hay cámara grande adelante. Entra con antorchas, no con orgullo."
            };
            case 1 -> new String[] {
                    "Pisa donde piso. La cueva ya tiene suficientes ideas malas sin tus aportes.",
                    "No caves con prisa. La prisa alimenta lagos de lava.",
                    "Una veta expuesta es una invitacion, no una promesa. Mira alrededor antes de celebrar."
            };
            default -> new String[] {
                    "No te pongas delante del pico. Parece obvio, pero he conocido gente menos lista.",
                    "Si escuchas lava, no corras hacia ella. Si, he tenido que decirlo antes.",
                    "La mina no quiere matarte. Solo acepta sugerencias muy rapido."
            };
        };
        return lines[random.nextInt(lines.length)];
    }
}
