CREATE TABLE IF NOT EXISTS users (
    name VARCHAR(100),
    email VARCHAR(100)
);

-- CREATE TABLE IF NOT EXISTS fraud_agg_minutely (
--   window_start TIMESTAMP,
--   accountId VARCHAR,
--   txn_count INT,
--   total_amount DOUBLE PRECISION,
--   PRIMARY KEY (window_start, accountId)
-- );

-- not used
-- CREATE TABLE IF NOT EXISTS fraud_agg_minutely (
--   window_start TIMESTAMP(3) NOT NULL,
--   accountId VARCHAR NOT NULL,
--   txn_count BIGINT NOT NULL,
--   total_amount DOUBLE PRECISION NOT NULL,
--   PRIMARY KEY (window_start, accountId)
-- );