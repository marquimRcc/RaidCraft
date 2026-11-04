# Roadmap — RaidCraft

## P0 — Escopo e arquitetura
- [x] Definir Java 21, Paper 1.21.11, Gradle e módulos separados.
- [x] Delimitar plugin próprio e geradores como demonstração inicial.
- [x] Publicar o código-fonte em repositório público.

## P1 — MVP de minério
- [x] Criar comandos, service, listener, enum de minérios e repositório YAML.
- [x] Criar painel HTML5 interativo offline.
- [x] Compilar e empacotar com dependências Paper em CI (Java 21).
- [x] Validar estrutura do JAR e bytecode Java 21.
- [x] Executar smoke test de geradores dentro de um servidor Paper 1.21.11 real.
- [ ] Concluir homologação manual com cliente Minecraft conforme TEST_PLAN.md.

## P2 — Demonstração Minecraft
- [x] Preparar servidor Paper 1.21.11 efêmero no GitHub Actions para smoke test.
- [x] Validar no servidor um gerador DIAMOND com drop de 2 itens e cooldown de 2s (CI).
- [ ] Criar no cliente Minecraft um gerador DIAMOND com 2 diamantes e cooldown de 30s.
- [ ] Verificar integridade de drops, reinícios e multiplayer.
- [ ] Registrar vídeo curto mostrando bloco exibido e item recebido.

## P3 — Abordagem comercial
- [ ] Confirmar com o cliente versão de Minecraft e servidor.
- [ ] Confirmar se os recursos surgem ao minerar ou por geração automática.
- [ ] Enviar vídeo e propor pequeno primeiro milestone.
- [ ] Definir separadamente orçamento para raids, claims e economia.

## P4 — Mecânicas de raids
- [ ] Definir regras PvP, bases/claims, proteção e recompensas.
- [ ] Implementar progressão e tiers dos geradores.
- [ ] Testar coexistência com plugins do servidor do cliente.

## P5 — Painel e operação
- [ ] Planejar API HTTP autenticada para gerenciamento remoto.
- [ ] Integrar o painel sem expor comandos administrativos.
- [ ] Persistência robusta, backup, observabilidade e métricas.

**Evidência de integração:** [Paper Runtime Smoke aprovado](https://github.com/marquimRcc/RaidCraft/actions/runs/41830787161). Esse teste cobre eventos Bukkit com jogador simulado, não substitui cliente conectado, multiplayer nem testes de plugins externos.
