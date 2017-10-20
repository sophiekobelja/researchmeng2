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
-- LTRIM(string, trimSet)/RTRIM(string, trimSet) is not supported anymore. Mastering the
-- output with errors for now. We may implement our own LTRIM_TRIMSET()/RTRIM_TRIMSET()
-- functions for testing only in the future and replace usages of LTRIM/RTRIM here.

AUTOCOMMIT OFF;

-- MODULE  DML112  

-- SQL Test Suite, V6.0, Interactive SQL, dml112.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0621 DATETIME NULLs!

   CREATE TABLE MERCH (
     ITEMKEY INT,
     ORDERED DATE,
     RDATE DATE,
     RTIME TIME,
     SOLD TIMESTAMP);
-- PASS:0621 If table is created?

   COMMIT WORK;

--O   CREATE TABLE TURNAROUND (
--O     ITEMKEY INT,
--O     MWAIT INTERVAL MONTH,
--O     DWAIT INTERVAL DAY TO HOUR);
-- PASS:0621 If table is created?

--O   COMMIT WORK;

--O   CREATE VIEW INVENTORY AS
--O     SELECT MERCH.ITEMKEY AS ITEMKEY, ORDERED,
--O     MWAIT, DWAIT FROM MERCH, TURNAROUND COR1 WHERE RDATE
--O     IS NOT NULL AND SOLD IS NULL AND
--O     MERCH.ITEMKEY = COR1.ITEMKEY
--O            UNION
--O     SELECT ITEMKEY, ORDERED,
--O     CAST (NULL AS INTERVAL MONTH),
--O     CAST (NULL AS INTERVAL DAY TO HOUR) FROM
--O     MERCH WHERE RDATE IS NOT NULL AND SOLD IS NULL
--O     AND MERCH.ITEMKEY NOT IN (SELECT ITEMKEY
--O     FROM TURNAROUND);
-- PASS:0621 If view is created?

--O   COMMIT WORK;

   INSERT INTO MERCH VALUES (0, DATE( '1993-11-23'), NULL, NULL, NULL);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (1, DATE( '1993-12-10'), DATE( '1994-01-03'),
          CAST (NULL AS TIME), NULL);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (2, DATE( '1993-12-11'), NULL,
--O          NULL, CAST ('TIMESTAMP ''1993-12-11 13:00:00''' AS TIMESTAMP));
          NULL, TIMESTAMP( '1993-12-11 13:00:00' ));
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (4, DATE( '1993-01-26'), DATE( '1993-01-27'),
          NULL, NULL);
-- PASS:0621 If 1 row is inserted?

--O   INSERT INTO TURNAROUND VALUES (2, INTERVAL '1' MONTH, 
--O          INTERVAL '20:0' DAY TO HOUR);
-- PASS:0621 If 1 row is inserted?

--O   INSERT INTO TURNAROUND VALUES (5, INTERVAL '5' MONTH,
--O          CAST (NULL AS INTERVAL DAY TO HOUR));
-- PASS:0621 If 1 row is inserted?

--O   INSERT INTO TURNAROUND VALUES (6, INTERVAL '2' MONTH, NULL);
-- PASS:0621 If 1 row is inserted?

--O   SELECT COUNT(*) FROM
--O     MERCH A, MERCH B WHERE A.SOLD = B.SOLD;
-- PASS:0621 If count = 1?

--O   SELECT COUNT(*) FROM
--O     MERCH A, MERCH B WHERE A.RTIME = B.RTIME;
-- PASS:0621 If count = 0?

--O   SELECT COUNT(*) FROM
--O     MERCH WHERE RDATE IS NULL;
-- PASS:0621 If count = 2?

--O   SELECT COUNT(*) FROM
--O     TURNAROUND WHERE DWAIT IS NOT NULL;
-- PASS:0621 If count = 1?

--O   SELECT DAY( RDATE)
--O     FROM MERCH, TURNAROUND WHERE MERCH.ITEMKEY =
--O     TURNAROUND.ITEMKEY;
-- PASS:0621 If 1 row selected and value is NULL?

   SELECT ITEMKEY FROM MERCH WHERE SOLD IS NOT NULL;
-- PASS:0621 If 1 row selected and ITEMKEY is 2?

