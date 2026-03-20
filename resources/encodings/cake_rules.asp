% SEZIONE 1: ANALISI DEI PIATTI E REGOLE DI BASE

% Calcola i pezzi totali su un piatto sommando le quantità di ogni colore
totalPieces(ID, S) :- plateInfo(ID, _, _), S = #sum{Q, C : plateInfo(ID, C, Q)}.

% Conta quanti colori diversi sono presenti su un piatto
numColors(ID, N) :- plateInfo(ID, _, _), #count{C : plateInfo(ID, C, _)} = N.

% Verifica se un piatto ha almeno uno spazio libero (massimo 6 pezzi)
plateHasSpace(ID) :- totalPieces(ID, S), S < 6.

% Definisce una mossa valida tra piatti vicini con lo stesso colore e spazio libero
strategicMove(D, R, C) :-
    plateInfo(D, C, Qdon), Qdon > 0,
    plateInfo(R, C, Qric),
    totalPieces(R, S), S < 6,
    neighborInfo(D, R),
    D != R.

% Definisce se una mossa porta al completamento di un piatto (6 pezzi dello stesso colore)
completionMove(D, R, C) :-
    strategicMove(D, R, C),
    numColors(R, 1),
    plateInfo(R, C, Qric),
    plateInfo(D, C, Qdon),
    Qric + Qdon == 6.

% SEZIONE 2: MOTORE DI GIOCO (Reazioni a catena)

% Per ogni mossa possibile, l'IA sceglie se eseguirla o ignorarla
move(D, R, C) | ignore(D, R, C) :- strategicMove(D, R, C).

% Vincoli: esegui esattamente una mossa alla volta se ne esistono di valide
:- #count{D,R,C : move(D,R,C)} > 1.
:- strategicMove(_,_,_), #count{D,R,C : move(D,R,C)} == 0.

% OTTIMIZZAZIONE DEL MOTORE DI GIOCO

% LIVELLO 10: Priorità massima nel completare i piatti da 6 pezzi
:~ move(D, R, C), not completionMove(D, R, C). [1@10, D, R, C]

% LIVELLO 5: Preferenza per lo spostamento verso piatti con pochi colori (più "puliti")
:~ move(D, R, C), numColors(R, N). [N@5, D, R, C]

% LIVELLO 3: Preferenza nell'estrarre pezzi dai piatti più disordinati (con tanti colori)
:~ move(D, R, C), numColors(D, N). [6-N@3, D, R, C]

% LIVELLO 1: Massimizziamo i pezzi che spostiamo dal donatore
:~ move(D, R, C), plateInfo(D, C, Qdon). [6-Qdon@1, D, R, C]

% SEZIONE 3: SUGGERITORE

% Prova a posizionare un piatto del tavolo in una delle celle vuote (-1)
place(X, Y, ID) | skipPlace(X, Y, ID) :- piattoDaInserire(ID), cella(X, Y, -1).

% Obbliga l'IA a suggerire esattamente una posizione di gioco
:- piattoDaInserire(_), #count{X, Y, ID : place(X, Y, ID)} != 1.

% Definizione delle adiacenze sulla griglia per simulare il futuro
adiacente(X, Y, X+1, Y) :- cella(X,Y,_), cella(X+1,Y,_).
adiacente(X, Y, X-1, Y) :- cella(X,Y,_), cella(X-1,Y,_).
adiacente(X, Y, X, Y+1) :- cella(X,Y,_), cella(X,Y+1,_).
adiacente(X, Y, X, Y-1) :- cella(X,Y,_), cella(X,Y-1,_).

% Calcola quali vicini avrebbe il piatto se venisse messo nella cella scelta
futureNeighbor(ID, NID) :- place(X,Y,ID), adiacente(X,Y,NX,NY), cella(NX,NY,NID), NID != -1.
futureNeighbor(NID, ID) :- place(X,Y,ID), adiacente(X,Y,NX,NY), cella(NX,NY,NID), NID != -1.

% Valuta le mosse che si sbloccherebbero dopo il piazzamento simulato
potentialMove(D, R, C) :-
    plateInfo(D,C,Q), Q>0, plateInfo(R,C,_), totalPieces(R,S), S<6,
    futureNeighbor(D,R), D != R.

% Verifica se il posizionamento suggerito permetterebbe un completamento immediato
potentialCompletion(D, R, C) :-
    potentialMove(D,R,C), numColors(R,1), plateInfo(R,C,Qr), plateInfo(D,C,Qd), Qr+Qd >= 6.

% OTTIMIZZAZIONE DEL SUGGERITORE

% LIVELLO 3: Penalità se la cella scelta non sblocca nessuna mossa utile
:~ place(X,Y,ID), #count{D,R,C : potentialMove(D,R,C)} == 0. [20@3, X, Y]

% LIVELLO 2: Preferenza per le celle che portano a un completamento futuro
:~ place(X,Y,ID), #count{D,R,C : potentialCompletion(D,R,C)} == 0. [10@2, X, Y]

% LIVELLO 1: Preferenza per le celle che uniscono pezzi a gruppi già grandi
:~ potentialMove(D, R, C), plateInfo(R, C, Q). [6-Q@1, D, R, C]

% --- OUTPUT ---
% #show move/3.
% #show place/3.