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

-- MODULE  DML165  

-- SQL Test Suite, V6.0, Interactive SQL, dml165.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0870 Non-identical descriptors in UNION!

   CREATE TABLE APPLES (
     KEY1 INT,
     APPLE_NAME CHAR (15));
-- PASS:0870 If table created successfully?

   COMMIT WORK;

   CREATE TABLE ORANGES (
     KEY2 FLOAT,
     ORANGE_NAME VARCHAR (10));
-- PASS:0870 If table ceated successfully?

   COMMIT WORK;

   INSERT INTO APPLES VALUES (
     1, 'Granny Smith');
-- PASS:0870 If 1 row inserted successfully?

   INSERT INTO APPLES VALUES (
     2, 'Red Delicious');
-- PASS:0870 If 1 row inserted successfully?

   INSERT INTO ORANGES VALUES (
     1.5E0, 'Navel');
-- PASS:0870 If 1 row inserted successfully?

   INSERT INTO ORANGES VALUES (
     2.5E0, 'Florida');
-- PASS:0870 If 1 row inserted successfully?

   SELECT * FROM APPLES UNION ALL SELECT * FROM ORANGES
     ORDER BY 1;
-- PASS:0870 If 4 rows returned in the following order?
--                col1                 col2
--                ====                 ====
-- PASS:0870 If   1.0 (+ or - 0.01)    Granny Smith?
-- PASS:0870 If   1.5 (+ or - 0.01)    Navel?
-- PASS:0870 If   2.0 (+ or - 0.01)    Red Delicious?
-- PASS:0870 If   2.5 (+ or - 0.01)    Florida?

   COMMIT WORK;

--O   DROP TABLE APPLES CASCADE;
   DROP TABLE APPLES ;
-- PASS:0870 If table dropped successfully?

   COMMIT WORK;

--O   DROP TABLE ORANGES CASCADE;
   DROP TABLE ORANGES ;
-- PASS:0870 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0870 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE