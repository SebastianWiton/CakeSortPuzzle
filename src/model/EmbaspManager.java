package model;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
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
    private final String[] encodingResourcePaths;

    public EmbaspManager(String solverPath, String[] encodingResourcePath) {
        this.solverPath = solverPath;
        this.encodingResourcePaths = encodingResourcePath;

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
        return (Move) solve(facts, Move.class, this.encodingResourcePaths[0]);
    }

    public Place getBestPlacement(List<Object> facts) {
        return (Place) solve(facts, Place.class, this.encodingResourcePaths[1]);
    }

    private Object solve(List<Object> javaFacts, Class<?> targetClass, String encodingResourcePath) {
        // Ad ogni chiamata viene creato un handler nuovo per evitare problemi di stato.
        Handler handler = new DesktopHandler(new DLV2DesktopService(solverPath));

        OptionDescriptor allModelsOption = new OptionDescriptor("-n 0 ");
        handler.addOption(allModelsOption);


        // Programma per le regole (encoding) caricato ad ogni chiamata
        InputProgram encoding = new ASPInputProgram();
        try (InputStream is = getClass().getResourceAsStream(encodingResourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String rules = reader.lines().collect(Collectors.joining("\n"));
            encoding.setPrograms(rules);
        } catch (Exception e) {
            System.err.println("ERRORE CRITICO: Impossibile caricare le regole ASP da: " + encodingResourcePath);
            e.printStackTrace();
            return null;
        }
        handler.addProgram(encoding);


        InputProgram facts = new ASPInputProgram();
        for (Object fact : javaFacts) {
            try {
                facts.addObjectInput(fact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        handler.addProgram(facts);

        Output output = handler.startSync();
        System.out.println("RAW OUTPUT SOLVER: " + output.getOutput());

        AnswerSets answerSets = (AnswerSets) output;

        for (Object x : answerSets.getOptimalAnswerSets()) {
            System.out.println(x);
        }


        if (answerSets.getAnswersets().isEmpty()) {
            System.out.println("DEBUG (EmbaspManager): Il solver non ha restituito nessuna soluzione (Answer Set vuoto).");
            return null;
        }

        try {
            // optimalSet è una lista di atomi, stringhe restituite dai solver
            AnswerSet optimalSet = answerSets.getOptimalAnswerSets().get(0);
            for (Object obj : optimalSet.getAtoms()) {
                // ci interessa solo l'oggetto specifico richiesto (move o place)
                if (targetClass.isInstance(obj)) {
                    return targetClass.cast(obj);
                }
            }
            System.out.println("DEBUG (EmbaspManager): Trovato un Answer Set, ma non conteneva un oggetto di tipo " + targetClass.getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}