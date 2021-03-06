--
--   Licensed to the Apache Software Foundation (ASF) under one or more
--   contributor license agreements.  See the NOTICE file distributed with
--   this work for additional information regarding copyright ownership.
--   The ASF licenses this file to You under the Apache License, Version 2.0
--   (the "License"); you may not use this file except in compliance with
--   the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--   See the License for the specific language governing permissions and
--   limitations under the License.
--
AUTOCOMMIT OFF;

-- MODULE   XTS730

-- SQL Test Suite, V6.0, Interactive SQL, xts730.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7030 Table name with 19 characters - delimited!

   CREATE TABLE "LONGIDENTIFIERSAAAA" (TNUM NUMERIC(5));
-- PASS:7030 If table created successfully?

   COMMIT WORK;

   CREATE TABLE "longidentifiersaaab" (TNUM NUMERIC(5));
-- PASS:7030 If table created successfully?

   COMMIT WORK;

   CREATE TABLE "0""LONGIDENTIFIERS_1" (TNUM NUMERIC(5));
-- PASS:7030 If table created successfully?

   COMMIT WORK;

   CREATE TABLE "0""LONGIDENTIFIERS_2" (TNUM NUMERIC(5));
-- PASS:7030 If table created successfully?

   COMMIT WORK;

   CREATE TABLE "lngIDENTIFIER% .,()" (TNUM NUMERIC(5));
-- PASS:7030 If table created successfully?

   COMMIT WORK;

--O   SELECT  COUNT(*)  
   SELECT   tablename 
--O         FROM INFORMATION_SCHEMA.TABLES
	from sys.systables
--O         WHERE TABLE_SCHEMA = 'CTS1' 
	where
--O         AND TABLE_TYPE = 'BASE TABLE'
          TABLETYPE = 'T'
--O         AND ( TABLE_NAME = 'LONGIDENTIFIERSAAAA'
--O            OR TABLE_NAME = 'longidentifiersaaab'
--O            OR TABLE_NAME = '0"LONGIDENTIFIERS_1'
--O            OR TABLE_NAME = '0"LONGIDENTIFIERS_2'
--O            OR TABLE_NAME = 'lngIDENTIFIER% .,()' );
         AND ( TABLENAME = 'LONGIDENTIFIERSAAAA'
            OR TABLENAME = 'longidentifiersaaab'
            OR TABLENAME = '0"LONGIDENTIFIERS_1'
            OR TABLENAME = '0"LONGIDENTIFIERS_2'
            OR TABLENAME = 'lngIDENTIFIER% .,()' );
-- PASS:7030 If COUNT = 5?

--O   SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
--O         WHERE TABLE_SCHEMA = 'CTS1' AND TABLE_TYPE = 'BASE TABLE'
--O         AND ( TABLE_NAME = 'LONGIDENTIFIERSAAAA'
--O            OR TABLE_NAME = 'longidentifiersaaab'
--O            OR TABLE_NAME = '0"LONGIDENTIFIERS_1'
--O            OR TABLE_NAME = '0"LONGIDENTIFIERS_2'
--O            OR TABLE_NAME = 'lngIDENTIFIER% .,()' )
--O         ORDER BY TABLE_NAME;
-- PASS:7030 If 5 rows are selected in following order?
--                    table_name
--                    ==========
-- PASS:7030 If   0"LONGIDENTIFIERS_1?
-- PASS:7030 If   0"LONGIDENTIFIERS_2?
-- PASS:7030 If   LONGIDENTIFIERSAAAA?
-- PASS:7030 If   lngIDENTIFIER% .,()?
-- PASS:7030 If   longidentifiersaaab?

   ROLLBACK WORK;

--O   DROP TABLE "LONGIDENTIFIERSAAAA" CASCADE;
   DROP TABLE "LONGIDENTIFIERSAAAA" ;
-- PASS:7030 If table dropped successfully?

--O   DROP TABLE "longidentifiersaaab" CASCADE;
   DROP TABLE "longidentifiersaaab" ;
-- PASS:7030 If table dropped successfully?

--O   DROP TABLE "0""LONGIDENTIFIERS_1" CASCADE;
   DROP TABLE "0""LONGIDENTIFIERS_1" ;
-- PASS:7030 If table dropped successfully?

--O   DROP TABLE "0""LONGIDENTIFIERS_2" CASCADE;
   DROP TABLE "0""LONGIDENTIFIERS_2" ;
-- PASS:7030 If table dropped successfully?

--O   DROP TABLE "lngIDENTIFIER% .,()" CASCADE;
   DROP TABLE "lngIDENTIFIER% .,()" ;
-- PASS:7030 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 7030 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
