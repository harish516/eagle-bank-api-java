
-- Create Eagle Bank user
CREATE USER eagle_user WITH PASSWORD 'eagle_password';

-- Grant privileges for user eagle_user
GRANT USAGE ON SCHEMA public TO eagle_user;
GRANT CREATE ON SCHEMA public TO eagle_user;

GRANT ALL PRIVILEGES ON DATABASE eagle_bank TO eagle_user;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create sequences for ID generation
CREATE SEQUENCE IF NOT EXISTS account_number_seq START WITH 1000000;
CREATE SEQUENCE IF NOT EXISTS transaction_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS user_id_seq START WITH 1;

-- Create custom functions for ID generation
CREATE OR REPLACE FUNCTION generate_account_number()
RETURNS TEXT AS $$
BEGIN
    RETURN '01' || LPAD(nextval('account_number_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_transaction_id()
RETURNS TEXT AS $$
BEGIN
    RETURN 'tan-' || encode(gen_random_bytes(6), 'hex');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_user_id()
RETURNS TEXT AS $$
BEGIN
    RETURN 'usr-' || encode(gen_random_bytes(6), 'hex');
END;
$$ LANGUAGE plpgsql; 