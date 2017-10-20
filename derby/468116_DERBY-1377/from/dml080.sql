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

-- MODULE DML080  

-- SQL Test Suite, V6.0, Interactive SQL, dml080.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION SCHANZLE
   create schema SHANZLE;
   set schema SHANZLE;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- NOTE Direct support for SQLCODE or SQLSTATE is not required
-- NOTE    in Interactive Direct SQL, as defined in FIPS 127-2.
-- NOTE   ********************* instead ***************************
-- NOTE If a statement raises an exception condition,
-- NOTE    then the system shall display a message indicating that
-- NOTE    the statement failed, giving a textual description
-- NOTE    of the failure.
-- NOTE If a statement raises a completion condition that is a
-- NOTE    "warning" or "no data", then the system shall display
-- NOTE    a message indicating that the statement completed,
-- NOTE    giving a textual description of the "warning" or "no data."


-- TEST:0462 SQLCODE 100: DELETE with no data!

   DELETE FROM HU.STAFF 
         WHERE EMPNUM = 'E7';
-- PASS:0462 If SQLCODE = 100 or no data, 0 rows deleted?

   DELETE FROM HU.STAFF;
-- PASS:0462 If 5 rows deleted?

   DELETE FROM HU.STAFF;
-- PASS:0462 If SQLCODE = 100 or no data, 0 rows deleted?

   DELETE FROM HU.STAFF
         WHERE EMPNUM = 'E1';
-- PASS:0462 If SQLCODE = 100 or no data, 0 rows deleted?

-- restore 
   ROLLBACK WORK;

-- END TEST >>> 0462 <<< END TEST
-- *********************************************

-- TEST:0463 SQLCODE 100: UPDATE with no data!

   UPDATE HU.STAFF
         SET CITY = 'NOWHERE'
         WHERE EMPNAME = 'NOBODY';
-- PASS:0463 If SQLCODE = 100 or no data, 0 rows updated? 

   UPDATE HU.STAFF
         SET GRADE = 11;
-- PASS:0463 If 5 rows updated?

   DELETE FROM HU.STAFF;
-- PASS:0463 If 5 rows deleted?

   UPDATE HU.STAFF
         SET CITY = 'NOWHERE';
-- PASS:0463 If SQLCODE = 100 or no data, 0 rows updated?

   UPDATE HU.STAFF
         SET CITY = 'NOWHERE'
         WHERE EMPNAME = 'NOBODY';
-- PASS:0463 If SQLCODE = 100 or no data, 0 rows updated?

-- restore
   ROLLBACK WORK;

-- END TEST >>> 0463 <<< END TEST
-- *************************************************////END-OF-MODULE
