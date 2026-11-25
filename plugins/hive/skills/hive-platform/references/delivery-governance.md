# Governance di CI, Git e pull request

Leggi questo riferimento quando il lavoro configura pipeline, protezioni del
repository, commit, pull request, approvazioni o merge.

## Separazione delle responsabilità

| Attività | Responsabile |
| --- | --- |
| Workflow CI, runner, cache e artefatti | Platform |
| Codice, test applicativi e preparazione della PR | Developer |
| Revisione del diff e delle evidenze | Reviewer |
| Approvazione finale | Umano o CODEOWNER autorizzato |
| Merge | Maintainer autorizzato |

Un controllo svolto dallo stesso agente che ha prodotto la modifica è
un'autorevisione e deve essere dichiarato. Non sostituisce l'approvazione umana.

## Inventario prima del commit

Prima di chiedere autorizzazione a committare, raccogli almeno:

- stato dei file tracciati e non tracciati;
- riepilogo e dimensione del diff;
- elenco delle eliminazioni e dei rename;
- file ignorati rilevanti, senza riversare indiscriminatamente cache e build;
- file binari o insolitamente grandi;
- output generati e motivo per cui devono essere versionati;
- test e controlli realmente eseguiti.

Controlla in particolare directory di build, log, dump di database, file IDE,
cache, credenziali, chiavi e configurazioni personali. Non usare `git add .` o
equivalenti prima di aver compreso l'intero inventario.

Quando sono presenti modifiche dell'utente estranee al compito, non includerle
e non ripristinarle. Se non è possibile separarle con sicurezza, fermati prima
del commit e chiedi indicazioni.

## Commit revisionabili

Ogni commit deve avere uno scopo descrivibile e verificabile. Mantieni insieme
codice e test necessari a dimostrare lo stesso comportamento; separa invece
operazioni indipendenti come riformattazione massiva, aggiornamento delle
dipendenze, rigenerazione o cambiamenti di infrastruttura.

Un commit grande può essere legittimo, per esempio per una migrazione o un
artefatto generato. In tal caso registra generatore, input, comando, motivazione
e modalità di verifica. Non suddividere il diff in commit privi di significato
solo per restare sotto una soglia numerica.

## Contenuto della pull request

La pull request deve rendere ricostruibile la decisione:

- obiettivo, ambito ed esclusioni;
- requisito, issue o ADR collegato;
- componenti, dati e ambienti interessati;
- migrazioni e compatibilità;
- verifiche eseguite e risultati;
- rischi, rollback e osservabilità;
- file generati intenzionalmente;
- limiti e controlli non eseguiti.

## Protezioni consigliate

Configura in base al rischio e alle capacità dell'hosting:

- branch protetto e divieto di force-push;
- controlli CI obbligatori e aggiornati sulla revisione da unire;
- almeno un'approvazione umana;
- conversazioni risolte prima del merge;
- CODEOWNERS per pipeline, sicurezza, migrazioni e aree critiche;
- nuova approvazione dopo modifiche sostanziali, quando supportata;
- merge soltanto da parte di maintainer autorizzati.

Le protezioni remote sono modifiche allo stato del repository: prepara la
configurazione o le istruzioni, ma applicale soltanto con autorizzazione
esplicita e dopo aver verificato organizzazione, repository e branch bersaglio.

