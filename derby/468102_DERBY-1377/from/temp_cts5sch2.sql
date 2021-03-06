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

--O-- SQL Test Suite, V6.0, Schema Definition, cts5sch2.sql
--O-- 59-byte ID
--O-- TEd Version #
--O-- date_time print
--O-- ***************************************************************
--O-- ****** THIS FILE SHOULD BE RUN UNDER SCHEMA ID CTS1 ******
--O-- ***************************************************************
--O
--O--  The following command is supported only at INTERMEDIATE level
--O  CREATE SCHEMA CTS1;
--O
--O--  The following command should be used if ENTRY level rather than
--O--  intermediate is supported.
--O--  CREATE SCHEMA AUTHORIZATION CTS1;
  CREATE SCHEMA CTS1;
  set schema CTS1;
--O
--O-- ************* create character set statements *****
--O
--O   CREATE CHARACTER SET CS GET SQL_TEXT;
--O
--O-- ************* create table statements *************
--O
--O
  CREATE TABLE ECCO (C1 CHAR(2));

  CREATE TABLE FIPS1
   (FIPS_TEST CHAR(20));


  CREATE TABLE STAFF
  (EMPNUM   CHAR(3) NOT NULL UNIQUE,
   EMPNAME  CHAR(20),
   GRADE    DECIMAL(4),
   CITY     CHAR(15));

  CREATE TABLE WORKS
  (EMPNUM   CHAR(3) NOT NULL,
   PNUM     CHAR(3) NOT NULL,
   HOURS    DECIMAL(5),
   UNIQUE(EMPNUM,PNUM));

  CREATE TABLE STAFF1
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));


  CREATE TABLE STAFF4
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));

  CREATE TABLE VTABLE
  (COL1   INTEGER,
   COL2   INTEGER,
   COL3   INTEGER,
   COL4   INTEGER,
   COL5   DECIMAL(7,2));


  CREATE TABLE STAFF3
  (EMPNUM   CHAR(3) NOT NULL,
   EMPNAME  CHAR(20),
   GRADE    DECIMAL(4),
   CITY     CHAR(15),
   UNIQUE (EMPNUM));


  CREATE TABLE PROJ3
  (PNUM     CHAR(3) NOT NULL,
   PNAME    CHAR(20),
   PTYPE    CHAR(6),
   BUDGET   DECIMAL(9),
   CITY     CHAR(15),
   UNIQUE (PNUM));


 CREATE TABLE STAFF7 (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15),
  PRIMARY KEY (EMPNUM),
  CHECK (GRADE BETWEEN 1 AND 20));

   CREATE TABLE WORKS3a
   (EMPNUM   CHAR(3) NOT NULL,
   PNUM     CHAR(3) NOT NULL,
   HOURS    DECIMAL(5),
   FOREIGN KEY (PNUM) REFERENCES PROJ3(PNUM));


   CREATE TABLE STAFFa
   ( HOURS   INTEGER,
     SALARY  DECIMAL(6),
     EMPNUM  CHAR(3),
     PNUM    DECIMAL(4),
     EMPNAME CHAR(20));

  CREATE TABLE STAFFb
   ( SALARY   DECIMAL(6),
     EMPNAME  CHAR(20),
     HOURS    INTEGER,
     PNUM     CHAR(3),
     CITY     CHAR(15),
     SEX      CHAR);

  CREATE TABLE STAFFc
  (  EMPNUM   CHAR(3) NOT NULL,
     EMPNAME  CHAR(20),
     GRADE    DECIMAL(4),
     CITY     CHAR(15),
     MGR      CHAR(3),
     UNIQUE   (EMPNUM));

  CREATE TABLE STAFFd
  (  EMPNUM   CHAR(3) NOT NULL,
     GRADE    DECIMAL(4),
     MGR      CHAR(3));

  CREATE TABLE STAFF_CTS
  (  PNUM   CHAR(3),
     CITY   CHAR(15),
     GRADE  DECIMAL(4),
     EMPNAME CHAR(20));

  CREATE TABLE STAFFz
  ( EMPNUM CHAR(3) REFERENCES STAFF3(EMPNUM),
    SALARY DECIMAL(6) CHECK (SALARY > 0));

  CREATE TABLE PROJ_DURATION
  ( MONTHS  INTEGER,
    TIME_LEFT   INTEGER,
    EMP_HOURS      INTEGER,
    CHECK (MONTHS > 0));

  CREATE TABLE STAFF_CTS2
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));

  CREATE TABLE EMPLOYEES2
  (  name     CHAR(10),
     empno    INTEGER);

  CREATE TABLE A
  (   p   INTEGER,
      q   INTEGER );

  CREATE TABLE TT
  (TTA   INTEGER,
   TTB   INTEGER,
   TTC   INTEGER);

  CREATE TABLE TU
  (TUD   CHAR(2),
   TUE   INTEGER);

