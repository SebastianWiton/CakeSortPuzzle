Cake Sort Puzzle 3D


Indice

    Regole del Gioco

    Funzionalità Implementate

    Sviluppi Futuri (EmbASP)

    Struttura del Progetto (Architettura MVC)

        Model 

        View 

        Controller 

    Logica di Gioco Dettagliata (Il Motore a Timer)

    Come Eseguire il Progetto

Regole del Gioco

L'obiettivo del gioco è ripulire il vassoio (Tray) unendo pezzi di torta dello stesso colore per completare i piatti (Plate).
Elementi di Gioco

    Vassoio (Tray): Una griglia dove i piatti vengono posizionati. Alcuni slot possono essere già occupati da piatti all'inizio della partita.

    Tavolo (Table): Un'area superiore che contiene una riserva di nuovi piatti (con un massimo di 4 pezzi) da cui il giocatore può attingere.

    Piatto (Plate): Un contenitore che può ospitare fino a un massimo di 6 pezzi di torta. Ogni piatto sul vassoio ha un ID numerico sequenziale per essere facilmente identificato.

    Pezzo di Torta (CakePiece): Un pezzo di torta di un colore specifico (es. giallo, rosa, viola).

Meccanica Principale

    Drag and Drop: Il giocatore trascina un piatto dal Tavolo e lo rilascia su uno slot vuoto del Vassoio. A questo punto, al piatto viene assegnato un nuovo ID sequenziale.

    Unione e Reazione a Catena: Quando un piatto viene posizionato, si scatena una sequenza di eventi gestita da un motore di gioco passo-passo:

        Fase 1 (Aggregazione Iniziale): Il piatto appena posizionato (P_Nuovo) "attrae" pezzi dello stesso colore da tutti i suoi vicini.

        Fase 2 (Reazioni a Catena): Il gioco entra in un ciclo in cui, un passo alla volta:

            Un piatto (P_Donatore) sposta i suoi pezzi su un piatto vicino (P_Ricevente) se quest'ultimo ha già pezzi dello stesso colore e ha spazio.

            Logica Anti-Loop: Per evitare scambi infiniti, lo spostamento avviene solo se il P_Ricevente ha un numero di pezzi di quel colore maggiore o uguale a quelli del P_Donatore. Questo crea un flusso a "cascata" verso i piatti che stanno accumulando un colore.

    Rimozione dei Piatti: Un piatto viene rimosso dal vassoio quando:

        È Completo: Contiene 6 pezzi dello stesso colore.

        È Vuoto: Ha donato tutti i suoi pezzi e rimane vuoto.

    Trasferimento Parziale: Se un piatto donatore ha più pezzi di quanti ne possa ricevere il piatto ricevente, solo il numero necessario di pezzi viene trasferito per completarlo o riempirlo.

Funzionalità Implementate

    Motore di Gioco Asincrono: Le reazioni a catena sono gestite da un javax.swing.Timer per evitare di bloccare l'interfaccia e per rendere visibile ogni "micro-mossa" all'utente.

    Cronologia Mosse: Un pulsante "Cronologia" permette di aprire una finestra di dialogo che mostra un log dettagliato di ogni azione avvenuta (piazzamento, spostamenti, rimozioni), descrivendo il contenuto dei piatti coinvolti.

    Controlli di Gioco: Pulsanti per resettare la partita, generare nuovi piatti quando il tavolo è vuoto, e tornare al menu principale.

Sviluppi Futuri (EmbASP)

Attualmente, la logica di gioco è implementata interamente in Java. La struttura del progetto è predisposta per l'integrazione con EmbASP per gestire le regole di unione e le reazioni a catena tramite Answer Set Programming (ASP).

    EmbaspManager.java: Classe vuota, pronta per contenere la logica di interazione con il solver ASP.

    encodings/cake_rules.asp: File vuoto, destinato a contenere i fatti e le regole ASP che definiranno il comportamento del gioco.

Bisogna sostituire la logica di GameLogic.java e GameController.java con chiamate al solver ASP, passando lo stato attuale del vassoio come un insieme di fatti e ricevendo le mosse da eseguire come output.
Struttura del Progetto (Architettura MVC)

Il progetto segue il pattern Model-View-Controller per garantire una chiara separazione delle responsabilità.
Model 

    Plate.java: Rappresenta i dati di un piatto. Contiene una lista di CakePiece, un ID tecnico interno e un displayId per l'utente.

    CakePiece.java: Modella un pezzo di torta, definito dal suo colore.

    GameLogic.java: Contiene la logica pura Java per lo spostamento dei pezzi.

View 

    AppFrame.java: Finestra principale (JFrame) con CardLayout.

    GamePanel.java: Contiene TablePanel, TrayPanel e i pulsanti di controllo.

    PlateComponent.java: JPanel personalizzato che disegna un piatto e i suoi pezzi tramite paintComponent.

Controller 

    GameController.java: Il coordinatore centrale che gestisce il Drag & Drop, il Timer per la logica di gioco, e la cronologia delle mosse.

Logica di Gioco Dettagliata (Il Motore a Timer)

Il flusso di eventi dopo un'azione dell'utente è gestito per garantire chiarezza visiva:

    Drop: L'utente rilascia un PlateComponent.

    handleDrop(): Al piatto viene assegnato un nuovo displayId e viene posizionato.

    processGameLogic(): Viene eseguita la prima fase di "aggregazione" (i vicini donano al piatto nuovo).

    Avvio del Timer: Il gameLoopTimer viene avviato.

    runGameLogicStep() (ad ogni "tick"):

        Cerca una e una sola azione possibile (spostamento o rimozione).

        Se la trova, la esegue, la registra nel log, e aggiorna la vista.

        Se non trova azioni, la catena di reazioni è finita e il timer viene fermato.

Come Eseguire il Progetto

    Configurazione Cruciale: Verificare che la cartella resources sia marcata come "Resources Root" nelle impostazioni del progetto dell'IDE. Questo permette al programma di caricare correttamente immagini e suoni.

    Eseguire il metodo main nella classe Main.java.