package battleship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScoreBoard {
    // ==========================================
    // SCOREBOARD (Jackson)
    // ==========================================
    private static final String SCORE_FILE = "scoreboard_local.json";

    public static void saveScore(String player, int points) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<ScoreRecord> scores = loadScores();
        scores.add(new ScoreRecord(player, points));
        try {
            mapper.writeValue(new File(SCORE_FILE), scores);
        } catch (IOException e) {
            System.err.println("Erro ao guardar o scoreboard: " + e.getMessage());
        }
    }

    public static List<ScoreRecord> loadScores() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(SCORE_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, ScoreRecord.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static class ScoreRecord {
        private String player;
        private int points;

        public ScoreRecord() {
        }

        public ScoreRecord(String player, int points) {
            this.player = player;
            this.points = points;
        }

        public String getPlayer() {
            return player;
        }

        public int getPoints() {
            return points;
        }

        @Override
        public String toString() {
            return player + " - " + points + " pts";
        }
    }
}
