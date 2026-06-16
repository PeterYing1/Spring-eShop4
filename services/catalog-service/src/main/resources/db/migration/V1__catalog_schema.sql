IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CatalogBrand')
BEGIN
CREATE TABLE CatalogBrand (
    Id    INT IDENTITY(1,1) PRIMARY KEY,
    Brand NVARCHAR(100) NOT NULL
);
END

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CatalogType')
BEGIN
CREATE TABLE CatalogType (
    Id   INT IDENTITY(1,1) PRIMARY KEY,
    Type NVARCHAR(100) NOT NULL
);
END

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Catalog')
BEGIN
CREATE TABLE Catalog (
    Id                INT IDENTITY(1,1) PRIMARY KEY,
    Name              NVARCHAR(50)  NOT NULL,
    Description       NVARCHAR(255) NULL,
    Price             DECIMAL(18,2) NOT NULL,
    PictureFileName   NVARCHAR(255) NULL,
    CatalogTypeId     INT NOT NULL,
    CatalogBrandId    INT NOT NULL,
    AvailableStock    INT NOT NULL DEFAULT 0,
    RestockThreshold  INT NOT NULL DEFAULT 0,
    MaxStockThreshold INT NOT NULL DEFAULT 0,
    OnReorder         BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_Catalog_CatalogType  FOREIGN KEY (CatalogTypeId)  REFERENCES CatalogType(Id)  ON DELETE CASCADE,
    CONSTRAINT FK_Catalog_CatalogBrand FOREIGN KEY (CatalogBrandId) REFERENCES CatalogBrand(Id) ON DELETE CASCADE
);
CREATE INDEX IX_Catalog_CatalogBrandId ON Catalog(CatalogBrandId);
CREATE INDEX IX_Catalog_CatalogTypeId  ON Catalog(CatalogTypeId);
END

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'IntegrationEventLog')
BEGIN
CREATE TABLE IntegrationEventLog (
    EventId       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    EventTypeName NVARCHAR(255)    NOT NULL,
    State         INT              NOT NULL,
    TimesSent     INT              NOT NULL DEFAULT 0,
    CreationTime  DATETIME2        NOT NULL,
    Content       NVARCHAR(MAX)    NOT NULL,
    TransactionId NVARCHAR(255)    NULL
);
END