--O  CREATE TABLE TT2
--O  (TTA INTEGER,
--O   TTB INTERVAL YEAR TO MONTH,
--O   TTC DECIMAL(6,0));
--O
  CREATE TABLE TV
  (A   INTEGER,
   B   CHAR);

  CREATE TABLE TW
  (D   CHAR,
   E   INTEGER);

  CREATE TABLE TX
  (TX1     INTEGER,
   TX2     CHARACTER(5),
   TX3     CHARACTER VARYING (10));

  CREATE TABLE COMP_BUDG
  (P_REF   CHAR(3) NOT NULL,
   BUDGET  DECIMAL(20),
   HOURS   INTEGER,
   SALARY  DECIMAL(6),
   FOREIGN KEY (P_REF) REFERENCES CTS2.PROJ_MAN(P_REF));

  CREATE TABLE PROJ_STATUS
  ( MGR    CHAR(15)  REFERENCES CTS2.PROJ_MAN(MGR),
    P_REF  CHAR(3),
    ONTIME CHAR,
    BUDGET DECIMAL(20),
    COST   DECIMAL(20));

  CREATE TABLE DATA_TYPE
  (  NUM   NUMERIC,
--O     DEC   DECIMAL,
     DECI   DECIMAL,
     ING   INTEGER,
     SMA   SMALLINT,
     FLO   FLOAT,
     REA   REAL,
     DOU   DOUBLE PRECISION);

--O  CREATE TABLE TTIME_BASE
--O  (PK           INTEGER,
--O   TT           TIME,
--O   TS           TIMESTAMP,
--O   TT2          TIME WITH TIME ZONE,
--O   TS2          TIMESTAMP WITH TIME ZONE,
--O   PRIMARY KEY (PK));
    
  CREATE TABLE CL_DATA_TYPE
   (CL_CHAR CHAR(10),
    CL_NUM NUMERIC,
    CL_DEC DECIMAL,
    CL_REAL REAL);

  CREATE TABLE CL_EMPLOYEE 
      (EMPNUM  NUMERIC(5) NOT NULL PRIMARY KEY,
       DEPTNO  CHAR(3),
       LOC     CHAR(15),
       EMPNAME CHAR(20),
       SALARY  DECIMAL(6),
       GRADE   DECIMAL(4),
       HOURS   DECIMAL(5));

  CREATE TABLE TEST6740A
   (TNUM NUMERIC(4),
    TCHARA CHAR(10));

  CREATE TABLE TEST6740B
   (TNUM NUMERIC(4),
    TCHARB CHAR(10));

  CREATE TABLE TEST6740C
   (TNUMERIC NUMERIC(4),
    TCHAR CHAR(10));

  CREATE TABLE TEST6840A
   (NUM_A NUMERIC(4),
    CH_A CHAR(10));

  CREATE TABLE TEST6840B
   (NUM_B NUMERIC(4),
    CH_B CHAR(10));

  CREATE TABLE TEST6840C
   (NUM_C1 NUMERIC(4),
    CH_C1 CHAR(10),
    NUM_C2 NUMERIC(4),
    CH_C2 CHAR(10));

  CREATE TABLE TEST12849B
   (col_num3 NUMERIC(3) NOT NULL,
    PRIMARY KEY (col_num3));

--RESOLVE: we don't support CASCADE
--O  CREATE TABLE TEST12849A
--O   (col_num1   NUMERIC(5) PRIMARY KEY,
--O    col_str1   VARCHAR(15) NOT NULL,
--O    col_str2   VARCHAR(10),
--O    col_num2   NUMERIC(5) CONSTRAINT constr_1 REFERENCES TEST12849A,
--O    col_str3   VARCHAR(25),
--O    col_num3   NUMERIC(7,2),
--O    col_num4   NUMERIC(3) NOT NULL
--O      CONSTRAINT constr_3 REFERENCES TEST12849B
--O      ON DELETE CASCADE,
--O    CONSTRAINT constr_2 UNIQUE (col_str1, col_str2));

  CREATE TABLE T4
   (STR110 CHAR(110) NOT NULL,
    NUM6   NUMERIC(6) NOT NULL,
    COL3   CHAR(10),
    COL4 CHAR(20),
    UNIQUE(STR110,NUM6));


