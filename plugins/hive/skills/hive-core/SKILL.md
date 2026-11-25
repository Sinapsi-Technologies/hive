---
name: hive-core
description: Verifica e usa i contratti Java di hive-core come dipendenza Maven in un'applicazione esagonale, per casi d'uso, comandi, porte, risultati tipizzati, aggregati, eventi e mapper.
---

# Hive Core

L'applicazione usa `io.sinapsi.hive:hive-core` come dipendenza Maven. Il JAR espone i contratti in `io.sinapsi.hive.core`. Mantieni i contratti applicativi indipendenti da framework, trasporti, persistenza e runtime di validazione.

## Verificare un contratto

Quando devi verificare una firma o un comportamento, consulta il `sources.jar` associato al JAR usato dall'applicazione, la Javadoc della stessa versione o le API pubbliche del binario con `javap -public`. Se il comportamento resta incerto, usa un test mirato nell'applicazione quando la fase di lavoro lo consente. Segnala i dettagli che restano da verificare.

Se la versione risolta non è già nota, usa `hive_dependency_graph` sul progetto applicativo selezionato. Se il tool è indisponibile o inconcludente, usa `mvn dependency:tree -Dincludes=io.sinapsi.hive:hive-core` con la configurazione della build. Quando serve confrontarla con la versione dichiarata o gestita, consulta il POM o `mvn help:effective-pom`.

## Definire un caso d'uso

1. Definisci il contratto in `application.ports.in` come interfaccia che estende `UseCase<I, O>`. `I` implementa `Command` e `UseCase` estende `InputPort` ed espone `O handle(I input)`.
2. Usa un comando immutabile per i dati in ingresso. Può convertire i propri valori in value object. Colloca nel dominio le regole di business, gli invarianti e la logica di creazione degli aggregati. Se usi `hive-validator` per validare il comando al confine applicativo, verifica `AbstractCommandFactory` nella versione adottata di quel modulo.
3. Per un esito tipizzato usa `Result<View>`. Colloca la view in `application.ports.in.views` come record o classe con factory `of(...)`, secondo le convenzioni del progetto. Il contratto in ingresso può così riferirsi alla view senza dipendere da `application.services`.
4. Implementa `handle` nel servizio applicativo, che coordina il caso d'uso, le transazioni e l'accesso alle capacità esterne. Definisci queste capacità come interfacce `OutputPort` in `application.ports.out` e implementale negli adapter. Per porte condivise come orologio, generazione di ID e pubblicazione di eventi, verifica cosa offre la versione adottata di `hive-port`.

## Gestire gli esiti

- `Result<T>` è sealed e ammette `Success<T>` e `Failure<T>`. Usa `Result.success(view)` con un valore non nullo. Per un'operazione riuscita senza dati, restituisci `Result<Unit>` con `Result.success(Unit.INSTANCE)`.
- Per un errore applicativo atteso usa `Result.failure(code, message)` o `Result.failure(code, message, details)`. Ogni bounded context può definire i propri `ErrorCode` con `value()`. `CommonErrorCode.VALIDATION_FAILED` è un codice condiviso facoltativo.
- `ResultError` contiene codice, messaggio e una copia immutabile dei dettagli. `ErrorDetail(field, message)` descrive un errore di campo. Usa `""` come `field` per un errore dell'intero oggetto.
- Usa `map` per una trasformazione del valore e `flatMap` quando la funzione restituisce un altro `Result`. Entrambi propagano il `ResultError` di un fallimento senza invocare la funzione. Nell'adapter usa `fold(onSuccess, onFailure)` per tradurre l'esito nel formato di trasporto. Il ramo di errore riceve un singolo `ResultError`.

## Usare i contratti di dominio e mapping

- `Identifier<T>` espone `value()` e `AggregateId<T>` lo estende. Se adotti `AggregateRoot<ID>`, l'ID deve implementare `AggregateId<?>` e l'aggregato espone `getId()` e `pullDomainEvents()`.
- Usa `DomainEvent` per un evento interno al bounded context e `IntegrationEvent` per un evento destinato a oltrepassarne il confine. Entrambi estendono `Event`. La pubblicazione e la consegna dipendono dalle porte e dagli adapter dell'applicazione.
- Usa `Mapper<S, T>.map` per una conversione unidirezionale e `BiMapper<A, B>` con `toTarget` e `toSource` per entrambe le direzioni. Colloca le implementazioni presso il confine applicativo o l'adapter che le usa.

## Verificare le modifiche all'applicazione

Quando la fase di lavoro prevede verifiche eseguibili, compila i moduli applicativi interessati ed esegui i test pertinenti dell'applicazione con la versione risolta. Usa i comandi e i profili di build del progetto.

## Moduli collegati

- Usa `hive-port` per i port tecnici condivisi basati su `OutputPort`.
- Usa `hive-adapter` per le implementazioni leggere predefinite di tali port.
- Usa `hive-validator` per validazione framework-free e command factory.
- Usa `jakarta-hive-validator` per collegare Jakarta Bean Validation tramite SPI.

Le skill dei moduli descrivono integrazione e limiti specifici; questa skill
resta autorevole per i contratti base di `hive-core`.
