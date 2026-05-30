# Hives & Colonies - Especificación Narrativa por Capítulos

## Objetivo del documento

Este documento expande la historia principal de `Hives & Colonies` en una estructura apta para implementación.

Cada capítulo incluye:

- Propósito narrativo.
- Estado emocional de Polen.
- Cadena de quests.
- Progresión de diálogo.
- Hitos de afinidad.
- Revelaciones de historia.
- Reglas de implementación para no romper la narrativa.

## Principios no negociables

- La historia es personal antes que épica.
- El reino importa porque Polen importa.
- Polen no debe sentirse como una dispensadora de quests.
- Polen siempre debe sonar humana, vulnerable y concreta.
- La progresión emocional debe acompañar la progresión mecánica.
- El jugador ayuda a reconstruir una comunidad, no solo una fábrica.

## Regla crítica del prólogo

Antes del cierre del Capítulo 0, el jugador nunca puede conocer el nombre de Polen.

Durante ese período:

- El nombre visible del NPC debe ser `???`.
- Cualquier texto de quest debe decir `la chica del claro`, `la joven del cerezo`, `la desconocida` o equivalente.
- Ningún ítem, avance, estructura, subtítulo, tooltip, libro, voice line o nombre de archivo mostrado al jugador puede usar `Polen`.
- Si el sistema requiere identificadores internos, deben permanecer internos y no ser visibles en UI.

La primera vez que el nombre aparece en pantalla debe ser parte de una escena o diálogo intencional de confianza:

`"Mi nombre es Polen."`

## Curva emocional general

### Inicio

Una chica desconocida vive sola en un claro. Está agotada, nerviosa y evita cualquier pregunta personal.

### Mitad

Polen se convierte en una amiga cercana. Empieza a ayudar a otros, propone ideas y se involucra en la colonia.

### Final

Polen acepta su herencia, comprende el peso de la ausencia de la Reina y elige liderar sin dejar de ser ella misma.

## Afinidad global

La afinidad no debe sentirse como una barra de romance ni como un medidor abstracto. Debe representar confianza, cercanía y responsabilidad compartida.

### Rangos sugeridos

- `0-9`: Desconfianza cautelosa.
- `10-24`: Reconocimiento.
- `25-39`: Confianza inicial.
- `40-54`: Amistad estable.
- `55-69`: Confianza profunda.
- `70-84`: Lealtad y propósito compartido.
- `85-100`: Vínculo fundacional para la coronación.

### Formas de subir afinidad

- Completar encargos personales de Polen.
- Mejorar refugios, caminos y espacios comunitarios.
- Proteger abejas y restaurar naturaleza.
- Recuperar registros del Reino Apícola.
- Resolver problemas de la colonia que afectan a otras personas.

### Formas de expresar afinidad

- Nuevas líneas de diálogo.
- Polen aparece con más frecuencia en la colonia.
- Polen deja de retroceder o disculparse tanto.
- Polen hace preguntas al jugador.
- Polen propone tareas por iniciativa propia.

## Matriz de revelaciones

### Lo que el jugador no sabe al inicio

- El nombre de Polen.
- Su relación con la realeza.
- La existencia formal del Reino Apícola.
- La identidad de la Reina.
- La razón de su exilio.
- La magnitud de lo perdido.

### Orden recomendado de revelación

1. Existe una chica sola en un claro.
2. La chica confía lo suficiente para decir su nombre.
3. Tiene una conexión inusual con las abejas.
4. Conoce símbolos, costumbres y técnicas que no debería conocer una aldeana cualquiera.
5. Existió un Reino Apícola organizado.
6. La Reina desapareció.
7. Polen es hija de la Reina.
8. Polen es la heredera legítima.
9. Polen decide aceptar la sucesión.

## Capítulo 0 - Prólogo

## La Chica del Claro

### Propósito

Introducir misterio, ternura y fragilidad.

El jugador no debe sentir que encontró a una autoridad, sino a una persona aislada que necesita tiempo para confiar.

### Estado emocional de Polen

- Nerviosa.
- Cansada.
- Educada pero evasiva.
- A la defensiva sin agresividad.
- Muy sola.

### Condiciones de entrada

- Mundo nuevo.
- Primer contacto con el diario o nota inicial.

### Cadena de quests

#### Q0.1 - Rumores entre los Cerezos

**Resumen:** El jugador recibe una nota con rumores sobre una joven que vive en una arboleda de cerezos.

