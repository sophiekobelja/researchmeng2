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

-- MODULE  DML134  

-- SQL Test Suite, V6.0, Interactive SQL, dml134.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
--O   ROLLBACK WORK;

-- date_time print

-- NO_TEST:0688 INFO_SCHEM:  Dynamic changes are visible!

-- Testing dynamic SQL

-- *********************************************

-- TEST:0689 Many Trans SQL features #1:  inventory system!

   CREATE TABLE COST_CODES (
  COSTCODE INT NOT NULL UNIQUE,
  COSTTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE CONDITION_CODES (
  CONDCODE INT NOT NULL UNIQUE,
  CONDTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE ITEM_CODES (
  ITEMCODE INT NOT NULL PRIMARY KEY,
  ITEMTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE INVENTORY (
--O  COSTCODE INT REFERENCES COST_CODES (COSTCODE),
--O  CONDCODE INT REFERENCES CONDITION_CODES (CONDCODE),
--O  ITEMCODE INT REFERENCES ITEM_CODES);
  COSTCODE INT ,
  CONDCODE INT ,
  ITEMCODE INT );
-- PASS:0689 If table is created?

--O   COMMIT;

--O   CREATE VIEW COMPLETES AS
--O  SELECT ITEMTEXT, CONDTEXT, COSTTEXT
--O    FROM INVENTORY NATURAL JOIN COST_CODES
--O                   NATURAL JOIN CONDITION_CODES
--O                   NATURAL JOIN ITEM_CODES;
-- PASS:0689 If view is created?

--O   COMMIT;

--O   CREATE VIEW INCOMPLETES AS
--O  SELECT ITEMTEXT, CONDTEXT, COSTTEXT
--O    FROM INVENTORY, COST_CODES, CONDITION_CODES, ITEM_CODES
--O      WHERE INVENTORY.ITEMCODE = ITEM_CODES.ITEMCODE
--O        AND ((INVENTORY.CONDCODE = CONDITION_CODES.CONDCODE
--O              AND INVENTORY.COSTCODE IS NULL
--O              AND COST_CODES.COSTCODE IS NULL)
--O          OR (INVENTORY.COSTCODE = COST_CODES.COSTCODE
--O              AND INVENTORY.CONDCODE IS NULL
--O              AND CONDITION_CODES.CONDCODE IS NULL));
-- PASS:0689 If view is created?

--O   COMMIT;

--O   CREATE VIEW VERBOSE_INV AS
--O  SELECT * FROM COMPLETES UNION SELECT * FROM INCOMPLETES;
-- PASS:0689 If view is created?

--O   COMMIT;

   INSERT INTO COST_CODES VALUES (
   NULL,
   RTRIM ('No cost code assigned                             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   0,
   RTRIM ('Expensive                                         '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   1,
   RTRIM ('Absurdly expensive                                '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   2,
   RTRIM ('Outrageously expensive                            '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   3,
   RTRIM ('Robbery; a complete and total rip-off             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   NULL,
   RTRIM ('Unknown                                           '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   1,
   RTRIM ('Slightly used                                     '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   2,
   RTRIM ('Returned as defective                             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   3,
   RTRIM ('Visibly damaged (no returns)                      '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   1,
   RTRIM ('Lousy excuse for a tape deck                      '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   3,
   RTRIM ('World''s worst VCR                                 '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   4,
   RTRIM ('Irreparable intermittent CD player                '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   7,
   RTRIM ('Self-destruct VGA monitor w/ critical need detect '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (3, NULL, 4);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (1, 2, 3);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (2, 3, 7);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (0, 3, 1);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (3, 1, 7);
-- PASS:0689 If 1 row is inserted?

--O   SELECT COUNT(*) FROM VERBOSE_INV;
-- PASS:0689 If count = 5?

--O   SELECT COUNT(*) FROM INCOMPLETES;
-- PASS:0689 If count = 1?

--O   SELECT COUNT(*) FROM COMPLETES;
-- PASS:0689 If count = 4?

--O   SELECT COUNT(*) FROM VERBOSE_INV
--O  WHERE ITEMTEXT = 'Irreparable intermittent CD player'
--O  AND CONDTEXT = 'Unknown'
--O  AND COSTTEXT = 'Robbery; a complete and total rip-off';
-- PASS:0689 If count = 1?

--O   SELECT COUNT(*) FROM VERBOSE_INV
--O  WHERE ITEMTEXT = 'Lousy excuse for a tape deck'
--O  AND CONDTEXT = 'Visibly damaged (no returns)'
--O  AND COSTTEXT = 'Expensive';
-- PASS:0689 If count = 1?

--O   SELECT COUNT(*) FROM VERBOSE_INV
--O  WHERE ITEMTEXT =
--O  'Self-destruct VGA monitor w/ critical need detect'
--O  AND CONDTEXT = 'Slightly used'
--O  AND COSTTEXT = 'Robbery; a complete and total rip-off';
-- PASS:0689 If count = 1?

--O   SELECT COUNT(*) FROM VERBOSE_INV
--O  WHERE ITEMTEXT =
--O  'Self-destruct VGA monitor w/ critical need detect'
--O  AND CONDTEXT = 'Visibly damaged (no returns)'
--O  AND COSTTEXT = 'Outrageously expensive';
-- PASS:0689 If count = 1?

--O   SELECT COUNT(*) FROM VERBOSE_INV
--O  WHERE ITEMTEXT = 'World''s worst VCR'
--O  AND CONDTEXT = 'Returned as defective'
--O  AND COSTTEXT = 'Absurdly expensive';
-- PASS:0689 If count = 1?

   COMMIT;

--O   DROP TABLE INVENTORY CASCADE;
   DROP TABLE INVENTORY ;
-- PASS:0689 If table and 3 views are dropped?

   COMMIT;

--O   DROP TABLE COST_CODES CASCADE;
   DROP TABLE COST_CODES ;
-- PASS:0689 If table is dropped?

   COMMIT;

--O   DROP TABLE CONDITION_CODES CASCADE;
   DROP TABLE CONDITION_CODES ;
-- PASS:0689 If table is dropped?

   COMMIT;

--O   DROP TABLE ITEM_CODES CASCADE;
   DROP TABLE ITEM_CODES ;
-- PASS:0689 If table is dropped?

   COMMIT;

-- END TEST >>> 0689 <<< END TEST

-- *********************************************

-- TEST:0690 Many Trans SQL features #2:  talk show schedule!

--O   CREATE TABLE PORGRAM (
--O  SEGNO    INT PRIMARY KEY,
--O  STARTS   TIME NOT NULL,
--O  LASTS    INTERVAL MINUTE TO SECOND NOT NULL,
--O  SEGMENT  VARCHAR (50));
-- PASS:0690 If table is created?

--O   COMMIT;

--O   CREATE VIEW GAPS AS
--O  SELECT * FROM PORGRAM AS OUTERR WHERE NOT EXISTS
--O    (SELECT * FROM PORGRAM AS INNERR WHERE OUTERR.STARTS
--O    + OUTERR.LASTS = INNERR.STARTS);
-- PASS:0690 If view is created?

--O   COMMIT;

--O   INSERT INTO PORGRAM VALUES (
--O  1, TIME( '12:00:00'),
--O  CAST ('10:00' AS INTERVAL MINUTE TO SECOND),
--O  'Monologue');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  2, TIME( '12:10:00'),
--O  CAST ('04:30' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  3, TIME( '12:14:30'),
--O  CAST ('12:30' AS INTERVAL MINUTE TO SECOND),
--O  'Braunschweiger, plug Explosion Man II');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  4, TIME( '12:27:00'),
--O  CAST ('03:00' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  5, TIME( '12:30:00'),
--O  CAST ('00:10' AS INTERVAL MINUTE TO SECOND),
--O  'Tease');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  6, TIME( '12:30:10'),
--O  CAST ('03:50' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials, local news');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  7, TIME( '12:34:00'),
--O  CAST ('11:00' AS INTERVAL MINUTE TO SECOND),
--O  'Spinal Tap, plug Asexual Harassment');
--O-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  8, TIME( '12:45:00'),
--O  CAST ('05:00' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  9, TIME( '12:50:00'),
--O  CAST ('05:00' AS INTERVAL MINUTE TO SECOND),
--O  'Spinal Tap, play Ode du Toilette');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  10, TIME( '12:55:00'),
--O  CAST ('03:00' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  11, TIME( '12:58:00'),
--O  CAST ('00:10' AS INTERVAL MINUTE TO SECOND),
--O  'Credits');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  12, TIME( '12:58:10'),
--O  CAST ('01:50' AS INTERVAL MINUTE TO SECOND),
--O  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

--O   INSERT INTO PORGRAM VALUES (
--O  13, TIME( '13:00:00'),
--O  CAST ('00:00' AS INTERVAL MINUTE TO SECOND),
--O  'END');
-- PASS:0690 If 1 row is inserted?

--O   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

--O   UPDATE PORGRAM
--O  SET STARTS = TIME( '12:14:30')
--O  WHERE SEGNO = 7;
-- PASS:0690 If 1 row is updated?

--O   UPDATE PORGRAM SET STARTS = STARTS -
--O  CAST ('01:30' AS INTERVAL MINUTE TO SECOND)
--O  WHERE SEGNO >= 4 AND SEGNO <= 6;
-- PASS:0690 If 3 rows are updated?

--O   UPDATE PORGRAM SET STARTS = TIME( '12:28:40') +
--O  CAST ('03:50' AS INTERVAL MINUTE TO SECOND)
--O  WHERE SEGNO = 3;
-- PASS:0690 If 1 row is updated?

--O   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

--O  SELECT SEGNO FROM PORGRAM ORDER BY STARTS;
-- PASS:0690 If 13 rows selected with SEGNO in the following order?
-- PASS:0690      1
-- PASS:0690      2
-- PASS:0690      7
-- PASS:0690      4
-- PASS:0690      5
-- PASS:0690      6
-- PASS:0690      3
-- PASS:0690      8
-- PASS:0690      9
-- PASS:0690     10
-- PASS:0690     11
-- PASS:0690     12
-- PASS:0690     13

--O   UPDATE PORGRAM SET LASTS = LASTS -
--O  CAST (30 AS INTERVAL SECOND) WHERE SEGNO
--O  = 10;
-- PASS:0690 If 1 row is updated?

--O   SELECT SEGNO FROM GAPS;
-- PASS:0690 If 1 row selected and SEGNO = 10?

--O   UPDATE PORGRAM SET LASTS = LASTS +
--O  CAST ('30' AS INTERVAL SECOND) WHERE
--O  SEGNO = 9;
-- PASS:0690 If 1 row is updated?

--O   UPDATE PORGRAM SET STARTS = STARTS +
--O  CAST (30. AS INTERVAL SECOND) WHERE
--O  SEGNO = 10;
-- PASS:0690 If 1 row is updated?

--O   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

--O   COMMIT;

--O   DROP TABLE PORGRAM CASCADE;
-- PASS:0690 If table and view are dropped?

--O   COMMIT;

-- END TEST >>> 0690 <<< END TEST

-- *********************************************

-- TEST:0691 INFO_SCHEM:  SQLSTATEs for length overruns!

   CREATE TABLE LONG1 (
  C1 INT,
  CHECK (
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL));
-- PASS:0691 If WARNING:  search condition too long for information schema?
-- PASS:0691 OR successful completion?

   ROLLBACK WORK;

   CREATE VIEW LONG2 AS
  SELECT * FROM USIG WHERE
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL;
-- PASS:0691 If WARNING:  query expression too long for information schema?
-- PASS:0691 OR successful completion?

   ROLLBACK WORK;

-- END TEST >>> 0691 <<< END TEST

-- *************************************************////END-OF-MODULE
