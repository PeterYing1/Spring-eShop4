IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'WebhookSubscriptions')
BEGIN
    CREATE TABLE WebhookSubscriptions (
        Id        INT IDENTITY(1,1) PRIMARY KEY,
        Type      NVARCHAR(50)  NOT NULL,
        UserId    NVARCHAR(255) NOT NULL,
        DestUrl   NVARCHAR(500) NOT NULL,
        Token     NVARCHAR(255) NOT NULL,
        CreatedAt DATETIME2     NOT NULL DEFAULT GETUTCDATE()
    );

    CREATE INDEX IX_WebhookSubscriptions_UserId ON WebhookSubscriptions(UserId);
    CREATE INDEX IX_WebhookSubscriptions_Type   ON WebhookSubscriptions(Type);
END
