# Localizacion De Dialogos

El flujo de dialogos de Polen ahora usa archivos fuente separados en vez de editar un unico archivo grande de lenguaje.

## Direccion objetivo

Cuando existan, usar estos archivos como fuente de autoria:

```text
src/main/resources/assets/polen/lang_parts/<locale>/ambient.json
src/main/resources/assets/polen/lang_parts/<locale>/chapters.json
src/main/resources/assets/polen/lang_parts/<locale>/events.json
src/main/resources/assets/polen/lang_parts/<locale>/memories.json
```

Las traducciones no dialogadas viven en:

```text
src/main/resources/assets/polen/lang_base/<locale>.json
```

Los archivos finales que Minecraft usa en runtime siguen estando en:

```text
src/main/resources/assets/polen/lang/<locale>.json
```

## Estado actual

Este flujo ya quedo conectado al build:

- `lang_base/*.json` guarda claves no dialogadas
- `lang_parts/<locale>/*.json` guarda dialogos separados
- `mergePolenLang` fusiona todo hacia `lang/*.json`
- `processResources` depende de ese merge

Tambien quedo conectado el lado runtime:

- `chapters.json` se usa como base para interacciones directas cuando no hay un contexto mas fuerte
- `ambient.json` puede dispararse por presencia y comportamiento pasivo, no solo por triggers puntuales
- la interaccion puede preferir lineas contextuales si Polen esta descansando, observando, buscando refugio, acercandose o haciendo una actividad tranquila
- `memories.json` puede activarse al descubrir entorno, no solo por momentos ligados a items o eventos manuales

## Regla de idioma

Para canon y continuidad narrativa:

- primero asegurar version en espanol
- luego mantener version en ingles alineada

Ningun cambio importante de lore deberia existir solo en un idioma si ese doc o ese dialogo ya tiene espejo publico.

## Regla practica

Al tocar dialogos:

1. editar las partes separadas si ya existen para ese locale
2. mantener alineado el output final en `lang/*.json`
3. verificar que el codigo realmente consuma esas lineas
4. actualizar docs si cambian canon, recuerdos o relaciones

Si se agrega una familia nueva de dialogo o un archivo situacional nuevo, tambien hay que actualizar el resolver runtime. Las partes separadas no se usan solas: el codigo tiene que mapear estados jugables a esas claves.

No editar a mano `lang/*.json` salvo que estes reparando el output generado despues de cambiar las fuentes separadas.

## Continuidad que debe respetarse

Los dialogos ahora deben poder convivir con:

- Befsh
- Cosmic
- Luna
- Noia
- Noris
- Jeff
- Vanilla
- la amnesia de Polen
- la guerra en Hive
- su futuro rol de reina prometida

El jugador no debe recibir todo eso de golpe.
Debe entrar por fragmentos, afinidad, ambiente y eventos.

## Hooks runtime actuales

Hoy el juego consume dialogos asi:

- `chapters.json`: interaccion base segun capitulo
- `ambient.json`: frases contextuales pasivas y respuestas sensibles al estado actual
- `events.json`: secuencias narrativas fijas como refugio o revelacion del nombre
- `memories.json`: fragmentos de memoria desbloqueados por descubrimiento del mundo o acciones narrativas cercanas
