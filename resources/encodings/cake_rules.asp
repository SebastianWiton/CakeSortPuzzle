% --- FATTI IN INPUT  ---
%
% plateInfo(ID, Colore, Quantita).
% neighborInfo(ID1, ID2).

% --- REGOLE INTERMEDIE  ---

% Calcolo del totale dei pezzi su ogni piatto.
totalPieces(ID, Total) :- plateInfo(ID, _, _), #sum{Q, C : plateInfo(ID, C, Q)} = Total.

% Definizione di quando un piatto ha spazio.
plateHasSpace(ID) :- totalPieces(ID, T), T < 6.

% Una mossa è "strategica" se segue la logica a cascata per evitare loop.
strategicMove(DonatorID, ReceiverID, Color) :-
    plateInfo(DonatorID, Color, Qdon), Qdon > 0,
    Qric = #sum{ Q : plateInfo(ReceiverID, Color, Q) },
    Qric >= Qdon,
    plateHasSpace(ReceiverID),
    neighborInfo(DonatorID, ReceiverID),
    DonatorID != ReceiverID.

% Una mossa è "di completamento".
completionMove(DonatorID, ReceiverID, Color) :-
    strategicMove(DonatorID, ReceiverID, Color),
    Qdon = #sum{Q : plateInfo(DonatorID, Color, Q)},
    Qric = #sum{Q : plateInfo(ReceiverID, Color, Q)},
    totalPieces(ReceiverID, TotRic),
    Qric + Qdon == 6,
    TotRic - Qric == 0.

% --- OTTIMIZZAZIONE ---

% Calcola il numero di colori diversi su un piatto.
numColors(ID, N) :- plateInfo(ID,_,_), N = #count{ C : plateInfo(ID, C, _) }.

% Una mossa è "di purificazione" se rende il piatto DONATORE monocolore
% (o vuoto), perché gli abbiamo tolto l'unico colore diverso che aveva.
purificationMove(DonatorID, ReceiverID, Color) :-
    strategicMove(DonatorID, ReceiverID, Color),
    numColors(DonatorID, 2), % Il donatore aveva esattamente 2 colori.
    plateInfo(DonatorID, Color, _), % Uno dei colori è quello che stiamo spostando.
    % E dopo lo spostamento, tutti i pezzi di quel colore se ne sono andati.
    % Verifichiamo che la quantità di pezzi del colore spostato sia uguale
    % al totale dei pezzi di quel colore sul donatore.
    Qmoved = #sum{Q : plateInfo(DonatorID, Color, Q)},
    totalPieces(ReceiverID, TotRic),
    TotRic + Qmoved <= 6. % Assicuriamoci che ci sia spazio per tutti.

% --- OUTPUT E OTTIMIZZAZIONE ---

% Genera tutte le mosse possibili (candidate)
move(D, R, C) :- strategicMove(D, R, C).

% Livello 2 (massima priorità): Premia le mosse di completamento.
#maximize{ 1@2, D, R, C : move(D, R, C), completionMove(D, R, C) }.

% Livello 1 (priorità intermedia): Premia le mosse che "purificano" il donatore.
#maximize{ 1@1, D, R, C : move(D, R, C), purificationMove(D, R, C) }.

% Livello 0 (priorità più bassa): Come spareggio, massimizza il numero di pezzi
% nel piatto donatore (per svuotarlo prima).
#maximize{ Q@0, D, R, C : move(D, R, C), totalPieces(D, Q) }.


#show move/3.