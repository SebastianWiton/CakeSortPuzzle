
Indice

Regole del Gioco

Funzionalità Implementate

L'Intelligenza Artificiale (Logica ASP)

Struttura del Progetto (Architettura MVC)

Logica di Gioco Dettagliata (Il Motore IA)

Come Eseguire il Progetto

Regole del Gioco

L'obiettivo del gioco è ripulire il vassoio (Tray) spostando e unendo pezzi di torta dello stesso colore per completare i piatti (Plate).

Elementi di Gioco

Vassoio (Tray): Una griglia dove i piatti vengono posizionati.

Tavolo (Table): Un'area superiore che contiene i piatti "in attesa" che possono essere giocati.

Piatto (Plate): Un contenitore che può ospitare fino a 6 pezzi di torta. Ogni piatto sul vassoio ha un ID numerico sequenziale per essere facilmente identificato dal solver.

Meccanica Principale

Drag and Drop: Il giocatore trascina un piatto dal Tavolo e lo rilascia su uno slot vuoto del Vassoio.

Risoluzione IA: Non appena il piatto tocca la griglia, il controllo passa interamente all'Intelligenza Artificiale. Il gioco entra in un ciclo (gestito da un Timer) in cui, un passo alla volta, l'IA analizza lo stato della plancia e decide la mossa migliore da eseguire per unire i colori.

Rimozione dei Piatti: Un piatto viene rimosso dal vassoio quando è completo (6 pezzi dello stesso colore) o viene completamente svuotato dai trasferimenti.

Funzionalità Implementate

Logica 100% AI-Driven con EmbASP: Tutti gli spostamenti dei pezzi (sia le reazioni immediate al drop che le catene successive) sono calcolati dal solver DLV2 tramite regole ASP. Nessuna mossa è "hardcoded" in Java.

Gestione Avanzata Modelli Ottimi: Il sistema Java è configurato per estrarre sempre l'ultimo Answer Set restituito da DLV2, garantendo l'applicazione rigorosa dei Weak Constraints.

Cronologia Mosse Dettagliata: Un log accessibile tramite pulsante mostra ogni singola mossa decisa dall'IA.

Suggeritore (Hint): Una funzione che interroga l'IA per suggerire al giocatore lo slot migliore in cui piazzare un nuovo piatto.

L'Intelligenza Artificiale (Logica ASP)

Il "cervello" del gioco risiede nel file cake_rules.asp. La logica non si limita a unire pezzi a caso, ma segue una gerarchia di priorità (implementata tramite Weak Constraints) progettata per imitare la strategia di un giocatore umano esperto:

La Regola Aurea - Completamento (Priorità Massima - Livello 10):

L'IA cercherà sempre, prima di ogni altra cosa, la mossa che permette di chiudere un piatto portandolo a 6 pezzi di un unico colore.

La Regola dell'Ordine (Priorità Alta - Livello 5):

Se non può completare, l'IA preferisce spostare i pezzi verso i piatti più "puliti". La penalità della mossa è direttamente proporzionale al numero di colori diversi presenti sul piatto ricevente, incoraggiando l'IA a non "sporcare" piatti quasi puri.

La Regola dell'Estrazione (Priorità Media - Livello 3):

A parità di ordine del ricevente, l'IA preferisce "estrarre" pezzi dai piatti molto disordinati. Viene premiato lo spostamento da piatti che hanno un'alta varietà di colori, per cercare di purificarli.

La Regola dell'Accumulo (Priorità Bassa - Livello 1):

Come spareggio finale, l'IA unisce i pezzi spostandoli verso il mucchio che è già più grande (minimizzando lo spazio vuoto sul ricevente).

Logica di Gioco Dettagliata (Il Motore IA)

Il flusso di esecuzione che unisce Java e ASP è il seguente:

Drop (Java): L'utente rilascia un piatto. La vista si aggiorna.

Avvio del Timer: Il gameLoopTimer (tick ogni 250ms) si avvia, innescando il ciclo decisionale.

runGameLogicStep() (ad ogni "tick"):

Scansione (Java): Traduce l'intero stato attuale del vassoio (pezzi, colori, adiacenze) in fatti testuali (es. plateInfo, neighborInfo).

Risoluzione (EmbASP/DLV2): I fatti vengono uniti all'encoding e passati al solver. DLV2 calcola i modelli stabili e applica l'ottimizzazione.

Esecuzione (Java): Il manager estrae la mossa ottima (Move) dall'ultimo Answer Set. Java esegue fisicamente lo spostamento dei pezzi e rimuove i piatti vuoti/completi.

Terminazione: Se DLV2 restituisce un set vuoto (nessuna mossa vantaggiosa possibile), il timer si ferma e il turno torna al giocatore.

Come Eseguire il Progetto

Configurazione delle Risorse:

Nel pannello di progetto, cliccare con il tasto destro sulla cartella resources.

Selezionare Mark Directory as -> Resources Root.

Eseguibile DLV2:

Assicurarsi che il file dlv2.exe sia presente nella cartella lib e correttamente referenziato in EmbaspManager.