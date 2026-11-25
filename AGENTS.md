# Flusso di lavoro del progetto

## Scelta del workflow e del ruolo

Per portare una funzionalità o un ticket attraverso più fasi usa
`plugins/hive/skills/hive-feature/SKILL.md`. Per configurare, leggere o
aggiornare il backlog usa `plugins/hive/skills/hive-backlog/SKILL.md`.

Prima di iniziare, usa le istruzioni del ruolo pertinente:

- requisiti e dominio: `plugins/hive/skills/hive-analyst/SKILL.md`;
- progettazione tecnica: `plugins/hive/skills/hive-architect/SKILL.md`;
- ambienti, CI e deployment: `plugins/hive/skills/hive-platform/SKILL.md`;
- implementazione: `plugins/hive/skills/hive-developer/SKILL.md`;
- revisione: `plugins/hive/skills/hive-reviewer/SKILL.md`.

Per i contratti e le convenzioni specifiche di Hive, usa
`plugins/hive/skills/hive-core/SKILL.md`. Per operare tramite Hive MCP, usa
`plugins/hive/skills/hive/SKILL.md`.

Carica gli altri ruoli soltanto quando sono necessari alla richiesta.

## Ambito

Esegui soltanto le fasi comprese nella richiesta dell'utente. Una richiesta di
analisi, progettazione o revisione non autorizza automaticamente lo sviluppo.

Per una correzione circoscritta, riusa requisiti e decisioni esistenti senza
imporre la ripetizione di tutte le fasi.

## Integrazione del backlog

Applica `plugins/hive/skills/hive-backlog/SKILL.md` come fonte autorevole del
workflow del backlog.

Durante l'inizializzazione di un progetto:

1. Verifica se è configurato un provider di backlog.
2. Se è configurato, riusalo senza chiederlo nuovamente.
3. Altrimenti, chiedi se collegare un provider esterno.
4. Proponi Local, GitHub, GitLab, Jira oppure Later.
5. Se l'utente rifiuta, usa Local come valore predefinito.
6. Salva la configurazione selezionata.
7. Non rendere mai obbligatorio un provider esterno.

Per il provider Local, conserva gli item sotto `documents/backlog/`. Per un
provider esterno, mantieni nel repository configurazione, riferimenti e
collegamenti necessari alla ripresa senza duplicare il ticket come seconda
fonte modificabile.

Non creare o modificare issue esterne senza l'autorizzazione necessaria.

## Passaggi tra le fasi

Quando la richiesta comprende il ciclo di una funzionalità, applica la skill
`hive-feature` e usa gli stati logici `backlog`, `discovery`, `ready`,
`development`, `review`, `approval` e `done`. Tratta `blocked` come una
condizione motivata, non come una fase.

Prima di progettare:

- verifica che comportamento, vincoli e regole interessate siano chiari;
- registra come aperte le ambiguità che possono cambiare la soluzione.

Prima di sviluppare:

- leggi analisi e decisioni architetturali pertinenti;
- leggi le decisioni di piattaforma quando il cambiamento coinvolge ambienti,
  integrazioni o unità distribuibili;
- risolvi le domande che bloccano la funzionalità;
- identifica criteri di accettazione osservabili.

Prima di consegnare:

- esegui le verifiche proporzionate alla modifica;
- controlla le modifiche effettive;
- distingui il lavoro completato da quello non verificato.

## Uso di Hive MCP

- Verifica il progetto con `hive_current_project`.
- Se necessario, selezionalo con `hive_select_project`.
- Usa i tool specifici per la generazione.
- Dopo ogni generazione, aggiorna l'indice con `hive_index_files`.
- Per aggiungere dipendenze, usa `verify=true`.
- Non interpretare `UNAVAILABLE` come un controllo superato.
- Non sovrascrivere con `force=true` senza consenso esplicito.

I nomi dei tool possono essere esposti con un prefisso dipendente dall'host. La
skill `hive` è la fonte autorevole per workflow e policy; gli schemi MCP live
sono la fonte autorevole per argomenti e risultati.

## Git, CI, pull request e approvazione

- La skill `hive-platform` mantiene workflow CI, runner, cache, artefatti e
  policy di delivery.
- La skill `hive-developer` prepara codice, test, branch, commit e pull request
  soltanto quando la richiesta li include.
- La skill `hive-reviewer` valuta il diff e le evidenze senza approvare il
  proprio lavoro come revisione indipendente.
- L'approvazione finale spetta a un umano o CODEOWNER autorizzato; il merge
  spetta a un maintainer autorizzato.
- Non eseguire commit, push, creazione di pull request, approvazioni o merge
  senza un mandato esplicito per l'operazione.

Prima di proporre un commit, controlla file modificati, nuovi, eliminati e
ignorati, misura il diff e individua build output, cache, log, binari, dump,
segreti e file generati. Non includere modifiche estranee dell'utente.

Se il cambiamento supera 50 file oppure 2.000 righe aggiunte e rimosse, richiedi
conferma esplicita prima del commit, spiegando la causa e proponendo una
suddivisione coerente quando possibile. Non spezzare artificialmente un
cambiamento atomico e non nascondere file per restare sotto la soglia.

Ogni pull request deve collegare requisito, issue o ADR pertinente e dichiarare
ambito, verifiche, migrazioni, rischi, rollback, file generati e parti non
verificate.

## Errori e ripresa

Dopo un errore che può avere prodotto modifiche:

- controlla lo stato effettivo prima di riprovare;
- conserva il lavoro già valido;
- registra il blocco e prosegui sulle parti indipendenti.

Per attività articolate, aggiorna `documents/work-status.md` con:

- obiettivo e ambito;
- fase corrente;
- documenti e file interessati;
- verifiche eseguite;
- decisioni aperte;
- prossimo passo.

Alla ripresa, confronta il registro con lo stato reale del progetto prima di
continuare.

## Revisione

Quando la richiesta comprende sviluppo e revisione:

- applica la skill `hive-reviewer` dopo l'implementazione;
- includi configurazioni di piattaforma e risultati CI nell'ambito della
  revisione quando sono stati modificati;
- correggi soltanto i difetti compresi nell'ambito autorizzato;
- ripeti le verifiche interessate dalle correzioni;
- dichiara esplicitamente se la revisione è un'autorevisione.