**Objetivos sugeridos:**

- Leer la nota inicial.
- Localizar un bioma o zona con cerezos.
- Seguir pistas vagas de viajeros.

**Función narrativa:**

- Sembrar curiosidad.
- Evitar cualquier mención al Reino Apícola.
- Presentar a la joven como un rumor, no como una heroína.

**Recompensa narrativa:**

- Desbloquea la investigación del claro.

#### Q0.2 - Flores y Silencio

**Resumen:** El jugador reúne materiales del entorno para entender por qué alguien viviría allí.

**Objetivos sugeridos:**

- Recolectar flores de cerezo.
- Encontrar una colmena silvestre.
- Observar señales de presencia humana discreta.

**Función narrativa:**

- Vincular el espacio con belleza, calma y melancolía.
- Preparar el contraste entre naturaleza viva y vida humana precaria.

#### Q0.3 - El Claro Oculto

**Resumen:** El jugador encuentra el campamento.

**Elementos ambientales obligatorios:**

- Fogata.
- Cama.
- Flores dispersas.
- Colmena cercana.
- Sensación de refugio temporal, no de hogar estable.

**Función narrativa:**

- Mostrar que la joven sobrevive, no que vive plenamente.

#### Q0.4 - La Desconocida

**Resumen:** Primer encuentro directo con `???`.

**Objetivos sugeridos:**

- Hablar con `???`.
- Retirarse y volver luego, respetando su espacio.
- Entregar una pequeña ayuda simple: comida, flores, carbón o lana.

**Función narrativa:**

- Enseñar que el vínculo no se compra; se cuida.
- Marcar que Polen aprecia la consideración más que el valor material.

#### Q0.5 - Un Nombre Compartido

**Resumen:** Tras varias ayudas pequeñas, `???` revela su nombre.

**Condición emocional:**

- El jugador fue constante, no invasivo.

**Momento clave:**

- Polen mira al jugador, duda y decide presentarse.

**Línea ancla:**

`"Has sido amable conmigo. No quiero seguir escondiéndome de ti. Mi nombre es Polen."`

**Función narrativa:**

- Cerrar el prólogo.
- Convertir el misterio en relación.

### Progresión de diálogo

#### Primer contacto

- `"Oh..."`
- `"No esperaba visitas."`
- `"Lo siento. No quiero hablar de mí."`
- `"¿Podrías dejarme sola un rato?"`

#### Tras la primera ayuda

- `"No tenías que hacer eso."`
- `"Gracias... de verdad."`
- `"Hace tiempo que nadie se detenía aquí."`

#### Antes de la revelación

- `"No eres como los demás viajeros."`
- `"Pensé que si alguien me encontraba, solo haría preguntas."`
- `"Contigo... el silencio no se siente tan pesado."`

#### Revelación del nombre

- `"Mi nombre es Polen."`
- `"No estoy lista para contar más. Pero quería que lo supieras."`

### Hitos de afinidad

- `Afinidad 5`: Polen deja de pedir al jugador que se vaya de inmediato.
- `Afinidad 10`: Acepta un regalo práctico.
- `Afinidad 15`: Dice su nombre y termina el prólogo.

### Revelaciones de historia

- Hay una chica viviendo sola.
- Tiene una relación evidente con las abejas y el claro.
- Se oculta por razones importantes, pero todavía no las comparte.

### Reglas de implementación

- No usar retratos con nombre antes de Q0.5.
- Si existe journal o questbook, todas las entradas previas deben usar `???`.
- No introducir todavía símbolos de corona, títulos reales ni lenguaje ceremonial.

## Capítulo 1 - Fundación

## Un Lugar Seguro

### Propósito

Transformar la relación de extraños en una primera amistad.

El jugador deja de ser visitante y empieza a convertirse en alguien confiable.

### Estado emocional de Polen

- Sigue siendo tímida.
- Se siente menos amenazada.
- Empieza a expresar preocupación por otros, no solo por sí misma.

### Tesis del capítulo

Antes de reconstruir un reino, alguien necesita un lugar donde no tenga miedo.

### Cadena de quests

#### Q1.1 - Un Techo Mejor

**Resumen:** El jugador mejora el refugio del claro o construye un pequeño hogar seguro.

**Objetivos sugeridos:**

- Construir paredes y techo.
- Añadir almacenamiento.
- Asegurar luz y calor.

**Función narrativa:**

