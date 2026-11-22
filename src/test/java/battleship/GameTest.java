package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}
	@Test
	@DisplayName("Print board without shots or legend")
	void printBoardWithoutShotsOrLegend() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));

			Game.printBoard(new Fleet(), List.of(), false, false);

		} finally {
			System.setOut(originalOut);
		}

		String result = output.toString();

		assertTrue(result.contains("A |"));
		assertFalse(result.contains("LEGENDA"));
	}
	@Test
	@DisplayName("Print board with legend")
	void printBoardWithLegend() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));

			Game.printBoard(new Fleet(), List.of(), false, true);

		} finally {
			System.setOut(originalOut);
		}

		String result = output.toString();

		assertTrue(result.contains("LEGENDA"));
		assertTrue(result.contains("Tiro certeiro"));
		assertTrue(result.contains("Tiro na água"));
	}
	@Test
	@DisplayName("Print board with shots hitting ships and water")
	void printBoardWithShots() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));

			Fleet fleet = new Fleet();
			fleet.addShip(new Barge(Compass.NORTH, new Position(2, 3)));

			Game game = new Game(fleet);

			List<IPosition> shots = List.of(
					new Position(2, 3),
					new Position(4, 5),
					new Position(6, 7)
			);

			game.fireShots(shots);
			Game.printBoard(fleet, game.getAlienMoves(), true, true);

		} finally {
			System.setOut(originalOut);
		}

		String result = output.toString();

		assertTrue(result.contains("*"));
		assertTrue(result.contains("o"));
		assertTrue(result.contains("LEGENDA"));
	}
	@Test
	@DisplayName("Print my board")
	void printMyBoardTest() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));
			game.printMyBoard(false, true);
		} finally {
			System.setOut(originalOut);
		}

		assertTrue(output.toString().contains("LEGENDA"));
	}

	@Test
	@DisplayName("Print alien board")
	void printAlienBoardTest() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));
			game.printAlienBoard(false, true);
		} finally {
			System.setOut(originalOut);
		}

		assertTrue(output.toString().contains("LEGENDA"));
	}

	@Test
	@DisplayName("Read enemy fire using classic coordinates")
	void readEnemyFireClassicTest() {
		String input = "A1 B2 C3";
		String result = game.readEnemyFire(new Scanner(input));

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Read enemy fire using separate column and row")
	void readEnemyFireSeparateCoordinatesTest() {
		String input = "a 1 b 2 c 3";
		String result = game.readEnemyFire(new Scanner(input));

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Read enemy fire with incomplete coordinate")
	void readEnemyFireIncompleteTest() {
		Scanner input = new Scanner("A B2 C3");

		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(input));
	}

	@Test
	@DisplayName("Read enemy fire with fewer than three positions")
	void readEnemyFireTooFewShotsTest() {
		Scanner input = new Scanner("A1 B2");

		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(input));
	}

	@Test
	@DisplayName("Read enemy fire with more than three positions")
	void readEnemyFireTooManyShotsTest() {
		Scanner input = new Scanner("A1 B2 C3 D4");

		String result = game.readEnemyFire(input);

		assertNotNull(result);
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Random enemy fire generates three shots")
	void randomEnemyFireTest() {
		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Score record constructor and getters")
	void scoreRecordTest() {
		ScoreBoard.ScoreRecord record = new ScoreBoard.ScoreRecord("Capitão", 500);

		assertEquals("Capitão", record.getPlayer());
		assertEquals(500, record.getPoints());
		assertEquals("Capitão - 500 pts", record.toString());
	}

	@Test
	@DisplayName("Score record empty constructor")
	void scoreRecordEmptyConstructorTest() {
		ScoreBoard.ScoreRecord record = new ScoreBoard.ScoreRecord();

		assertNull(record.getPlayer());
		assertEquals(0, record.getPoints());
	}

	@Test
	@DisplayName("JSON shots with multiple positions")
	void jsonShotsMultiplePositionsTest() {
		List<IPosition> positions = List.of(
				new Position(0, 0),
				new Position(4, 5),
				new Position(9, 9)
		);

		String json = Game.jsonShots(positions);

		assertTrue(json.contains("\"row\""));
		assertTrue(json.contains("\"column\""));
		assertTrue(json.contains("\"A\""));
		assertTrue(json.contains("\"E\""));
		assertTrue(json.contains("\"J\""));
	}

	@Test
	@DisplayName("JSON shots with empty list")
	void jsonShotsEmptyTest() {
		String json = Game.jsonShots(List.of());

		assertNotNull(json);
		assertTrue(json.trim().startsWith("["));
		assertTrue(json.trim().endsWith("]"));
		assertFalse(json.contains("\"row\""));
	}

	@Test
	@DisplayName("Save and load score")
	void saveAndLoadScoreTest() {
		ScoreBoard.saveScore("TesteJUnit", 750);

		List<ScoreBoard.ScoreRecord> scores = ScoreBoard.loadScores();

		assertTrue(scores.stream().anyMatch(
				record -> "TesteJUnit".equals(record.getPlayer())
						&& record.getPoints() == 750
		));
	}

	@Test
	@DisplayName("Get alien fleet")
	void getAlienFleetTest() {
		assertNotNull(game.getAlienFleet());
	}

	@Test
	@DisplayName("Get my moves")
	void getMyMovesTest() {
		assertNotNull(game.getMyMoves());
	}

	@Test
	@DisplayName("Fire shots with fewer than three positions")
	void fireShotsWithTooFewPositionsTest() {
		List<IPosition> shots = List.of(
				new Position(0, 0),
				new Position(1, 1)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(shots));
	}

	@Test
	@DisplayName("Fire shots with more than three positions")
	void fireShotsWithTooManyPositionsTest() {
		List<IPosition> shots = List.of(
				new Position(0, 0),
				new Position(1, 1),
				new Position(2, 2),
				new Position(3, 3)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(shots));
	}

	@Test
	@DisplayName("Over with player name")
	void overWithPlayerNameTest() {
		InputStream originalIn = System.in;
		try {
			System.setIn(new ByteArrayInputStream("Capitão Teste\n".getBytes()));
			game.over();
		} finally {
			System.setIn(originalIn);
		}
	}

	@Test
	@DisplayName("Over with empty player name")
	void overWithEmptyPlayerNameTest() {
		InputStream originalIn = System.in;
		try {
			System.setIn(new ByteArrayInputStream("\n".getBytes()));
			game.over();
		} finally {
			System.setIn(originalIn);
		}
	}
	@Test
	@DisplayName("Random enemy fire with fewer than three candidate positions")
	void randomEnemyFireWithFewCandidatesTest() {
		List<IPosition> shots = new ArrayList<>();

		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				if (row != 9 || col != 9) {
					shots.add(new Position(row, col));
				}
			}
		}

		// Registar os tiros anteriores em grupos de três
		for (int i = 0; i < shots.size(); i += 3) {
			game.fireShots(shots.subList(i, i + 3));
		}

		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertEquals(3, game.getAlienMoves()
				.get(game.getAlienMoves().size() - 1)
				.getShots().size());
	}

	@Test
	@DisplayName("Print board ignores shots outside the board")
	void printBoardWithOutsideShotTest() {
		game.fireShots(List.of(
				new Position(-1, 5),
				new Position(4, 4),
				new Position(5, 5)
		));

		assertDoesNotThrow(() ->
				Game.printBoard(
						new Fleet(),
						game.getAlienMoves(),
						true,
						false
				)
		);
	}


	@Test
	@DisplayName("Print board with sunk ship")
	void printBoardWithSunkShipTest() {
		Fleet fleet = new Fleet();
		Ship ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		ship.sink();

		assertDoesNotThrow(() ->
				Game.printBoard(fleet, List.of(), false, false)
		);
	}

	@Test
	@DisplayName("Fire shots with repeated position")
	void fireShotsWithRepeatedPositionTest() {
		Position repeated = new Position(2, 3);

		game.fireShots(List.of(
				repeated,
				new Position(4, 4),
				new Position(5, 5)
		));

		game.fireShots(List.of(
				repeated,
				new Position(6, 6),
				new Position(7, 7)
		));

		assertTrue(game.getRepeatedShots() > 0);
	}

}