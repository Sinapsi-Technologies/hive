---
name: jakarta-hive-validator
description: Integra o verifica l'adapter `io.sinapsi.hive:jakarta-hive-validator` che traduce Jakarta Bean Validation nella validazione dei Command Hive tramite Java SPI. Attivala per constraint Jakarta; non usarla per regole e invarianti propri del dominio.
---

# Jakarta Hive Validator

`jakarta-hive-validator` collega Jakarta Bean Validation al contratto
framework-free di `hive-validator`. Le implementazioni vivono nel package
infrastrutturale `io.sinapsi.hive.infrastructure.validation`; application e
factory continuano a dipendere dai contratti Hive.

## Composizione runtime

Il modulo include il descriptor Java SPI per
`JakartaValidationProvider`. Normalmente non istanziare né registrare il provider
a mano: aggiungi il modulo al runtime e lascia che `ValidationProviders.get()` lo
scopra. Deve esserci un solo `ValidationProvider` nel classpath effettivo.

`jakarta.validation-api` è un'API, non un motore completo. Assicurati che il
runtime contenga un'implementazione compatibile, per esempio Hibernate Validator,
ed eventuali dipendenze richieste dalla configurazione scelta. Se il provider
Jakarta non può inizializzare il `ValidatorFactory`, l'errore è di configurazione
e non deve diventare un `Result` di validazione.

Verifica sempre versioni e dipendenze realmente risolte tramite Hive MCP, Maven,
`sources.jar` o Javadoc. Non copiare le versioni degli esempi senza considerare
BOM, runtime e compatibilità dell'applicazione.

## Definire i command

Mantieni i command immutabili e applica annotazioni Jakarta ai componenti che
descrivono vincoli di input. Non inserire chiamate al validator nel command e non
spostare nelle annotazioni gli invarianti che appartengono al dominio.

Il flusso ordinario resta quello di `hive-validator`:

1. la factory costruisce il command;
2. `AbstractCommandFactory.validate` usa il provider scoperto via SPI;
3. `JakartaCommandValidator` valida con il gruppo predefinito;
4. tutte le violazioni diventano `ErrorDetail` ordinati per field e messaggio;
5. la factory restituisce `VALIDATION_FAILED` preservando i dettagli.

Il valore rifiutato e i tipi Jakarta non vengono inclusi negli errori Hive. Non
assumere supporto implicito per gruppi, sequenze di gruppi o una factory Jakarta
personalizzata: l'API corrente usa `Validator.validate(command)` e il validator
predefinito costruito dal provider disponibile.

## Uso diretto e composizione

`JakartaCommandValidator.create()` è utile nei test o per composizione avanzata.
Implementa `CommandValidator<T>`, quindi può essere concatenato con regole
applicative tramite `and(...)`. La composizione è ordinata e fail-fast tra i
validator: se Jakarta trova violazioni, i validator successivi non vengono
eseguiti.

## Verifica

- Conferma che `ValidationProviders.get()` restituisca
  `JakartaValidationProvider` nella composizione runtime reale.
- Copri command valido, violazione singola, violazioni multiple e constraint su
  tipi di command differenti.
- Verifica field path e messaggi senza asserire un ordine diverso da quello
  deterministico fornito dall'adapter.
- Verifica che un fallimento impedisca l'esecuzione del caso d'uso.
- Mantieni il modulo domain privo di dipendenze Jakarta e controlla che il wiring
  resti nell'infrastruttura.

Usa `hive-validator` per il contratto, la factory, la semantica di `Result` e le
regole applicative non espresse dai constraint Jakarta.
