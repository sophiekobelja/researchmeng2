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
-- MODULE DML050

-- SQL Test Suite, V6.0, Interactive SQL, dml050.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0226 FIPS sizing - 10 tables in SQL statement!
-- FIPS sizing TEST

     SELECT EMPNUM, EMPNAME 
     FROM STAFF 
     WHERE EMPNUM IN 
       (SELECT EMPNUM  FROM WORKS 
        WHERE PNUM IN 
          (SELECT PNUM  FROM PROJ 
           WHERE PTYPE IN 
             (SELECT PTYPE  FROM PROJ 
              WHERE PNUM IN 
                (SELECT PNUM  FROM WORKS 
                 WHERE EMPNUM IN 
                   (SELECT EMPNUM  FROM WORKS 
                    WHERE PNUM IN 
                      (SELECT PNUM   FROM PROJ 
                       WHERE PTYPE IN
                         (SELECT PTYPE  FROM PROJ
                          WHERE CITY IN
                            (SELECT CITY  FROM STAFF
                             WHERE EMPNUM IN
                               (SELECT EMPNUM  FROM WORKS
                                WHERE HOURS = 20 
                                AND PNUM = 'P2' )))))))));

-- PASS:0226 If 4 rows selected excluding EMPNUM='E5', EMPNAME='Ed'?

-- END TEST >>> 0226 <<< END TEST
-- *************************************************////END-OF-MODULE