- El gesto dice: `puedes dejar de sobrevivir por un momento`.

#### Q1.2 - Cosas Pequeñas

**Resumen:** Polen pide ayuda con necesidades cotidianas.

**Objetivos sugeridos:**

- Conseguir comida sencilla.
- Llevar mantas o lana.
- Preparar un espacio ordenado.

**Función narrativa:**

- Humanizarla.
- Mostrar hábitos, torpeza y gratitud.

#### Q1.3 - Un Camino de Regreso

**Resumen:** El jugador conecta el claro con la futura colonia.

**Objetivos sugeridos:**

- Marcar un sendero.
- Colocar antorchas o faroles.
- Crear un trayecto seguro.

**Función narrativa:**

- El vínculo físico refleja el vínculo emocional.

#### Q1.4 - Primera Visita

**Resumen:** Polen acepta visitar la colonia del jugador.

**Objetivos sugeridos:**

- Preparar un pequeño jardín o banco.
- Mostrarle el asentamiento.
- Interactuar con un espacio comunitario simple.

**Función narrativa:**

- Primer momento en el que Polen imagina un futuro compartido.

#### Q1.5 - Un Lugar Seguro

**Resumen:** Polen admite que se siente más tranquila cerca del jugador.

**Línea ancla:**

`"No pensé que volvería a sentirme segura en un lugar construido por alguien más."`

**Función narrativa:**

- Cierre emocional del capítulo.

### Momentos de personaje

- Polen intenta ayudar y derrama algo o se equivoca; se avergüenza y luego se ríe un poco.
- Se sorprende de que el jugador prepare espacio para ella sin hacer preguntas.
- Empieza a dejar pequeños detalles personales en la colonia: flores, miel, notas cortas.

### Progresión de diálogo

- `"No estoy acostumbrada a que alguien piense en estas cosas por mí."`
- `"Este lugar... se siente distinto cuando estás aquí."`
- `"Puedo intentar ayudar, aunque no prometo no tropezarme."`
- `"Gracias por no presionarme."`

### Hitos de afinidad

- `Afinidad 20`: Polen empieza a aparecer fuera del claro.
- `Afinidad 25`: Polen visita la colonia.
- `Afinidad 30`: Polen expresa confianza explícita.

### Revelaciones de historia

- Polen lleva tiempo huyendo o escondiéndose.
- Sabe cómo observar espacios seguros y rutas discretas.
- Todavía no habla de su familia ni de su origen.

## Capítulo 2 - La Fuente

## Descubrimientos Arcanos

### Propósito

Introducir `Ars Nouveau` como una vía de curiosidad y servicio, no de dominación.

### Estado emocional de Polen

- Más abierta.
- Fascinada por lo nuevo.
- Todavía insegura de su propio valor.

### Tesis del capítulo

La magia le interesa a Polen porque puede reparar, cultivar y proteger.

### Cadena de quests

#### Q2.1 - Trazos en el Polvo

**Resumen:** Polen encuentra símbolos o residuos arcanos y siente curiosidad.

**Objetivos sugeridos:**

- Recolectar materiales básicos de magia.
- Crear el primer aparato o tomo arcano.
- Compartir el hallazgo con Polen.

#### Q2.2 - Lo Invisible También Crece

**Resumen:** Primeros experimentos con hechizos de utilidad.

**Objetivos sugeridos:**

- Usar magia para iluminar.
- Usar magia para cultivar.
- Usar magia para reparar un pequeño problema de la colonia.

**Función narrativa:**

- Enfatizar utilidad cotidiana.

#### Q2.3 - Preguntas que Polen Sí Quiere Hacer

**Resumen:** Polen comienza a hacer preguntas concretas sobre cómo funciona la magia.

**Objetivos sugeridos:**

- Reunir componentes para investigación.
- Desbloquear una estación o ritual inicial.
- Presenciar una reacción de asombro de Polen.

#### Q2.4 - Un Hechizo para Ayudar

**Resumen:** La magia resuelve una necesidad comunitaria específica.

**Objetivos sugeridos:**

- Automatizar una tarea pequeña.
- Mejorar cosechas o iluminación.
- Salvar recursos o tiempo para otros.

#### Q2.5 - Maravilla Compartida

**Resumen:** Polen articula que la magia no le atrae por poder, sino por posibilidad.

**Línea ancla:**

`"Si algo así puede hacer la vida más amable para los demás... entonces vale la pena aprenderlo."`

