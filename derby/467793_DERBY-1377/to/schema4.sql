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

-- SQL Test Suite, V6.0, Schema Definition, schema4.std
-- 59-byte ID
-- TEd Version #
-- date_time print
-- ******************************************************************
-- ****** THIS FILE SHOULD BE RUN UNDER AUTHORIZATION ID SULLIVAN1
-- ******************************************************************

-- This is a standard schema definition.

--O   CREATE SCHEMA AUTHORIZATION SULLIVAN1
   CREATE SCHEMA SULLIVAN1;
   set schema SULLIVAN1;

   CREATE TABLE AUTH_TABLE (FIRST1 INTEGER, SECOND2 CHAR);

   CREATE VIEW MUL_SCH 
       AS SELECT EMPNUM, SECOND2
          FROM HU.STAFF, AUTH_TABLE
          WHERE GRADE = FIRST1;
    
--O    GRANT ALL PRIVILEGES ON AUTH_TABLE TO HU
--O    GRANT SELECT ON MUL_SCH TO HU

-- The following tables are used to run concurrency program pairs
--    e.g MPA001 and MPB001 use the tables with prefix MP1_ 

 CREATE TABLE MP1_MM2 (NUMTEST NUMERIC(10));
 CREATE TABLE MP1_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP1_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);

 CREATE TABLE MP2_MM2 (NUMTEST NUMERIC(10));
 CREATE TABLE MP2_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP2_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);

 CREATE TABLE MP3_MM2 (NUMTEST NUMERIC(10));
 CREATE TABLE MP3_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP3_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);

 CREATE TABLE MP4_MM2 (NUMTEST NUMERIC(10));
 CREATE TABLE MP4_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP4_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);


 CREATE TABLE MP5_AA (ANUM NUMERIC(4));
 CREATE TABLE MP5_AA_INDEX (ANUM NUMERIC(4) NOT NULL UNIQUE);
 CREATE TABLE MP5_TT (TESTTYPE CHAR(3), KOUNT DECIMAL(4));

-- The following tables are used to run interactive concurrency program pairs.

 CREATE TABLE TTT (ANUM NUMERIC(4) NOT NULL UNIQUE, AUTHOR CHAR(1));
 CREATE TABLE TT (DOLLARS NUMERIC(4), ANUM NUMERIC(4));
 CREATE TABLE AA (ANUM NUMERIC(4));
--O this is a dup of a table in a different schema...
 CREATE TABLE BB (BNUM NUMERIC(4));
--O this is a dup of a table in a different schema...

--  Test GRANT UPDATE for additional columns beyond those WITH GRANT OPTION.
--  expect error message!
--  In SCHEMA1 for USER HU is the following grant:
--         GRANT SELECT,UPDATE(EMPNUM,EMPNAME) ON STAFF3
--          TO ..., SULLIVAN1 WITH GRANT OPTION
-- If the following self-grant by SULLIVAN1 will not compile/execute, delete it.
--O   GRANT SELECT,UPDATE ON HU.STAFF3
--O           TO SULLIVAN1
--O           WITH GRANT OPTION


 CREATE TABLE MP6_MM2 (NUMTEST NUMERIC(10));
 CREATE TABLE MP6_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP6_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);
 CREATE TABLE MP6_AA (ANUM NUMERIC(4));
 CREATE TABLE MP6_BB (BNUM NUMERIC(4));


 CREATE TABLE MP7_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP7_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);
 CREATE TABLE MP7_AA (ANUM NUMERIC(4));
 CREATE TABLE MP7_BB (BNUM NUMERIC(4));


 CREATE TABLE MP8_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP8_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                          DOLLARS INTEGER);
 CREATE TABLE MP8_AA (ANUM NUMERIC(4) NOT NULL, 
--O                     AUTHOR CHAR(1), UNIQUE (ANUM))
                     AUTHOR CHAR(1), constraint mp8_aa_con UNIQUE (ANUM));
 CREATE TABLE MP8_BB (NUMTEST NUMERIC(9));

 CREATE TABLE MP9_NN (NUMTEST NUMERIC(9));
CREATE TABLE MP9_NEXTKEY (KEYNUM INTEGER, AUTHOR CHAR(1),
                         DOLLARS INTEGER);
 CREATE TABLE MP9_AA (ANUM NUMERIC(4));
 CREATE TABLE MP9_BB (BNUM NUMERIC(4));

CREATE TABLE USG102 (C1 INT, C_1 INT);
CREATE TABLE USG103 (C1 INT, C_1 INT);

-- ************* End of Schema *************
