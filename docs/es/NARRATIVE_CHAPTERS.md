# Hives And Colonies: Polen

## Estructura Narrativa por Capítulos

La campaña principal se divide en 16 capítulos organizados en 4 actos.

La historia avanza mediante tres progresiones paralelas:

1. La relación entre el jugador y Polen.
2. El crecimiento de la colonia.
3. El descubrimiento del legado de la civilización antigua.

La coherencia del proyecto depende de que esas tres líneas siempre avancen juntas. Ningún sistema debe crecer por encima del arco emocional que lo sostiene.

## Principios de Coherencia

- La historia es personal antes que épica.
- La colonia importa porque protege personas, no porque produce más.
- Polen nunca debe sentirse como una dispensadora de quests.
- Las abejas, la magia y la colonia deben revelar el pasado de forma gradual, no mediante exposición directa.
- El objetivo final no es restaurar exactamente el mundo antiguo, sino decidir qué merece continuar.
- El final no exige una coronación. Exige una elección consciente sobre el legado hallado.

## Regla Crítica del Prólogo

Antes del cierre del Capítulo 0, el jugador no puede conocer el nombre de Polen.

Durante ese período:

- El NPC debe mostrarse como `???`.
- Los textos visibles deben usar `la desconocida`, `la joven del claro` o equivalentes.
- Ningún quest, tooltip, estructura o diálogo visible puede mostrar `Polen`.
- Los identificadores internos pueden existir, pero no deben filtrarse a la interfaz.

La primera vez que el nombre aparece en pantalla debe ser una escena deliberada de confianza:

`"Mi nombre es Polen."`

## Matriz de Revelaciones

Orden recomendado de revelación:

1. Existe una chica viviendo sola en un claro.
2. El jugador se gana su confianza básica.
3. Polen revela su nombre.
4. La colonia empieza a convertirse en un lugar seguro para ambos.
5. Polen reconoce símbolos y técnicas que no debería conocer.
6. Abejas y magia comparten un vínculo antiguo.
7. Existió una civilización avanzada organizada alrededor de ese conocimiento.
8. La caída de esa civilización no fue natural.
9. Polen posee una conexión directa con ese legado.
10. El jugador y Polen deciden qué parte de ese legado merece sobrevivir.

---

# ACTO I - LA DESCONOCIDA

Tema central:

¿Quién es Polen?

La historia es íntima, pequeña y concreta. La colonia todavía no es importante por sí misma; solo importa como promesa de refugio.

## Capítulo 0 - Primer Encuentro

### Estado Narrativo

Polen es una completa desconocida.

No confía en el jugador.

No habla sobre su pasado.

### Objetivo

Establecer el primer vínculo sin forzar intimidad.

### Sistemas

- Exploración inicial
- Primer diálogo
- Afinidad básica

### Progresión

- Rumores sobre una joven en un claro.
- Descubrimiento del campamento.
- Primeras ayudas pequeñas.
- Conversaciones breves y cautelosas.

### Revelación

Polen revela su nombre.

### Resultado

- `NAME_REVEALED`
- `CHAPTER_0_COMPLETE`

## Capítulo 1 - Un Lugar Seguro

### Estado Narrativo

Polen ha decidido permanecer cerca.

Todavía no confía completamente.

### Objetivo

Demostrar que el jugador puede ofrecer seguridad sin invadir.

### Sistemas

- Construcción básica
- Refugio
- Supervivencia

### Progresión

- Cama
- Refugio
- Iluminación
- Seguridad
- Primer sendero entre el claro y la base

### Revelación

Polen admite que se siente más tranquila cerca del jugador.

### Resultado

- `PLAYER_HAS_SHELTER`

## Capítulo 2 - Los Primeros Colonos

### Estado Narrativo

Polen comienza a observar al jugador con curiosidad.

La idea de comunidad deja de parecerle imposible.

### Objetivo

Transformar un refugio en el inicio de una comunidad.

### Sistemas

- MineColonies

### Progresión

- Build Tool
- Town Hall
- Builder Hut
- Primer colono

### Revelación

Polen observa que el jugador construye pensando en el futuro, no solo en la urgencia del presente.

### Resultado

- `COLONY_FOUNDED`

## Capítulo 3 - El Source

### Estado Narrativo

La confianza comienza a crecer.

Polen ya no solo recibe ayuda; empieza a participar en lo que ocurre.

### Objetivo

Introducir Ars Nouveau como herramienta de cuidado, curiosidad y memoria.