### Momentos de personaje

- Polen toca una superficie encantada con cautela infantil.
- Se disculpa por hacer demasiadas preguntas y el jugador valida su curiosidad.
- Empieza a imaginar cómo combinar naturaleza, abejas y magia.

### Progresión de diálogo

- `"Nunca había visto algo así tan de cerca."`
- `"No da miedo cuando lo usas así."`
- `"¿Crees que esto podría servir para cuidar un jardín? ¿O una colmena?"`
- `"Quisiera entenderlo, no para tener más, sino para perder menos."`

### Hitos de afinidad

- `Afinidad 35`: Polen hace preguntas activamente.
- `Afinidad 40`: Polen empieza a proponer aplicaciones.
- `Afinidad 45`: Polen comparte ideas para ayudar a la comunidad.

### Revelaciones de historia

- Polen fue criada en un entorno donde conocimiento y deber estaban conectados.
- Tiene facilidad para comprender sistemas complejos, aunque dude de sí misma.
- Empieza a insinuarse que recibió educación poco común.

## Capítulo 3 - Industria Apícola

## Herencia Olvidada

### Propósito

Introducir `Productive Bees` y volver imposible ignorar la conexión de Polen con las abejas.

### Estado emocional de Polen

- Emocionada.
- Nostálgica sin saber cuánto revelar.
- Internamente dividida entre alegría y dolor.

### Tesis del capítulo

Las abejas no son solo producción. Son memoria, cultura y pertenencia.

### Cadena de quests

#### Q3.1 - El Zumbido Familiar

**Resumen:** Polen reacciona de forma íntima al trabajo con abejas.

**Objetivos sugeridos:**

- Conseguir primeras abejas manejables.
- Construir un apiario básico.
- Observar el comportamiento de distintas especies.

#### Q3.2 - Miel, Cera y Recuerdos

**Resumen:** Polen recuerda técnicas o costumbres sin explicar cómo las conoce.

**Objetivos sugeridos:**

- Procesar miel y cera.
- Crear productos útiles con derivados.
- Mejorar el cuidado de colmenas.

#### Q3.3 - Lenguaje de Colmena

**Resumen:** Polen identifica patrones que sorprenden al jugador.

**Objetivos sugeridos:**

- Criar o combinar especies.
- Resolver un problema ambiental para estabilizar producción.
- Documentar observaciones.

**Función narrativa:**

- Polen empieza a sonar como alguien formada dentro de una tradición perdida.

#### Q3.4 - Símbolos Antiguos

**Resumen:** Aparecen emblemas, sellos o diseños apícolas asociados a una cultura anterior.

**Objetivos sugeridos:**

- Recuperar un sello, placa o fragmento decorativo.
- Compararlo con conocimiento de Polen.
- Conservarlo en la colonia.

#### Q3.5 - Herencia Olvidada

**Resumen:** Polen admite que algunas cosas de las abejas le resultan demasiado familiares.

**Línea ancla:**

`"Hay cosas que recuerdo sin querer recordarlas. Como si una parte de mí nunca hubiera dejado ese lugar."`

### Momentos de personaje

- Polen se calma al lado de colmenas activas.
- Corrige una técnica de manejo casi por reflejo y luego se queda callada.
- Se emociona al ver una colonia sana y le cuesta explicar por qué.

### Progresión de diálogo

- `"No las molestes demasiado... se ponen nerviosas si sienten apuro."`
- `"La cera guarda más de lo que parece."`
- `"Perdón. Hablé como si ya hubiera hecho esto antes."`
- `"A veces siento que las abejas recuerdan algo que yo intento olvidar."`

### Hitos de afinidad

- `Afinidad 50`: Polen comparte conocimiento técnico espontáneo.
- `Afinidad 55`: Polen reconoce que su pasado está ligado a las abejas.
- `Afinidad 58`: Se habilitan quests con símbolos antiguos.

### Revelaciones de historia

- Polen posee conocimiento heredado, no improvisado.
- Existe una tradición apícola refinada desaparecida o fragmentada.
- El jugador empieza a sospechar que Polen viene de algo mucho mayor que un simple hogar rural.

## Capítulo 4 - Logística

## Construyendo un Reino

### Propósito

Hacer que la colonia deje de ser una suma de máquinas y pase a sentirse como un asentamiento organizado.

### Estado emocional de Polen

- Más presente.
- Más responsable.
- Todavía teme ocupar demasiado espacio.

