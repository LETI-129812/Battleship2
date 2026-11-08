<!-- 129759 - Rodrigo Alves -->
# Checkstyle - Nomes de métodos (129759)

## Configuração

- Identificador escolhido: **métodos**, regra `MethodName` (padrão por omissão: `^[a-z][a-zA-Z0-9]*$`).
- Ficheiro de configuração: `checkstyle-129759.xml` (raiz do projeto).
- Workflow: `.github/workflows/checkstyle-129759.yaml`, com a action `dbelyaev/action-checkstyle@v3`, que corre em cada pull request e falha quando há violações (`fail_level: error`).

## Execuções do quality gate

- Execução que **falha** (violação provocada): https://github.com/LETI-129812/Battleship2/actions/runs/42507530135
- Execução que **passa** (nome corrigido): https://github.com/LETI-129812/Battleship2/actions/runs/42508208592/job/128815351149?pr=14

## Proteção do ramo main

Não foi possível configurar a proteção do ramo `main`: o meu acesso ao repositório (Collaborator) não dá acesso a Settings → Branches, e essa funcionalidade depende do tipo de acesso e do plano do repositório. Por isso o merge não ficou bloqueado quando o gate falhou.

## O que o Checkstyle não consegue verificar

O Checkstyle só verifica a forma do nome, não o seu significado: um método chamado `x1` cumpre a regra `MethodName`, mas não diz o que faz. Também não avalia se o nome é coerente com o comportamento do método, nem se a convenção escolhida é a mais adequada ao projeto. Por fim, a regra só se aplica aos métodos, por isso o resto dos identificadores (classes, constantes, atributos) fica sem verificação.