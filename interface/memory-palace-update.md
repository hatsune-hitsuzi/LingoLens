# Memory Palace Update

## Basic

| Field | Value |
| --- | --- |
| Path | `/api/memory-palace/update` |
| Method | POST |
| Auth | Bearer token required |

## Request

```json
{
  "palaceId": 951000000000001,
  "sceneName": "Tokyo Station",
  "displayLanguage": "en"
}
```

## Response

```json
{
  "result": {"rCode": "0", "msg": "success"},
  "palace": {
    "palaceId": 951000000000001,
    "sceneName": "Tokyo Station",
    "localizedSceneName": "Tokyo Station",
    "coverUrl": "",
    "savedPhotos": [],
    "wordCount": 6,
    "updateTime": "2026/06/15"
  }
}
```

The backend validates ownership and rejects duplicate palace names under the same user.
