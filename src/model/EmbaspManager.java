package model;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPMapper;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class EmbaspManager {

    private final String solverPath;
    private final String encodingResourcePath;

    public EmbaspManager(String solverPath, String encodingResourcePath) {
        this.solverPath = solverPath;
        this.encodingResourcePath = encodingResourcePath;

        try {
            ASPMapper mapper = ASPMapper.getInstance();
            mapper.registerClass(Move.class);
            mapper.registerClass(Place.class);
            mapper.registerClass(CellaInfo.class);
            mapper.registerClass(PlateInfo.class);
            mapper.registerClass(PiattoDaInserireInfo.class);
            mapper.registerClass(NeighborInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Move getNextMove(List<Object> facts) {
        return (Move) solve(facts, Move.class);
    }

    public Place getBestPlacement(List<Object> facts) {
        return (Place) solve(facts, Place.class);
    }

    private Object solve(List<Object> javaFacts, Class<?> targetClass) {
        Handler handler = new DesktopHandler(new DLV2DesktopService(solverPath));

        InputProgram encoding = new ASPInputProgram();
        try (InputStream is = getClass().getResourceAsStream(encodingResourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String rules = reader.lines().collect(Collectors.joining("\n"));
            encoding.setPrograms(rules);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        handler.addProgram(encoding);

        InputProgram facts = new ASPInputProgram();
        facts.addProgram("\n");
        for (Object fact : javaFacts) {
            if (fact instanceof PlateInfo) {
                PlateInfo p = (PlateInfo) fact;
                // Costruiamo la stringa esatta: plateInfo(1, "red", 2).
                facts.addProgram("plateInfo(" + p.id + ", \"" + p.color + "\", " + p.quantity + ").");
            }
            else if (fact instanceof NeighborInfo) {
                NeighborInfo n = (NeighborInfo) fact;
                facts.addProgram("neighborInfo(" + n.id1 + ", " + n.id2 + ").");
            }
        }
        System.out.println("--- INPUT INVIATO A DLV ---");
        System.out.println(facts.getPrograms());
        System.out.println("-------------------------");


        handler.addProgram(facts);

        Output output = handler.startSync();
        System.out.println("ERRORI DLV: " + output.getErrors());
        System.out.println("OUTPUT DLV: " + output.getOutput());
        AnswerSets answerSets = (AnswerSets) output;

        if (answerSets.getAnswersets().isEmpty()) {
            System.out.println("DEBUG (EmbaspManager): Nessun Answer Set trovato.");
            return null;
        }

        try {
            List<AnswerSet> sets = answerSets.getAnswersets();
            AnswerSet optimalSet = sets.get(sets.size() - 1);

            // Debug: Stampa cosa ha pensato l'IA
            System.out.println("PENSIERO IA: " + String.join(", ", optimalSet.getAnswerSet()));

            for (Object obj : optimalSet.getAtoms()) {
                if (targetClass.isInstance(obj)) {
                    return targetClass.cast(obj);
                }
            }
            // Se arriviamo qui, c'è un answer set ma nessuna mossa
            System.out.println("DEBUG (EmbaspManager): Trovato Answer Set ma nessuna mossa (Move).");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}