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

-- MODULE DML047

-- SQL Test Suite, V6.0, Interactive SQL, dml047.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0222 FIPS sizing -- Length(240) of a character string!
-- FIPS sizing TEST
-- NOTE:0222 Literal length is only 78

-- setup
     INSERT INTO T240 VALUES(
'Now is the time for all good men and women to come to the aid of their country'
);
-- PASS:0222 If 1 row is inserted?

     SELECT * 
          FROM T240;
-- PASS:0222 If STR240 starts with 'Now is the time for all good men'?
-- PASS:0222 and ends 'and women to come to the aid of their country'?

-- restore
     ROLLBACK WORK;

-- END TEST >>> 0222 <<< END TEST
-- *************************************************////END-OF-MODULE
