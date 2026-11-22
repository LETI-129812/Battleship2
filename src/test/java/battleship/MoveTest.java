package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários da classe {@link Move}.
 * <p>
 * Os testes de saída "verbose" assumem Game.NUMBER_SHOTS == 3 (3 tiros por jogada).
 */
class MoveTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;
    private List<IPosition> shots;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        shots = List.of(mock(IPosition.class), mock(IPosition.class), mock(IPosition.class));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Único ponto a adaptar se o record ShotResult tiver outra assinatura. */
    private static IGame.ShotResult result(boolean valid, boolean repeated, IShip ship, boolean sunk) {
        return new IGame.ShotResult(valid, repeated, ship, sunk);
    }

    private static IShip ship(String category) {
        IShip s = mock(IShip.class);
        when(s.getCategory()).thenReturn(category);
        return s;
    }

    private static IGame.ShotResult miss() {
        return result(true, false, null, false);
    }

    private static IGame.ShotResult repeatedShot() {
        return result(true, true, null, false);
    }

    private static IGame.ShotResult outside() {
        return result(false, false, null, false);
    }

    private static IGame.ShotResult hit(IShip ship) {
        return result(true, false, ship, false);
    }

    private static IGame.ShotResult sunk(IShip ship) {
        return result(true, false, ship, true);
    }

    private JsonNode json(IGame.ShotResult... rs) throws Exception {
        return mapper.readTree(new Move(1, shots, List.of(rs)).processEnemyFire(false));
    }

    /** Devolve a linha escrita no modo verbose (a primeira linha da consola). */
    private String verboseLine(IGame.ShotResult... rs) {
        capturedOut.reset();
        new Move(1, shots, List.of(rs)).processEnemyFire(true);
        return capturedOut.toString(StandardCharsets.UTF_8).lines().findFirst().orElse("").stripTrailing();
    }

    // ------------------------------------------------------------------
    // Construtor, getters e toString
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Construtor e getters devolvem os valores recebidos")
    void testGetters() {
        List<IGame.ShotResult> results = List.of(miss(), miss(), miss());
        Move move = new Move(5, shots, results);

        assertEquals(5, move.getNumber());
        assertSame(shots, move.getShots());
        assertSame(results, move.getShotResults());
    }

    @Test
    @DisplayName("toString mostra número da jogada e número de tiros e resultados")
    void testToString() {
        Move move = new Move(2, shots, List.of(miss(), miss()));

        assertEquals("Move{number=2, shots=3, results=2}", move.toString());
    }

    // ------------------------------------------------------------------
    // processEnemyFire: JSON devolvido
    // ------------------------------------------------------------------

    @Test
    @DisplayName("JSON: três tiros na água")
    void testJsonAllMisses() throws Exception {
        JsonNode j = json(miss(), miss(), miss());

        assertEquals(3, j.get("validShots").asInt());
        assertEquals(3, j.get("missedShots").asInt());
        assertEquals(0, j.get("repeatedShots").asInt());
        assertEquals(0, j.get("outsideShots").asInt());
        assertEquals(0, j.get("sunkBoats").size());
        assertEquals(0, j.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON: tiros inválidos são ignorados e contam como exteriores")
    void testJsonInvalidShotsAreOutside() throws Exception {
        JsonNode j = json(outside(), outside(), outside());

        assertEquals(0, j.get("validShots").asInt());
        assertEquals(0, j.get("repeatedShots").asInt());
        assertEquals(3, j.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("JSON: mistura de tiro exterior, repetido e na água")
    void testJsonOutsideRepeatedAndMiss() throws Exception {
        JsonNode j = json(outside(), repeatedShot(), miss());

        assertEquals(1, j.get("validShots").asInt());
        assertEquals(1, j.get("repeatedShots").asInt());
        assertEquals(1, j.get("missedShots").asInt());
        assertEquals(1, j.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("JSON: navio afundado aparece em sunkBoats e não em hitsOnBoats")
    void testJsonSunkBoat() throws Exception {
        IShip frigate = ship("Fragata");
        JsonNode j = json(sunk(frigate), miss(), miss());

        assertEquals(1, j.get("sunkBoats").size());
        assertEquals("Fragata", j.get("sunkBoats").get(0).get("type").asText());
        assertEquals(1, j.get("sunkBoats").get(0).get("count").asInt());
        assertEquals(0, j.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON: navio atingido mas não afundado aparece em hitsOnBoats")
    void testJsonHitBoat() throws Exception {
        IShip frigate = ship("Fragata");
        JsonNode j = json(hit(frigate), hit(frigate), miss());

        assertEquals(0, j.get("sunkBoats").size());
        assertEquals(1, j.get("hitsOnBoats").size());
        assertEquals("Fragata", j.get("hitsOnBoats").get(0).get("type").asText());
        assertEquals(2, j.get("hitsOnBoats").get(0).get("hits").asInt());
    }

    @Test
    @DisplayName("JSON: dois barcos diferentes, um afundado e outro só atingido")
    void testJsonSunkAndHitDifferentBoats() throws Exception {
        JsonNode j = json(sunk(ship("Fragata")), hit(ship("Nau")), miss());

        assertEquals(3, j.get("validShots").asInt());
        assertEquals(1, j.get("sunkBoats").size());
        assertEquals("Fragata", j.get("sunkBoats").get(0).get("type").asText());
        assertEquals(1, j.get("hitsOnBoats").size());
        assertEquals("Nau", j.get("hitsOnBoats").get(0).get("type").asText());
    }

    @Test
    @DisplayName("JSON: sem resultados todos os tiros são exteriores")
    void testJsonEmptyResults() throws Exception {
        JsonNode j = json();

        assertEquals(0, j.get("validShots").asInt());
        assertEquals(3, j.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("Modo não verbose não escreve a linha 'Jogada nº'")
    void testNonVerboseDoesNotPrintSummary() {
        new Move(1, shots, List.of(miss(), miss(), miss())).processEnemyFire(false);

        assertFalse(capturedOut.toString(StandardCharsets.UTF_8).contains("Jogada"));
    }

    @Test
    @DisplayName("Modo verbose escreve a linha 'Jogada nº' e o JSON")
    void testVerbosePrintsSummaryAndJson() {
        new Move(4, shots, List.of(miss(), miss(), miss())).processEnemyFire(true);

        String out = capturedOut.toString(StandardCharsets.UTF_8);
        assertTrue(out.startsWith("Jogada nº4 -> "));
        assertTrue(out.contains("\"validShots\""));
    }

    // ------------------------------------------------------------------
    // processEnemyFire: texto do modo verbose (um teste por ramo)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Verbose: só um tiro repetido (singular) e tiros exteriores (plural)")
    void testVerboseSingleRepeatedAndOutsidePlural() {
        assertEquals("Jogada nº1 -> 1 tiro repetido, 2 tiros exteriores", verboseLine(repeatedShot()));
    }

    @Test
    @DisplayName("Verbose: só tiros repetidos (plural)")
    void testVerboseOnlyRepeatedPlural() {
        assertEquals("Jogada nº1 -> 3 tiros repetidos",
                verboseLine(repeatedShot(), repeatedShot(), repeatedShot()));
    }

    @Test
    @DisplayName("Verbose: dois repetidos e um tiro exterior (singular)")
    void testVerboseRepeatedAndOutsideSingular() {
        assertEquals("Jogada nº1 -> 2 tiros repetidos, 1 tiro exterior",
                verboseLine(repeatedShot(), repeatedShot()));
    }

    @Test
    @DisplayName("Verbose: três tiros na água")
    void testVerboseAllMisses() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 3 tiros na água",
                verboseLine(miss(), miss(), miss()));
    }

    @Test
    @DisplayName("Verbose: um tiro válido na água e dois exteriores")
    void testVerboseSingleValidMiss() {
        assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 2 tiros exteriores",
                verboseLine(miss()));
    }

    @Test
    @DisplayName("Verbose: um tiro na água e um tiro exterior (singular)")
    void testVerboseOutsideSingular() {
        assertEquals("Jogada nº1 -> 2 tiros válidos: 2 tiros na água, 1 tiro exterior",
                verboseLine(miss(), miss()));
    }

    @Test
    @DisplayName("Verbose: um acerto (singular) num navio mais tiros na água")
    void testVerboseSingleHit() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 1 tiro num(a) Fragata + 2 tiros na água",
                verboseLine(hit(ship("Fragata")), miss(), miss()));
    }

    @Test
    @DisplayName("Verbose: vários acertos (plural) no mesmo navio")
    void testVerboseMultipleHits() {
        IShip frigate = ship("Fragata");
        assertEquals("Jogada nº1 -> 3 tiros válidos: 2 tiros num(a) Fragata + 1 tiro na água",
                verboseLine(hit(frigate), hit(frigate), miss()));
    }

    @Test
    @DisplayName("Verbose: um navio afundado (singular)")
    void testVerboseSingleSunk() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 1 Fragata ao fundo + 2 tiros na água",
                verboseLine(sunk(ship("Fragata")), miss(), miss()));
    }

    @Test
    @DisplayName("Verbose: vários navios do mesmo tipo afundados (plural)")
    void testVerboseMultipleSunk() {
        IShip frigate = ship("Fragata");
        assertEquals("Jogada nº1 -> 3 tiros válidos: 2 Fragatas ao fundo + 1 tiro na água",
                verboseLine(sunk(frigate), sunk(frigate), miss()));
    }

    @Test
    @DisplayName("Verbose: só acertos, sem tiros na água (remove o separador final)")
    void testVerboseOnlyHitsNoMisses() {
        IShip frigate = ship("Fragata");
        assertEquals("Jogada nº1 -> 3 tiros válidos: 3 tiros num(a) Fragata",
                verboseLine(hit(frigate), hit(frigate), hit(frigate)));
    }

    @Test
    @DisplayName("Verbose: só navios afundados, sem tiros na água")
    void testVerboseOnlySunkNoMisses() {
        IShip frigate = ship("Fragata");
        assertEquals("Jogada nº1 -> 3 tiros válidos: 3 Fragatas ao fundo",
                verboseLine(sunk(frigate), sunk(frigate), sunk(frigate)));
    }

    @Test
    @DisplayName("Verbose: tiros válidos e um tiro repetido (singular)")
    void testVerboseValidAndRepeatedSingular() {
        assertEquals("Jogada nº1 -> 2 tiros válidos: 2 tiros na água, 1 tiro repetido",
                verboseLine(miss(), miss(), repeatedShot()));
    }

    @Test
    @DisplayName("Verbose: tiros válidos e tiros repetidos (plural)")
    void testVerboseValidAndRepeatedPlural() {
        assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 2 tiros repetidos",
                verboseLine(miss(), repeatedShot(), repeatedShot()));
    }

    @Test
    @DisplayName("Verbose: sem tiros válidos nem repetidos só aparecem os exteriores")
    void testVerboseOnlyOutside() {
        assertEquals("Jogada nº1 -> 3 tiros exteriores",
                verboseLine(outside(), outside(), outside()));
    }

    @Test
    @DisplayName("Verbose: sem resultados só aparecem os exteriores")
    void testVerboseNoResults() {
        assertEquals("Jogada nº1 -> 3 tiros exteriores", verboseLine());
    }
}