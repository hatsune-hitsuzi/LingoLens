ALTER TABLE memory_palaces
    ADD COLUMN IF NOT EXISTS sort_order INTEGER;

WITH ranked_palaces AS (
    SELECT
        id,
        row_number() OVER (
            PARTITION BY user_pk_id
            ORDER BY update_time DESC, id DESC
        ) - 1 AS next_sort_order
    FROM memory_palaces
)
UPDATE memory_palaces palace
SET sort_order = ranked.next_sort_order
FROM ranked_palaces ranked
WHERE palace.id = ranked.id
  AND palace.sort_order IS NULL;

ALTER TABLE memory_palaces
    ALTER COLUMN sort_order SET DEFAULT 0;

ALTER TABLE memory_palaces
    ALTER COLUMN sort_order SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_memory_palaces_user_sort
    ON memory_palaces(user_pk_id, sort_order ASC, update_time DESC, id DESC);
