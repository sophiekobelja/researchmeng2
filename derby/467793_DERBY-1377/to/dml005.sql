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
-- MODULE DML005

-- SQL Test Suite, V6.0, Interactive SQL, dml005.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0011 FIPS sizing - DECIMAL (15)!
-- FIPS sizing TEST

-- setup
--O     DELETE FROM LONGINT;
     DELETE FROM LONGINTTAB;

-- setup
--O     INSERT INTO LONGINT
     INSERT INTO LONGINTTAB
            VALUES(123456789012345.);
-- PASS:0011 If 1 row is inserted?

     SELECT LONG_INT, LONG_INT /1000000, LONG_INT - 123456789000000.
--O          FROM LONGINT;
          FROM LONGINTTAB;

-- PASS:0011 If values are (123456789012345, 123456789, 12345), but?
-- PASS:0011 Second value may be between 123456788 and 123456790?

-- END TEST >>> 0011 <<< END TEST
-- *************************************************////END-OF-MODULE
