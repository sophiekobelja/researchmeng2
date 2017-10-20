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

-- MODULE DML004

-- SQL Test Suite, V6.0, Interactive SQL, dml004.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0008 SQLCODE 100:SELECT on empty table !
     SELECT EMPNUM,HOURS
          FROM WORKS
          WHERE PNUM = 'P8'
          ORDER BY EMPNUM DESC;

-- PASS:0008 If 0 rows selected, SQLCODE = 100, end of data?

-- END TEST >>> 0008 <<< END TEST
-- ****************************************************************

-- TEST:0009 SELECT NULL value!

-- setup
     INSERT INTO WORKS
            VALUES('E9','P9',NULL);
-- PASS:0009 If 1 row is inserted?

     SELECT EMPNUM
          FROM WORKS
          WHERE HOURS IS NULL;
-- PASS:0009 If EMPNUM = 'E9'?

          SELECT EMPNUM, HOURS
               FROM WORKS
               WHERE PNUM = 'P9'
               ORDER BY EMPNUM DESC;

-- PASS:0009 If EMPNUM = 'E9' and HOURS is NULL?

-- restore
     ROLLBACK WORK;


-- END TEST >>> 0009 <<< END TEST
-- ******************************************************************

-- NO_TEST:0161 FETCH NULL value without indicator, SQLCODE < 0!

-- Testing Indicators

-- **********************************************************

-- NO_TEST:0162 FETCH NULL value with indicator syntax!

-- Testing indicators

-- ****************************************************************

-- NO_TEST:0010 FETCH truncated CHAR column with indicator!

-- Testing indicators
-- *************************************************////END-OF-MODULE