--O   SELECT HOUR( AVG (DWAIT))
--O      FROM MERCH, TURNAROUND WHERE
--O      MERCH.ITEMKEY = TURNAROUND.ITEMKEY OR
--O      TURNAROUND.ITEMKEY NOT IN
--O        (SELECT ITEMKEY FROM MERCH);
-- PASS:0621 If 1 row selected and value is 0?

--O   SELECT COUNT(*)
--O     FROM INVENTORY WHERE MWAIT IS NULL
--O     AND DWAIT IS NULL;
-- PASS:0621 If count = 2?

   COMMIT WORK;

--O   DROP TABLE MERCH CASCADE;
   DROP TABLE MERCH ;
-- PASS:0621 If table is dropped?

   COMMIT WORK;

--O   DROP TABLE TURNAROUND CASCADE;
-- PASS:0621 If table is dropped?

--O   COMMIT WORK;

-- END TEST >>> 0621 <<< END TEST

-- *********************************************

-- TEST:0623 OUTER JOINs with NULLs and empty tables!


   CREATE TABLE JNULL1 (C1 INT, C2 INT);
-- PASS:0623 If table is created?

   COMMIT WORK;

   CREATE TABLE JNULL2 (D1 INT, D2 INT);
-- PASS:0623 If table is created?

   COMMIT WORK;

   CREATE VIEW JNULL3 AS
     SELECT C1, D1, D2 FROM JNULL1 LEFT OUTER JOIN JNULL2
     ON C2 = D2;
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL4 AS
     SELECT D1, D2 AS C2 FROM JNULL2;
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL5 AS
     SELECT C1, D1, JNULL1.C2 FROM JNULL1 RIGHT OUTER JOIN JNULL4
     ON (JNULL1.C2 = JNULL4.C2);
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL6 (C1, C2, D1, D2) AS
     SELECT * FROM JNULL1 LEFT OUTER JOIN JNULL4
     ON (JNULL1.C2 = JNULL4.C2);
-- PASS:0623 If view is created?

   COMMIT WORK;

   INSERT INTO JNULL1 VALUES (NULL, NULL);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (1, NULL);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (NULL, 1);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (1, 1);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (2, 2);
-- PASS:0623 If 1 row is inserted?

   SELECT COUNT(*) FROM JNULL3;
-- PASS:0623 If count = 5?

   SELECT COUNT(*) FROM JNULL3
     WHERE D2 IS NOT NULL OR D1 IS NOT NULL;
-- PASS:0623 If count = 0?

   SELECT COUNT(*) FROM JNULL5;
----     ON (C2);
----     SELECT D1, D2 AS C2 FROM JNULL2;
-- PASS:0623 If count = 0?

   SELECT COUNT(*) FROM JNULL6
     WHERE C2 IS NOT NULL;
-- PASS:0623 If count = 3?

   INSERT INTO JNULL2
     SELECT * FROM JNULL1;
-- PASS:0623 If 5 rows are inserted?

   UPDATE JNULL2
     SET D2 = 1 WHERE D2 = 2;
-- PASS:0623 If 1 row is updated?

   SELECT COUNT(*) FROM JNULL3;
-- PASS:0623 If count = 9?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE C1 IS NULL;
-- PASS:0623 If count = 4?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE D1 IS NULL;
-- PASS:0623 If count = 5?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE D2 IS NULL;
-- PASS:0623 If count = 3?

   SELECT AVG(D1) * 10 
     FROM JNULL3;
-- PASS:0623 If value is 15 (approximately)?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 = 1;
-- PASS:0623 If count = 6?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 IS NULL;
-- PASS:0623 If count = 2?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 = C1
        AND D1 IS NULL;
-- PASS:0623 If count = 2?

   COMMIT WORK;

--O   DROP TABLE JNULL1 CASCADE;
   DROP VIEW JNULL3 ;
   DROP VIEW JNULL5 ;
   DROP VIEW JNULL6 ;
   DROP VIEW JNULL4 ;
   DROP TABLE JNULL1 ;
-- PASS:0623 If table is dropped?

   COMMIT WORK;

--O   DROP TABLE JNULL2 CASCADE;
   DROP TABLE JNULL2 ;
-- PASS:0623 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0623 <<< END TEST

-- *********************************************

-- TEST:0625 ADD COLUMN and DROP COLUMN!

   CREATE TABLE CHANGG
     (NAAM CHAR (14) NOT NULL PRIMARY KEY, AGE INT);
