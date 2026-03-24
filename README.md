## Indice
1.  [Regole del Gioco](#regole-del-gioco)
2.  [Funzionalità Implementate](#funzionalità-implementate)
3.  [L'Intelligenza Artificiale (Logica ASP)](#lintelligenza-artificiale-logica-asp)
4.  [Logica di Gioco Dettagliata (Il Motore IA)](#logica-di-gioco-dettagliata-il-motore-ia)
5.  [Come Eseguire il Progetto](#come-eseguire-il-progetto)

## Regole del Gioco

L'obiettivo del gioco è ripulire il vassoio (`Tray`) spostando e unendo pezzi di torta dello stesso colore per completare i piatti (`Plate`).

### Elementi di Gioco
*   **Vassoio (Tray):** Una griglia dove i piatti vengono posizionati.
*   **Tavolo (Table):** Un'area superiore che contiene i piatti "in attesa" che possono essere giocati.
*   **Piatto (Plate):** Un contenitore che può ospitare fino a **6 pezzi di torta**. Ogni piatto sul vassoio ha un ID numerico sequenziale per essere facilmente identificato dal solver.

### Meccanica Principale
1.  **Drag and Drop:** Il giocatore trascina un piatto dal Tavolo e lo rilascia su uno slot vuoto del Vassoio.
2.  **Risoluzione IA:** Non appena il piatto tocca la griglia, il controllo passa interamente all'Intelligenza Artificiale. Il gioco entra in un ciclo (gestito da un Timer) in cui, un passo alla volta, l'IA analizza **lo stato della griglia** e decide la mossa migliore da eseguire per unire i colori.
3.  **Rimozione dei Piatti:** Un piatto viene rimosso dal vassoio quando è **completo** (6 pezzi dello stesso colore) o viene completamente **svuotato** dai trasferimenti.

## Funzionalità Implementate
*   **Logica AI-Driven con EmbASP:** Tutti gli spostamenti dei pezzi (sia le reazioni immediate al drop che le catene successive) sono calcolati dal solver **DLV2** tramite regole ASP.
*   **Suggeritore Strategico (Hint System):** Una funzione che analizza tutti i piatti disponibili sul tavolo e tutte le celle vuote del vassoio, simulando le reazioni a catena future per consigliare all'utente il posizionamento ottimale.
*   **Cronologia Mosse Dettagliata:** Un log accessibile tramite pulsante mostra ogni singola mossa decisa dall'IA.
*   **Controlli di Gioco:** Pulsanti per resettare la partita, generare nuovi piatti e tornare al menu.

## Logica ASP

La logica definita nei file ASP segue una gerarchia di priorità (implementata tramite *Weak Constraints*) progettata per imitare la strategia logica del gioco:

### Ottimizzazione e Priorità
1.  **La Regola di Completamento (Priorità Massima - Livello 10):** L'IA cercherà sempre, prima di ogni altra cosa, la mossa che permette di completare un piatto portandolo a 6 pezzi di un unico colore.
2.  **La Regola dell'Ordine (Priorità Alta - Livello 5):** Se non può completare, l'IA preferisce spostare i pezzi verso i piatti più "puliti". La mossa viene penalizzata in base a quanti colori diversi ci sono sul piatto di destinazione: meno colori ci sono, più la mossa è considerata vantaggiosa.
3.  **La Regola dell'Estrazione (Priorità Media - Livello 3):** L'IA preferisce estrarre pezzi dai piatti molto misti per purificarli. Viene data priorità allo spostamento di pezzi da piatti che hanno una grande varietà di colori.
4.  **La Regola dell'Accumulo (Priorità Bassa - Livello 1):** Come spareggio finale, se tutte le altre condizioni sono pari, l'IA dà la priorità al piatto che può spostare il maggior numero di pezzi.

### Logica del Suggeritore
Per fornire il suggerimento di piazzamento, l'IA esegue una simulazione: identifica le celle vuote (ID -1), calcola matematicamente le nuove adiacenze che si verrebbero a creare posizionando un piatto in una determinata coordinata e ne valuta le conseguenze strategiche future.

## Logica di Gioco Dettagliata (Il Motore IA)

Il flusso di esecuzione che unisce Java e ASP è il seguente:

1.  **Drop (Java):** L'utente rilascia un piatto. La vista si aggiorna per mostrarlo.
2.  **Avvio del Timer:** Il `gameLoopTimer` (con un tick ogni 250ms) si avvia, innescando il ciclo decisionale dell'IA.
3.  **`runGameLogicStep()` (ad ogni "tick"):**
    *   **Scansione (Java):** Il codice traduce l'intero stato attuale del vassoio (numero di pezzi, colori, piatti vicini) in fatti testuali (es. `plateInfo`, `neighborInfo`).
    *   **Risoluzione (EmbASP/DLV2):** I fatti vengono uniti alle regole del gioco e passati al solver. DLV2 calcola i modelli validi e applica l'ottimizzazione gerarchica per trovare l'**ultimo Answer Set** (l'OPTIMUM).
    *   **Esecuzione (Java):** Il manager estrae la mossa dal risultato e aggiorna fisicamente i modelli e la grafica.
    *   **Terminazione:** Se DLV2 non trova nessuna mossa utile da fare, il timer si ferma e il turno torna al giocatore.

## Come Eseguire il Progetto

1. **Configurazione delle Risorse:**
    *   Nel pannello di progetto di IntelliJ, cliccare con il tasto destro sulla cartella `resources`.
    *   Selezionare **Mark Directory as -> Resources Root**.
