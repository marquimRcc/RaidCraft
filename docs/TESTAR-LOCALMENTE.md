# Instalação e teste local — RaidCraft MVP

Estes scripts preparam **Paper 1.21.11 + RaidCraft** no próprio computador, usando o código público da branch `main` e compilando localmente com **Java 21 JDK**.

> **Importante:** o GitHub Actions já validou o plugin com um jogador simulado dentro do processo do servidor Paper. As instruções abaixo servem para o passo seguinte: um teste **manual com um cliente Minecraft Java 1.21.11 conectado**. Isso ainda não está homologado.

## Pré-requisitos

- Minecraft **Java Edition** 1.21.11 (cliente oficial), para entrar no servidor.
- **Java 21 JDK**, incluindo os comandos `java` e `javac`, disponíveis no PATH.
- No Linux: `bash`, `curl`, `unzip`, `python3` e `sha256sum`.
- No Windows: **PowerShell 5.1 ou 7+**.
- Internet na primeira execução; o script baixa o código, Gradle 8.14 se necessário e o Paper oficial.
- Recomendado ao menos 3 GB de RAM livres para o servidor e espaço adicional para o cliente.
- Leia os termos de uso: https://aka.ms/MinecraftEULA . **Aceite apenas se concordar.** O script somente escreve `eula=true` quando você passa o parâmetro explícito.

## Linux (incluindo RegataOS)

No terminal, **após o PR do instalador estar na main**, execute:

```bash
curl -fsSL https://raw.githubusercontent.com/marquimRcc/RaidCraft/main/scripts/preparar-raidcraft-lab.sh -o /tmp/preparar-raidcraft-lab.sh; bash /tmp/preparar-raidcraft-lab.sh --accept-eula --start
```

**Observação:** a segunda parte aceita a EULA expressamente. Retire `--accept-eula --start` para apenas preparar os arquivos sem aceitar ou iniciar o servidor.

## Windows 10/11 (PowerShell)

No PowerShell, **após o PR do instalador estar na main**:

```powershell
$p="$env:TEMP\Preparar-RaidCraft-Lab.ps1"; Invoke-WebRequest "https://raw.githubusercontent.com/marquimRcc/RaidCraft/main/scripts/Preparar-RaidCraft-Lab.ps1" -OutFile $p; powershell.exe -NoProfile -ExecutionPolicy Bypass -File $p -AcceptEula -Start
```

Retire `-AcceptEula -Start` para preparar os arquivos sem aceitar os termos ou iniciar o servidor. O script criado localmente pode ser revisado antes da execução.

## Passo a passo dentro do jogo

1. Quando o Paper indicar que o servidor iniciou, use no console: `op SEU_NICK` (substitua pelo seu usuário real do Minecraft).
2. Abra o **Minecraft Java Edition 1.21.11**, Multiplayer → Adicionar servidor, endereço `127.0.0.1`.
3. Dentro do mundo, olhe para um **bloco de pedra sólido** próximo (até 6 blocos).
4. Execute `/rc create diamond01 DIAMOND 2 30`.
5. Confirme que o bloco virou minério de diamante e que a mineração entrega **2 diamantes**.
6. Observe o bloco temporariamente como bedrock e a regeneração após **30 segundos**.
7. Execute `/rc list`, `/rc info diamond01`, `/rc remove diamond01`, e confirme as mensagens.
8. Execute `/rc create iron01 IRON 3 10`, minere e confirme **3 RAW_IRON**.
9. Pare o servidor digitando `stop` no console, reinicie e verifique a persistência dos geradores.

### Onde ficam os arquivos?

- Linux: `~/RaidCraft-Lab/server/`
- Windows: `%USERPROFILE%\\RaidCraft-Lab\\server\\`
- Plugin: `server/plugins/RaidCraft-0.1.0.jar`
- Log principal: `server/logs/latest.log`
- Persistência: `server/plugins/RaidCraft/generators.yml`

O servidor é configurado por padrão com `server-ip=127.0.0.1` e `online-mode=true`: ele aceita conexões **somente do próprio computador**. Os scripts preservam `server.properties`, mundo e arquivos de plugin existentes ao serem executados novamente.

### Limitações e segurança

- O instalador constrói o plugin a partir do código público atual da branch `main`; não depende da autenticação da API de artefatos GitHub Actions.
- O Paper vem da **API oficial PaperMC Fill v3**, somente canal STABLE, com validação de SHA-256. A distribuição Gradle também tem SHA-256 validado.
- Não instala nem altera o Java do computador. Se Java 21 JDK estiver ausente, ele informa o requisito e encerra.
- A primeira inicialização do servidor gera um mundo novo e pode demorar; isso não interfere na instalação Windows/Linux existente fora de `RaidCraft-Lab`.
- Não encaminhe a porta do servidor para a Internet e não copie `RaidCraft-Smoke.jar` para este ambiente: o plugin de testes é **exclusivo do CI**.
- O teste com cliente real e outros plugins deve ser concluído antes de apresentar o MVP como homologado comercialmente.
