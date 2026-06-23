# Claro inicial de Polen

## Proposito

Este documento fija tres cosas del comienzo jugable:

- como es el refugio inicial de Polen
- donde debe aparecer ese refugio
- que item sirve para localizarla durante el prologo

No describe aun la implementacion final del generador.

Describe la verdad canonica y la especificacion que el codigo futuro debe respetar.

## Regla principal

Polen no empieza sin techo.

El jugador no la "salva" de la nada.

La encuentra ya sobreviviendo sola en un claro apartado, dentro de un refugio pequeño, improvisado y claramente temporal.

Eso significa:

- ya existe supervivencia
- todavia no existe pertenencia
- todavia no existe hogar compartido

## El lugar

### Nombre de trabajo

`Claro de Polen`

### Bioma y contexto

Debe aparecer en `Overworld` y sentirse ligado a cerezos o praderas floridas, abejas y silencio.

La mejor lectura es:

- `cherry_grove`
- `meadow`
- claro abierto con flores y sensacion de refugio natural
- flores visibles en el entorno
- una colmena o nido de abejas cercano

### Reglas de ubicacion

El claro debe cumplir estas reglas:

- entre `300` y `900` bloques del spawn mundial
- no dentro de aldeas
- no dentro de estructuras grandes
- no bajo tierra
- no en biomas hostiles como desierto, nieve extrema, pantano oscuro o picos
- con cielo abierto y radio visual suficiente para reconocerlo como un claro real

### Senales obligatorias del lugar

- cerezos rodeando el area
- flores en el suelo
- una colmena cercana a `8-16` bloques
- una fogata visible
- el refugio visible desde el borde del claro

## El refugio inicial

### Fantasia correcta

No es una casa.

No es una base.

No es una residencia.

Es un refugio de alguien que lleva tiempo aguantando sola con lo minimo.

### Tipo de estructura

Un `lean-to` o campamento semitechado, abierto por delante.

### Huella y forma

Especificacion base:

- huella aproximada de `5x4` bloques
- frente abierto hacia el claro
- fondo apoyado contra postes o troncos
- techo bajo e irregular
- solo una o dos paredes parciales
- sin puerta
- sin ventanas

### Paleta de bloques

El refugio debe usar materiales simples y cercanos al claro:

- `stripped_cherry_log`
- `cherry_planks`
- `cherry_slab`
- `cherry_stairs`
- `white_wool` o `white_carpet` como remiendo ligero
- `campfire`
- `white_bed`
- `barrel`

### Elementos obligatorios

- una cama simple ya usada
- una fogata a `2-3` bloques del frente
- un barril o contenedor pequeno
- un pequeño suelo pisado con `coarse_dirt` o `path`
- al menos una senal de vida tranquila, como flores recogidas o bocetos

### Elementos prohibidos

No debe incluir:

- puerta formal
- vidrio
- horno bien montado
- cofre grande
- decoracion rica
- simetria limpia
- sensacion de casa terminada

### Regla visual

La estructura debe leerse asi:

"alguien puede pasar la noche aqui"

pero no asi:

"alguien ya construyo su hogar definitivo aqui"

## Relacion con la progresion

Este refugio inicial no equivale a `PLAYER_HAS_SHELTER`.

Distincion obligatoria:

- el refugio inicial explica como Polen sobrevive antes del jugador
- `PLAYER_HAS_SHELTER` marca el momento en que un refugio mejor empieza a sentirse como nuevo comienzo compartido

El prologo necesita precariedad.

`FOUNDATION` necesita mejora y reconocimiento.

## Item localizador

### Nombre canonico

`hiveheart_charm`

### Rol correcto

No es un item de combate.

No es un premio tardio.

No es un charm de afinidad comun.

Es un localizador de prologo para encontrar a la chica del claro.

### Familia recomendada

`focus item`

Razon:

- orienta al jugador
- conecta con resonancia suave
- no pertenece todavia a colonia o residencia

### Adquisicion correcta

Debe llegar al jugador como recompensa de la primera mision o encargo del prologo, junto con la pista inicial sobre alguien perdida cerca de ese claro.

La forma correcta actual de esa entrega es esta:

- el jugador encuentra a `SoaMarjorie` y `Lucy` conversando dentro de una aldea
- idealmente en un interior con lectura de taberna, posada o punto de reunion tranquilo
- Lucy es quien menciona a la joven misteriosa vista en el claro
- Lucy entrega el `hiveheart_charm` como croquis marcado a mano
- Soa refuerza el tono de cautela, pero no reemplaza a Lucy como disparador de la pista

No debe depender de crafting para el primer encuentro.

Regla fuerte:

- no usar `royal_pollen`
- no pedir materiales que solo tienen sentido despues de conocer a Polen

### Comportamiento

Antes del primer encuentro:

- apunta al `prologueClearingCenter`
- pulsa o resuena mas fuerte al acercarse
- no muestra coordenadas exactas
- no teletransporta
- no invoca a Polen

Comportamiento sugerido por distancia:

- mas de `128` bloques: senal debil
- entre `128` y `32` bloques: senal estable
- menos de `32` bloques: senal calida y corta para empujar exploracion visual

Despues del primer encuentro:

- deja de actuar como localizador principal del claro
- puede quedar como recuerdo
- puede reciclarse luego como componente narrativo o upgrade, pero no debe seguir resolviendo el encuentro por si solo

### Restricciones

`hiveheart_charm` no debe:

- reemplazar la exploracion
- anular la lectura del entorno
- trivializar el primer encuentro
- funcionar como GPS permanente de Polen en todo momento

## Decision de contenido

`hiveheart_charm` queda definido como item de prologo no crafteable en su primera version.

Si mas adelante existe una version crafteable, debe ser otra etapa del objeto o una variacion mejorada, no la herramienta base del primer encuentro.
