# Smoke Test no Paper real

Esta pasta contém um **plugin de teste isolado**, `RaidCraftSmoke`, compilado somente para o GitHub Actions. **Não instale o RaidCraft-Smoke.jar em um servidor de produção.**

O workflow `.github/workflows/paper-smoke.yml` executa um servidor Paper 1.21.11 temporário no runner do GitHub, com Java 21, e instala tanto o RaidCraft quanto o plugin de teste.

A rotina verifica, dentro do processo do Paper:
- carregamento efetivo do RaidCraft;
- criação de um gerador de DIAMOND, com 2 itens e cooldown de 2 segundos;
- persistência YAML;
- interceptação de `BlockBreakEvent` pelo listener registrado;
- drop real de 2 itens DIAMOND;
- prevenção de duplicação durante cooldown;
- regeneração do bloco;
- recarga do YAML, nova mineração e remoção persistida.

O teste usa um **Player simulado por proxy** para disparar eventos Bukkit no ambiente sem jogadores; não substitui testes de cliente Minecraft conectado nem valida todos os efeitos de outros plugins. Em caso de falha, o workflow publica `paper.log` como artefato.

A versão Paper é fixa em 1.21.11 e a seleção de build usa apenas o canal STABLE do serviço Fill v3 do PaperMC, com SHA-256 verificado e User-Agent identificado.
