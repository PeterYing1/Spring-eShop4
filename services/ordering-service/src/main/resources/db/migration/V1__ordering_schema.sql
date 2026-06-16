-- =============================================================================
-- V1__ordering_schema.sql
-- Full DDL for the ordering schema (SQL Server)
-- =============================================================================

-- Create schema
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'ordering')
    EXEC('CREATE SCHEMA ordering');

-- =============================================================================
-- Lookup tables
-- =============================================================================

-- cardtypes
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'cardtypes' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.cardtypes (
        Id   INT          NOT NULL,
        Name NVARCHAR(200) NOT NULL,
        CONSTRAINT PK_cardtypes PRIMARY KEY (Id)
    );
    INSERT INTO ordering.cardtypes (Id, Name) VALUES (1, 'Amex');
    INSERT INTO ordering.cardtypes (Id, Name) VALUES (2, 'Visa');
    INSERT INTO ordering.cardtypes (Id, Name) VALUES (3, 'MasterCard');
END;

-- orderstatus
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'orderstatus' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.orderstatus (
        Id   INT          NOT NULL,
        Name NVARCHAR(200) NOT NULL,
        CONSTRAINT PK_orderstatus PRIMARY KEY (Id)
    );
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (1, 'submitted');
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (2, 'awaitingvalidation');
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (3, 'stockconfirmed');
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (4, 'paid');
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (5, 'shipped');
    INSERT INTO ordering.orderstatus (Id, Name) VALUES (6, 'cancelled');
END;

-- =============================================================================
-- Buyer aggregate
-- =============================================================================

-- buyers
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'buyers' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.buyers (
        Id           INT           NOT NULL IDENTITY(1,1),
        IdentityGuid NVARCHAR(200) NOT NULL,
        Name         NVARCHAR(200) NOT NULL,
        CONSTRAINT PK_buyers        PRIMARY KEY (Id),
        CONSTRAINT UQ_buyers_guid   UNIQUE (IdentityGuid)
    );
END;

-- paymentmethods
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'paymentmethods' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.paymentmethods (
        Id             INT           NOT NULL IDENTITY(1,1),
        BuyerId        INT           NOT NULL,
        Alias          NVARCHAR(200) NULL,
        CardHolderName NVARCHAR(200) NOT NULL,
        CardNumber     NVARCHAR(25)  NOT NULL,
        CardTypeId     INT           NOT NULL,
        Expiration     DATETIME2     NOT NULL,
        CONSTRAINT PK_paymentmethods PRIMARY KEY (Id),
        CONSTRAINT FK_paymentmethods_buyers
            FOREIGN KEY (BuyerId) REFERENCES ordering.buyers (Id)
            ON DELETE CASCADE,
        CONSTRAINT FK_paymentmethods_cardtypes
            FOREIGN KEY (CardTypeId) REFERENCES ordering.cardtypes (Id)
    );

    CREATE INDEX IX_paymentmethods_BuyerId ON ordering.paymentmethods (BuyerId);
END;

-- =============================================================================
-- Order aggregate
-- =============================================================================

-- orders
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'orders' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.orders (
        Id              INT           NOT NULL IDENTITY(1,1),
        OrderDate       DATETIME2     NOT NULL,
        Address_City    NVARCHAR(200) NULL,
        Address_Country NVARCHAR(200) NULL,
        Address_State   NVARCHAR(200) NULL,
        Address_Street  NVARCHAR(200) NULL,
        Address_ZipCode NVARCHAR(18)  NULL,
        BuyerId         INT           NULL,
        OrderStatusId   INT           NOT NULL,
        Description     NVARCHAR(MAX) NULL,
        PaymentMethodId INT           NULL,
        CONSTRAINT PK_orders PRIMARY KEY (Id),
        CONSTRAINT FK_orders_buyers
            FOREIGN KEY (BuyerId) REFERENCES ordering.buyers (Id),
        CONSTRAINT FK_orders_orderstatus
            FOREIGN KEY (OrderStatusId) REFERENCES ordering.orderstatus (Id),
        CONSTRAINT FK_orders_paymentmethods
            FOREIGN KEY (PaymentMethodId) REFERENCES ordering.paymentmethods (Id)
    );

    CREATE INDEX IX_orders_BuyerId      ON ordering.orders (BuyerId);
    CREATE INDEX IX_orders_OrderStatusId ON ordering.orders (OrderStatusId);
END;

-- orderItems
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'orderItems' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.orderItems (
        Id          INT             NOT NULL IDENTITY(1,1),
        OrderId     INT             NOT NULL,
        ProductId   INT             NOT NULL,
        ProductName NVARCHAR(200)   NOT NULL,
        UnitPrice   DECIMAL(18, 2)  NOT NULL,
        Discount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
        Units       INT             NOT NULL DEFAULT 1,
        PictureUrl  NVARCHAR(MAX)   NULL,
        CONSTRAINT PK_orderItems PRIMARY KEY (Id),
        CONSTRAINT FK_orderItems_orders
            FOREIGN KEY (OrderId) REFERENCES ordering.orders (Id)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_orderItems_OrderId ON ordering.orderItems (OrderId);
END;

-- =============================================================================
-- Idempotency
-- =============================================================================

-- requests (idempotency table used by IdentifiedCommandHandler)
IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'requests' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.requests (
        Id   NVARCHAR(36) NOT NULL,
        Name NVARCHAR(200) NOT NULL,
        Time DATETIME2     NOT NULL,
        CONSTRAINT PK_requests PRIMARY KEY (Id)
    );
END;

-- =============================================================================
-- Integration event outbox
-- =============================================================================

IF NOT EXISTS (SELECT * FROM sys.tables
               WHERE name = 'IntegrationEventLog' AND schema_id = SCHEMA_ID('ordering'))
BEGIN
    CREATE TABLE ordering.IntegrationEventLog (
        EventId         UNIQUEIDENTIFIER NOT NULL,
        EventTypeName   NVARCHAR(500)    NOT NULL,
        State           INT              NOT NULL DEFAULT 0,
        TimesSent       INT              NOT NULL DEFAULT 0,
        CreationTime    DATETIME2        NOT NULL,
        Content         NVARCHAR(MAX)    NOT NULL,
        TransactionId   NVARCHAR(36)     NULL,
        CONSTRAINT PK_IntegrationEventLog PRIMARY KEY (EventId)
    );

    CREATE INDEX IX_IntegrationEventLog_TransactionId
        ON ordering.IntegrationEventLog (TransactionId);
END;
