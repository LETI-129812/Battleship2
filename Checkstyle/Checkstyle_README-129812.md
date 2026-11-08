# 129812 Tiago Moura - Parte B (Checkstyle)

| Regra / Módulo Checkstyle | Descrição da Propriedade | Limiar Configurado | Motivação / Impacto no Código |
| :--- | :--- | :--- | :--- |
| MethodLength | max | 40 linhas | Garante que os métodos permanecem curtos e legíveis, impedindo a introdução de Long Methods. |

**Reflexão:**
Integrar o Checkstyle no Maven é bastante prático porque valida o estilo do código logo na compilação local e no GitHub, bloqueando erros de formatação antes do merge. A principal diferença em relação ao Qodana é que, enquanto o Qodana analisa bugs e problemas de segurança mais complexos, o Checkstyle foca-se de forma rápida e direta em garantir que as convenções de código da equipa são cumpridas.