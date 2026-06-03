# Polen Entity

Polen es una entidad autónoma diseñada alrededor de estados internos y comportamiento emergente.

## Arquitectura

PolenEntity contiene:

- Afinidad con jugadores
- Estado de IA
- Estado emocional
- Memoria
- Residencia
- Actividades autónomas
- Sistema de observación

La lógica principal no vive en la entidad directamente.

La entidad delega comportamiento a:

- PolenAiFacade
- Goals
- Tasks
- Memory systems

## Tick

Cada tick:

```java
PolenAiFacade.tickServer(this);
PolenAiFacade.tickClient(this);
```

La entidad actúa como punto central de datos y persistencia.

## Responsabilidades

### PolenEntity

- Persistencia
- Sincronización
- Interacción con jugadores
- Navegación
- Estado general

### PolenAiFacade

- Actualización IA
- Evaluación de necesidades
- Selección de intenciones
- Coordinación de tareas
