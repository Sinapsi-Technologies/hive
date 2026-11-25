---
name: hive-validator
description: Progetta, integra o verifica la validazione framework-free dei Command Hive con `CommandValidator`, `ValidationProvider`, Java SPI e `AbstractCommandFactory`. Attivala per regole di input applicative; non usarla per sostituire invarianti del dominio.
---

# Hive Validator

`hive-validator` definisce il contratto framework-free per validare command
immutabili durante la loro creazione, prima dell'esecuzione del caso d'uso. Il
modulo dipende da `hive-core` e non impone Jakarta o un altro framework.

## Confine della validazione

Usa questa validazione per forma e ammissibilità dell'input applicativo. Mantieni
nel dominio gli invarianti che devono valere indipendentemente dal trasporto o
dal caso d'uso. I command dichiarano dati e restano immutabili: non devono
validarsi da soli.

## Flusso raccomandato

1. Definisci un command che implementa `Command`.
2. Crea una factory che estende `AbstractCommandFactory<C>`.
3. Costruisci il command nella factory e passa l'istanza a `validate`.
4. Componi il `Result<C>` con il caso d'uso tramite `flatMap`, così un input non
   valido non raggiunge `handle`.

`AbstractCommandFactory` ottiene il validator una volta alla costruzione tramite
`ValidationProviders.get()`. Converte soltanto `CommandValidationException` in
`Result.failure(CommonErrorCode.VALIDATION_FAILED, ..., details)`. Errori di
configurazione o eccezioni inattese devono propagarsi: non mascherarli come input
non valido.

## Validator e dettagli

- `CommandValidator<T>.validate` non restituisce un valore.
- `and(...)` esegue i validator in ordine ed è fail-fast tra un validator e il
  successivo.
- `noop()` è l'elemento neutro.
- Una violazione attesa usa `CommandValidationException` con una lista non vuota
  di `ErrorDetail`; la lista viene copiata e resa immutabile.
- Un singolo validator può raccogliere più errori prima di sollevare l'eccezione.
  Usa `field=""` per un errore relativo all'intero oggetto.

## Provider via Java SPI

`ValidationProviders` usa `ServiceLoader` su
`io.sinapsi.hive.validation.ValidationProvider`:

- nessun provider: `NoopValidationProvider` e nessuna validazione effettiva;
- un provider: viene usato per le factory;
- più provider: errore immediato per configurazione ambigua.

Registra un provider in
`META-INF/services/io.sinapsi.hive.validation.ValidationProvider`. Il provider è
caricato e memorizzato staticamente per classloader: non assumere che cambiare il
classpath o il descriptor durante lo stesso processo ricarichi la selezione.

## Verificare la versione e l'integrazione

- Verifica firme e comportamento nella versione risolta tramite Hive MCP, Maven,
  `sources.jar`, Javadoc o `javap -public`.
- Testa provider assente, provider unico e configurazione multipla quando
  pertinenti al packaging.
- Verifica che un command invalido produca tutti gli `ErrorDetail` attesi e che
  il caso d'uso non venga invocato.
- Verifica separatamente gli errori inattesi, che devono restare eccezioni.
- Compila ed esegui i test con la reale composizione runtime, non soltanto con il
  contratto `hive-validator` isolato.

Per Bean Validation usa `jakarta-hive-validator`; non implementare un secondo
provider quando quello Jakarta è già presente nel runtime.
