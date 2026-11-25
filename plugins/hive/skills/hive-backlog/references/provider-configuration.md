# Configurazione dei provider di backlog

Leggi questo riferimento quando inizializzi o cambi il provider del backlog.
Conserva la configurazione in `.hive/backlog.yml` e adattala alle convenzioni
già presenti nel progetto.

## Campi comuni

```yaml
version: 1
provider:
  type: local

statuses:
  backlog: Backlog
  discovery: Discovery
  ready: Ready
  development: In Progress
  review: In Review
  approval: Awaiting Approval
  done: Done
```

La mappatura associa gli stati logici Hive agli stati esistenti. Non creare o
rinominare automaticamente colonne remote. Ometti le mappature non configurate
e segnala le transizioni che non possono essere sincronizzate.

## Local

```yaml
version: 1
provider:
  type: local
  path: documents/backlog
```

I file locali sono autorevoli e vengono versionati insieme al repository.

## GitHub

```yaml
version: 1
provider:
  type: github
  repository: organization/repository
  project: 12
```

`project` è facoltativo. Le issue restano autorevoli anche senza GitHub
Projects. Usa il connettore GitHub, un server MCP o un client autenticato
disponibile; non salvare token nel file.

## GitLab

```yaml
version: 1
provider:
  type: gitlab
  project: group/project
```

Usa identificatori e URL del server effettivo quando l'istanza non è quella
pubblica. Conserva le credenziali fuori dal repository.

## Jira

```yaml
version: 1
provider:
  type: jira
  project: HIVE
  site: https://example.atlassian.net
```

`site` non deve contenere credenziali. Verifica tipi di issue e transizioni
disponibili prima di proporre aggiornamenti.

## Later

```yaml
version: 1
provider:
  type: later
```

Usa `later` soltanto come decisione temporanea esplicita. Non fingere che
esista un backlog operativo; registra il punto aperto e consenti il lavoro solo
quando obiettivo e ambito sono comunque identificabili.

## Cambio del provider

Non cambiare provider come effetto collaterale di un work item. Prima della
migrazione definisci identità, mapping degli stati, commenti, allegati,
collegamenti e fonte autorevole durante il passaggio. Verifica il risultato
prima di dismettere la fonte precedente e non duplicare automaticamente ticket
remoti.

