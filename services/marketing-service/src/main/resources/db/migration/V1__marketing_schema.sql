IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Campaigns')
BEGIN
CREATE TABLE Campaigns (
    Id           INT IDENTITY(1,1) PRIMARY KEY,
    Description  NVARCHAR(500) NULL,
    CampaignTypeId INT NOT NULL DEFAULT 1,
    CampaignType NVARCHAR(100) NULL,
    [From]       DATETIME2 NOT NULL,
    [To]         DATETIME2 NOT NULL,
    PictureUrl   NVARCHAR(500) NULL,
    DetailsUrl   NVARCHAR(500) NULL,
    ActionText   NVARCHAR(255) NULL
);
END
