% --- COSTANTI ---
quantity(1..6).
colore(C):- plateInfo(_,C,_).

% --- AUSILIARI ---
% piatti che sono sul tavolo
piazzati(P,C,Q) :- plateInfo(P,C,Q), cella(_,_,P).
daInserire(P,C,Q) :- piattoDaInserire(P), plateInfo(P,C,Q).

% piatti che hanno colore in comune con quello che devo inserire
piattiColoreComune(P2,Colore) :-
    daInserire(P1,Colore,_),
    piazzati(P2,Colore,_),
    P1 != P2.

% il numero totale delle fette che sono sul piatto generico P
totaleFettePiatto(P,S) :-
    plateInfo(P,_,_),
    S=#sum{F,C : plateInfo(P,C,F)}.

% il numero di posti disponibili sul piatto generico P
spazioPiatto(P,S) :-
    totaleFettePiatto(P,T),
    S= 6 - T.

% crezione della tabella delle adiacenze
adiacente(X, Y, X + 1, Y) :- cella(X,Y,_), cella(X + 1,Y,_).
adiacente(X, Y, X - 1, Y) :- cella(X,Y,_), cella(X - 1,Y,_).
adiacente(X, Y, X, Y + 1) :- cella(X,Y,_), cella(X,Y + 1,_).
adiacente(X, Y, X, Y - 1) :- cella(X,Y,_), cella(X,Y - 1,_).
% Adiacenze Diagonali
adiacente(X, Y, X + 1, Y + 1) :- cella(X,Y,_), cella(X + 1,Y + 1,_).
adiacente(X, Y, X - 1, Y - 1) :- cella(X,Y,_), cella(X - 1,Y - 1,_).
adiacente(X, Y, X + 1, Y - 1) :- cella(X,Y,_), cella(X + 1,Y - 1,_).
adiacente(X, Y, X - 1, Y + 1) :- cella(X,Y,_), cella(X - 1,Y + 1,_).

% calcolo delle celle adiacenti alla mossa scelta
neighbour(P) :- place(X,Y,_), adiacente(X,Y,X1,Y1), cella(X1,Y1,P), P != -1.

piattiScambiabili(P) :- piattoDaInserire(P).
piattiScambiabili(P2) :- piattoDaInserire(P), neighbour(P2), piattiColoreComune(P2,_), P!=P2.

% mi salvo l'indice di entropia, così da poter fare una diseguaglianza postuma
monocromaticoPre(P) :- piattoDaInserire(P), #count{C : daInserire(P,C,Q), Q>0} = 1.
monocromaticoPre(P2) :- neighbour(P2), #count{C : plateInfo(P2,C,Q), Q>0} = 1.

% --- GUESS & CHECK ---
% Genero tutte i possibili posizionamenti del piatto
place(X,Y,P) | noPlace(X,Y,P) :- piattoDaInserire(P), cella(X,Y,-1).
% Solo una scelta alla volta è permessa
:- #count{P,X,Y:place(X,Y,P)} != 1.

% Genero tutti i possibili spostamenti di fette fra il piatto appena piazzato ed i suoi neighbour
spostamentoPossibile(Donatore,Ricevente,Colore,X):-
    piattiScambiabili(Donatore),
    piattiScambiabili(Ricevente),
    plateInfo(Donatore,Colore,Q),
    plateInfo(Ricevente,Colore,_),
    Donatore != Ricevente,
    quantity(X),
    X <= Q,
    spazioPiatto(Ricevente,S),
    X <= S.

% --- Guess & Check sullo spostamento
sposto(D,R,C,X) | noSposto(D,R,C,X) :- spostamentoPossibile(D,R,C,X).
:- piattoDaInserire(P), sposto(D,R,_,_), D!=P,R!=P.

% il piatto P non può donare al piatto P1 due numeri differenti di torte dello stesso colore
:- sposto(D,R,C,X), sposto(D,R,C,X1), X1 != X.
:- #count{D,R,C,X:sposto(D,R,C,X)} = 0.

