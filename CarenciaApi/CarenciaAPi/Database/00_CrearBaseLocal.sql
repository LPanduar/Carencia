/* =========================================================
   CARENCIA - Sólo para tu PC (SQL Server local / Express)
   Crea la base vacía. Después ejecuta CarenciaDB.sql dentro de ella.
   En un hosting (MonsterASP, Azure...) NO se usa: la base ya te la dan creada.
   ========================================================= */
IF DB_ID('CarenciaDB') IS NULL
    CREATE DATABASE CarenciaDB;
GO
