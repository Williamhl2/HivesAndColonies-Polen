# Polen AI

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
