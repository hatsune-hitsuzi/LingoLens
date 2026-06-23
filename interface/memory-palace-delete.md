# Memory Palace Delete

## Basic

| Field | Value |
| --- | --- |
| Path | `/api/memory-palace/delete` |
| Method | POST |
| Auth | Bearer token required |

## Request

```json
{
  "palaceId": 951000000000001
}
```

## Response

```json
{
  "result": {"rCode": "0", "msg": "deleted"},
  "palaceId": 951000000000001,
  "deleted": true
}
```

Deleting a palace removes rows from `memory_palace_words` for that palace, then removes the palace row. Global `word_assets` and AR scan history are kept.
