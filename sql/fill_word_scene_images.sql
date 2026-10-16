-- Fill existing word image fields with bundled mobile scene assets.
-- The mobile app resolves asset://word_scenes/<word_key>.png from
-- Lingolens_mobile/assets/word_scenes/.

WITH fixed(word_key, image_url) AS (
    VALUES
        ('aisle', 'asset://word_scenes/aisle.png'),
        ('announcement', 'asset://word_scenes/announcement.png'),
        ('archive', 'asset://word_scenes/archive.png'),
        ('aroma', 'asset://word_scenes/aroma.png'),
        ('barcode', 'asset://word_scenes/barcode.png'),
        ('barista', 'asset://word_scenes/barista.png'),
        ('beverage', 'asset://word_scenes/beverage.png'),
        ('cashier', 'asset://word_scenes/cashier.png'),
        ('charger', 'asset://word_scenes/charger.png'),
        ('commuter', 'asset://word_scenes/commuter.png'),
        ('counter', 'asset://word_scenes/counter.png'),
        ('cursor', 'asset://word_scenes/cursor.png'),
        ('deadline', 'asset://word_scenes/deadline.png'),
        ('discount', 'asset://word_scenes/discount.png'),
        ('escalator', 'asset://word_scenes/escalator.png'),
        ('espresso', 'asset://word_scenes/espresso.png'),
        ('keyboard', 'asset://word_scenes/keyboard.png'),
        ('luggage', 'asset://word_scenes/luggage.png'),
        ('menu', 'asset://word_scenes/menu.png'),
        ('microwave', 'asset://word_scenes/microwave.png'),
        ('monitor', 'asset://word_scenes/monitor.png'),
        ('mug', 'asset://word_scenes/mug.png'),
        ('napkin', 'asset://word_scenes/napkin.png'),
        ('notebook', 'asset://word_scenes/notebook.png'),
        ('pastry', 'asset://word_scenes/pastry.png'),
        ('platform', 'asset://word_scenes/platform.png'),
        ('prototype', 'asset://word_scenes/prototype.png'),
        ('ticket', 'asset://word_scenes/ticket.png'),
        ('timetable', 'asset://word_scenes/timetable.png'),
        ('transfer', 'asset://word_scenes/transfer.png'),
        ('umbrella', 'asset://word_scenes/umbrella.png'),
        ('voucher', 'asset://word_scenes/voucher.png')
)
UPDATE memory_palace_words AS word
SET
    source_image_url = fixed.image_url,
    update_time = now()
FROM fixed, word_assets AS asset
WHERE asset.word_key = fixed.word_key
  AND word.word_asset_id = asset.id;

WITH fixed(word_key, image_url) AS (
    VALUES
        ('aisle', 'asset://word_scenes/aisle.png'),
        ('announcement', 'asset://word_scenes/announcement.png'),
        ('archive', 'asset://word_scenes/archive.png'),
        ('aroma', 'asset://word_scenes/aroma.png'),
        ('barcode', 'asset://word_scenes/barcode.png'),
        ('barista', 'asset://word_scenes/barista.png'),
        ('beverage', 'asset://word_scenes/beverage.png'),
        ('cashier', 'asset://word_scenes/cashier.png'),
        ('charger', 'asset://word_scenes/charger.png'),
        ('commuter', 'asset://word_scenes/commuter.png'),
        ('counter', 'asset://word_scenes/counter.png'),
        ('cursor', 'asset://word_scenes/cursor.png'),
        ('deadline', 'asset://word_scenes/deadline.png'),
        ('discount', 'asset://word_scenes/discount.png'),
        ('escalator', 'asset://word_scenes/escalator.png'),
        ('espresso', 'asset://word_scenes/espresso.png'),
        ('keyboard', 'asset://word_scenes/keyboard.png'),
        ('luggage', 'asset://word_scenes/luggage.png'),
        ('menu', 'asset://word_scenes/menu.png'),
        ('microwave', 'asset://word_scenes/microwave.png'),
        ('monitor', 'asset://word_scenes/monitor.png'),
        ('mug', 'asset://word_scenes/mug.png'),
        ('napkin', 'asset://word_scenes/napkin.png'),
        ('notebook', 'asset://word_scenes/notebook.png'),
        ('pastry', 'asset://word_scenes/pastry.png'),
        ('platform', 'asset://word_scenes/platform.png'),
        ('prototype', 'asset://word_scenes/prototype.png'),
        ('ticket', 'asset://word_scenes/ticket.png'),
        ('timetable', 'asset://word_scenes/timetable.png'),
        ('transfer', 'asset://word_scenes/transfer.png'),
        ('umbrella', 'asset://word_scenes/umbrella.png'),
        ('voucher', 'asset://word_scenes/voucher.png')
)
UPDATE ar_scan_words AS word
SET
    source_image_url = fixed.image_url,
    update_time = now()
FROM fixed, word_assets AS asset
WHERE asset.word_key = fixed.word_key
  AND word.word_asset_id = asset.id;

WITH fixed(word_key, image_url) AS (
    VALUES
        ('aisle', 'asset://word_scenes/aisle.png'),
        ('announcement', 'asset://word_scenes/announcement.png'),
        ('archive', 'asset://word_scenes/archive.png'),
        ('aroma', 'asset://word_scenes/aroma.png'),
        ('barcode', 'asset://word_scenes/barcode.png'),
        ('barista', 'asset://word_scenes/barista.png'),
        ('beverage', 'asset://word_scenes/beverage.png'),
        ('cashier', 'asset://word_scenes/cashier.png'),
        ('charger', 'asset://word_scenes/charger.png'),
        ('commuter', 'asset://word_scenes/commuter.png'),
        ('counter', 'asset://word_scenes/counter.png'),
        ('cursor', 'asset://word_scenes/cursor.png'),
        ('deadline', 'asset://word_scenes/deadline.png'),
        ('discount', 'asset://word_scenes/discount.png'),
        ('escalator', 'asset://word_scenes/escalator.png'),
        ('espresso', 'asset://word_scenes/espresso.png'),
        ('keyboard', 'asset://word_scenes/keyboard.png'),
        ('luggage', 'asset://word_scenes/luggage.png'),
        ('menu', 'asset://word_scenes/menu.png'),
        ('microwave', 'asset://word_scenes/microwave.png'),
        ('monitor', 'asset://word_scenes/monitor.png'),
        ('mug', 'asset://word_scenes/mug.png'),
        ('napkin', 'asset://word_scenes/napkin.png'),
        ('notebook', 'asset://word_scenes/notebook.png'),
        ('pastry', 'asset://word_scenes/pastry.png'),
        ('platform', 'asset://word_scenes/platform.png'),
        ('prototype', 'asset://word_scenes/prototype.png'),
        ('ticket', 'asset://word_scenes/ticket.png'),
        ('timetable', 'asset://word_scenes/timetable.png'),
        ('transfer', 'asset://word_scenes/transfer.png'),
        ('umbrella', 'asset://word_scenes/umbrella.png'),
        ('voucher', 'asset://word_scenes/voucher.png')
),
cover_candidates AS (
    SELECT DISTINCT ON (word.palace_id)
        word.palace_id,
        fixed.image_url
    FROM memory_palace_words AS word
    JOIN word_assets AS asset ON asset.id = word.word_asset_id
    JOIN fixed ON fixed.word_key = asset.word_key
    ORDER BY word.palace_id, word.create_time ASC, word.id ASC
)
UPDATE memory_palaces AS palace
SET
    cover_url = cover_candidates.image_url,
    update_time = now()
FROM cover_candidates
WHERE palace.id = cover_candidates.palace_id;
