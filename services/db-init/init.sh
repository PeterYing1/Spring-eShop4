#!/bin/bash
set -e

SQLCMD=/opt/mssql-tools18/bin/sqlcmd

echo "Waiting for SQL Server to be ready..."
for i in $(seq 1 30); do
  $SQLCMD -S sqldata -U sa -P "Pass@word" -No -Q "SELECT 1" > /dev/null 2>&1 && echo "SQL Server is ready." && break
  echo "  attempt $i/30 — retrying in 3s..."
  sleep 3
done

echo "Creating databases..."
$SQLCMD -S sqldata -U sa -P "Pass@word" -No -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'CatalogDb')  CREATE DATABASE [CatalogDb];
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'OrderingDb') CREATE DATABASE [OrderingDb];
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MarketingDb') CREATE DATABASE [MarketingDb];
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'WebhooksDb') CREATE DATABASE [WebhooksDb];
PRINT 'Done.';
"
echo "All SQL Server databases are ready."