### Tesis del capítulo

El liderazgo empieza en la capacidad de pensar en las necesidades de muchos.

### Cadena de quests

#### Q4.1 - Orden Entre Cajas

**Resumen:** El crecimiento del asentamiento exige organización.

**Objetivos sugeridos:**

- Mejorar almacenamiento.
- Clasificar recursos.
- Crear rutas o áreas de trabajo.

#### Q4.2 - Rutas Seguras

**Resumen:** Polen propone pensar en cómo se mueve la gente, no solo los ítems.

**Objetivos sugeridos:**

- Construir caminos.
- Conectar zonas clave.
- Hacer el lugar navegable de noche.

#### Q4.3 - Espacios para Todos

**Resumen:** El jugador crea áreas con propósito comunitario.

**Objetivos sugeridos:**

- Comedor, plaza, jardín o taller común.
- Señalética o decoración funcional.
- Espacios que reduzcan sensación de precariedad.

#### Q4.4 - Polen Toma la Iniciativa

**Resumen:** Por primera vez, Polen sugiere un plan estructural completo.

**Objetivos sugeridos:**

- Seguir uno de sus planes.
- Ajustar producción a necesidades humanas.
- Resolver un cuello de botella.

#### Q4.5 - Construyendo un Reino

**Resumen:** Polen comprende que un reino no empieza con una corona, sino con sistemas que cuidan a la gente.

**Línea ancla:**

`"Supongo que un lugar se vuelve hogar cuando empieza a estar pensado para más de una persona."`

### Momentos de personaje

- Polen observa los caminos y corrige distancias pensando en niños, ancianos o cargas pesadas.
- Pide disculpas antes de dar una instrucción, luego aprende a formularla con más seguridad.
- Empieza a quedarse en la colonia más tiempo que en el claro.

### Progresión de diálogo

- `"Si alguien tuviera que correr hasta aquí de noche, ¿podría hacerlo sin perderse?"`
- `"Quizá podríamos guardar esto más cerca del taller."`
- `"No intento mandarte. Solo... creo que podría funcionar mejor."`
- `"Estoy pensando como si este lugar fuera a durar."`

### Hitos de afinidad

- `Afinidad 60`: Polen da sugerencias de organización.
- `Afinidad 63`: Polen expresa preocupación por habitantes anónimos.
- `Afinidad 66`: Polen es reconocible como figura de referencia en la colonia.

### Revelaciones de historia

- Polen fue educada para pensar en comunidad y continuidad.
- Sus ideas de infraestructura reflejan entrenamiento previo, no mera intuición.
- El lenguaje de `reino` puede aparecer, pero todavía sin confirmar su vínculo directo con él.

## Capítulo 5 - Exploración

## Buscando Respuestas

### Propósito

Abrir el mundo y convertir la historia en investigación activa.

### Estado emocional de Polen

- Inquieta.
- Esperanzada y temerosa al mismo tiempo.
- Dispuesta a enfrentar recuerdos si eso trae respuestas.

### Tesis del capítulo

Buscar la verdad implica arriesgar la tranquilidad ganada.

### Cadena de quests

#### Q5.1 - Huellas Antiguas

**Resumen:** Se detectan restos o registros de una cultura apícola más antigua.

**Objetivos sugeridos:**

- Explorar ruinas, santuarios o archivos.
- Recolectar páginas, placas o mapas.
- Volver con pruebas tangibles.

#### Q5.2 - Registros Fragmentados

**Resumen:** Los documentos son incompletos y necesitan interpretación.

**Objetivos sugeridos:**

- Traducir símbolos.
- Restaurar páginas dañadas.
- Relacionar hallazgos con lo que Polen sabe.

#### Q5.3 - La Reina Ausente

**Resumen:** Por primera vez aparece una referencia clara a una Reina desaparecida.

**Objetivos sugeridos:**

- Encontrar una crónica oficial o semioficial.
- Identificar fechas o lugares clave.
- Entender que la desaparición no fue un rumor menor.

#### Q5.4 - Lo que Polen Calló

**Resumen:** Polen admite que conocía parte de esa historia, aunque no completa.

**Objetivos sugeridos:**

- Hablar con Polen tras un hallazgo fuerte.
- Entregarle un registro significativo.
- Permitir una escena de silencio o duda antes de que responda.

#### Q5.5 - Buscando Respuestas

