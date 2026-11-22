| Local | Nome do cheiro no código | Nome da refabricação | Número de alun@ |
| :--- | :--- | :--- | :--- |
| `Ship::occupies` | Field Shadowing (parâmetro `pos` oculta `this.pos`) | Rename | 129759 |
| `Ship::Ship` | Magic String (`"Ship's category must not be null"`) | Introduce Constant | 129759 |
| `Ship::getAdjacentPositions` | Unnecessary Local Variable (`adjacents`) | Inline Variable | 129759 |
| `Ship::isInsideBoard` | Duplicate Expression (`Game.BOARD_SIZE - 1`) | Introduce Variable | 129759 |
| `Ship::isInsideBoard` | Complex Conditional | Extract Method | 129759 |