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

-- MODULE   XTS742

-- SQL Test Suite, V6.0, Interactive SQL, xts742.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1
   set schema CTS1;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7042 COUNT ALL <literal>!

   SELECT COUNT(ALL 115.5), COUNT(ALL 'ATHINA'), COUNT(ALL 255), 
         COUNT(*) FROM CL_DATA_TYPE;
-- PASS:7042 If COUNTs are 6, 6, 6, 6?

--O   INSERT INTO CTS1.CL_DATA_TYPE VALUES(NULL,55,225,10);
   INSERT INTO CL_DATA_TYPE VALUES(NULL,55,225,10);
-- PASS:7042 If 1 row inserted successfully?

--O   INSERT INTO CTS1.CL_DATA_TYPE VALUES(NULL,15,140,NULL);
   INSERT INTO CL_DATA_TYPE VALUES(NULL,15,140,NULL);
-- PASS:7042 If 1 row inserted successfully?

   SELECT COUNT(*),COUNT(ALL 119), COUNT(ALL 'GIORGOS') ,
         COUNT(CL_CHAR),
         COUNT(CL_REAL) FROM CL_DATA_TYPE;
-- PASS:7042 If COUNTs are 8, 8, 8, 6, 7?
-- PASS:7042 If WARNING - null value eliminated in set function?

--O   INSERT INTO CTS1.CL_DATA_TYPE VALUES(NULL,0,0,NULL);
   INSERT INTO CL_DATA_TYPE VALUES(NULL,0,0,NULL);
-- PASS:7042 If 1 row inserted successfully?

   SELECT COUNT(*), COUNT(ALL 1000), COUNT(ALL 'STEFOS'),
         COUNT(CL_CHAR),
         COUNT(CL_REAL) FROM CL_DATA_TYPE;
-- PASS:7042 If COUNTs = 9, 9, 9, 6, 7?
-- PASS:7042 If WARNING - null value eliminated in set function?

   ROLLBACK WORK;

-- END TEST >>> 7042 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