**Resumen:** Polen y el jugador acuerdan buscar la verdad juntos.

**Línea ancla:**

`"No sé si estoy lista para saberlo todo. Pero sí sé que ya no quiero seguir huyendo de ello."`

### Momentos de personaje

- Polen reconoce un símbolo real y se queda sin palabras.
- Guarda una página contra el pecho antes de entregársela al jugador para estudiarla.
- Pide tiempo antes de hablar, y esa pausa debe respetarse.

### Progresión de diálogo

- `"He visto este diseño antes... o algo muy parecido."`
- `"Pensé que si no buscaba respuestas, tampoco tendría que enfrentar lo que significaban."`
- `"Mi madre..."`
- `"Perdón. Todavía me cuesta decirlo en voz alta."`

### Hitos de afinidad

- `Afinidad 68`: Polen comparte recuerdos incompletos.
- `Afinidad 72`: Se menciona por primera vez a la Reina.
- `Afinidad 75`: Polen acepta investigar activamente.

### Revelaciones de historia

- Existió una Reina reconocida por registros formales.
- La desaparición de la Reina tuvo impacto político y social.
- Polen conocía la existencia de esa historia desde antes, lo que confirma su cercanía personal.

## Capítulo 6 - Industria Arcana

## Poder y Responsabilidad

### Propósito

Escalar la magia y la infraestructura sin abandonar el tono humano.

### Estado emocional de Polen

- Más firme.
- Más serena bajo presión.
- Consciente de que sus decisiones empiezan a afectar a muchos.

### Tesis del capítulo

El poder solo tiene valor narrativo si obliga a elegir cómo cuidar mejor a otros.

### Cadena de quests

#### Q6.1 - Sistemas Mayores

**Resumen:** La colonia alcanza una escala que exige coordinación avanzada.

**Objetivos sugeridos:**

- Expandir automatización mágica.
- Sostener producción compleja.
- Integrar distintos subsistemas.

#### Q6.2 - Coste y Cuidado

**Resumen:** Polen cuestiona el precio humano o ecológico del progreso.

**Objetivos sugeridos:**

- Reequilibrar una producción excesiva.
- Restaurar un área afectada.
- Elegir soluciones eficientes pero no destructivas.

#### Q6.3 - La Voz que Guía

**Resumen:** Polen dirige una decisión importante de infraestructura o defensa pasiva.

**Objetivos sugeridos:**

- Seguir un plan propuesto por ella.
- Preparar contingencias.
- Resolver una crisis logística con serenidad.

#### Q6.4 - Lo que Significa Liderar

**Resumen:** Polen deja de pensar que liderar es mandar y entiende que es sostener.

**Objetivos sugeridos:**

- Completar una tarea que beneficie a muchos a largo plazo.
- Sacrificar comodidad inmediata por estabilidad.
- Reflejar responsabilidad en la propia base.

#### Q6.5 - Poder y Responsabilidad

**Resumen:** Polen asume que su voz ya tiene peso y decide usarla con cuidado.

**Línea ancla:**

`"Antes pensaba que lo peligroso era tener poder. Ahora creo que lo peligroso es no hacerse responsable de él."`

### Momentos de personaje

- Polen defiende una solución más lenta pero más justa.
- Corrige al jugador si una expansión pone en riesgo colmenas o ecosistemas.
- Acepta que otros la escuchan y deja de minimizarse inmediatamente.

### Progresión de diálogo

- `"Podemos hacerlo más rápido. No estoy segura de que debamos."`
- `"Si este lugar va a crecer, tiene que crecer sin devorarlo todo."`
- `"No quiero repetir errores que todavía ni entiendo por completo."`
- `"Si esperan una decisión de mí, entonces debo dar una."`

### Hitos de afinidad

- `Afinidad 78`: Polen asume responsabilidad pública.
- `Afinidad 80`: Polen toma decisiones difíciles.
- `Afinidad 82`: La colonia la reconoce como líder moral.

### Revelaciones de historia

- Polen fue criada bajo expectativas de deber, aunque haya intentado huir de ellas.
- El Reino Apícola probablemente cayó o se fracturó también por decisiones de poder, no solo por una tragedia aislada.

## Capítulo 7 - El Reino Perdido

## Verdades Reveladas

### Propósito

Revelar los secretos centrales de la historia con peso emocional, no como giro vacío.

### Estado emocional de Polen

- Vulnerable.
- Determinada.
- Dolida, pero ya no paralizada.

