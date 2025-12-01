package model;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
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

    private final Handler handler;
    private final InputProgram encoding;

    public EmbaspManager(String solverPath, String encodingResourcePath) {
        this.handler = new DesktopHandler(new DLV2DesktopService(solverPath));
        this.encoding = new ASPInputProgram();

        // Carica le regole ASP come risorsa interna
        try (InputStream is = getClass().getResourceAsStream(encodingResourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String rules = reader.lines().collect(Collectors.joining("\n"));
            encoding.setPrograms(rules);
            handler.addProgram(encoding);

        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle regole ASP da: " + encodingResourcePath);
            e.printStackTrace();
        }
    }

    public Move getNextMove(List<Object> facts) {
        InputProgram factsProgram = new ASPInputProgram();
        for (Object fact : facts) {
            try {
                factsProgram.addObjectInput(fact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        handler.addProgram(factsProgram);
        Output output = handler.startSync();
        handler.removeProgram(factsProgram);

        AnswerSets answerSets = (AnswerSets) output;

        if (!answerSets.getAnswersets().isEmpty()) {
            try {
                AnswerSet optimalSet = answerSets.getAnswersets().get(0);
                for (Object obj : optimalSet.getAtoms()) {
                    if (obj instanceof Move) {
                        return (Move) obj;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    public Place getBestPlacement(List<Object> facts) {
        InputProgram factsProgram = new ASPInputProgram();
        for (Object fact : facts) {
            try {
                factsProgram.addObjectInput(fact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        handler.addProgram(factsProgram);
        Output output = handler.startSync();
        handler.removeProgram(factsProgram);

        AnswerSets answerSets = (AnswerSets) output;
        if (!answerSets.getAnswersets().isEmpty()) {
            try {
                AnswerSet optimalSet = answerSets.getAnswersets().get(0);
                for (Object obj : optimalSet.getAtoms()) {
                    if (obj instanceof Place) {
                        return (Place) obj;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
}