---
name: hive-feature
description: Use when taking one Hive feature or backlog item through the necessary lifecycle from triage and analysis to architecture, platform work, implementation, review, and delivery gates.
---

# Hive Feature

## Incarico

Coordina una funzionalità dall'ingresso fino all'esito richiesto, attivando
soltanto le fasi necessarie e mantenendo tracciabilità tra backlog, requisiti,
decisioni, codice, verifiche e review.

Usa `hive-backlog` come fonte del work item e del suo stato. Usa le skill di
ruolo come fonti autorevoli per il lavoro di ciascuna fase:

- `hive-analyst` per comportamento e dominio;
- `hive-architect` per confini, contratti e unità distribuibili;
- `hive-platform` per ambienti, infrastruttura, CI e delivery;
- `hive-developer` per implementazione e test;
- `hive-reviewer` per la revisione.

Per contratti e tool Hive usa rispettivamente `hive-core` e `hive`. Non
riassumere le skill di ruolo in sostituzione delle loro istruzioni: carica quella
pertinente quando inizia la fase.

## Ambito e autorizzazioni

La richiesta determina quali fasi sono autorizzate. Una richiesta di analisi
non autorizza lo sviluppo. Una richiesta di implementazione consente le
modifiche locali necessarie, ma non autorizza automaticamente:

- aggiornamenti a ticket o progetti remoti;
- commit o push;
- creazione o approvazione di pull request;
- merge, release o deployment;
- modifiche a sistemi o dati esterni.

Esegui le fasi necessarie a rendere corretta la consegna, ma fermati prima delle
azioni che richiedono un mandato ulteriore. Non presentare come indipendente una
review eseguita dallo stesso agente che ha implementato la modifica.

## 1. Selezionare o creare il work item

Se la richiesta indica un ticket, recuperalo dal provider configurato con
`hive-backlog`. Se descrive una funzionalità senza ticket, usa tale descrizione
come work item locale temporaneo e proponi la sua registrazione; non creare
automaticamente un'issue remota.

Conferma:

- identificatore e obiettivo;
- ambito ed esclusioni;
- stato e priorità disponibili;
- criteri di accettazione o loro fonte;
- dipendenze e blocchi;
- artefatti già esistenti;
- risultato richiesto dall'utente in questa esecuzione.

Se riprendi un lavoro, confronta lo stato registrato con repository, provider,
CI e pull request prima di proseguire.

## 2. Eseguire il triage

Determina le fasi necessarie attraverso l'impatto reale:

| Segnale | Fase |
| --- | --- |
| comportamento, attori o regole ambigui | Analyst |
| nuovi confini, dati, contratti o qualità significative | Architect |
| nuovi container, database, broker, CI o deployment | Platform |
| codice o configurazione da modificare | Developer |
| risultato da valutare prima della consegna | Reviewer |

Una piccola correzione con requisiti e architettura già chiari può iniziare dallo
sviluppo. Un nuovo sistema con descrizione generica richiede invece il minimo di
analisi e architettura necessario alla prima vertical slice. Non progettare in
anticipo parti non richieste e non introdurre servizi distribuiti per abitudine.

Registra piano delle fasi, motivazione delle fasi saltate, gate e decisioni
aperte. Usa `discovery` come stato mentre analyst, architect o Platform stanno
risolvendo prerequisiti.

## 3. Analisi

Applica `hive-analyst` quando il comportamento non è sufficientemente definito.
La fase è pronta quando:

- obiettivo, attori e comportamento nell'ambito sono chiari;
- regole ed esiti rilevanti sono verificabili;
- criteri di accettazione distinguono successo, rifiuti ed eccezioni importanti;
- le domande residue non cambiano sostanzialmente la soluzione da progettare.

Per una modifica piccola può bastare l'aggiornamento mirato del ticket o del
documento esistente. Non imporre un'analisi completa del prodotto.

## 4. Architettura

Applica `hive-architect` quando la feature richiede una decisione tecnica non
locale. La fase è pronta quando:

- responsabilità e confini interessati sono definiti;
- proprietà dei dati, contratti e flussi critici sono chiari;
- unità distribuibili e dipendenze necessarie sono decise;
- rischi, migrazioni e verifiche attese sono documentati;
- le decisioni durevoli hanno un ADR quando appropriato.

Non equiparare automaticamente bounded context, modulo, servizio e container.

## 5. Platform

Applica `hive-platform` se la feature cambia ambienti, servizi infrastrutturali,
CI o deployment. La fase è pronta quando:

- la topologia necessaria deriva dalle decisioni dell'architect;
- configurazione e segreti hanno responsabilità definite;
- avvio, health, dati, migrazioni e reset sono riproducibili;
- CI e controlli di delivery coprono il rischio introdotto;
- rollback e limiti operativi pertinenti sono dichiarati.

Se l'ambiente esistente è sufficiente, registra la verifica e salta la modifica
di Platform.

## 6. Sviluppo

Prima di modificare codice, verifica che i gate necessari siano soddisfatti e
applica `hive-developer`. Implementa una vertical slice verificabile, preserva
le modifiche estranee dell'utente e aggiorna documentazione direttamente
interessata.

Al termine:

- esegui test e controlli proporzionati;
- ispeziona file modificati, nuovi ed eliminati;
- misura il diff e individua artefatti inattesi;
- distingue controlli superati, falliti e non eseguiti;
- porta il work item in review soltanto quando il risultato è realmente
  disponibile per la valutazione.

Applica le soglie e la governance Git definite da `hive-platform`. Non creare
un commit come semplice effetto del completamento del codice.

## 7. Revisione e correzioni

Applica `hive-reviewer` al diff e alle evidenze effettive. Quando è richiesta
indipendenza, usa un contesto o revisore separato se disponibile; altrimenti
dichiara l'autorevisione.

Se emergono difetti compresi nell'ambito:

1. registra i rilievi senza modificare il codice durante la sola review;
2. torna alla fase developer per le correzioni autorizzate;
3. ripeti i controlli influenzati;
4. riesegui la review sui rilievi e sulle regressioni pertinenti.

Non spostare il work item in approval finché restano rilievi bloccanti o
verifiche essenziali mancanti.

## 8. Delivery

Quando il risultato è pronto, presenta:

- work item, ambito e criteri soddisfatti;
- documenti e decisioni aggiornati;
- file e componenti interessati;
- test, CI e review con i rispettivi limiti;
- dimensione del diff e file generati intenzionali;
- commit, push, pull request o deployment ancora da autorizzare.

Se l'utente autorizza un'operazione Git o remota, eseguila secondo
`hive-platform` e `hive-backlog`, verifica l'esito e aggiorna soltanto i
collegamenti realmente creati. L'approvazione tecnica del reviewer non
sostituisce l'approvazione umana o CODEOWNER richiesta dal repository.

Segna `done` soltanto quando la condizione di completamento concordata è
avvenuta; codice locale pronto, PR aperta e merge completato sono stati diversi.

## Stato e ripresa

Per lavori articolati mantieni lo stato nel percorso adottato dal progetto. In
assenza di convenzioni:

- usa `documents/work-status.md` per un solo lavoro attivo;
- usa `documents/backlog/items/<id>-work-status.md` per lavori concorrenti.

Registra obiettivo, fase, artefatti, verifiche, decisioni aperte, modifiche
remote e prossimo passo. Dopo un errore che può avere prodotto effetti,
controlla lo stato reale prima di riprovare e conserva il lavoro già valido.