### Tesis del capítulo

La verdad no libera de inmediato; primero obliga a aceptar el dolor que siempre estuvo ahí.

### Cadena de quests

#### Q7.1 - Archivo de la Miel Dorada

**Resumen:** El jugador accede a un archivo, santuario o cámara con registros decisivos.

**Objetivos sugeridos:**

- Localizar la entrada.
- Superar pruebas de acceso o restauración.
- Recuperar documentos íntegros.

#### Q7.2 - La Historia del Reino Apícola

**Resumen:** Se reconstruye la historia política y cultural del reino.

**Objetivos sugeridos:**

- Organizar registros.
- Vincular artefactos, mapas y sellos.
- Exponer la cronología en la colonia.

#### Q7.3 - La Hija de la Reina

**Resumen:** Se confirma que Polen es la hija de la Reina desaparecida.

**Objetivos sugeridos:**

- Presentar prueba irrefutable.
- Hablar con Polen en un entorno íntimo.
- Evitar tono triunfal; la revelación debe doler.

#### Q7.4 - Lo que Sucedió con la Reina

**Resumen:** La verdad sobre la desaparición empieza a emerger, aunque puede seguir incompleta hasta el final.

**Opciones compatibles con la guía:**

- La Reina desapareció intentando proteger el reino.
- La Reina selló una amenaza o partió en una misión sin retorno.
- La Reina fue separada del reino por una crisis mayor aún no cerrada.

**Importante:**

- No trivializar la ausencia.
- No convertirla en simple excusa para coronar a Polen.

#### Q7.5 - Verdades Reveladas

**Resumen:** Polen deja de negar su herencia.

**Línea ancla:**

`"No soy mi madre. Pero tampoco puedo seguir fingiendo que no soy su hija."`

### Momentos de personaje

- Polen llora en privado o en una escena contenida, no melodramática.
- El jugador no la `convierte` en heredera; solo la acompaña mientras acepta lo que ya era verdad.
- Polen recuerda una enseñanza de la Reina asociada a abejas, paciencia o cuidado.

### Progresión de diálogo

- `"Siempre supe que si encontraba la verdad, también iba a encontrarme a mí misma dentro de ella."`
- `"Quise ser nadie porque ser alguien dolía demasiado."`
- `"Ella no desapareció solo de un reino. Desapareció de mi vida."`
- `"Tengo miedo. Pero ya no quiero vivir escondiéndome detrás de ese miedo."`

### Hitos de afinidad

- `Afinidad 84`: Polen comparte el núcleo de su pasado.
- `Afinidad 87`: Se confirma su linaje.
- `Afinidad 90`: Polen acepta públicamente quién es.

### Revelaciones de historia

- El Reino Apícola fue real, complejo y digno de ser reconstruido.
- La Reina desaparecida era la madre de Polen.
- Polen es la heredera legítima.
- La restauración del reino deja de ser metáfora y se vuelve objetivo explícito.

## Capítulo 8 - Coronación

## La Sucesora de la Reina

### Propósito

Concluir el arco personal de Polen con una aceptación elegida, no impuesta.

### Estado emocional de Polen

- Serena.
- Emocionada.
- Consciente del peso del cargo.
- Esperanzada.

### Tesis del capítulo

La coronación no premia poder acumulado; reconoce crecimiento, comunidad y responsabilidad asumida.

### Cadena de quests

#### Q8.1 - Preparativos del Alba

**Resumen:** La colonia se prepara para un momento fundacional.

**Objetivos sugeridos:**

- Reunir materiales ceremoniales.
- Embellecer la colonia o el espacio real.
- Integrar símbolos de abejas, flores, miel y comunidad.

#### Q8.2 - Testigos del Nuevo Reino

**Resumen:** La coronación debe sentirse respaldada por la comunidad reconstruida.

**Objetivos sugeridos:**

- Completar aportes de distintos sectores del asentamiento.
- Mostrar que la prosperidad ya beneficia a muchos.
- Activar presencia de aliados o representantes.

#### Q8.3 - La Última Duda

**Resumen:** Polen expresa temor final antes de aceptar.

**Objetivos sugeridos:**

- Hablar con Polen en privado.
- Recordarle hechos concretos del camino compartido.
- No convencerla con grandilocuencia, sino con verdad.

#### Q8.4 - La Corona y la Colmena

**Resumen:** Escena de coronación o investidura.

**Elementos emocionales clave:**