CREATE TABLE EMPTY740
(COL_1   CHAR(10),
 COL_2   VARCHAR(5),
 COL_3   NUMERIC(5),
 COL_4   DECIMAL(6),
 COL_5   TIME);


CREATE TABLE TABX760
  ( DEPTNO  NUMERIC(5) UNIQUE NOT NULL,
  EMPNAME CHAR(20)   UNIQUE NOT NULL, 
  SALARY  DECIMAL(7));

--OCREATE TABLE TABCS
--O   ( COLUN   NUMERIC(5) UNIQUE,
--O     COLSTR1 CHAR(10)    CHARACTER SET CS,
--O     COLSTR2 VARCHAR(10) CHARACTER SET CS);


  CREATE TABLE CL_STANDARD
   (
    COL_NUM1 NUMERIC(4),
    COL_CH1  CHAR(10),
    COL_NUM2 NUMERIC(4),
    COL_CH2  CHAR(10));

  CREATE TABLE TABLE728a 
   (
    C1 CHAR(10),
    C2 CHAR(10));

  CREATE TABLE TABLE728b
   (
     COL_1 CHAR(10),
     COL_2 CHAR(10));


--O  CREATE TABLE TAB734
--O   ( CSTR1 NCHAR(10),
--O     CSTR2 NCHAR VARYING(12));


--O-- LATIN1 is not required by SQL-92 DWF 1996-02-21
--O--  CREATE TABLE TABLATIN1
--O--  ( COL1 CHARACTER(10) CHARACTER SET LATIN1,
--O--    COL2 CHAR(12)      CHARACTER SET LATIN1,
--O--    COL3 VARCHAR(15)   CHARACTER SET LATIN1,
--O--    COL4 NUMERIC(5));

  CREATE TABLE ET
  (col1    CHAR(3),
   col2    CHAR(20),
   col3    DECIMAL(4),
   col4    CHAR(15),
   col5    INTEGER,
   col6    INTEGER);

  CREATE TABLE TTSTORE
  (numx    INTEGER,
   colthu  INTEGER,
   coltmu  INTEGER,
   TT      TIME);

--O  CREATE TABLE TTSTORE2
--O  (num     INTEGER,
--O   colthu  INTEGER,
--O   coltmu  INTEGER,
--O   TT2     TIME WITH TIME ZONE);

  CREATE TABLE CONCATBUF (ZZ CHAR(240));


CREATE VIEW TESTREPORT AS
    SELECT TESTNO, RESULT, TESTTYPE
    FROM HU.TESTREPORT;

--OCREATE VIEW TTIME (PK, TT, TS) AS
--O    SELECT PK, TT, TS
--O    FROM TTIME_BASE;
--O
--OCREATE VIEW TTIME2 (PK, TT2, TS2) AS
--O    SELECT PK, TT2, TS2
--O    FROM TTIME_BASE;
--O
--OCREATE VIEW TTIME3 (PK, TT, TT2, TS2) AS
--O    SELECT PK, TT, TT2, TS2
--O    FROM TTIME_BASE;
--O
--O-- ************* create domain statements ***********
--O
--O   CREATE DOMAIN esal AS INTEGER 
--O   CHECK (VALUE > 500);
--O
--O   CREATE DOMAIN atom CHARACTER
--O   CHECK ('a' <= VALUE)
--O   CHECK ('m' >= VALUE);
--O
--O   CREATE DOMAIN smint INTEGER
--O   CHECK (1<= VALUE)
--O   CHECK (100 >= VALUE);
--O
--O-- ************* grant statements follow *************
--O
--O   GRANT ALL PRIVILEGES ON CONCATBUF TO PUBLIC;
--O
--O   GRANT SELECT ON CTS1.ECCO TO PUBLIC;
--O
--O   GRANT INSERT ON TESTREPORT TO PUBLIC WITH GRANT OPTION;
--O
--O   GRANT SELECT ON CTS1.DATA_TYPE TO CTS4;
--O
--O-- ************* End of Schema *************
commit;
