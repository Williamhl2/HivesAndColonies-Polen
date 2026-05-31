# Polen AI

## Ultima pasada de limpieza

Tambien se extrajeron estas responsabilidades:

- `PolenInteractionController`
- `PolenAmbientDialogueController`
- `PolenDangerMemoryTracker`
- `PolenGoalRegistry`
- `PolenSpeakerResolver`
- `PolenChapterDialogueResolver`
- `PolenAmbientToneResolver`
- `PolenAmbientDialogueResolver`

Objetivo practico:

- que `PolenEntity` conserve estado y ciclo de vida
- que seleccion de dialogo, emision ambiental, memoria de peligro y registro de goals no vuelvan a inflarlo

## Debug y tests

`/polen ai get` ahora incluye mas señal:

- mood
- razon del mood
- `unsafeArea`
- `shouldSeekSafety`
- `shouldUseUnsafeDialogue`
- `nearRememberedInterest`

Tambien se agrego una base minima de tests unitarios puros con JUnit 5 para:

- tono ambiental por afinidad
- resolucion de dialogo por capitulo
- construccion de keys de ambient dialogue
- matematica de memoria de peligro

## Estado actual del refactor

`PolenEntity` sigue siendo la coordinadora principal, pero ya no debe crecer como archivo unico.

Responsabilidades ya extraidas:

- `PolenInteractionController`
  - interaccion principal con jugador
  - reveal del nombre
  - trigger de dialogo normal y progreso basico
- `PolenAmbientDialogueController`
  - cooldown y broadcast local de dialogos ambientales
- `PolenDangerMemoryTracker`
  - memoria temporal de zonas peligrosas
  - expiracion y persistencia NBT de esa memoria
- `PolenGoalRegistry`
  - registro de prioridades de goals fuera de la entidad
- `PolenMoodController`
  - calculo de moods segun entorno, afinidad y actividad
- `PolenMemoryHandler`
  - descubrimiento de intereses, resting spot y semillas de memoria local
- `PolenSafetyEvaluator`
  - spots seguros, spots peligrosos y validacion de refugio/superficie
- `PolenQuietActivityController`
  - hobbies pasivos, temporizador sincronizado y particulas cliente
- `PolenNbtHelper`
  - guardado y carga reutilizable de `BlockPos` en NBT
- `PolenRoutinePlanner`
  - seleccion de destinos contextuales para rutina
  - validacion de spots recordados
  - filtro de intereses seguros
- `PolenSafetyNavigator`
  - decision de huida
  - busqueda de spots seguros alcanzables
  - replanteo de ruta cuando Polen se atasca o no logra salir
- `PolenMagicController`
  - blink de emergencia con particulas y sonido
  - microhechizos durante quiet activity
  - puente practico entre IA y el eje narrativo de Ars Nouveau

Responsabilidades que aun conserva `PolenEntity`:

- estado sincronizado de la entidad
- persistencia de estado propio
- ciclo base de `tick`
- algunos getters/setters de estado compartido entre goals y controladores

Regla de mantenimiento actual:

- si una responsabilidad puede probarse o evolucionar sin conocer toda la entidad, debe vivir fuera de `PolenEntity`
- si una clase externa solo necesita comportamiento, debe depender del controller/planner correcto y no de una fachada publica nueva en `PolenEntity`

## Ajuste reciente de escape y peligro

La logica de peligro ahora separa tres conceptos:

- `unsafe`
  - lugar poco comodo para hobbies o rutina pasiva
- `shouldSeekSafety`
  - lugar que justifica salir activamente
- `unsafe dialogue`
  - dialogo reservado para contextos tipo cueva o encierro, no para exterior normal

Efectos practicos:

- Polen ya no deberia quejarse de "salir de aqui" solo por estar al aire libre en un contexto no cavernoso
- la huida reintenta una nueva ruta si la anterior termina mal
- la busqueda de escape ahora prioriza spots alcanzables y con ganancia vertical cuando conviene subir
- si no existe salida fisica razonable, Polen puede hacer blink a una superficie segura cercana en vez de quedarse congelada

## Magia y Ars

Polen no usa magia como mago de combate ni como sistema generico.

La implementacion actual la trata como magia intima e instintiva:

- escape magico cuando queda atrapada en un encierro imposible
- pequenas respuestas visuales del source cuando canta o dibuja en calma
- dialogo ambiental propio para esos momentos

Esto mantiene alineados:

- su caracter reservado
- el foco narrativo en intimidad antes que epica
- la presencia real de Ars Nouveau dentro de su comportamiento

## Objetivo

La IA de Polen no debe sentirse como la de un aldeano genérico.

Su comportamiento debe reflejar personalidad, contexto y progreso.

Los rasgos actuales que guían la implementación son:

- tímida
- curiosa
- reservada
- observadora
- inclinada a dibujar y cantar cuando se siente segura

