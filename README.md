
## Indice
1.  [Regole del Gioco](#regole-del-gioco)
2.  [Funzionalità Implementate](#funzionalità-implementate)
3.  [L'Intelligenza Artificiale (Logica ASP)](#lintelligenza-artificiale-logica-asp)
4.  [Struttura del Progetto (Architettura MVC)](#struttura-del-progetto-architettura-mvc)
5.  [Logica di Gioco Dettagliata (Il Motore a Timer)](#logica-di-gioco-dettagliata-il-motore-a-timer)
6.  [Come Eseguire il Progetto](#come-eseguire-il-progetto)

## Regole del Gioco

L'obiettivo del gioco è ripulire il vassoio (`Tray`) unendo pezzi di torta dello stesso colore per completare i piatti (`Plate`).

### Elementi di Gioco
*   **Vassoio (Tray):** Una griglia dove i piatti vengono posizionati.
*   **Tavolo (Table):** Un'area superiore che contiene una riserva di nuovi piatti.
*   **Piatto (Plate):** Un contenitore che può ospitare fino a **6 pezzi di torta**. Ogni piatto sul vassoio ha un ID numerico sequenziale (es. Piatto 1, Piatto 2) per essere facilmente identificato.

### Meccanica Principale
1.  **Drag and Drop:** Il giocatore trascina un piatto dal Tavolo e lo rilascia su uno slot vuoto del Vassoio.
2.  **Unione e Reazione a Catena:** Quando un piatto viene posizionato, si scatena una sequenza di eventi gestita dal motore di gioco:
    *   **Fase 1 (Aggregazione Iniziale):** Il piatto appena posizionato (`P_Nuovo`) "attrae" pezzi dello stesso colore dai suoi vicini.
    *   **Fase 2 (Reazioni a Catena):** Il gioco entra in un ciclo in cui, un passo alla volta, l'AI decide la mossa migliore da eseguire.
3.  **Rimozione dei Piatti:** Un piatto viene rimosso dal vassoio quando è **completo** (6 pezzi dello stesso colore) o **vuoto**.

## Funzionalità Implementate
*   **Motore di Gioco Asincrono:** Le reazioni a catena sono gestite da un `javax.swing.Timer` per rendere visibile ogni "micro-mossa" all'utente.
*   **Logica AI con EmbASP:** Le decisioni su quale mossa eseguire sono delegate a un solver **ASP (Answer Set Programming)**.
*   **Cronologia Mosse Dettagliata:** Un pulsante "Cronologia" mostra un log descrittivo di ogni azione avvenuta.
*   **Controlli di Gioco:** Pulsanti per resettare la partita, generare nuovi piatti e tornare al menu.

## Logica ASP

La decisione su quale mossa eseguire è guidata da un insieme di regole logiche definite nel file `cake_rules.asp` e risolte da DLV2.

La strategia dell'AI è la seguente:
1.  **Anti-Loop:** Per evitare scambi infiniti, i pezzi si spostano solo verso un piatto che ha già una concentrazione uguale o maggiore di quel colore.
2.  **Priorità al Completamento:** L'AI dà priorità assoluta alle mosse che portano al completamento di un piatto.
3.  **Ottimizzazione della Scelta:** Se ci sono più mosse valide, l'AI sceglie quella che proviene da un piatto "donatore" con il maggior numero di pezzi, con l'obiettivo di liberare il vassoio più velocemente.


## Logica di Gioco Dettagliata (Il Motore a Timer)

1.  **Drop:** L'utente rilascia un piatto.
2.  **Aggregazione:** Avviene la prima unione di pezzi verso il piatto nuovo.
3.  **Avvio del Timer:** Il `gameLoopTimer` si avvia.
4.  **`runGameLogicStep()` (ad ogni "tick"):**
    *   Converte lo stato del vassoio in "fatti" ASP.
    *   Chiama `EmbaspManager` per chiedere al solver la mossa migliore.
    *   Esegue la mossa restituita dal solver e aggiorna la vista.
    *   Se il solver non restituisce più mosse, il timer si ferma.

## Come Eseguire il Progetto

1. **Configurazione delle Dipendenze:** Assicurarsi che il file `lib/embASP.jar` sia aggiunto come libreria esterna nelle impostazioni del progetto.
2.  **Configurazione delle Risorse:**
    *   Nel pannello di progetto, cliccare con il tasto destro sulla cartella `resources`.
    *   Selezionare **Mark Directory as -> Resources Root**.
    