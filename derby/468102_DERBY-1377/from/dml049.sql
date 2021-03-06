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

-- MODULE DML049

-- SQL Test Suite, V6.0, Interactive SQL, dml049.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0225 FIPS sizing -- ten tables in FROM clause!
-- FIPS sizing TEST

-- setup
     INSERT INTO TEMP_S
            SELECT EMPNUM,GRADE,CITY
                 FROM STAFF
                 WHERE GRADE > 11;
-- PASS:0225 If 4 rows are inserted ?

     INSERT INTO STAFF1
            SELECT *
                 FROM STAFF;
-- PASS:0225 If 5 rows are inserted?

     INSERT INTO WORKS1
            SELECT *
                 FROM WORKS;
-- PASS:0225 If 12 rows are inserted?

     INSERT INTO STAFF4
            SELECT *
                 FROM STAFF;
-- PASS:0225 If 5 rows are inserted?

     INSERT INTO PROJ1
            SELECT *
                 FROM PROJ;
-- PASS:0225 If 6 rows are inserted?

                 SELECT STAFF.EMPNUM,PROJ.PNUM,WORKS.HOURS,
                        STAFF3.GRADE,STAFF4.CITY,WORKS1.HOURS,
                        TEMP_S.GRADE,PROJ1.PNUM,STAFF1.GRADE,
                        UPUNIQ.COL2
                 FROM   STAFF,PROJ,WORKS,STAFF3,STAFF4,WORKS1,
                        TEMP_S,PROJ1,STAFF1,UPUNIQ
                 WHERE  STAFF.EMPNUM = WORKS.EMPNUM    AND
                        PROJ.PNUM = WORKS.PNUM         AND
                        STAFF3.EMPNUM = WORKS.EMPNUM   AND
                        STAFF4.EMPNUM = WORKS.EMPNUM   AND
                        WORKS1.EMPNUM = WORKS.EMPNUM   AND
                        WORKS1.PNUM = WORKS.PNUM       AND
                        TEMP_S.EMPNUM = WORKS.EMPNUM   AND
                        PROJ1.PNUM = WORKS.PNUM        AND
                        STAFF1.EMPNUM = WORKS.EMPNUM   AND
                        UPUNIQ.COL2 = 'A'
                 ORDER BY 1, 2;
	;
-- PASS:0225 If 10 rows are selected ?
-- PASS:0225 If first STAFF.EMPNUM='E1',PROJ.PNUM='P1',WORKS.HOURS=40?
-- PASS:0225 If last  STAFF.EMPNUM='E4',PROJ.PNUM='P5',WORKS.HOURS=80?

-- restore
     ROLLBACK WORK;

-- END TEST >>> 0225 <<< END TEST

-- *************************************************////END-OF-MODULE
