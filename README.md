# RaidCraft MVP — Minecraft Java Edition

Protótipo open-source de geradores de minério configuráveis para **Paper 1.21.11**, feito em **Java 21** e **Gradle**.

> **Estado:** código-fonte inicial publicado. Compilação com Paper API e testes dentro do Minecraft ainda pendentes. O painel HTML é uma demonstração visual offline e **não** controla o servidor.

## Objetivo

Provar uma regra simples de gameplay: **o minério que o jogador vê é o recurso que ele recebe**. O plugin oferece blocos de minérios nativos, drops fixos, quantidade e cooldown ajustáveis e persistência YAML. Não inclui raids PvP, mapa, clãs, loja, economia, API HTTP ou suporte a Folia.

## Estrutura

```text
src/main/java/dev/raidcraft/
  RaidCraftPlugin.java                 inicialização
  command/RaidCraftCommand.java       /rc ...
  listener/GeneratorListener.java     quebra, explosões e pistões
  model/Generator.java                configuração do gerador
  model/OreKind.java                  bloco e item correspondente
  service/GeneratorService.java       drops, cooldown, restauração
  storage/GeneratorRepository.java    persistência YAML
src/main/resources/
  plugin.yml
  config.yml
painel-demo.html                       interface de demonstração offline
ROADMAP.md                             próximas fases
TEST_PLAN.md                           checklist de homologação
```

## Requisitos

- Minecraft **Java Edition** (não Bedrock)
- Servidor **Paper 1.21.11**
- **JDK 21**
- **Gradle 8.14+** ou Gradle do IntelliJ IDEA
- Internet na primeira compilação para resolver a dependência Paper API

## Build e instalação

```bash
gradle clean build
```

O JAR esperado é `build/libs/RaidCraft-0.1.0.jar`. Baixe Paper no [site oficial](https://papermc.io/downloads/paper) e inicie o servidor. Leia e aceite a EULA em `eula.txt` antes de reiniciar. Copie o JAR para `plugins/`, inicie o servidor e confirme `/plugins`.

No console, conceda OP com `op SEU_NICK`. No Minecraft, olhe para um bloco sólido comum a até 6 blocos e use:

```text
/rc create diamond01 DIAMOND 2 30
/rc create iron01 IRON 3 10
/rc list
/rc info diamond01
/rc remove iron01
/rc reload
```

O primeiro comando deve configurar o bloco como minério de diamante que, ao ser minerado, entrega **2 diamantes**, vira temporariamente bedrock e volta após **30 segundos**. O segundo gera **3 RAW_IRON** a cada quebra válida, com regeneração em **10 segundos**.

A permissão `raidcraft.admin` é padrão para operadores (OP).

## Minérios disponíveis

| Argumento | Bloco visível | Item entregue |
|---|---|---|
| DIAMOND | DIAMOND_ORE | DIAMOND |
| EMERALD | EMERALD_ORE | EMERALD |
| IRON | IRON_ORE | RAW_IRON |
| GOLD | GOLD_ORE | RAW_GOLD |
| COPPER | COPPER_ORE | RAW_COPPER |
| COAL | COAL_ORE | COAL |
| REDSTONE | REDSTONE_ORE | REDSTONE |
| LAPIS | LAPIS_ORE | LAPIS_LAZULI |
| QUARTZ | NETHER_QUARTZ_ORE | QUARTZ |

Os geradores são salvos em `plugins/RaidCraft/generators.yml`. O painel `painel-demo.html` pode ser aberto no navegador, sem dependências, para ilustrar a configuração.

## Limitações conhecidas

- **Ainda não compilado ou testado no Paper.** Não anunciar como plugin homologado.
- O cooldown em andamento não é persistido após reinício.
- O armazenamento é YAML local para pequenos servidores; não é indicado para milhares de geradores sem melhorias.
- A regeneração usa substituição temporária por bedrock. Fortune e Silk Touch não alteram o drop.
- Compatibilidade com plugins de proteção, anticheat e claims exige homologação.
- Não há integração do painel HTML com o plugin; ele é apenas demonstrativo.
- Não há implementação de raids, bases, progressão, economia nem PvP neste MVP.

Consulte [ROADMAP.md](ROADMAP.md) e [TEST_PLAN.md](TEST_PLAN.md).

## Origem e escopo comercial

Projeto autoral, criado como prova de conceito para uma oportunidade freelance. Referências externas de gameplay não são código-fonte deste projeto. Uma contratação deve confirmar versão de Paper/Purpur/Folia, regras de claims/raids e comportamento esperado dos geradores.

Este repositório é público; nenhum código privado de clientes deve ser publicado aqui.