## Archivo principal

- [PolenEntity.java](../../src/main/java/com/hivesandcolonies/polen/entity/PolenEntity.java)

Este archivo concentra:

- estado sincronizado
- mood
- actividad tranquila
- memoria básica de lugares
- diálogos ambientales contextuales
- actualización periódica de IA pasiva

## Estado sincronizado actual

### Actividad tranquila

Valores:

- `none`
- `singing`
- `drawing`

Uso:

- controlar partículas cliente
- permitir que goals sepan si Polen ya está ocupada

### Mood

Ver [PolenMood.java](../../src/main/java/com/hivesandcolonies/polen/entity/ai/PolenMood.java).

Estados:

- `CALM`
- `TIMID`
- `CURIOUS`
- `INSPIRED`
- `UNSETTLED`

Reglas actuales:

- jugador desconocido demasiado cerca -> `TIMID`
- lluvia/tormenta en cielo abierto -> `UNSETTLED`
- hobby activo -> `INSPIRED`
- cerca de intereses recordados -> `CURIOUS`
- fallback -> `CALM`

## Memoria actual

Polen recuerda tres cosas:

- `favoriteFlowerPos`
- `favoriteHivePos`
- `restingPos`
- `dangerousSpotPos`

Estas posiciones:

- se descubren por escaneo local o por goals
- se guardan en NBT de la entidad
- alimentan la rutina contextual
- también pueden bloquear destinos futuros si una zona fue percibida como peligrosa

## Goals actuales por prioridad

### `PolenKeepDistanceGoal`

Archivo:

- [PolenKeepDistanceGoal.java](../../src/main/java/com/hivesandcolonies/polen/entity/ai/goal/PolenKeepDistanceGoal.java)

Responsabilidad:

- retroceder si un jugador sin suficiente afinidad invade el espacio personal

### `PolenRoutineGoal`

Archivo:

- [PolenRoutineGoal.java](../../src/main/java/com/hivesandcolonies/polen/entity/ai/goal/PolenRoutineGoal.java)

Responsabilidad:

- darle intención a sus desplazamientos
- usar clima, hora y memoria para elegir destino

Lógica actual:

- noche o lluvia -> descansar / volver a `restingPos`
- mañana o día -> visitar flores o colmenas recordadas

### `PolenIdleHobbyGoal`

Archivo:

- [PolenIdleHobbyGoal.java](../../src/main/java/com/hivesandcolonies/polen/entity/ai/goal/PolenIdleHobbyGoal.java)

Responsabilidad:

- ejecutar dibujo o canto cuando el entorno es seguro

### `PolenCuriousInterestGoal`

Archivo:

- [PolenCuriousInterestGoal.java](../../src/main/java/com/hivesandcolonies/polen/entity/ai/goal/PolenCuriousInterestGoal.java)

Responsabilidad:

- buscar flores, nidos o colmenas y observarlas
- reforzar curiosidad y alimentar memoria

## Señales visuales actuales

- canto -> `NOTE`
- dibujo -> `ENCHANT`

No son una solución final de animación. Son telemetría visual simple para que el comportamiento sea visible sin assets adicionales.

## Debug

Usar:

```text
/polen ai get
```

Devuelve:

- mood actual
- actividad tranquila actual
- posiciones recordadas

## Dialogos ambientales

Archivo:

- [PolenDialogueManager.java](../../src/main/java/com/hivesandcolonies/polen/dialogue/PolenDialogueManager.java)

La IA puede acompañar acciones pasivas con líneas cortas y variables.

Reglas actuales:

- solo se disparan al inicio de acciones significativas
- se envían a jugadores cercanos, no globalmente
- cada jugador recibe una variante según su afinidad con Polen
- existe cooldown para evitar spam

Situaciones actuales:

- `ambient_singing`
- `ambient_drawing`
- `ambient_curiosity`
- `ambient_timid`
- `ambient_unsafe`

Tonos actuales:

- `guarded`
- `soft`
- `warm`

La selección del tono depende de la afinidad del jugador que recibe la línea, no de un estado global único.

## Seguridad y cuevas

La IA actual trata cuevas, oscuridad y zonas subterráneas como contextos inseguros.

Capas actuales:

- `PolenSeekSafetyGoal` fuerza salida a una zona más segura cuando ya está en peligro.
- `PolenSafeStrollGoal` reemplaza el paseo aleatorio por uno que filtra destinos inseguros.
- rutina, hobbies y curiosidad se cancelan si Polen detecta un área insegura.
- al comenzar la salida de una zona peligrosa puede emitir diálogo ambiental `ambient_unsafe`.
- la posición peligrosa queda recordada por un tiempo y se evita en decisiones futuras.

La intención no es solo evitar daño. También debe comunicar que Polen percibe esos lugares como incómodos o amenazantes.

Heurística actual de seguridad:

