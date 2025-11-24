-- Definition of the database for the tests
-- public.asset_type definition

-- Drop table
DROP TABLE IF EXISTS asset_datapoint;
DROP TABLE IF EXISTS asset;

-- Activos
CREATE TABLE asset (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL UNIQUE,
  type_code TEXT NOT NULL,
  category_code TEXT NOT NULL,
  currency CHAR(3) NOT NULL,
  archived BOOLEAN NOT NULL DEFAULT FALSE,
  archived_at DATE DEFAULT NULL
);
CREATE INDEX ON asset(archived);


-- Datapoints (moneda original)
CREATE TABLE asset_datapoint (
  asset_id INT REFERENCES asset(id) ON DELETE CASCADE,
  d DATE NOT NULL,
  balance NUMERIC(20,2) NOT NULL,
  gain NUMERIC(20,2),
  contribution NUMERIC(20,2),
  PRIMARY KEY (asset_id, d)
);
CREATE INDEX ON asset_datapoint(d);
