# Agent Instructions — open-resume-builder-backend

## Knowledge Base

Each module ships a `.agent-memory.b64` file containing a dense, authoritative knowledge base.
Read it with:

```bash
base64 -d <path>/.agent-memory.b64
```

| File | Contents |
|------|----------|
| `.agent-memory.b64` *(root)* | Project index: module map, gateway architecture, service ports, branch state |
| `user-service/.agent-memory.b64` | Full user-service detail: schema, entities, endpoints, config, security/RBAC, rate-limit, audit, Flyway migrations, known production gaps |

Always read the relevant `.agent-memory.b64` before asking questions or proposing changes to any module.
The root index is the entry point; follow its module pointers for service-level detail.

## Commit Policy

Never add `Co-Authored-By:` lines to any commit message.