- prefiere superficie o zonas muy cercanas a superficie
- exige luz local suficiente
- no exige ver cielo directo, para no tratar bosques o copas de árboles como cuevas
- la memoria de peligro usa distancia horizontal corta para evitar una cueva sin bloquear toda la superficie cercana

## Como expandir la IA sin romperla

### Añadir un nuevo mood

1. Extender `PolenMood`.
2. Actualizar `updateMood()` en `PolenEntity`.
3. Si afecta comportamiento, hacer que algún goal lo consulte.

### Añadir una nueva memoria

1. Agregar campo en `PolenEntity`.
2. Guardar/cargar en NBT.
3. Definir quién la descubre y quién la usa.

### Añadir un nuevo hobby

1. Crear nuevo valor de actividad tranquila.
2. Ajustar `pickQuietActivity()`.
3. Añadir feedback visual o de animación.
4. Revisar `PolenIdleHobbyGoal`.

### Añadir una nueva rutina

1. Decidir si depende de tiempo, clima, capítulo o afinidad.
2. Modificar `getRoutineTarget()` o crear un goal separado.
3. Mantener prioridades limpias. No poner todo dentro de un solo `Goal`.

## Errores a evitar

- convertir la IA en una lista de random strolls con textos encima
- meter lógica de progreso narrativo en goals de navegación
- spamear mensajes al jugador desde `tick`
- añadir hobbies sin memoria ni contexto
- ignorar afinidad cuando el comportamiento debería cambiar con la relación

## Siguientes capas recomendadas

- comentarios ambientales raros y contextuales
- reacción a destrucción de flores o agresión a abejas
- rutas más claras dentro de la colonia
- lugares favoritos persistentes asociados a estructuras narrativas reales
- animaciones dedicadas para dibujo, observación y canto

---

## Evolución de Personalidad por Progreso Narrativo

La IA actual representa la personalidad inicial de Polen, pero no debe limitar su arco completo.

Polen empieza tímida, reservada y sensible a la cercanía, pero puede evolucionar hacia una personalidad más segura, alegre y expresiva cuando la historia y la afinidad lo justifican.

Documento relacionado:

- [POLEN_CHARACTER_ARC.md](POLEN_CHARACTER_ARC.md)

### Principio

El progreso global de la historia no reemplaza la afinidad individual.

Una Polen de capítulos tardíos puede ser segura y alegre con sus personas cercanas, pero seguir siendo reservada con jugadores desconocidos o de baja afinidad.

### Capítulos 0-1

Personalidad predominante:

- tímida
- cautelosa
- reservada

Comportamiento esperado:

- mantener distancia
- usar `TIMID` con facilidad
- priorizar seguridad
- cantar o dibujar solo en contextos muy seguros

### Capítulos 2-3

Personalidad predominante:

- tranquila
- curiosa
- observadora

Comportamiento esperado:

- visitar lugares seguros de la colonia
- observar construcción y Source
- usar más `CURIOUS` e `INSPIRED`

### Capítulos 4-7

Personalidad predominante:

- compañera
- curiosa activa
- más cálida con alta afinidad

Comportamiento esperado:

- recordar flores y colmenas importantes
- usar lugares favoritos
- mover `restingPos` hacia espacios seguros de la colonia
- asociar la Residencia de Polen como centro emocional al final de la etapa

### Capítulos 8-11

Personalidad predominante:

- buscadora
- inspirada
- vulnerable ante recuerdos

Comportamiento esperado:

- reaccionar a ruinas, símbolos y registros
- alternar entre `INSPIRED` y `UNSETTLED`
- volver a lugares seguros tras eventos narrativos fuertes

### Capítulos 12-13

Personalidad predominante:

- introspectiva
- honesta
- en proceso de aceptación

Comportamiento esperado:

- reducir evasión narrativa
- sostener conversaciones más directas
- usar `CALM` después de eventos de aceptación

### Capítulos 14-15 y postgame

Personalidad predominante:

- segura
- alegre
- expresiva con cercanos
- protectora

Comportamiento esperado:

- iniciar más interacciones contextuales
- cantar o dibujar con menos vergüenza
- usar la residencia y espacios comunitarios con naturalidad
- conservar distancia con jugadores de baja afinidad

## Posibles moods futuros

Los moods actuales son suficientes para la primera etapa, pero capítulos tardíos podrían justificar nuevos estados.

Opciones futuras:

- `CONFIDENT`
- `JOYFUL`
- `PROTECTIVE`

No añadirlos hasta que exista comportamiento real que los use.

## Regla de Afinidad

La afinidad debe seguir afectando tono y comportamiento incluso cuando la historia avance.

Ejemplo:

- Capítulo 15 + baja afinidad: Polen es segura, pero formal.
- Capítulo 15 + alta afinidad: Polen es segura, alegre y cercana.

Esto evita que el progreso global borre la relación individual con cada jugador.
