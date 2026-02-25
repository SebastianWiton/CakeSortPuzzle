% FATTI IN INPUT
%
% plateInfo(ID, Colore, Quantita).
% neighborInfo(ID1, ID2).

% REGOLE INTERMEDIE

% Calcolo del totale dei pezzi su ogni piatto.
totalPieces(ID, Total) :- plateInfo(ID, _, _), #sum{Q, C : plateInfo(ID, C, Q)} = Total.

% Calcolo del numero di colori diversi su un piatto.
numColors(ID, N) :- plateInfo(ID,_,_), N = #count{ C : plateInfo(ID, C, _) }.

% Definizione di quando un piatto ha spazio.
plateHasSpace(ID) :- totalPieces(ID, T), T < 6.

% Mossa strategica di base: c'è un colore in comune e spazio per riceverlo.
strategicMove(DonatorID, ReceiverID, Color) :-
    plateInfo(DonatorID, Color, Qdon), Qdon > 0,
    plateInfo(ReceiverID, Color, Qric), % Il ricevente deve avere lo stesso colore
    plateHasSpace(ReceiverID),          % E deve avere spazio
    neighborInfo(DonatorID, ReceiverID),
    DonatorID != ReceiverID.

% Una mossa è di completamento solo se il piatto ricevente è monocolore
% e la mossa lo porta a 6 pezzi totali.
completionMove(DonatorID, ReceiverID, Color) :-
    strategicMove(DonatorID, ReceiverID, Color),
    numColors(ReceiverID, 1),
    plateInfo(ReceiverID, Color, Qric),
    plateInfo(DonatorID, Color, Qdon),
    Qric + Qdon == 6.

% OUTPUT E VINCOLI

% 1. GENERAZIONE
move(D, R, C) | ignore(D, R, C) :- strategicMove(D, R, C).

% 2. VINCOLI DI NUMERO
% Non farne mai più di una alla volta
:- #count{ D,R,C : move(D,R,C) } > 1.

% Se c'è almeno una mossa strategica possibile, allora devi farne una.
:- strategicMove(_,_,_), #count{ D,R,C : move(D,R,C) } == 0.

% OTTIMIZZAZIONE

% LIVELLO 10: Completamento.
% Completa sempre i piatti se possibile.
:~ move(D, R, C), not completionMove(D, R, C). [1@10, D, R, C]

% LIVELLO 5: La Regola dell'Ordine.
% L'IA preferirà sempre spostare i pezzi verso i piatti più "puliti" (con meno colori misti).
% Se mandi un pezzo su un piatto con 1 colore paghi 1. Su uno con 3 colori paghi 3.
:~ move(D, R, C), numColors(R, ColoriRic). [ColoriRic@5, D, R, C]

% LIVELLO 3: Estrazione.
% Se l'ordine è uguale, preferisci estrarre pezzi dai piatti molto disordinati per pulirli.
% Vogliamo massimizzare i colori del donatore, quindi minimizziamo (6 - ColoriDonatore).
:~ move(D, R, C), numColors(D, ColoriDon). [6-ColoriDon@3, D, R, C]

% LIVELLO 1: Accumulo
% Se tutto il resto è identico, unisci i pezzi al mucchio che è già più grande.
:~ move(D, R, C), plateInfo(R, C, Qric). [6-Qric@1, D, R, C]


% #show move/3.