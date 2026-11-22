package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * @date: 20/02/2026
 * @time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {
		int validShots = 0, repeatedShots = 0, missedShots = 0;
		Map<String, Integer> sunkBoatsCount = new HashMap<>();
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) continue;
			if (result.repeated()) {
				repeatedShots++;
			} else {
				validShots++;
				if (result.ship() == null) {
					missedShots++;
				} else {
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk()) {
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
					}
				}
			}
		}

		// CORRIGIDO: Adicionado o operador de subtração (-) que faltava
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		if (verbose) {
			printVerboseResult(validShots, repeatedShots, missedShots, outsideShots, sunkBoatsCount, hitsPerBoat);
		}

		return generateJsonResponse(validShots, repeatedShots, missedShots, outsideShots, sunkBoatsCount, hitsPerBoat);
	}

	private void printVerboseResult(int validShots, int repeatedShots, int missedShots, int outsideShots, Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		StringBuilder output = new StringBuilder();

		if (validShots == 0 && repeatedShots > 0) {
			output.append(repeatedShots).append(repeatedShots > 1 ? " tiros repetidos" : " tiro repetido");
		} else if (validShots > 0) {
			appendValidShotsSummary(output, validShots, repeatedShots, missedShots, sunkBoatsCount, hitsPerBoat);
		}

		appendOutsideShots(output, outsideShots);

		System.out.println("Jogada nº" + this.number + " -> " + output);
	}

	private void appendValidShotsSummary(StringBuilder output, int validShots, int repeatedShots, int missedShots, Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		output.append(validShots).append(validShots > 1 ? " tiros válidos: " : " tiro válido: ");

		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			int count = entry.getValue();
			output.append(count).append(" ").append(entry.getKey()).append(count > 1 ? "s ao fundo + " : " ao fundo + ");
		}

		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				int hits = entry.getValue();
				output.append(hits).append(hits > 1 ? " tiros num(a) " : " tiro num(a) ").append(entry.getKey()).append(" + ");
			}
		}

		if (missedShots > 0) {
			output.append(missedShots).append(missedShots > 1 ? " tiros na água" : " tiro na água");
		} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
			output.setLength(output.length() - 2);
		}

		if (repeatedShots > 0) {
			output.append(", ").append(repeatedShots).append(repeatedShots > 1 ? " tiros repetidos" : " tiro repetido");
		}
	}

	private void appendOutsideShots(StringBuilder output, int outsideShots) {
		if (outsideShots > 0) {
			if (!output.isEmpty()) output.append(", ");
			output.append(outsideShots).append(outsideShots > 1 ? " tiros exteriores" : " tiro exterior");
		}
	}


	private String generateJsonResponse(
			int validShots,
			int repeatedShots,
			int missedShots,
			int outsideShots,
			Map<String, Integer> sunkBoatsCount,
			Map<String, Integer> hitsPerBoat) {

		Map<String, Object> response = new HashMap<>();

		response.put("validShots", validShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);
		response.put("outsideShots", outsideShots);
		response.put("sunkBoats", buildBoatList(sunkBoatsCount, "count"));
		response.put("hitsOnBoats", buildUnsunkBoatList(sunkBoatsCount, hitsPerBoat));

		try {
			ObjectMapper mapper = new ObjectMapper()
					.enable(SerializationFeature.INDENT_OUTPUT);

			String json = mapper.writeValueAsString(response);
			System.out.println(json);

			return json;

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar JSON", e);
		}
	}

	private List<Map<String, Object>> buildBoatList(
			Map<String, Integer> boats, String valueKey) {

		List<Map<String, Object>> result = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : boats.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put(valueKey, entry.getValue());
			result.add(boat);
		}

		return result;
	}

	private List<Map<String, Object>> buildUnsunkBoatList(
			Map<String, Integer> sunkBoats,
			Map<String, Integer> hitsPerBoat) {

		Map<String, Integer> unsunkBoats = new HashMap<>(hitsPerBoat);
		unsunkBoats.keySet().removeAll(sunkBoats.keySet());

		return buildBoatList(unsunkBoats, "hits ");
	}

}