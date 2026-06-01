# Residencia de Polen

## Objetivo

La Residencia de Polen es una estructura narrativa única asociada a Polen.

No es una vivienda común de MineColonies.

No puede asignarse a ciudadanos normales.

Existe una sola por mundo.

Su función es servir como hogar narrativo de Polen y como marcador visible de su pertenencia a la colonia.

---

## Polen Core

La residencia se identifica mediante un bloque especial:

```text
Polen Core
```

El `Polen Core` representa la presencia simbólica de Polen dentro de la colonia.

Solo puede existir uno por mundo.

---

## Validación de Residencia

Una residencia válida requiere:

- `Polen Core`
- cama
- puerta
- techo
- espacio interior habitable

Cuando estas condiciones se cumplen:

```text
PolenShelterDetector
↓
Polen Residence Established
↓
POLEN_RESIDENCE_ESTABLISHED
```

La validación no debe dispararse demasiado temprano. Narrativamente, la residencia solo tiene sentido cuando Polen ya dejó de sentirse una visitante temporal.

---

## Relación con MineColonies

La residencia puede construirse con apoyo de MineColonies, pero no forma parte del sistema estándar de viviendas.

Reglas:

- no es una `Residence` común
- no puede ser reclamada por colonos
- no entra en la lógica normal de alojamiento
- su propiedad pertenece exclusivamente al sistema narrativo de Polen

La estructura existe para reforzar historia y pertenencia, no para sumar población.

---

## Persistencia

World Story Data sugerido:

```java
BlockPos polenResidencePos;

boolean polenResidenceEstablished;
```

Opcionalmente puede guardarse también un nivel narrativo de la residencia:

```java
int polenResidenceTier;
```

---

## Evolución Narrativa

La residencia debe crecer con la historia.

### Capítulo 1

`Refugio Prestado`

Un espacio básico y seguro. Todavía no es su hogar definitivo.

### Capítulo 4

`Casa de Polen I`

El refugio deja de ser provisional y empieza a integrarse a la colonia.

### Capítulo 7

`Residencia de Polen`

Hito principal de pertenencia. Polen acepta que ese lugar también es suyo.

### Capítulo 12

`Archivo Apis`

La residencia incorpora memoria, registros y objetos del pasado. Ya no solo protege a Polen; también preserva historia.

### Capítulo 15

`Pabellón del Nuevo Comienzo`

Estado final. La residencia expresa futuro compartido, no restauración nostálgica.

Todas las versiones conservan el mismo `Polen Core`.

---

## Reglas de Coherencia

- No debe existir una residencia completa antes del Capítulo 7.
- Su evolución visual debe acompañar el crecimiento emocional de Polen.
- El estado final no debe depender de una coronación ni de un título monárquico.
- Si el canon futuro usa imaginería real, debe aparecer como capa histórica, no como reemplazo del sentido de hogar.