### Sistemas

- Source
- Magia básica

### Progresión

- Primer Source
- Primeros experimentos
- Primer uso práctico para la colonia

### Revelación

Polen reconoce símbolos familiares.

Por primera vez parece recordar algo real, no solo sentir nostalgia.

### Resultado

- `POLEN_FIRST_MEMORY`

---

# ACTO II - LOS CIMIENTOS

Tema central:

Construir algo que merezca durar.

La historia deja de ser solo refugio y empieza a convertirse en proyecto compartido.

## Capítulo 4 - Ecos del Pasado

### Estado Narrativo

Polen comienza a recordar fragmentos aislados.

La colonia ya no es provisional.

### Objetivo

Expandir la colonia mientras aparecen los primeros patrones del pasado.

### Sistemas

- MineColonies
- Infraestructura temprana

### Progresión

- Builder funcional
- Primera expansión
- Caminos y zonas comunes

### Revelación

Los símbolos encontrados no son casuales. Se repiten en estructuras, herramientas y recuerdos.

## Capítulo 5 - Industria Apícola

### Estado Narrativo

Polen muestra un interés inusual por ciertas abejas.

Su vínculo con ellas ya no puede leerse como una simple afinidad personal.

### Objetivo

Introducir Productive Bees como herencia viva y no solo como producción.

### Sistemas

- Apiarios
- Producción de miel
- Primeras especies especiales

### Progresión

- Primer apiario
- Derivados básicos
- Crianza inicial

### Revelación

Las abejas parecen haber tenido una importancia histórica, técnica y cultural.

## Capítulo 6 - Voces del Pasado

### Estado Narrativo

Los recuerdos de Polen se vuelven más frecuentes.

La curiosidad del jugador pasa de intuición a investigación.

### Objetivo

Conectar magia y abejas como partes de una misma tradición perdida.

### Sistemas

- Productive Bees
- Ars Nouveau
- Primeros registros fragmentados

### Progresión

- Experimentos híbridos
- Símbolos compatibles
- Hallazgos en ruinas o documentos

### Revelación

Ambos sistemas estuvieron relacionados anteriormente dentro de una cultura más compleja.

## Capítulo 7 - Un Lugar Propio

### Estado Narrativo

Polen ya no es una visitante.

La colonia la reconoce como parte de su vida cotidiana.

### Objetivo

Establecer la Residencia de Polen como hito de pertenencia, no como simple vivienda.

### Sistemas

- MineColonies
- Polen Residence

### Progresión

- Diseño del espacio
- Construcción de la residencia
- Reconocimiento narrativo dentro de la colonia

### Revelación

Polen acepta que pertenece a ese lugar y deja de tratar su presencia como algo temporal.

### Resultado

- `POLEN_RESIDENCE_ESTABLISHED`

---

# ACTO III - LOS SECRETOS PERDIDOS

Tema central:

¿Qué ocurrió antes?

La colonia ya existe. Ahora el pasado empieza a exigir interpretación.

## Capítulo 8 - Caminos Lejanos

### Estado Narrativo

La búsqueda del pasado comienza activamente.

Polen ya no quiere solo recordar; quiere entender.

### Objetivo

Abrir el mundo mediante exploración dirigida.

### Sistemas

- Exploración
- Estructuras antiguas
- Biomas y rutas lejanas

### Progresión

- Viajes
- Ruinas
- Santuarios o archivos olvidados

### Revelación

Existió una civilización avanzada con presencia territorial amplia.

## Capítulo 9 - Buscando Respuestas

### Estado Narrativo

La investigación se vuelve más metódica.

El jugador y Polen ya trabajan como una sola línea narrativa.

### Objetivo

Recopilar información dispersa y empezar a reconstruir una cronología.

### Sistemas

- Documentos
- Fragmentos
- Reliquias

### Progresión

- Recuperar registros
- Comparar símbolos
- Ordenar hipótesis

### Revelación

La caída de aquella civilización no fue natural ni accidental.

## Capítulo 10 - Industria Arcana

### Estado Narrativo

La colonia alcanza una escala suficiente para sostener magia avanzada.

El progreso ya exige decisiones éticas y prácticas.

### Objetivo

Dominar sistemas avanzados sin perder el foco humano de la narrativa.

### Sistemas

- Ars Nouveau avanzado
- Integración con infraestructura de colonia

### Progresión

- Redes arcanas mayores
- Automatización útil
- Soluciones para sostener comunidad y exploración

