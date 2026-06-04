# Sistema De Progresion

## Resumen

La progresion esta dividida intencionalmente en dos ejes estructurales:

- progreso global de la historia
- progreso de relacion por jugador

Esa separacion no es opcional.

Narrativamente, esos dos ejes ya no solo sostienen confianza.
Tambien deben sostener:

- recuperacion de memoria
- duelo
- revelaciones graduales sobre Hive, la guerra y el elenco
- el arco largo de la reina prometida

## Progreso global

Debe guardar verdades compartidas como:

- capitulos
- flags de historia
- primeros recuerdos importantes
- hitos de colonia
- revelaciones de personajes
- revelaciones de legado o realeza

## Progreso por jugador

Debe guardar cercania personal como:

- cuanta confianza tiene Polen en ese jugador
- que tan abierta se siente con esa persona
- si ciertos recuerdos dolorosos pueden aparecer de forma segura cerca de ese jugador

## Regla de orquestacion

Las escenas, revelaciones y desbloqueos no deben salir de condiciones sueltas repartidas por cualquier parte del codigo.

Deben pasar por los managers de progresion y eventos de historia.

## Regla para nuevas features

1. Si algo cambia capitulos, memoria compartida o revelaciones globales, va al progreso global.
2. Si algo cambia confianza, intimidad o cercania con un jugador, va al progreso por jugador.
3. Si hace falta una escena, la orquestacion va en el manager de eventos.
4. Si cambia comportamiento visible, documentar tambien el impacto en IA.
5. Si revela canon nuevo, actualizar version en espanol e ingles.
