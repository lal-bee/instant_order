# CLAUDE.md

## Project Overview
This repository is an online ordering / takeout system with three main parts:

1. `hanye-take-out-springboot3` — Java backend
2. `hanye-take-out-vue3` — Vue 3 admin web frontend
3. `instant_order_user` — user-side client / mini-program related code

The repo README states the stack is:
- Frontend: Vue 3, TypeScript, Pinia, Element Plus, ECharts
- Backend: Spring Boot 3, MyBatis, Redis
- Recommended environment: Node 20.x, JDK 17+, Maven 3.9.x :contentReference[oaicite:0]{index=0}

---

## Tech Stack
### Backend
- Java 17
- Spring Boot 3.2.x
- MyBatis
- MySQL
- Redis
- Lombok
- Fastjson
- Apache HttpClient

The backend appears to be a Maven multi-module project with modules:
- `common`
- `pojo`
- `server` :contentReference[oaicite:1]{index=1}

### Frontend
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts :contentReference[oaicite:2]{index=2}

---

## Repository Structure
- `hanye-take-out-springboot3/` — backend source
- `hanye-take-out-vue3/` — admin frontend
- `instant_order_user/` — user-side code
- `docs/` — project docs
- `hanye_take_out.sql` — main database SQL
- `db-fix-manager-store-chain.sql` — database patch / fix script
- `README.md` — setup notes and project intro :contentReference[oaicite:3]{index=3}

---

## How to Work in This Repo

### General Rules
- Prefer minimal, targeted changes over broad refactors.
- Before changing code, first understand which subproject is affected:
    - backend API / business logic → `hanye-take-out-springboot3`
    - admin UI / pages / permissions → `hanye-take-out-vue3`
    - user-side ordering flow / mini-program issues → `instant_order_user`
- Keep existing naming style and directory conventions.
- Do not change database schema casually; if schema changes are needed, also provide SQL migration scripts.
- When touching permissions, store isolation, or role logic, verify both backend authorization and frontend visibility.

### Backend Rules
- Follow Spring Boot layered structure:
    - controller
    - service
    - mapper
    - entity / dto / vo
- Keep business logic in service layer, not controller.
- For DB changes:
    - update mapper XML / mapper interface consistently
    - check DTO / VO / entity impacts
    - note whether existing SQL files need patch scripts
- If adding an API:
    1. define request/response objects clearly
    2. implement service logic
    3. add mapper SQL if needed
    4. keep return format consistent with current project style
- Preserve compatibility with existing admin and user endpoints unless explicitly asked to break them.

### Frontend Rules
- Use the existing Vue 3 + TS + Pinia + Element Plus stack.
- Reuse existing API modules, router conventions, and store patterns before creating new ones.
- For page changes:
    - keep forms, tables, dialogs consistent with current UI style
    - ensure role-based button visibility and route access stay correct
- For permission-related tasks:
    - check menu visibility
    - check page-level access
    - check operation buttons
    - check data-scope restrictions

### SQL / Data Rules
- Main schema comes from `hanye_take_out.sql`.
- If fixing store / chain / manager relations, check whether `db-fix-manager-store-chain.sql` already covers part of the change.
- Never drop or rewrite production-like data blindly.
- Prefer additive fixes:
    - new columns
    - new indexes
    - patch scripts
    - backfill statements

---

## Common Commands

### Frontend (`hanye-take-out-vue3`)
```bash
cd hanye-take-out-vue3
D:\Develop\nvm\nodejs\npm install
D:\Develop\nvm\nodejs\npm run dev
D:\Develop\nvm\nodejs\npm run build
D:\Develop\nvm\nodejs\npm run preview