-- PASS:0625 If table is created?

   COMMIT WORK;

   CREATE VIEW CHANGGVIEW AS
     SELECT * FROM CHANGG;
-- PASS:0625 If view is created?

   COMMIT WORK;

--O   ALTER TABLE CHANGG
--O     DROP NAAM RESTRICT;
-- PASS:0625 If ERROR, view references NAAM?

--O   COMMIT WORK;

   INSERT INTO CHANGG VALUES ('RALPH', 22);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('RUDOLPH', 54);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('QUEEG', 33);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('BESSIE', 106);
-- PASS:0625 If 1 row is inserted?

   SELECT COUNT(*) 
     FROM CHANGG WHERE DIVORCES IS NULL;
-- PASS:0625 If ERROR, column does not exist?

   COMMIT WORK;

   ALTER TABLE CHANGG ADD NUMBRR CHAR(11);
-- PASS:0625 If column is added?

   COMMIT WORK;

   SELECT MAX(AGE) FROM CHANGGVIEW;
-- PASS:0625 If value is 106?

   SELECT MAX(NUMBRR) FROM CHANGGVIEW;
-- PASS:0625 If ERROR, column does not exist ?

   COMMIT WORK;

--O   DROP VIEW CHANGGVIEW CASCADE;
   DROP VIEW CHANGGVIEW ;
-- PASS:0625 If view is dropped?

   COMMIT WORK;

--O   ALTER TABLE CHANGG
--O     ADD COLUMN DIVORCES INT DEFAULT 0;
-- PASS:0625 If column is added?

--O   COMMIT WORK;

--O   SELECT COUNT(*) 
--O     FROM CHANGG WHERE NUMBRR IS NOT NULL
--O     OR DIVORCES <> 0;
-- PASS:0625 If count = 0?

--O   UPDATE CHANGG
--O     SET NUMBRR = '837-47-1847', DIVORCES = 3
--O     WHERE NAAM = 'RUDOLPH';
-- PASS:0625 If 1 row is updated?

--O   UPDATE CHANGG
--O     SET NUMBRR = '738-47-1847', DIVORCES = NULL
--O     WHERE NAAM = 'QUEEG';
-- PASS:0625 If 1 row is updated?

   DELETE FROM CHANGG
     WHERE NUMBRR IS NULL;
-- PASS:0625 If 2 rows are deleted?

--O   INSERT INTO CHANGG (NAAM, AGE, NUMBRR)
--O     VALUES ('GOOBER', 16, '000-10-0001');
-- PASS:0625 If 1 row is inserted?

--O   INSERT INTO CHANGG
--O     VALUES ('OLIVIA', 20, '111-11-1111', 0);
-- PASS:0625 If 1 row is inserted?

--O   SELECT AGE, NUMBRR, DIVORCES
--O     FROM CHANGG
--O     WHERE NAAM = 'RUDOLPH';
-- PASS:0625 If 1 row selected with values 54, 837-47-1847, 3 ?

--O   SELECT AGE, NUMBRR, DIVORCES
--O     FROM CHANGG
--O     WHERE NAAM = 'QUEEG';
-- PASS:0625 If 1 row selected with values 33, 738-47-1847, NULL ?

--O   SELECT AGE, NUMBRR, DIVORCES
--O     FROM CHANGG
--O     WHERE NAAM = 'GOOBER';
-- PASS:0625 If 1 row selected with values 16, 000-10-0001, 0 ?

--O   SELECT AGE, NUMBRR, DIVORCES
--O     FROM CHANGG
--O     WHERE NAAM = 'OLIVIA';
-- PASS:0625 If 1 row selected with values 20, 111-11-1111, 0 ?

   SELECT COUNT(*) FROM CHANGG;
-- PASS:0625 If count = 4?

   COMMIT WORK;

--O   ALTER TABLE CHANGG DROP AGE CASCADE;
-- PASS:0625 If column is dropped?

--O   COMMIT WORK;

--O   ALTER TABLE CHANGG DROP COLUMN DIVORCES RESTRICT;
-- PASS:0625 If column is dropped?

--O   COMMIT WORK;

--O   SELECT COUNT(*) 
--O     FROM CHANGG WHERE AGE > 30;
-- PASS:0625 If ERROR, column does not exist?

--O   SELECT COUNT(*) 
--O     FROM CHANGG WHERE DIVORCES IS NULL;
-- PASS:0625 If ERROR, column does not exist?

