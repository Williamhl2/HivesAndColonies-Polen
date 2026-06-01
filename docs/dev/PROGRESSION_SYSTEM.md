# Progression System

## Resumen

La progresión está separada en dos ejes:

- progreso global del mundo
- relación individual por jugador

Esa separación es intencional.

## Progreso global del mundo

Archivos:

- [PolenChapterManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenChapterManager.java)
- [PolenStoryFlagsManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenStoryFlagsManager.java)
- [PolenWorldStorySavedData.java](../../src/main/java/com/hivesandcolonies/polen/progression/world/PolenWorldStorySavedData.java)
- [PolenWorldStoryData.java](../../src/main/java/com/hivesandcolonies/polen/progression/world/PolenWorldStoryData.java)

Guarda:

- capítulo actual
- flags de historia
- UUID de Polen
- estado de spawn

Se persiste en el overworld mediante `SavedData`.

## Relacion por jugador

Archivos:

- [PolenAffinityLevels.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAffinityLevels.java)
- [PolenAffinityManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAffinityManager.java)
- [PolenPlayerRelationshipManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/player/PolenPlayerRelationshipManager.java)
- [PolenPlayerRelationshipData.java](../../src/main/java/com/hivesandcolonies/polen/progression/player/PolenPlayerRelationshipData.java)

Guarda:

- afinidad
- cantidad de interacciones
- tareas completadas
- último tiempo de interacción
- flags de relación específicos por jugador

## Flags actuales

Ver [PolenStoryFlag.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenStoryFlag.java).

Actualmente:

- `NAME_REVEALED`
- `CHAPTER_0_COMPLETE`
- `PLAYER_HAS_SHELTER`

Estos flags son globales del mundo, no por jugador.

## Eventos narrativos

Archivo:

- [PolenStoryEventManager.java](../../src/main/java/com/hivesandcolonies/polen/story/PolenStoryEventManager.java)

Responsabilidad:

- reproducir secuencias de diálogo
- marcar flags
- avanzar capítulo cuando corresponda
- otorgar advancements

## Advancements

Archivo:

- [PolenAdvancementManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAdvancementManager.java)

Funciona como capa de servicio.

No contiene lógica narrativa compleja; solo resuelve IDs y otorga criterios.

## Regla de diseño recomendada

Cuando agregues una nueva feature narrativa:

1. Decide si pertenece al mundo o al jugador.
2. Si cambia capítulos o hitos globales, usar `PolenWorldStorySavedData`.
3. Si cambia confianza o historial personal, usar `PolenPlayerRelationshipManager`.
4. Si requiere escena, crear un método en `PolenStoryEventManager`.
5. Si requiere recompensa visual en progreso, conectar advancement.

## Errores comunes a evitar

- Guardar afinidad en flags globales.
- Poner lógica de progreso compleja dentro de `PolenEntity.tick()`.
- Saltar capítulos desde diálogos normales sin evento formal.
- Duplicar constantes de capítulo o flags fuera de sus managers.
