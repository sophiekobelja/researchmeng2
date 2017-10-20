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

-- MODULE SDL012

-- SQL Test Suite, V6.0, Interactive SQL, sdl012.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0148 CREATE Table with NOT NULL!

     INSERT INTO STAFF1(EMPNAME,GRADE,CITY)
            VALUES('Carmen',40,'Boston');
-- PASS:0148 If ERROR, NOT NULL constraint, 0 rows inserted?
-- NOTE:0148 Not Null Column EMPNUM is missing.

--O      SELECT COUNT(*)
      SELECT *
           FROM STAFF1;
-- PASS:0148 If count = 0?

-- restore
     ROLLBACK WORK;

-- END TEST >>> 0148 <<< END TEST
-- *************************************************////END-OF-MODULE