- Polen no se siente superior a nadie.
- Agradece a la comunidad y al jugador.
- Reconoce a la Reina ausente sin anular el presente.

#### Q8.5 - Un Reino que Vuelve a Florecer

**Resumen:** Cierre jugable y emocional.

**Resultados sugeridos:**

- Desbloqueo de estado final de Polen.
- Cambios visibles en la colonia.
- Últimas líneas que miran al futuro en lugar de solo al pasado.

**Línea ancla:**

`"Si voy a llevar esta corona, quiero hacerlo como aprendí aquí: cuidando lo pequeño para que lo grande pueda vivir."`

### Momentos de personaje

- Polen admite que todavía tiene miedo, pero ya no deja que el miedo decida por ella.
- Menciona el claro de los cerezos como el lugar donde dejó de estar sola.
- Su coronación debe sentirse íntima incluso si es solemne.

### Progresión de diálogo

- `"Durante mucho tiempo creí que aceptar esto significaba perderme."`
- `"Ahora creo que también puede significar encontrar el lugar al que pertenezco."`
- `"No reconstruimos un reino para recordar lo que se perdió. Lo reconstruimos para que vuelva a haber futuro."`
- `"Gracias por quedarte."`

### Hitos de afinidad

- `Afinidad 92`: Polen enfrenta su última duda.
- `Afinidad 95`: Polen acepta la sucesión.
- `Afinidad 100`: Coronación y cierre del arco principal.

### Revelaciones de historia

- Polen elige el rol de Reina; no solo lo hereda.
- El Reino Apícola renace como comunidad restaurada, no como simple nostalgia.
- La historia termina con esperanza activa.

## Escenas opcionales de transición

Estas escenas no reemplazan quests, pero ayudan a dar continuidad emocional.

### Entre Capítulo 0 y 1

- Polen deja una flor en el nuevo refugio.
- Polen pregunta si puede volver a verte mañana.

### Entre Capítulo 2 y 3

- Polen observa abejas mientras el jugador trabaja y murmura una canción o dicho antiguo.

### Entre Capítulo 4 y 5

- Polen mira el horizonte y admite que teme lo que puedan encontrar.

### Entre Capítulo 6 y 7

- Polen guarda silencio ante un símbolo real y tarda en tocarlo.

### Entre Capítulo 7 y 8

- Polen visita el claro de cerezos por última vez antes de la coronación.

## Vocabulario emocional recomendado

### Polen debe sonar como

- Cálida.
- Reservada.
- Inteligente.
- Observadora.
- Suavemente insegura.
- Más honesta que elocuente.

### Evitar

- Discursos grandiosos constantes.
- Órdenes secas.
- Tono militar.
- Confianza exagerada antes del Capítulo 6.
- Sarcasmo cínico dominante.

## Guías de implementación para quests y UI

### Naming

- Antes de la revelación: usar `???`.
- Después de la revelación: usar `Polen`.
- Después de Capítulo 7: se puede usar `Polen, heredera del Reino Apícola` en contextos solemnes.
- Después de Capítulo 8: se puede usar `Reina Polen`, pero no como reemplazo total de su nombre personal en diálogos íntimos.

### Quest text

- Cada quest debe tener objetivo práctico y función emocional.
- Evitar textos que solo digan `recolecta X`.
- Incluir siempre por qué ese paso importa para Polen, la colonia o la investigación.

### Recompensas narrativas

- Nuevas líneas de diálogo.
- Nuevos lugares donde Polen puede aparecer.
- Cartas, notas o recuerdos.
- Decoraciones vinculadas a abejas, cerezos, miel o símbolos reales.

### Entornos

- El claro debe conservarse importante incluso cuando la colonia crezca.
- El asentamiento debe evolucionar visualmente junto al arco emocional.
- Las ruinas del Reino Apícola deben transmitir pérdida digna, no solo saqueo.

## Resumen ejecutivo

La historia funciona si el jugador siente esta progresión:

1. Encontré a alguien que no quería ser encontrada.
2. Gané su confianza sin arrancarle su pasado a la fuerza.
3. Construimos un lugar seguro.
4. Descubrimos que su historia era mayor de lo que parecía.
5. La ayudé a enfrentar una herencia dolorosa.
6. Ella eligió convertirse en la líder que podía ser.

Si en cualquier punto la narrativa convierte a Polen en un simple marcador de progreso, la historia pierde su centro.
