| Local | Nome do cheiro no código | Nome da refabricação | Número de alun@ |
| :--- | :--- | :--- | :--- |
| `Ship::occupies` | Field Shadowing (parâmetro `pos` oculta `this.pos`) | Rename | 129759 |
| `Ship::Ship` | Magic String (`"Ship's category must not be null"`) | Introduce Constant | 129759 |
| `Ship::getAdjacentPositions` | Unnecessary Local Variable (`adjacents`) | Inline Variable | 129759 |
| `Ship::isInsideBoard` | Duplicate Expression (`Game.BOARD_SIZE - 1`) | Introduce Variable | 129759 |
| `Ship::isInsideBoard` | Complex Conditional | Extract Method | 129759 |
| `Game::fireSingleShot`     | Duplicate Expression |  Introduce Variable  |    129812  |
| `Caravel::Caravel`    | Magic Number |  Introduce Constant | 129812 |
| `Game::getRemainingShips` | Unnecessary Local Variable (`floatingShips`) | Inline Variable | 129812 |
| `Game::saveScore` | Large Class | Move Members | 129812 |
| `Game::printBoardFrame` | Long Method | Extract Method | 129812 |
| `Move::Move` | Inconsistent Naming (parâmetros `moveShots`/`moveResults` vs campos `shots`/`shotResults`) | Rename | 129811 |
| `Move::printVerboseResult`, `Move::generateJsonResponse` | Long Parameter List (6 parâmetros) | Introduce Parameter Object | 129811 |
| `Move::printVerboseResult` | Long Method | Extract Method | 129811 |
