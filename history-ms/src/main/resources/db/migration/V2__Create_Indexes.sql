CREATE INDEX IF NOT EXISTS idx_history_servicename ON history(servicename);

CREATE INDEX IF NOT EXISTS idx_history_timestamp ON history(timestamp);

ANALYZE history;