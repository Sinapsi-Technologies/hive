# Ambienti di sviluppo multi-container

Leggi questo riferimento quando il lavoro riguarda Docker, Compose, database,
broker o più unità distribuibili eseguite insieme.

## Dal disegno ai processi

Non dedurre un container da ogni package, modulo Maven o bounded context. Parti
dalle unità distribuibili approvate dall'architect e rappresenta ciascun
processo necessario all'ambiente. Un singolo file Compose può coordinarle senza
implicare che abbiano lo stesso ciclo di rilascio in produzione.

Una topologia Spring Cloud può, per esempio, contenere:

| Categoria | Esempi | Nota |
| --- | --- | --- |
| Piattaforma | Eureka Server, Config Server, API Gateway | Processi autonomi soltanto se la decisione architetturale li richiede |
| Applicazione | customer-service, order-service | Ogni unità distribuibile ha immagine, health e configurazione proprie |
| Dati | PostgreSQL, MongoDB, Redis | Chiarire proprietà, persistenza e isolamento dei dati |
| Messaggistica | RabbitMQ, Kafka | Definire bootstrap, topic/queue, retry e dati persistenti |
| Osservabilità | collector, metriche, log, tracing | Attivabili tramite profili quando non servono sempre |

## Struttura Compose

Preferisci una configurazione comprensibile a una proliferazione di file. Usa
file aggiuntivi o profili quando separano esigenze reali, per esempio:

- dipendenze minime per lo sviluppo ordinario;
- stack completo per test di integrazione;
- strumenti opzionali di osservabilità o amministrazione;
- sostituzione di un servizio locale con uno eseguito dall'IDE.

Per ciascun servizio specifica solo ciò che deve essere condiviso. Pubblica
porte sull'host soltanto quando un processo esterno alla rete Compose deve
raggiungerle. Assegna nomi DNS interni stabili attraverso il nome del servizio,
non tramite indirizzi IP.

Usa healthcheck coerenti con la capacità realmente necessaria. Un database che
accetta connessioni non dimostra che lo schema sia migrato; un broker avviato
non dimostra che exchange, queue o topic siano pronti. Mantieni inizializzazione
e readiness osservabili e idempotenti.

## Configurazione e segreti

Conserva nel repository soltanto valori locali non sensibili e template come
`.env.example`. Documenta origine, precedenza e obbligatorietà delle variabili.
Non copiare segreti di produzione nell'ambiente di sviluppo.

Se è presente un Config Server, chiarisci dove risiede la configurazione,
quale bootstrap è necessario e come evitare una dipendenza circolare con gli
altri servizi di piattaforma. Se è presente Eureka, definisci il comportamento
quando il registry non è ancora pronto. Se è presente un gateway, verifica che
route e health non mascherino l'indisponibilità dei servizi a valle.

## Dati, migrazioni e reset

Definisci separatamente:

- creazione dello schema o delle risorse del broker;
- migrazioni versionate;
- dati minimi di sviluppo;
- dati sintetici opzionali;
- reset applicativo;
- cancellazione distruttiva dei volumi.

Le procedure distruttive devono nominare il bersaglio e richiedere conferma
quando possono eliminare lavoro utile. Verifica almeno una ripartenza con dati
persistenti e, quando pertinente, un avvio pulito.

## Parità tra ambienti

La parità significa rendere equivalenti i comportamenti rilevanti, non copiare
necessariamente la topologia di produzione sul laptop. Documenta differenze
come replica singola, autenticazione semplificata, storage locale o servizi
simulati, insieme ai rischi che restano da verificare altrove.

