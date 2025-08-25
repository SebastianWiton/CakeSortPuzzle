

% --- FATTI IN INPUT  ---
%
% plateInfo(ID, Colore, Quantita).
%   - Descrive quanti pezzi di un certo colore ci sono su un piatto.
%
% neighborInfo(ID1, ID2).
%   - Indica che il piatto ID1 è vicino al piatto ID2.


% --- REGOLE INTERMEDIE  ---

% Un piatto ha spazio se il totale dei suoi pezzi è minore di 6.
% Calcolo del totale dei pezzi su ogni piatto.
totalPieces(ID, Total) :- plateInfo(ID, _, _), #sum{Q, C : plateInfo(ID, C, Q)} = Total.
% Definizione di quando un piatto ha spazio.
plateHasSpace(ID) :- totalPieces(ID, T), T < 6.

% Una mossa è "considerabile" se soddisfa le condizioni di base per uno spostamento.
considerableMove(DonatorID, ReceiverID, Color) :-
    plateInfo(DonatorID, Color, Qdon), Qdon > 0, % Il donatore deve avere pezzi di quel colore.
    plateInfo(ReceiverID, Color, Qric), Qric > 0, % Anche il ricevente deve avere pezzi dello stesso colore.
    plateHasSpace(ReceiverID),                   % Il ricevente deve avere spazio disponibile.
    neighborInfo(DonatorID, ReceiverID),         % I due piatti devono essere vicini.
    DonatorID != ReceiverID.                     % Un piatto non può donare a se stesso.

% Una mossa è "strategica" se segue la  logica a cascata per evitare loop.
% I pezzi si spostano solo verso un piatto con una concentrazione uguale o maggiore di quel colore.
strategicMove(DonatorID, ReceiverID, Color) :-
    considerableMove(DonatorID, ReceiverID, Color),
    plateInfo(DonatorID, Color, Qdon),
    plateInfo(ReceiverID, Color, Qric),
    Qric >= Qdon.

% Una mossa è "di completamento" se riempie completamente un piatto con pezzi dello stesso colore.
completionMove(DonatorID, ReceiverID, Color) :-
    strategicMove(DonatorID, ReceiverID, Color),
    plateInfo(ReceiverID, Color, Qric),
    plateInfo(DonatorID, Color, Qdon),
    Qric + Qdon == 6. % Il piatto ricevente avrà esattamente 6 pezzi di quel colore dopo la mossa.

% --- SELEZIONE DELLA MOSSA MIGLIORE (OTTIMIZZAZIONE) ---

% Priorità assoluta alle mosse che completano un piatto.
is_completion_possible :- completionMove(_, _, _).

% Una mossa è ottimale se è una mossa di completamento.
bestMove(D, R, C) :- completionMove(D, R, C).

% Se non ci sono mosse di completamento, allora una mossa ottimale è una qualsiasi mossa strategica.
bestMove(D, R, C) :- strategicMove(D, R, C), not is_completion_possible.


% --- OUTPUT ---
%
% Scegli esattamente una mossa tra tutte le bestMove disponibili
1 { move(D, R, C) : bestMove(D, R, C) } 1.
% Se ci sono più bestMove, scegli quella il cui piatto donatore è il più pieno
#maximize{ Q@1, D, R, C : move(D, R, C), totalPieces(D, Q) }.

% Dice al solver di mostrare solo il risultato 'move/3' e di nascondere tutti i fatti intermedi.
#show move/3.