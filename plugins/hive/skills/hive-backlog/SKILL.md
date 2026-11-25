---
name: hive-backlog
description: Use when configuring, reading, triaging, or updating a Hive backlog backed by local Markdown, GitHub, GitLab, or Jira, including work-item states, traceability, synchronization, and authorization boundaries.
---

# Hive Backlog

## Incarico

Gestisci il backlog come punto d'ingresso operativo del lavoro. Mantieni
riconoscibili identità, stato, priorità, dipendenze e collegamenti di ogni work
item senza duplicare requisiti, decisioni architetturali o codice.

Questa skill governa work item e transizioni. La skill `hive-feature` usa tali
informazioni per attivare analyst, architect, Platform, developer e reviewer.
Il backlog non decide autonomamente la soluzione tecnica e non amplia l'ambito
autorizzato dall'utente.

## Fonti autorevoli

Assegna una sola fonte autorevole a ciascun tipo di informazione:

| Informazione | Fonte |
| --- | --- |
| Titolo, priorità, assegnazione e stato | Provider del backlog configurato |
| Comportamento e criteri dettagliati | Requisiti collegati |
| Decisioni tecniche | Architettura e ADR collegati |
| Implementazione | Repository Git |
| Verifiche | Test e CI |
| Revisione e approvazioni | Rapporto di review e pull request |

Con un provider esterno, non creare una copia Markdown completa del ticket come
seconda fonte modificabile. Conserva nel repository il riferimento stabile, i
collegamenti agli artefatti e gli eventuali metadati necessari alla ripresa.

## Configurazione del provider

Riusa la configurazione esistente. In sua assenza, durante l'inizializzazione
proponi Local, GitHub, GitLab, Jira oppure Later; usa Local se l'utente non vuole
un provider esterno. Non rendere obbligatoria una connessione remota.

Salva soltanto configurazione non sensibile in `.hive/backlog.yml`. Non inserire
token, credenziali, cookie o secret nel repository. Per schemi e regole dei
provider leggi
[references/provider-configuration.md](references/provider-configuration.md).

Se il provider configurato non è raggiungibile o non dispone degli strumenti
necessari, non creare silenziosamente un backlog alternativo. Dichiara la
connessione mancante e continua soltanto sulle attività indipendenti.

## Modello del work item

Mantieni almeno:

- identificatore stabile e provider;
- titolo, obiettivo e ambito;
- stato e priorità;
- criteri di accettazione o collegamento alla loro fonte;
- dipendenze, blocchi e decisioni aperte;
- ruolo o fase corrente;
- riferimenti a requisiti, ADR, branch, commit e pull request;
- verifiche disponibili e approvazioni mancanti;
- versione o ETag del provider quando disponibile.

Non usare il ticket come contenitore di tutta la conoscenza del progetto.
Collega le fonti autorevoli invece di copiarle.

## Stati e transizioni

Usa come modello logico predefinito:

```text
backlog → discovery → ready → development → review → approval → done
```

Mappa questi stati su quelli del provider senza rinominare automaticamente le
colonne o gli stati remoti. `blocked` è una condizione con causa e responsabile,
non una fase alternativa.

Applica i gate seguenti:

| Transizione | Condizione minima |
| --- | --- |
| backlog → discovery | work item identificato e nell'ambito |
| discovery → ready | comportamento chiaro e decisioni bloccanti risolte |
| ready → development | criteri osservabili e dipendenze necessarie disponibili |
| development → review | implementazione dichiarata, diff noto e verifiche pertinenti eseguite o segnate come mancanti |
| review → approval | rilievi bloccanti risolti e controlli interessati ripetuti |
| approval → done | approvazione richiesta, merge o condizione di completamento effettivamente avvenuti |

Le fasi interne alla discovery sono proporzionali. Attiva analyst, architect o
Platform soltanto quando il work item presenta il relativo bisogno. Non
spostare una card per far apparire completo un gate non soddisfatto.

## Backlog locale

In assenza di convenzioni diverse usa:

```text
documents/backlog/
├── index.yml
└── items/
    └── <id>.md
```

Nel provider Local, il file del work item è autorevole. Assegna identificatori
stabili, mantieni l'indice coerente e modifica soltanto gli item compresi nella
richiesta. Gli aggiornamenti restano cambiamenti del repository e non
autorizzano automaticamente un commit.

Se più work item sono attivi contemporaneamente, conserva lo stato di ripresa
in `documents/backlog/items/<id>-work-status.md` o nel percorso equivalente già
adottato. Per un solo lavoro articolato può essere usato
`documents/work-status.md`.

## Provider esterni

Usa un connettore, MCP o client autenticato disponibile per leggere il provider.
La skill descrive il workflow ma non fornisce credenziali né sostituisce lo
strumento di connessione.

Prima di ogni scrittura remota:

1. rileggi item e versione corrente;
2. verifica repository, progetto e identificatore;
3. mostra la modifica proposta e il suo effetto;
4. controlla l'autorizzazione applicabile;
5. applica una sola volta l'aggiornamento;
6. rileggi il risultato e registra l'esito effettivo.

Una richiesta di implementazione autorizza la modifica locale necessaria, non
la creazione o modifica automatica di issue, commenti, progetti o stati remoti.
Una standing authorization è valida soltanto entro provider, repository,
operazioni e durata esplicitamente concordati.

Usa versione, ETag o altro controllo di concorrenza quando disponibile. Se il
ticket è cambiato dopo la lettura, non sovrascrivere: confronta gli aggiornamenti
e chiedi una decisione quando non possono essere uniti senza cambiare intento.

## Tracciabilità Git e delivery

Collega branch, commit e pull request al work item senza dichiararli esistenti
prima della loro creazione effettiva. Applica la governance della skill
`hive-platform` per inventario del diff, soglie, CI, approvazioni e merge.

Non creare commit, push, pull request, approvazioni o merge senza un mandato
esplicito per l'operazione. Il reviewer formula il giudizio tecnico; l'approvazione
finale resta a un umano o CODEOWNER autorizzato.

## Ripresa e consegna

Alla ripresa confronta provider, file locali, Git, CI e pull request. Non usare
uno stato memorizzato come prova che il mondo esterno sia rimasto invariato.

Alla consegna riporta:

- work item e provider;
- stato precedente e stato effettivo finale;
- artefatti e riferimenti collegati;
- modifiche locali e remote realmente applicate;
- gate soddisfatti e verifiche disponibili;
- blocchi, conflitti o autorizzazioni ancora necessarie.

