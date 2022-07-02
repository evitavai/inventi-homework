CREATE TABLE IF NOT EXISTS "bank_accounts"
(
    id             BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    account_number VARCHAR(32) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "bank_account_transactions"
(
    id               BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    account_number   VARCHAR(32)  NOT NULL,
    transaction_date TIMESTAMP    NOT NULL,
    beneficiary      VARCHAR(255) NOT NULL,
    comment          VARCHAR(255),
    amount           NUMERIC      NOT NULL,
    currency         VARCHAR(16)  NOT NULL,
    is_withdrawal    BOOLEAN DEFAULT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE "bank_account_transactions"
    ADD CONSTRAINT ACCOUNT_BALANCE_ACCOUNT_NUMBER_FK
        FOREIGN KEY (account_number) REFERENCES "bank_accounts" (account_number);