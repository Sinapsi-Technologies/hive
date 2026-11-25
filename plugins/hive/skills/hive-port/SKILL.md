---
name: hive-port
description: Usa i port tecnici condivisi di `io.sinapsi.hive:hive-port` in un'applicazione Hive per tempo, identificatori, pubblicazione eventi e file storage. Attivala quando devi scegliere, integrare o verificare questi output port, non per progettare un port specifico del dominio.
---

# Hive Port

Usa `hive-port` quando un caso d'uso Hive necessita una capacità tecnica comune
senza dipendere dalla relativa tecnologia. Il modulo dipende da `hive-core` e
fornisce contratti framework-free; non contiene adapter.

## Verificare la versione

Prima di consolidare firme o comportamento, verifica la versione effettivamente
risolta dall'applicazione tramite Hive MCP, Maven, `sources.jar`, Javadoc o
`javap -public`. Non copiare automaticamente la versione del repository nelle
applicazioni consumer.

## Scegliere il port

| Port | Capacità | Vincolo principale |
| --- | --- | --- |
| `ClockPort` | Restituire l'istante corrente con `now()` | Restituisce `Instant` |
| `IdGeneratorPort<T>` | Generare un identificatore | `T extends Identifier<?>` |
| `PublishEventPort<E>` | Pubblicare un evento | `E extends Event` |
| `FileStoragePort` | Salvare e caricare byte tramite path logico | API `save`/`load` sincrona |

Tutti estendono `OutputPort` di `hive-core`.

Colloca la dipendenza nel layer applicativo e iniettala nel servizio che orchestra
il caso d'uso. Il dominio non deve dipendere da questi port tecnici. Gli adapter
concreti appartengono all'infrastruttura e vengono collegati nel composition root.

## Decidere tra port condiviso e port specifico

Usa un port condiviso solo quando il suo contratto esprime esattamente la
capacità necessaria. Definisci invece un output port specifico dell'applicazione
quando servono semantica di business, atomicità, query, metadati, streaming,
versionamento, idempotenza o errori che il contratto generico non rappresenta.

Non modellare un repository di dominio tramite `FileStoragePort` e non usare
`PublishEventPort` come sostituto implicito di outbox, consegna garantita o
transazione distribuita.

## Verificare l'integrazione

- Compila il modulo applicativo con la versione risolta di `hive-port` e
  `hive-core`.
- Nei test del caso d'uso usa fake o lambda deterministici quando il port lo
  consente; non dipendere dall'orologio o dalla casualità reali.
- Verifica che application non importi classi di `hive-adapter` e che domain non
  importi `io.sinapsi.hive.ports`.
- Se modifichi dipendenze tramite Hive MCP, applica il workflow autorevole della
  skill `hive`, inclusi verifica della versione, build e sicurezza.

Per le implementazioni predefinite leggere usa la skill `hive-adapter`. Per i
contratti base `Identifier`, `Event` e `OutputPort` usa `hive-core`.
