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

-- MODULE DML033

-- SQL Test Suite, V6.0, Interactive SQL, dml033.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0135 Upper and loer case letters are distinct!

-- setup
     INSERT INTO WORKS
            VALUES('UPP','low',100);
-- PASS:0135 If 1 row is inserted?

      SELECT EMPNUM,PNUM
           FROM WORKS
           WHERE EMPNUM='UPP' AND PNUM='low';
-- PASS:0135 If EMPNUM = 'UPP' and PNUM = 'low'?

      SELECT EMPNUM,PNUM
           FROM WORKS
           WHERE EMPNUM='upp' OR PNUM='LOW';
-- PASS:0135 If 0 rows are selected - out of data?

-- restore
     ROLLBACK WORK;
-- END TEST >>> 0135 <<< END TEST
-- *************************************************////END-OF-MODULE
