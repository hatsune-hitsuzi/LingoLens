# Memory Palace Reorder

## Basic

| Field | Value |
| --- | --- |
| Path | `/api/memory-palace/reorder` |
| Method | POST |
| Content-Type | `application/json` |
| Auth | Bearer token required |
| Service | `com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService#reorder` |

## Request

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `palaceIds` | Long[] | Yes | Full ordered palace id list for the current user. `palaceIds[0]` is the top/latest card. |

Example:

```json
{
  "palaceIds": [951000000000003, 951000000000001, 951000000000002]
}
```

## Response

| Field | Type | Description |
| --- | --- | --- |
| `result.rCode` | String | `0` means success, `1` means failure. |
| `result.msg` | String | Result message. |
| `acceptedCount` | Integer | Number of palace rows updated for the current user. |

Example:

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "acceptedCount": 3
}
```

## Rules

- The backend only updates rows owned by the current authenticated user.
- The backend stores the order in `memory_palaces.sort_order`.
- Reordering does not update `memory_palaces.update_time`, so palace display dates keep their original semantic meaning.
- The Flutter client performs optimistic UI reorder first; if the API call fails, it rolls back to the previous order.
