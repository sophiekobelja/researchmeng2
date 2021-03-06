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

-- MODULE  YTS811  

-- SQL Test Suite, V6.0, Interactive SQL, yts811.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1
   set schema CTS1;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7568 WHERE <search condition> referencing column!

   CREATE VIEW V_DATA_TYPE AS
     SELECT SUM(NUM) AS VT1, ING AS VT2, SMA AS VT3
     FROM DATA_TYPE
     GROUP BY ING, SMA;
-- PASS:7568 If view created successfully?

   COMMIT WORK;

--O   INSERT INTO CTS1.DATA_TYPE (ING, SMA) VALUES
   INSERT INTO DATA_TYPE (ING, SMA) VALUES
     (1,1);
-- PASS:7568 If 1 row inserted successfully?

--O   INSERT INTO CTS1.DATA_TYPE (NUM, ING, SMA)
   INSERT INTO DATA_TYPE (NUM, ING, SMA)
     VALUES (2,2,3);
-- PASS:7568 If 1 row inserted successfully?

--O   INSERT INTO CTS1.DATA_TYPE (NUM, ING, SMA)
   INSERT INTO DATA_TYPE (NUM, ING, SMA)
     VALUES (3,4,5);
-- PASS:7568 If 1 row inserted successfully?

--O   INSERT INTO CTS1.DATA_TYPE (NUM, ING, SMA)
   INSERT INTO DATA_TYPE (NUM, ING, SMA)
     VALUES (2,2,3);
-- PASS:7568 If 1 row inserted successfully?

--O   INSERT INTO CTS1.DATA_TYPE (NUM, ING, SMA)
   INSERT INTO DATA_TYPE (NUM, ING, SMA)
     VALUES (5,4,3);
-- PASS:7568 If 1 row inserted successfully?

   SELECT VT1, VT2, VT3
     FROM V_DATA_TYPE
     WHERE NOT VT1 = 0
     ORDER BY VT2, VT3;
-- PASS:7568 If WARNING - null value eliminated in set function?
-- PASS:7568 If 3 rows are returned in the following order?
--                VT1     VT2     VT3
--                ===     ===     ===
-- PASS:7568 If   4       2       3  ?
-- PASS:7568 If   5       4       3  ?
-- PASS:7568 If   3       4       5  ?

   SELECT VT1, VT2, VT3
     FROM V_DATA_TYPE WHERE VT2 = 1;
-- PASS:7568 If WARNING - null value eliminated in set function?
-- PASS:7568 If  NULL, 1, 1?

   SELECT SUM(NUM) 
     FROM DATA_TYPE
     WHERE NUM IS NOT NULL;
-- PASS:7568 If SUM = 12?

   SELECT SUM(VT1) AS SUNTA1 
     FROM V_DATA_TYPE AS TB3;
-- PASS:7568 If WARNING - null value eliminated in set function?
-- PASS:7568 If SUM = 12?

   ROLLBACK WORK;

   DROP VIEW V_DATA_TYPE;

   COMMIT WORK;

-- END TEST >>> 7568 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
