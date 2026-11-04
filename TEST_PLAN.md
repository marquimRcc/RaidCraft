# Homologação manual — RaidCraft MVP

Os itens abaixo são para a **homologação manual com cliente Minecraft conectado**. Não marcá-los como concluídos com base somente no CI.

### Verificações automáticas já aprovadas

Em 04/11/2026, a [execução Paper Runtime Smoke](https://github.com/marquimRcc/RaidCraft/actions/runs/41830787161) validou dentro de um Paper 1.21.11 real: habilitação do plugin, criação de gerador, persistência, evento de mineração com jogador simulado, dois diamantes reais, proteção contra duplicidade, cooldown, reload e remoção.

### Checklist manual

- [ ] `gradle clean build` conclui usando JDK 21.
- [ ] JAR instala e `/plugins` exibe RaidCraft sem stacktrace.
- [ ] Jogador sem OP não consegue executar `/rc create`.
- [ ] OP olha para pedra e usa `/rc create diamond01 DIAMOND 2 30`.
- [ ] Bloco se transforma visualmente em minério de diamante.
- [ ] Ao minerar, aparecem exatamente dois diamantes reais.
- [ ] Bloco fica temporariamente bedrock sem drops adicionais.
- [ ] 30 segundos depois, minério reaparece.
- [ ] `/rc create iron01 IRON 3 10` entrega três RAW_IRON.
- [ ] `/rc list` e `/rc info diamond01` mostram as configurações.
- [ ] IDs duplicados e duas configurações no mesmo bloco são recusados.
- [ ] Explosões e pistões não destroem ou movimentam geradores.
- [ ] Reinício do servidor preserva geradores e posições.
- [ ] `/rc remove diamond01` impede que o minério regenere depois.
- [ ] Dois jogadores não conseguem duplicar drops por mineração simultânea.
- [ ] Verificar coexistência com plugins de proteção de terreno/claims.
- [ ] Painel HTML5 abre no navegador e apresenta uma simulação interativa.

**Status:** smoke test automático em Paper concluído; homologação manual com cliente Minecraft conectado pendente.
