ALTER TABLE word_assets
    ADD COLUMN IF NOT EXISTS enrichment_status VARCHAR(24) NOT NULL DEFAULT 'pending';

UPDATE word_assets
SET enrichment_status = CASE
    WHEN asset_source IN ('llm', 'llm_image', 'hatsune_mock') THEN 'enriched'
    ELSE 'pending'
END
WHERE enrichment_status IS NULL
   OR enrichment_status = '';

CREATE INDEX IF NOT EXISTS idx_word_assets_enrichment_status_update_time
    ON word_assets (enrichment_status, update_time);

UPDATE word_assets
SET canonical_word = INITCAP(REPLACE(word_key, '-', ' '))
WHERE canonical_word = word_key;

COMMENT ON COLUMN word_assets.enrichment_status IS
    'Word asset enrichment lifecycle: pending, enriched, or failed. Scheduled repair retries pending/failed rows by word_key.';
