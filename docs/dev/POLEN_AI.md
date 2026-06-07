# Polen AI

## Vision general

La IA de Polen no existe solo para mover una entidad.

Tiene que sostener tres cosas al mismo tiempo:

- legibilidad jugable
- coherencia emocional
- continuidad con el canon narrativo

Eso significa que la IA debe reflejar:

- seguridad o amenaza
- confianza o distancia
- rutina o desorientacion
- memoria recuperada o vacios de memoria

## Modelo por capas

```text
Needs
-> Intent
-> Task
-> Goal
-> Movement / Action
```

## Needs

Representan presiones internas.

Ejemplos actuales o esperados:

- Safety
- Rest
- Social
- Curiosity
- Comfort
- Memory pull

## Intent

Describe que quiere hacer Polen en este momento.

Ejemplos:

- ponerse a salvo
- descansar
- acercarse a alguien de confianza
- observar algo que le resulta calmante
- investigar un lugar o estimulo ligado a recuerdos

## Task

Describe como ejecutar la intencion sin mezclar toda la logica en una sola clase gigante.

Debe servir para:

- arbitrar prioridades
- cortar actividades no urgentes cuando aparece peligro
- recuperarse de fallos de navegacion o contexto

## Goal

Implementa el comportamiento fisico concreto.

La seguridad siempre tiene prioridad sobre actividades recreativas.

## Regla narrativa

No toda conducta tranquila significa que Polen "esta bien".

En esta narrativa:

- observar flores o abejas puede ser curiosidad
- tambien puede ser autorregulacion
- tambien puede ser un disparador de memoria

La IA debe dejar espacio para esas lecturas, pero sin perder funcionalidad basica.

## Comportamientos visibles esperados

La fantasia jugable actual de Polen incluye:

- mantener distancia de desconocidos
- acercarse mas a jugadores de confianza
- buscar refugio cuando el entorno empeora
- usar `blink` cuando moverse falla o queda atrapada
- colocar luz propia por la noche
- cantar, dibujar, reflexionar y sintonizarse con source cuando se siente segura
- observar flores, abejas y spots tranquilos como parte de su identidad

## Alineacion con la narrativa nueva

La IA actual y futura debe poder convivir con estas verdades canonicas:

- Polen perdio recuerdos
- es curandera por formacion
- carga duelo por la guerra de Hive
- tiene relaciones importantes fuera del jugador
- terminara ocupando un rol legendario mas grande

Eso no obliga a meter exposicion directa en cada comportamiento.

Si obliga a que las decisiones visibles no contradigan su identidad.

## Estado actual de implementacion

La version jugable actual sigue concentrada en:

- encuentro
- confianza temprana
- refugio
- hobbies tranquilos
- autonomia inicial

Todavia no refleja por completo:

- recuperacion profunda de recuerdos
- referencias sistémicas al elenco completo
- reaccion narrativa avanzada a guerra, duelo o destino

## Dialogos y memoria

La separacion en `lang_base/` y `lang_parts/` ya esta conectada al runtime.

Hoy la IA y la interaccion ya pueden usar:

- dialogo base por capitulo
- dialogo ambiental por situacion
- eventos narrativos fijos
- fragmentos de memoria al descubrir entorno

Eso igual no significa que todo el contenido narrativo futuro ya exista.
Al agregar lineas nuevas, hay que revisar tanto el texto como el resolver que decide cuando usarlas.

## Ajuste reciente

La investigacion de intereses tranquilos ahora:

- recuerda por un rato el ultimo interes observado o fallido
- evita reengancharse enseguida al mismo spot
- abandona el intento si no logra resolver el trayecto en un tiempo razonable

Interpretacion correcta:

- si, mirar flores encaja con su fantasia
- no, quedarse bloqueada ahi indefinidamente no es el comportamiento deseado

## Regla de desarrollo

Cuando se agregue o ajuste comportamiento, validar siempre:

1. que la conducta se entienda a simple vista
2. que tenga sentido con afinidad, seguridad y etapa narrativa
3. que no contradiga la amnesia ni la recuperacion gradual
4. que no reduzca a Polen a "mueble con dialogo"
5. que no rompa movimiento, refugio o bucles basicos
