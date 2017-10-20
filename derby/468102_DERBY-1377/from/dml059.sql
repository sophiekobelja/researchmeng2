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

-- MODULE DML059

-- SQL Test Suite, V6.0, Interactive SQL, dml059.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--0   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0257 SELECT MAX, MIN (COL1 + or - COL2)!

-- setup
     INSERT INTO VTABLE
            VALUES(10,11,12,13,15);
-- PASS:0257 If 1 row is inserted?

-- setup
     INSERT INTO VTABLE
            VALUES(100,111,1112,113,115);
-- PASS:0257 If 1 row is inserted?

     SELECT COL1, MAX(COL2 + COL3), MIN(COL3 - COL2)
          FROM VTABLE
          GROUP BY COL1
          ORDER BY COL1;
-- PASS:0257 If 4 rows are selected in order with values:?
-- PASS:0257    (    0,    3,    1) ?
-- PASS:0257    (   10,   50,    1)?
-- PASS:0257    (  100, 1223,  100)?
-- PASS:0257    ( 1000, 1000, 5000)?

-- restore
     ROLLBACK WORK; 

-- END TEST >>> 0257 <<< END TEST

-- *********************************************************************

-- TEST:0258 SELECT SUM(2*COL1*COL2) in HAVING SUM(COL2*COL3)!

-- setup
     INSERT INTO VTABLE
            VALUES (10,11,12,13,15);
-- PASS:0258 if 1 row is inserted?

-- setup
     INSERT INTO VTABLE
            VALUES (100,111,1112,113,115);
-- PASS:0258 if 1 row is inserted ?

     SELECT COL1,SUM(2 * COL2 * COL3)
                  FROM VTABLE
                  GROUP BY COL1
                  HAVING SUM(COL2 * COL3) > 2000 
                  OR SUM(COL2 * COL3) < -2000
                  ORDER BY COL1;

-- PASS:0258 If 2 rows are selected?
-- PASS:0258 If first row has values (100, 366864)    ?
-- PASS:0258 If second row has values (1000, -12000000)    ?


-- restore
     ROLLBACK WORK;

-- END TEST >>> 0258 <<< END TEST

-- *********************************************************************

-- TEST:0259 SOME, ANY in HAVING clause!

-- setup
     INSERT INTO VTABLE
            VALUES(10,11,12,13,15);
-- PASS:0259 If 1 row is inserted?

-- setup
     INSERT INTO VTABLE
            VALUES(100,111,1112,113,115);
-- PASS:0259 If 1 row is inserted?

     SELECT COL1, MAX(COL2)
          FROM VTABLE
          GROUP BY COL1
          HAVING MAX(COL2) > ANY (SELECT GRADE FROM STAFF)
          AND MAX(COL2) < SOME (SELECT HOURS FROM WORKS)
          ORDER BY COL1;
-- PASS:0259 If 1 row is selected and COL1 = 10 and MAX(COL2) = 20?

-- restore
     ROLLBACK WORK;

-- END TEST >>> 0259 <<< END TEST

-- *******************************************************************

-- TEST:0260 EXISTS in HAVING clause!

-- setup
     INSERT INTO VTABLE
            VALUES(10,11,12,13,15);
-- PASS:0260 If 1 row is inserted?

-- setup
     INSERT INTO VTABLE
            VALUES(100,111,1112,113,115);
-- PASS:0260 If 1 row is inserted?

     SELECT COL1, MAX(COL2)
          FROM VTABLE
          GROUP BY COL1
          HAVING EXISTS (SELECT *
                               FROM STAFF
                               WHERE EMPNUM = 'E1')
                               AND MAX(COL2) BETWEEN 10 AND 90
          ORDER BY COL1;
-- PASS:0260 If 1 row is selected and COL1 = 10 and MAX(COL2) = 20?

-- restore
     ROLLBACK WORK;

-- END TEST >>> 0260 <<< END TEST

-- ******************************************************************

-- TEST:0264 WHERE, HAVING without GROUP BY!

     SELECT SUM(COL1) 
          FROM VTABLE
          WHERE 10 + COL1 > COL2
          HAVING MAX(COL1) > 100;
-- PASS:0264 If SUM(COL1) = 1000?

     SELECT SUM(COL1)
          FROM VTABLE
          WHERE 1000 + COL1 >= COL2
          HAVING MAX(COL1) > 100;
-- PASS:0264 If SUM(COL1) = 1110?

-- END TEST >>> 0264 <<< END TEST

-- *************************************************////END-OF-MODULE
