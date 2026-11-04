# Roadmap — RaidCraft

## P0 — Escopo e arquitetura
- [x] Definir Java 21, Paper 1.21.11, Gradle e módulos separados.
- [x] Delimitar plugin próprio e geradores como demonstração inicial.
- [x] Publicar o código-fonte em repositório público.

## P1 — MVP de minério
- [x] Criar comandos, service, listener, enum de minérios e repositório YAML.
- [x] Criar painel HTML5 interativo offline.
- [ ] Compilar efetivamente com dependências Paper.
- [ ] Corrigir quaisquer erros detectados pela compilação.
- [ ] Validar comportamento no jogo conforme TEST_PLAN.md.

## P2 — Demonstração Minecraft
- [ ] Preparar sandbox Paper 1.21.11 e aceitar EULA.
- [ ] Criar gerador DIAMOND com 2 diamantes e cooldown de 30s.
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

**Importante:** elementos marcados como código implementado não equivalem a aprovação em testes de integração.