### Revelación

La magia antigua dependía de recursos, conocimiento y equilibrio que ya no existen fácilmente.

## Capítulo 11 - El Consejo Perdido

### Estado Narrativo

El pasado deja de parecer un mito difuso y empieza a mostrar estructura política y social.

### Objetivo

Descubrir cómo se organizaba la civilización antigua y qué voces la guiaban.

### Sistemas

- Archivos
- Reliquias ceremoniales
- Espacios históricos

### Progresión

- Nombres relevantes
- Cargos o roles
- Registro de decisiones antiguas

### Revelación

Existía una estructura social compleja.

Polen reconoce nombres importantes antes de poder explicar por qué.

---

# ACTO IV - EL DESPERTAR

Tema central:

¿Qué hacer con el legado encontrado?

El conflicto final no es vencer a alguien, sino decidir qué reconstruir y qué dejar atrás.

## Capítulo 12 - El Reino Perdido

### Estado Narrativo

El jugador y Polen tienen ya suficiente información para reconstruir una historia amplia.

### Objetivo

Ordenar el pasado completo y separar memoria, mito y propaganda.

### Sistemas

- Investigación final
- Integración de registros
- Espacios de preservación en la colonia

### Progresión

- Completar la cronología
- Unificar hallazgos
- Exponer la verdad dentro de la colonia

### Revelación

La civilización antigua fue real.

Sus logros superaban lo imaginado, pero también cargaban contradicciones internas.

## Capítulo 13 - La Heredera

### Estado Narrativo

Polen enfrenta finalmente su pasado.

Ya no puede tratar sus recuerdos como simples fragmentos aislados.

### Objetivo

Aceptar quién es realmente sin reducirla a un título.

### Sistemas

- Afinidad alta
- Escenas de memoria
- Hallazgos de linaje o pertenencia

### Progresión

- Recuerdos personales
- Confirmación de vínculo
- Conversación de aceptación

### Revelación

Polen posee una conexión directa con el legado descubierto.

La naturaleza exacta de dicha conexión depende del canon definitivo: heredera, descendiente, última guardiana o figura equivalente.

### Resultado

- `POLEN_LEGACY_ACCEPTED`

## Capítulo 14 - La Elección

### Estado Narrativo

La verdad ya fue encontrada.

Ahora toca decidir cómo vivir con ella.

### Objetivo

Definir qué hacer con el conocimiento recuperado.

### Sistemas

- MineColonies
- Productive Bees
- Ars Nouveau
- Decisiones narrativas finales

### Progresión

- Debates sobre uso del legado
- Restauración selectiva
- Renuncia a repetir errores antiguos

### Tema

- Legado
- Responsabilidad
- Futuro

### Revelación

Reconstruir no significa repetir.

## Capítulo 15 - Un Nuevo Comienzo

### Estado Narrativo

Polen ya no busca respuestas.

Ahora busca construir un futuro.

### Objetivo

Completar la visión de la colonia como primer paso hacia una nueva era.

### Sistemas

- MineColonies
- Productive Bees
- Ars Nouveau

### Progresión

- Integración plena de sistemas
- Espacios finales de comunidad
- Cierre del arco compartido

### Resultado

La colonia se convierte en el primer paso hacia una nueva etapa histórica.

La historia termina donde comienza el futuro.

### Tema Final

No se trata de recuperar el pasado.

Se trata de construir algo mejor.

### Resultado de Campaña

- `STORY_MAIN_COMPLETE`

---

## Guías de Implementación

### Polen

- Antes del final del Capítulo 0, usar siempre `???`.
- Su tono debe ser humano, reservado y observador.
- No debe hablar como noble, reina o figura ceremonial durante los primeros actos.
- Sus recuerdos deben aparecer como ecos concretos, no como amnesia total conveniente.

### Colonia

- Debe crecer al ritmo de la relación.
- Cada expansión importante necesita una función emocional además de mecánica.
- La Residencia de Polen debe sentirse ganada, no desbloqueada porque sí.

### Pasado Antiguo

- Debe insinuarse antes de explicarse.
- No todo debe resolverse con una única revelación.
- El pasado debe contener logros admirables y errores reales.

### Cierre

- El final debe dejar claro que Polen no existe para restaurar una jerarquía por nostalgia.
- Si el canon futuro incorpora realeza de forma explícita, debe funcionar como consecuencia de su arco, no como destino automático.
- El mensaje final del proyecto es comunidad, responsabilidad y futuro compartido.