--O   SELECT NAAM 
--O     FROM CHANGG
--O     WHERE NUMBRR LIKE '%000%';
-- PASS:0625 If 1 row selected with value GOOBER ?

--O   COMMIT WORK;

--O   CREATE TABLE REFERENCE_CHANGG (
--O    NAAM CHAR (14) NOT NULL PRIMARY KEY
--O    REFERENCES CHANGG);
-- PASS:0625 If table is created?

--O   COMMIT WORK;

--O   INSERT INTO REFERENCE_CHANGG VALUES ('NO SUCH NAAM');
-- PASS:0625 If RI ERROR, parent missing, 0 rows inserted?

--O   COMMIT WORK;

--O   ALTER TABLE CHANGG DROP NAAM RESTRICT;
-- PASS:0625 If ERROR, referential constraint exists?

--O   COMMIT WORK;

--O   ALTER TABLE CHANGG DROP NAAM CASCADE;
-- PASS:0625 If column is dropped?

--O   COMMIT WORK;

--O   INSERT INTO REFERENCE_CHANGG VALUES ('NO SUCH NAAM');
-- PASS:0625 If 1 row is inserted?

--O   COMMIT WORK;

--O   ALTER TABLE CHANGG DROP NUMBRR RESTRICT;
-- PASS:0625 If ERROR, last column may not be dropped?

--O   COMMIT WORK;

--O   DROP TABLE CHANGG CASCADE;
   DROP TABLE CHANGG ;
-- PASS:0625 If table is dropped?

   COMMIT WORK;

--O   DROP TABLE REFERENCE_CHANGG CASCADE;
-- PASS:0625 If table is dropped?

--O   COMMIT WORK;

-- END TEST >>> 0625 <<< END TEST

-- *********************************************

-- TEST:0631 Datetimes in a <default clause>!

--O   CREATE TABLE OBITUARIES (
--O    NAAM CHAR (14) NOT NULL PRIMARY KEY,
--O    BORN DATE DEFAULT DATE( '1880-01-01'),
--O    DIED DATE DEFAULT CURRENT_DATE,
--O    ENTERED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--O    TESTING1 DATE,
--O    TESTING2 TIMESTAMP);
-- PASS:0631 If table is created?

--O   COMMIT WORK;

--O   CREATE TABLE BIRTHS (
--O   NAAM CHAR (14) NOT NULL PRIMARY KEY,
--O    CHECKIN TIME (0)
--O        DEFAULT TIME( '00:00:00'),
--O    LABOR INTERVAL HOUR
--O        DEFAULT INTERVAL '4' HOUR,
--O    CHECKOUT TIME
--O        DEFAULT CURRENT_TIME,
--O    TESTING TIME);
-- PASS:0631 If table is created?

--O   COMMIT WORK;

--O   INSERT INTO OBITUARIES (NAAM, TESTING1, TESTING2)
--O     VALUES ('KEITH', CURRENT_DATE, CURRENT_TIMESTAMP);
-- PASS:0631 If 1 row is inserted?

--O   INSERT INTO BIRTHS (NAAM, TESTING)
--O     VALUES ('BJORN', CURRENT_TIME);
-- PASS:0631 If 1 row is inserted?

--O   SELECT HOUR( CHECKIN) +
--O                MINUTE( CHECKIN) +
--O                SECOND( CHECKIN)
--O   FROM BIRTHS;
-- PASS:0631 If 1 row selected with value 0?

--O   SELECT HOUR( LABOR) FROM BIRTHS;
-- PASS:0631 If 1 row selected with value 4?

--O   SELECT COUNT (*) FROM BIRTHS
--O     WHERE TESTING <> CHECKOUT OR CHECKOUT IS NULL;
-- PASS:0631 If count = 0?

--O   SELECT COUNT (*) FROM OBITUARIES
--O     WHERE BORN <> DATE( '1880-01-01')
--O     OR BORN IS NULL
--O     OR DIED <> TESTING1
--O     OR DIED IS NULL
--O     OR ENTERED <> TESTING2
--O     OR ENTERED IS NULL;
-- PASS:0631 If count = 0?

--O   COMMIT WORK;

--O   DROP TABLE BIRTHS CASCADE;
-- PASS:0631 If table is dropped?

--O   COMMIT WORK;

