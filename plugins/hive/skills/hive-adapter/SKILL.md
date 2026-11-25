---
name: hive-adapter
description: Integra o verifica gli adapter leggeri di `io.sinapsi.hive:hive-adapter` per clock, UUID, eventi in memoria e file locali. Attivala per test, demo, sviluppo locale o applicazioni semplici; non usarla come garanzia di infrastruttura produttiva.
---

# Hive Adapter

`hive-adapter` fornisce implementazioni dependency-light dei port tecnici di
`hive-port`. L'application layer dipende dai port; soltanto il composition root
e l'infrastruttura devono conoscere questi adapter.

## Verificare la versione

Controlla la versione risolta e le API pubbliche tramite Hive MCP, Maven,
`sources.jar`, Javadoc o `javap -public`. Il modulo dipende da `hive-port`; usa
versioni compatibili e non fissare una versione basandoti sugli esempi.

## Scegliere l'adapter

| Port | Adapter | Uso appropriato |
| --- | --- | --- |
| `ClockPort` | `SystemClockAdapter` | UTC reale o `Clock` iniettato nei test |
| `IdGeneratorPort<T>` | `UuidGeneratorAdapter<T>` | Identificatori con valore `UUID` |
| `PublishEventPort<E>` | `InMemoryEventPublisherAdapter<E>` | Test e flussi locali non durevoli |
| `FileStoragePort` | `LocalFileStorageAdapter` | Byte su filesystem locale sotto una root |

### Comportamenti da preservare

- `SystemClockAdapter()` usa `Clock.systemUTC()`; in test passa un `Clock.fixed`
  o un altro clock controllato.
- `UuidGeneratorAdapter` richiede una factory `UUID -> Identifier<UUID>`; il
  costruttore avanzato accetta anche un `Supplier<UUID>` deterministico.
- `InMemoryEventPublisherAdapter` conserva gli eventi nell'ordine di
  pubblicazione ed espone snapshot immutabili. Non è un broker, un'outbox o uno
  storage durevole e non garantisce consegna tra processi.
- `LocalFileStorageAdapter` normalizza i path, rifiuta l'uscita lessicale dalla
  root, crea le directory parent e converte gli errori I/O in
  `UncheckedIOException`. Non presentarlo come sandbox per input ostile o come
  object storage: collegamenti simbolici, concorrenza, permessi, atomicità e
  durabilità richiedono una valutazione o un adapter dedicato.

## Integrazione

Istanzia gli adapter nel composition root e iniettali tramite le interfacce di
`hive-port`. Non costruirli dentro entità, use case o servizi applicativi. Usa
adapter specifici della tecnologia quando servono transazioni, persistenza
remota, messaging reale, retry, osservabilità o requisiti operativi.

## Verifica

- Testa il servizio applicativo contro il port, con dipendenze deterministiche.
- Testa separatamente il wiring e il comportamento dell'adapter scelto.
- Per file locali usa una directory temporanea e copri path ammessi, traversal,
  file mancanti e fallimenti I/O pertinenti.
- Per eventi in memoria verifica ordine e snapshot senza attribuire garanzie di
  produzione.
- Compila ed esegui i test dei moduli coinvolti con le versioni risolte.

Usa `hive-port` per decidere se il contratto condiviso è sufficiente; definisci
un port e un adapter applicativi quando non lo è.
