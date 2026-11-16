# UC00619 — Sessão 15 — Exercício 2 — Solução

O `library-jpa` do Exercício 1, protegido com Spring Security (HTTP Basic, utilizadores em memória).

Utilizadores: `ana / ana123` (USER) e `admin / admin123` (USER e ADMIN).

## Regras

1. `GET /api/books/**` → público
2. `POST /api/books/*/reviews` → autenticado
3. resto de `/api/books/**` → ADMIN
4. qualquer outro pedido → autenticado

## Consola H2

Com a segurança ligada, a consola H2 fica bloqueada. Se for precisa (só em dev): permitir `/h2-console/**`
e acrescentar `.headers(h -> h.frameOptions(f -> f.sameOrigin()))`.