--O   DROP TABLE OBITUARIES CASCADE;
-- PASS:0631 If table is dropped?

--O   COMMIT WORK;

-- END TEST >>> 0631 <<< END TEST

-- *********************************************

-- TEST:0633 TRIM function!

   CREATE TABLE WEIRDPAD (
    NAAM CHAR (14),
    SPONSOR CHAR (14),
    PADCHAR CHAR (1));
-- PASS:0633 If table is created?

   COMMIT WORK;

   INSERT INTO WEIRDPAD (NAAM, SPONSOR) VALUES
     ('KATEBBBBBBBBBB', '000000000KEITH');
-- PASS:0633 If 1 row is inserted?

   INSERT INTO WEIRDPAD (NAAM, SPONSOR) VALUES
     ('    KEITH     ', 'XXXXKATEXXXXXX');
-- PASS:0633 If 1 row is inserted?

   SELECT LTRIM (RTRIM (SPONSOR,'X'),'X')
     FROM WEIRDPAD
     WHERE LTRIM (RTRIM (NAAM)) = 'KEITH';
-- PASS:0633 If 1 row selected with value KATE ? 

   SELECT LTRIM (SPONSOR, 'X') 
     FROM WEIRDPAD
     WHERE RTRIM (NAAM) = '    KEITH';
-- PASS:0633 If 1 row selected with value KATEXXXXXX ?

   SELECT LTRIM (SPONSOR, 'X') 
     FROM WEIRDPAD
     WHERE RTRIM (SPONSOR, 'X') = 'XXXXKATE';
-- PASS:0633 If 1 row selected with value KATEXXXXXX ?

   SELECT LTRIM (B.NAAM)  FROM WEIRDPAD A,
     WEIRDPAD B WHERE RTRIM (LTRIM (A.NAAM, 'B'),'B')
                    = RTRIM (LTRIM (B.SPONSOR, 'X'),'X');
-- PASS:0633 If 1 row selected with value KEITH ?

   SELECT COUNT(*) FROM WEIRDPAD A,
     WEIRDPAD B WHERE LTRIM (A.SPONSOR, '0')
                    = RTRIM (LTRIM (B.NAAM, ' '), ' ');
-- PASS:0633 If count = 1?

   SELECT RTRIM (NAAM, 'BB')
     FROM WEIRDPAD WHERE NAAM LIKE 'KATE%';
-- PASS:0633 If ERROR, length of trim character must be 1 ?

   INSERT INTO WEIRDPAD (NAAM, SPONSOR)
     SELECT DISTINCT LTRIM (HU.STAFF.CITY, 'D'), 
                     RTRIM (PTYPE, 'n')
     FROM HU.STAFF, HU.PROJ 
     WHERE EMPNAME = 'Alice';
-- PASS:0633 If 3 rows are inserted?

   SELECT COUNT(*) FROM WEIRDPAD;
-- PASS:0633 If count = 5?

   UPDATE WEIRDPAD
     SET SPONSOR = LTRIM (RTRIM (SPONSOR, 'X'), 'X'),
             NAAM = RTRIM (NAAM, 'B');
-- PASS:0633 If 5 rows are updated?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE NAAM = 'KATE' OR SPONSOR = 'KATE';
-- PASS:0633 If count = 2?

   DELETE FROM WEIRDPAD WHERE
     LTRIM('Kest', 'K') = LTRIM(SPONSOR, 'T');
-- PASS:0633 If 1 row is deleted?

   SELECT COUNT(*) FROM WEIRDPAD;
-- PASS:0633 If count = 4?

   UPDATE WEIRDPAD
      SET PADCHAR = '0'
     WHERE SPONSOR = '000000000KEITH'
        OR NAAM    = 'eale';
-- PASS:0633 If 3 rows are updated?

   UPDATE WEIRDPAD
      SET SPONSOR = NULL
     WHERE SPONSOR = 'Desig';
-- PASS:0633 If 1 row is updated?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE RTRIM (SPONSOR, PADCHAR) IS NULL;
-- PASS:0633 If count = 2?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE LTRIM (SPONSOR, PADCHAR) = 'KEITH';
-- PASS:0633 If count = 1?

   COMMIT WORK;

--0   DROP TABLE WEIRDPAD CASCADE;
   DROP TABLE WEIRDPAD;
-- PASS:0633 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0633 <<< END TEST

-- *************************************************////END-OF-MODULE