% il piatto ricevente non può ricevere più di quanto spazio ha
:- sposto(_,R,_,_),
   spazioPiatto(R,S),
   SommaRicevuta = #sum{X,D,C : sposto(D,R,C,X)},
   SommaDonata = #sum{X,D,C : sposto(R,D,C,X)},
   SommaRicevuta > SommaDonata+S.

% aleatorio per somma ricevuto | Questo è stato creato perché quando si andava a lavora con somme direttamente su sposto() venivano creati come se ci fosse una somma iniziale, una intermedia ed una finale.
rSposto(D,P,C,X) :- sposto(D,P,C,X).
dSposto(D,P,C,X) :- sposto(D,P,C,X).

% Genero i nuovi piatti in base agli spostamenti
ricevuto(P,C,Q) :-
    #count{L,M,N,O : rSposto(L,M,N,O)} != 0,
    piattiScambiabili(P),
    plateInfo(P,C,_),
    Q = #sum{X,D:rSposto(D,P,C,X)}.

donato(P,C,Q) :-
    piattiScambiabili(P),
    plateInfo(P,C,_),
    Q = #sum{X,R:dSposto(P,R,C,X)},
    #count{L,M,N,O : rSposto(L,M,N,O)} != 0.

nuovoPiatto(P,C,QF) :-
    ricevuto(P,C,QR),
    donato(P,C,QD),
    plateInfo(P,C,QO),
    QF = QO + QR - QD.

% scarto tutti i risultati che hanno in se l'over donazione
:- nuovoPiatto(_,_,QF), QF<0.

% mossa fantasma: Svuoto il mio piatto
fantasma(P):- piattoDaInserire(P), nuovoPiatto(P,_,_), #sum{Q,C : nuovoPiatto(P,C,Q)} = 0.

% completo un piatto, 6 fette di un solo colore
vuoto(P) :- nuovoPiatto(P,_,_), #sum{F,C : nuovoPiatto(P,C,F)} = 0.
monocromaticoPost(P) :- nuovoPiatto(P,_,_), not vuoto(P), #count{C : nuovoPiatto(P,C,Q), Q>0} = 1.
completo(P) :- nuovoPiatto(P,_,_), monocromaticoPost(P), #sum{Q,C:nuovoPiatto(P,C,Q)} = 6.

% --- Weak Constraint ---
% @5 Completa un piatto
:~ X = #count{M : completo(M)}, T = 20-X. [T@5]

% @4 Penalizzo l'aumento dell'entropia della monocromia
% Pago in base ad un delta che confronta l'entropia di colore dei piatti
:~ X = #count{M : monocromaticoPre(M)},
   Y = #count{N : monocromaticoPost(N)},
   T = 20 - (Y - X).   [T@4]

% @3 Valutazione e penalizzazione dei piatti troppo misti
totaleFetteNuovoPiatto(P,S) :- nuovoPiatto(P,_,_), S=#sum{F,C : nuovoPiatto(P,C,F)}.
maxColore(P,N):- nuovoPiatto(P,_,_), N = #max{Q,C : nuovoPiatto(P,C,Q)}.
% normalizzo il valore con *100/N così da ottenere una percentuale e dare valore alla distribuzione. Altrimenti 1yellow,1red avrebbe lo stesso peso di 3yellow,1red. rendendo a percentuale si diventa un po' più precisi.
score(P,S) :- maxColore(P,M), totaleFettePiatto(P,N), N > 0, S = (N-M)*100/N.
:~ nuovoPiatto(P,_,_), score(P,S). [S@3,P]

% @2 Mossa fantasma, cella risparmiata
:~ S = #count{X : fantasma(X)}, T=1-S. [T@2]

% @1 Massimizzare i Neighbour
:~ #count{X : piattiScambiabili(X)} = T, Z = 9-T. [Z@1]