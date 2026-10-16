-- Cleans seed placeholder text from word_assets.language_payloads.
-- The application keeps unknown generated fields as empty strings; defaults are
-- applied from configuration at projection time. Rows touched here are marked
-- pending so the scheduled word-asset repair can regenerate true LLM payloads.

WITH cleaned AS (
    SELECT
        asset.id,
        jsonb_agg(
            CASE
                WHEN item.value ->> 'lang' IN ('zh', 'zh-CN', 'ja', 'en') THEN
                    item.value
                    || jsonb_build_object(
                        'explanation',
                        CASE
                            WHEN COALESCE(item.value ->> 'explanation', '') ILIKE '%I noticed the % in the scene%'
                              OR COALESCE(item.value ->> 'explanation', '') ILIKE '%I notice the % in the scene%'
                              OR COALESCE(item.value ->> 'explanation', '') ILIKE '%Captured spatial anchor%'
                              OR COALESCE(item.value ->> 'explanation', '') LIKE '%この画面で%を見つけた%'
                              OR COALESCE(item.value ->> 'explanation', '') LIKE '%この場面で%を見つけた%'
                              OR COALESCE(item.value ->> 'explanation', '') LIKE '%这个场景%注意到%'
                              OR COALESCE(item.value ->> 'explanation', '') LIKE '%�%'
                            THEN ''
                            ELSE COALESCE(item.value ->> 'explanation', '')
                        END,
                        'example',
                        CASE
                            WHEN COALESCE(item.value ->> 'example', '') ILIKE '%I noticed the % in the scene%'
                              OR COALESCE(item.value ->> 'example', '') ILIKE '%I notice the % in the scene%'
                              OR COALESCE(item.value ->> 'example', '') ILIKE '%Captured spatial anchor%'
                              OR COALESCE(item.value ->> 'example', '') LIKE '%この画面で%を見つけた%'
                              OR COALESCE(item.value ->> 'example', '') LIKE '%この場面で%を見つけた%'
                              OR COALESCE(item.value ->> 'example', '') LIKE '%这个场景%注意到%'
                              OR COALESCE(item.value ->> 'example', '') LIKE '%�%'
                            THEN ''
                            ELSE COALESCE(item.value ->> 'example', '')
                        END,
                        'word',
                        CASE
                            WHEN item.value ->> 'lang' IN ('zh', 'zh-CN', 'ja')
                              AND regexp_replace(lower(COALESCE(item.value ->> 'word', '')), '[^a-z0-9]+', '', 'g')
                                  = regexp_replace(lower(asset.word_key), '[^a-z0-9]+', '', 'g')
                            THEN ''
                            ELSE COALESCE(item.value ->> 'word', '')
                        END
                    )
                ELSE item.value
            END
            ORDER BY item.ordinality
        ) AS language_payloads,
        bool_or(
            COALESCE(item.value ->> 'explanation', '') ILIKE '%I noticed the % in the scene%'
            OR COALESCE(item.value ->> 'explanation', '') ILIKE '%I notice the % in the scene%'
            OR COALESCE(item.value ->> 'explanation', '') ILIKE '%Captured spatial anchor%'
            OR COALESCE(item.value ->> 'explanation', '') LIKE '%この画面で%を見つけた%'
            OR COALESCE(item.value ->> 'explanation', '') LIKE '%この場面で%を見つけた%'
            OR COALESCE(item.value ->> 'explanation', '') LIKE '%这个场景%注意到%'
            OR COALESCE(item.value ->> 'explanation', '') LIKE '%�%'
            OR COALESCE(item.value ->> 'example', '') ILIKE '%I noticed the % in the scene%'
            OR COALESCE(item.value ->> 'example', '') ILIKE '%I notice the % in the scene%'
            OR COALESCE(item.value ->> 'example', '') ILIKE '%Captured spatial anchor%'
            OR COALESCE(item.value ->> 'example', '') LIKE '%この画面で%を見つけた%'
            OR COALESCE(item.value ->> 'example', '') LIKE '%この場面で%を見つけた%'
            OR COALESCE(item.value ->> 'example', '') LIKE '%这个场景%注意到%'
            OR COALESCE(item.value ->> 'example', '') LIKE '%�%'
            OR (
                item.value ->> 'lang' IN ('zh', 'zh-CN', 'ja')
                AND regexp_replace(lower(COALESCE(item.value ->> 'word', '')), '[^a-z0-9]+', '', 'g')
                    = regexp_replace(lower(asset.word_key), '[^a-z0-9]+', '', 'g')
            )
        ) AS dirty
    FROM word_assets AS asset
    CROSS JOIN LATERAL jsonb_array_elements(asset.language_payloads::jsonb)
        WITH ORDINALITY AS item(value, ordinality)
    GROUP BY asset.id
)
UPDATE word_assets AS asset
SET language_payloads = cleaned.language_payloads::text,
    enrichment_status = 'pending',
    update_time = NOW() - INTERVAL '10 minutes'
FROM cleaned
WHERE cleaned.dirty
  AND asset.id = cleaned.id;

-- The words observed in the 2026-06-24 scan are forced back into the repair queue
-- even if only one language pair had previously marked them as enriched.
UPDATE word_assets
SET enrichment_status = 'pending',
    update_time = NOW() - INTERVAL '10 minutes'
WHERE word_key IN (
    'stool',
    'traffic cone',
    'traffic tone',
    'electrical box',
    'electric box',
    'stuffed toy',
    'cable',
    'gamepad',
    'figurine',
    'base',
    'paper',
    'tube'
);
