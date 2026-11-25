---
name: mermaid
description: Crea o aggiorna diagrammi Mermaid quando relazioni, sequenze, stati, dipendenze o confini architetturali risultano più chiari in forma visuale.
---

# Mermaid

## Procedura

1. Individuare la relazione da spiegare e il livello di dettaglio richiesto.
2. Ricavare elementi e collegamenti dalle informazioni disponibili. Segnalare le ipotesi rilevanti nel testo di accompagnamento.
3. Scegliere il tipo di diagramma:
   - `flowchart`: processi, decisioni e dipendenze.
   - `sequenceDiagram`: messaggi tra attori o componenti nel tempo.
   - `stateDiagram-v2`: stati e transizioni di una singola entità.
   - `erDiagram`: entità, relazioni e cardinalità.
   - `classDiagram`: struttura di tipi e relazioni del modello.
4. Rappresentare un argomento per diagramma. Separare panoramica e dettagli quando la lettura diventa difficile.
5. Controllare sintassi, significato delle frecce, coerenza dei nomi e leggibilità.

## Scrittura

- Usare blocchi Markdown con linguaggio `mermaid`; nei file `.mmd` scrivere il solo sorgente.
- Usare identificatori semplici e stabili, distinti dalle etichette leggibili.
- Scrivere etichette brevi nella lingua del documento, mantenendo i termini del dominio.
- Nei flowchart racchiudere le etichette tra virgolette, ad esempio `ordine["Ordine ricevuto"]`.
- Preferire `flowchart TD`; usare `LR` per diagrammi brevi. Mantenere al massimo cinque elementi affiancati.
- Etichettare decisioni e collegamenti quando il loro significato sarebbe ambiguo.
- Usare forme e stili predefiniti. Aggiungere colori o altre personalizzazioni soltanto se richiesti o necessari a distinguere un significato.
- Mantenere il sorgente portabile: usare sintassi comune, etichette testuali e raggruppamenti semplici, senza HTML, direttive di configurazione o collegamenti interattivi.
- Conservare nei diagrammi aggiornati nomi e identificatori già coerenti con la documentazione.

## Verifica e consegna

- Verificare che ogni elemento e relazione siano sostenuti dalle fonti o esplicitamente indicati come ipotesi.
- Per i flussi controllare diramazioni ed esiti; per le sequenze ordine e direzione dei messaggi; per gli stati le transizioni; per le entità le cardinalità.
- Se è disponibile un parser o renderer Mermaid, usarlo per verificare il sorgente e correggere gli errori. Dichiarare la verifica eseguita solo se effettivamente svolta.
- Restituire il diagramma con il minimo testo necessario a comprenderlo. Se richiesto il solo codice, consegnare il solo sorgente.
- Per esportazioni in documenti o PDF, conservare il sorgente modificabile e generare SVG, oppure PNG quando richiesto dal formato di destinazione.
