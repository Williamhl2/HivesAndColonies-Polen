# Resumen Tecnico Del Proyecto

## Que es este proyecto

`Hives And Colonies: Polen` es un mod NeoForge para Minecraft 1.21.1 construido alrededor de un personaje narrativo llamado Polen.

El repositorio y la implementacion actual siguen siendo Polen-centricos, pero el alcance narrativo ya se esta abriendo a un elenco mas amplio. Incluso se esta considerando un posible renombre publico como `Hives & Colonies: Characters`.

Hoy el proyecto combina cinco capas:

1. Polen como entidad companera dentro del mundo.
2. Progresion de historia y capitulos.
3. Relacion y afinidad por jugador.
4. IA, seguridad y magia sutil orientadas al personaje.
5. Contenido de items y bloques para narrativa, colonia y futuros accesorios.

## Base narrativa

El canon que debe preservarse en docs e implementacion es:

- Hive es un planeta multi-especie sin humanos nativos.
- Los humanos invaden Hive mas adelante.
- Polen sobrevive a esa historia, pero pierde sus recuerdos.
- Esos recuerdos vuelven gradualmente en el nuevo mundo donde empieza el juego.
- Polen fue entrenada en magia curativa.
- Befsh, Cosmic, Luna, Noia, Noris, Jeff y Vanilla forman parte real de su continuidad.
- Su arco largo incluye convertirse en la "reina prometida" o reina legendaria del nuevo mundo.

## Regla importante

- el inicio jugable debe sentirse intimo y cercano
- el canon completo igual debe quedar documentado
- la documentacion no debe negar verdades futuras solo porque el jugador aun no las conoce

## Meta tecnica

El codebase debe permitir que Polen y futuros personajes relacionados crezcan sin convertir el mod en:

- una coleccion de triggers aislados
- una clase de entidad gigante
- una quest script disfrazada de gameplay

Eso implica:

- datos globales para progreso compartido
- datos por jugador para confianza y afinidad
- IA dividida por dominios pequenos
- dialogo que escale mas alla de un solo archivo monolitico
- animacion cliente separada de la IA del servidor
- familias de contenido que puedan crecer con el tiempo

## Estado actual

La version jugable actual sigue mucho mas cerca de:

- primeros encuentros
- construccion de confianza
- refugio y rutina
- comportamiento autonomo temprano
- primeras pistas de recuperacion de memoria

Problema conocido:

- Polen puede quedar atrapada mirando flores y dejar de moverse como deberia
