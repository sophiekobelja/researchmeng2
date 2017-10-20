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
--
-- General trigger test
--

create function triggerFiresMin(s varchar(128)) returns varchar(1) PARAMETER STYLE JAVA LANGUAGE JAVA NO SQL
  EXTERNAL NAME 'org.apache.derbyTesting.functionTests.util.Triggers.triggerFiresMinimal';
create function triggerFires(s varchar(128)) returns varchar(1) PARAMETER STYLE JAVA LANGUAGE JAVA NO SQL
  EXTERNAL NAME 'org.apache.derbyTesting.functionTests.util.Triggers.triggerFires';

drop table x;
create table x (x int, y int, z int, constraint ck1 check (x > 0));
create view v as select * from x;

-- ok
create trigger t1 NO CASCADE before update of x,y on x for each row mode db2sql values 1;

-- trigger already exists
create trigger t1 NO CASCADE before update of x,y on x for each row mode db2sql values 1;
-- trigger already exists
create trigger app.t1 NO CASCADE before update of x,y on x for each row mode db2sql values 1;

-- make sure system tables look as we expect
select cast(triggername as char(10)), event, firingtime, type, state, referencedcolumns from sys.systriggers;

select cast(triggername as char(10)), CAST (TRIGGERDEFINITION AS VARCHAR(180)), STMTNAME from sys.systriggers t, sys.sysstatements s 
		where s.stmtid = t.actionstmtid;

select cast(triggername as char(10)), tablename from sys.systriggers t, sys.systables tb
		where t.tableid = tb.tableid;

values SYSCS_UTIL.SYSCS_CHECK_TABLE('SYS', 'SYSTRIGGERS');
drop trigger t1;

-- not in sys schema
create trigger sys.tr NO CASCADE before insert on x for each row mode db2sql values 1;

-- not on table in sys schema
create trigger tr NO CASCADE before insert on sys.systables for each row mode db2sql values 1;

-- duplicate columns, not allowed
create trigger tr NO CASCADE before update of x, x on x for each row mode db2sql values 1;

-- no params in column list
create trigger tr NO CASCADE before update of x, ? on x for each row mode db2sql values 1;

-- invalid column
create trigger tr NO CASCADE before update of doesnotexist on x for each row mode db2sql values 1;

-- not on view
create trigger tr NO CASCADE before insert on v for each row mode db2sql values 1;

-- error to use table qualifier
create trigger tr NO CASCADE before update of x.x on x for each row mode db2sql values 1;

-- error to use schema.table qualifier
create trigger tr NO CASCADE before update of app.x.x on x for each row mode db2sql values 1;

-- no params in trigger action
-- bad
create trigger tr NO CASCADE before delete on x for each row mode db2sql select * from x where x = ?;

create trigger stmttrigger NO CASCADE before delete on x for each statement mode db2sql values 1;
select triggername, type from sys.systriggers where triggername = 'STMTTRIGGER';
drop trigger stmttrigger;

create trigger rowtrigger NO CASCADE before delete on x for each row mode db2sql values 1;
select triggername, type from sys.systriggers where triggername = 'ROWTRIGGER';
drop trigger rowtrigger;

-- fool around with depedencies

-- CREATE TRIGGER
create trigger t2 NO CASCADE before update of x,y on x for each row mode db2sql values 1;

-- CREATE CONSTRAINT
alter table x add constraint ck2 check(x > 0);

-- DROP VIEW
drop view v;

-- CREATE VIEW
create view v as select * from x;

-- CREATE INDEX
create index ix on x(x);

-- DROP TRIGGER: to the other types we have here
drop trigger t2;

-- DROP INDEX
drop index ix; 

-- DROP CONSTRAINT
alter table x drop constraint ck2;

-- MAKE SURE TRIGGER SPS IS RECOMPILED IF TABLE IS ALTERED.
create table y (x int, y int, z int);

create trigger tins after insert on x referencing new_table as newtab for each statement mode db2sql insert into y select x, y, z from newtab;

insert into x values (1, 1, 1);
alter table x add column w int default 100;
alter table x add constraint nonulls check (w is not null);
insert into x values (2, 2, 2, 2);
select * from y;
drop trigger tins;
drop table y;

-- prove that by dropping the underlying table, we have dropped the trigger
-- first, lets create a few other triggers
create trigger t2 NO CASCADE before update of x,y on x for each row mode db2sql values 1;
create trigger t3 after update of x,y on x for each statement mode db2sql values 1;
create trigger t4 after delete on x for each statement mode db2sql values 1;
select cast(triggername as char(10)), tablename from sys.systriggers t, sys.systables  tb
		where t.tableid = tb.tableid order by 1;
drop view v;
drop table x;
select cast(triggername as char(10)), tablename from sys.systriggers t, sys.systables  tb
		where t.tableid = tb.tableid order by 1;

--
-- schema testing
--
create table x (x int, y int, z int);
create schema test;

create trigger test.t1 NO CASCADE before delete on x for each row mode db2sql values 1;
set schema test;

create trigger t2 NO CASCADE before delete on app.x for each row mode db2sql values 1;

select schemaname, triggername from sys.systriggers t, sys.sysschemas s
	where s.schemaid = t.schemaid;

set schema app;
-- fails
drop schema test restrict;

drop trigger test.t2;

-- fails
drop schema test restrict;

set schema test;
drop trigger t1;
set schema app;

-- ok this time
drop schema test restrict;

create table t (x int, y int, c char(1));

--
-- Test trigger firing order
--
create trigger t1 after insert on t for each row mode db2sql
	values app.triggerFiresMin('3rd');
create trigger t2 after insert on t for each statement mode db2sql
	values app.triggerFiresMin('1st');
create trigger t3 no cascade before insert on t for each row mode db2sql
	values app.triggerFiresMin('4th');
create trigger t4 after insert on t for each row mode db2sql
	values app.triggerFiresMin('2nd');
create trigger t5 no cascade before insert on t for each statement mode db2sql
	values app.triggerFiresMin('5th');
insert into t values (1,1,'1');
drop trigger t1;
drop trigger t2;
drop trigger t3;
drop trigger t4;
drop trigger t5;

-- try multiple values, make sure result sets don't get screwed up
-- this time we'll print out result sets
create trigger t1 after insert on t for each row mode db2sql
	values app.triggerFires('3rd');
create trigger t2 no cascade before insert on t for each statement mode db2sql
	values app.triggerFires('1st');
create trigger t3 after insert on t for each row mode db2sql
	values app.triggerFires('4th');
create trigger t4 no cascade before insert on t for each row mode db2sql
	values app.triggerFires('2nd');
create trigger t5 after insert on t for each statement mode db2sql
	values app.triggerFires('5th');
insert into t values 
	(2,2,'2'),
	(3,3,'3'),
	(4,4,'4');

delete from t;
drop trigger t1;
drop trigger t2;
drop trigger t3;
drop trigger t4;
drop trigger t5;

--
-- Test firing on empty change sets, 
-- statement triggers fire, row triggers
-- do not.
--
create trigger t1 after insert on t for each row mode db2sql
	values app.triggerFires('ROW: empty insert, should NOT fire');
create trigger t2 after insert on t for each statement mode db2sql
	values app.triggerFires('STATEMENT: empty insert, ok');
insert into t select * from t;
drop trigger t1;
drop trigger t2;

create trigger t1 after update on t for each row mode db2sql
	values app.triggerFires('ROW: empty update, should NOT fire');
create trigger t2 after update on t for each statement mode db2sql
	values app.triggerFires('STATEMENT: empty update, ok');
update t set x = x;
drop trigger t1;
drop trigger t2;

create trigger t1 after delete on t for each row mode db2sql
	values app.triggerFires('ROW: empty delete, should NOT fire');
create trigger t2 after delete on t for each statement mode db2sql
	values app.triggerFires('STATEMENT: empty delete, ok');
delete from t;
drop trigger t1;
drop trigger t2;
drop table x;

--
-- Trigger ordering wrt constraints
--
create table p (x int not null, constraint pk primary key (x));
insert into p values 1,2,3;
create table f (x int, 
		constraint ck check (x > 0),
		constraint fk foreign key (x) references p);
create trigger t1 no cascade before insert on f for each row mode db2sql
	values app.triggerFiresMin('BEFORE constraints');
create trigger t2 after insert on f for each row mode db2sql
	values app.triggerFiresMin('AFTER constraints');

-- INSERT
-- fails, ck violated
insert into f values 0;

alter table f drop constraint ck;

-- fails, fk violated
insert into f values 0;

alter table f drop foreign key fk;

-- ok
insert into f values 0;

delete from f;
alter table f add constraint ck check (x > 0);
alter table f add constraint fk foreign key (x) references p;
drop trigger t1;
drop trigger t2;
insert into f values (1);


-- UPDATE
create trigger t1 no cascade before update on f for each row mode db2sql
	values app.triggerFiresMin('BEFORE constraints');
create trigger t2 after update on f for each row mode db2sql
	values app.triggerFiresMin('AFTER constraints');

-- fails, ck violated
update f set x = 0;

alter table f drop constraint ck;

-- fails, fk violated
update f set x = 0;

alter table f drop foreign key fk;

-- ok
update f set x = 0;

delete from f;
alter table f add constraint ck check (x > 0);
alter table f add constraint fk foreign key (x) references p;
drop trigger t1;
drop trigger t2;


-- DELETE
insert into f values 1;
create trigger t1 no cascade before delete on p for each row mode db2sql
	values app.triggerFiresMin('BEFORE constraints');
create trigger t2 after delete on p for each row mode db2sql
	values app.triggerFiresMin('AFTER constraints');

-- fails, fk violated
delete from p;

alter table f drop foreign key fk;

-- ok
delete from p;

drop table f;
drop table p;

--
-- Prove that we are firing the proper triggers based
-- on the columns we are changing;
--
drop table t;
create table t (c1 int, c2 int);
create trigger tins after insert on t for each row mode db2sql
	values app.triggerFiresMin('insert');
create trigger tdel after delete on t for each row mode db2sql
	values app.triggerFiresMin('delete');
create trigger tupc1 after update of c1 on t for each row mode db2sql
	values app.triggerFiresMin('update c1');
create trigger tupc2 after update of c2 on t for each row mode db2sql
	values app.triggerFiresMin('update c2');
create trigger tupc1c2 after update of c1,c2 on t for each row mode db2sql
	values app.triggerFiresMin('update c1,c2');
create trigger tupc2c1 after update of c2,c1 on t for each row mode db2sql
	values app.triggerFiresMin('update c2,c1');
insert into t values (1,1);
update t set c1 = 1;
update t set c2 = 1;
update t set c2 = 1, c1 = 1;
update t set c1 = 1, c2 = 1;
delete from t;

-- Make sure that triggers work with delimited identifiers
-- Make sure that text munging works correctly
create table trigtable("cOlUmN1" int, "cOlUmN2  " int, "cOlUmN3""""  " int);
create table trighistory("cOlUmN1" int, "cOlUmN2  " int, "cOlUmN3""""  " int);
insert into trigtable values (1, 2, 3);
create trigger "tt1" after insert on trigtable
referencing NEW as NEW for each row mode db2sql
insert into trighistory ("cOlUmN1", "cOlUmN2  ", "cOlUmN3""""  ") values (new."cOlUmN1" + 5, "NEW"."cOlUmN2  " * new."cOlUmN3""""  ", 5);
maximumdisplaywidth 2000;
select cast(triggername as char(10)), CAST (TRIGGERDEFINITION AS VARCHAR(180)), STMTNAME from sys.systriggers t, sys.sysstatements s 
		where s.stmtid = t.actionstmtid and triggername = 'tt1';
insert into trigtable values (1, 2, 3);
select * from trighistory;
drop trigger "tt1";
create trigger "tt1" after insert on trigtable
referencing new as new for each row mode db2sql
insert into trighistory ("cOlUmN1", "cOlUmN2  ", "cOlUmN3""""  ") values (new."cOlUmN1" + new."cOlUmN1", "NEW"."cOlUmN2  " * new."cOlUmN3""""  ", new."cOlUmN2  " * 3);
select cast(triggername as char(10)), CAST (TRIGGERDEFINITION AS VARCHAR(180)), STMTNAME from sys.systriggers t, sys.sysstatements s 
		where s.stmtid = t.actionstmtid and triggername = 'tt1';
insert into trigtable values (1, 2, 3);
select * from trighistory;
drop table trigtable;
drop table trighistory;

-- trigger bug that got fixed mysteriously
-- between xena and buffy
create table trigtable1(c1 int, c2 int);
create table trighistory(trigtable char(30), c1 int, c2 int);
create trigger trigtable1 after update on trigtable1
referencing OLD as oldtable
for each row mode db2sql
insert into trighistory values ('trigtable1', oldtable.c1, oldtable.c2);
insert into trigtable1 values (1, 1);
update trigtable1 set c1 = 11, c2 = 11;
select * from trighistory;
drop table trigtable1;
drop table trighistory;

-- Test for bug 3495 - triggers were causing deferred insert, which
-- caused the insert to use a TemporaryRowHolderImpl. This was not
-- being re-initialized properly when closed, and it was trying to
-- re-insert the row from the first insert.
autocommit off;
drop table t;
create table t (x int);
create trigger tr after insert on t for each statement mode db2sql values 1;
prepare ps as 'insert into t values (?)';
execute ps using 'values (1)';
execute ps using 'values (2)';
select * from t;

-- Test MODE DB2SQL not as reserved keyword. beetle 4546 
drop table db2sql;
drop table db2sql2;
create table db2sql  (db2sql int, mode int, yipng int);
create table db2sql2 (db2sql2 int);

-- Test MODE DB2SQL on trigger.  beetle 4546
drop trigger db2sqltr1;
create trigger db2sqltr1 after insert on db2sql 
for each row
MODE DB2SQL 
insert into db2sql2 values (1);

-- Test optimizer plan of trigger action. Beetle 4826
autocommit on;
drop table parent;

create table t1(a int not null primary key, b int);
create table parent (a int not null primary key, b int);

create trigger trig1 AFTER DELETE on t1
referencing OLD as OLD for each row mode db2sql
delete from parent where a = OLD.a;

insert into t1 values (0, 1);
insert into t1  values (1, 1);
insert into t1  values (2, 1);
insert into t1  values (3, 1);

insert into parent values (0, 1);
insert into parent values (1, 1);
insert into parent values (2, 1);
insert into parent values (3, 1);
insert into parent values (4, 1);

autocommit off ;
delete from t1 where a = 3;
select type, mode, tablename from syscs_diag.lock_table order by tablename, type;
rollback;
autocommit on;
drop table t1;
drop table parent;

-- Test use of old AND new referencing names within the same trigger (beetle 5725).

create table x(x int);
insert into x values (2), (8), (78);
create table removed (x int);

-- statement trigger
create trigger t1 after update of x on x referencing
 old_table as old new_table as new for each statement mode db2sql insert into
 removed select * from old where x not in (select x from 
 new where x < 10);

select * from x;
select * from removed;
update x set x=18 where x=8;
select * from x;
select * from removed;

-- row trigger
create trigger t2 after update of x on x referencing
 old as oldrow new as newrow for each row mode db2sql insert into
 removed values (newrow.x + oldrow.x);

update x set x=28 where x=18;
select * from x;
select * from removed;

-- do an alter table, then make sure triggers recompile correctly.

alter table x add column y int;
update x set x=88 where x > 44;
select * from x;
select * from removed;

drop table x;
drop table removed;

create table x (x int, constraint ck check (x > 0));

-- after
create trigger tgood after insert on x for each statement mode db2sql insert into x values 666;
insert into x values 1;
select * from x;
drop trigger tgood;

create trigger tgood after insert on x for each statement mode db2sql delete from x;
insert into x values 1;
select * from x;
drop trigger tgood;

create trigger tgood after insert on x for each statement mode db2sql update x set x = x+100;
insert into x values 1;
select * from x;
drop trigger tgood;
delete from x;

create trigger tgood after insert on x
for each statement mode db2sql insert into x values (666), (999), (333);
insert into x values 1;
select * from x order by 1;
drop trigger tgood;
delete from x;

create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into x values (n.x);
insert into x values 7;
select * from x order by 1;
drop trigger tgood;
delete from x;

create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into x values (333), (999), (333);
insert into x values 1;
select * from x order by 1;
drop trigger tgood;
drop table x;

-- Derby-388: When a set of inserts/updates is performed on a table
-- and each update fires a trigger that in turn performs other updates,
-- Derby will sometimes try to recompile the trigger in the middle
-- of the update process and will throw an NPE when doing so.

create procedure d388 () language java parameter style java modifies sql data
	external name 'org.apache.derbyTesting.functionTests.tests.lang.userDefMethods.derby388';

-- Just call the procedure; it will do the rest.
call d388();

-- Derby-85: It turns out that if a table t1 exists in a non-default schema 
-- and the default schema (e.g., "SOMEUSER") doesn't exist yet (because no 
-- objects have been created in that schema), then attempts to create a 
-- trigger on t1 using its qualified name will lead to a null pointer 
-- exception in the Derby engine. 
connect 'wombat;user=someuser';
autocommit off;
create table myschema.mytable (i int);
create trigger mytrigger after update on myschema.mytable for each row mode db2sql select * from sys.systables;
rollback;
disconnect;

-- DERBY-438 - Working triggers with BLOB columns
set connection CONNECTION0;
autocommit on;
create table t438 (id int,  cost decimal(6,2), bl blob);
create table t438_t (id int, bl blob, l int, nc decimal(6,2), oc decimal(6,2));
create trigger tr_438 after update on t438
referencing new as n old as o
for each row mode db2sql
insert into t438_t(id, bl, l, nc, oc) values (n.id, n.bl, length(n.bl), n.cost, o.cost);

-- initially just some small BLOB values.
insert into t438 values (1, 34.53, cast (X'124594322143423214ab35f2e34c' as blob));
insert into t438 values (0, 95.32, null);
insert into t438 values (2, 22.21, cast (X'aa' as blob));
select id, cost, length(bl) from t438 order by 1;

update t438 set cost = cost + 1.23;
select id, length(bl), l, nc, oc from t438_t order by 1,5,4;

select id, cast (bl as blob(20)) from t438 order by 1;
select id, cast (bl as blob(20)) from t438_t order by 1;

drop table t438;
drop table t438_t;

-- now re-start with CLOB types
create table t438 (id int,  cost decimal(6,2), cl clob);
create table t438_t (id int, cl clob, l int, nc decimal(6,2), oc decimal(6,2));
create trigger tr_438 after update on t438
referencing new as n old as o
for each row mode db2sql
insert into t438_t(id, cl, l, nc, oc) values (n.id, n.cl, length(n.cl), n.cost, o.cost);

-- initially just some small CLOB values.
insert into t438 values (1, 34.53, cast ('Italy''s centre-left leader Romano Prodi insists his poll victory is valid as contested ballots are checked.' as clob));
insert into t438 values (0, 95.32, null);
insert into t438 values (2, 22.21, cast ('free' as clob));
select id, cost, length(cl) from t438 order by 1;

update t438 set cost = cost + 1.23;
select id, length(cl), l, nc, oc from t438_t order by 1,5,4;

select id, cast (cl as clob(60)) from t438 order by 1;
select id, cast (cl as clob(60)) from t438_t order by 1;

drop table t438;
drop table t438_t;

-- Testcase showing DERBY-1258
create table tsn (I integer, "i" integer);
create table tsn_t (a integer, b integer);
create trigger tr_sn after insert on tsn
referencing new as n
for each row mode db2sql
insert into tsn_t(a, b) values (n.I, n."i");
insert into tsn values (1, 234);
select * from tsn;
-- Should have 1,234 as data in tsn_t
select * from tsn_t;
drop table tsn;
drop table tsn_t;

-- Testcase showing DERBY-1064
CREATE TABLE T10641 ( X INT PRIMARY KEY );
CREATE TABLE T10641_DELETIONS ( X INT );
CREATE TABLE T10642 (
    Y INT,
    CONSTRAINT Y_AND_X FOREIGN KEY(Y) REFERENCES T10641(X) ON DELETE CASCADE);
CREATE TABLE T10642_DELETIONS ( Y INT );
CREATE TRIGGER TRIGGER_T10641
    AFTER DELETE ON T10641
    REFERENCING OLD AS OLD_ROW
    FOR EACH ROW MODE DB2SQL
    INSERT INTO T10641_DELETIONS VALUES (OLD_ROW.X);
CREATE TRIGGER TRIGGER_T10642
    AFTER DELETE ON T10642
    REFERENCING OLD AS OLD_ROW
    FOR EACH ROW MODE DB2SQL
    INSERT INTO T10642_DELETIONS VALUES (OLD_ROW.Y);
INSERT INTO T10641 VALUES (0);
INSERT INTO T10642 VALUES (0);
INSERT INTO T10641 VALUES (1);
INSERT INTO T10642 VALUES (1);
SELECT * FROM T10641;
SELECT * FROM T10642;
DELETE FROM T10641;
SELECT * FROM T10641;
SELECT * FROM T10642;
SELECT * FROM T10641_DELETIONS;
SELECT * FROM T10642_DELETIONS; 

-- DERBY-1652
create table test (testid integer not null 
    generated always as identity (start with 1, increment by 1), 
    info integer not null, ts timestamp not null default '1980-01-01-00.00.00.000000');
create trigger update_test 
    after update on test 
    referencing old as old 
    for each row mode db2sql 
    update test set ts=current_timestamp where testid=old.testid;
insert into test(info) values (1),(2),(3);
UPDATE TEST SET INFO = 1 WHERE TESTID = 2;
drop table test;

-- DERBY-1621
-- creating and dropping index on the table in the trigger action
create table t1 (i int);
create table t2 (i int);
create trigger tt after insert on t1 for each statement mode db2sql insert into t2 values 1;
insert into t1 values 1;
create unique index tu on t2(i);
insert into t1 values 1;
select * from t2;
insert into t1 values 1;
select * from t2;
drop index tu;
select * from t2;
insert into t1 values 1;
select * from t2;
drop trigger tt;

-- dropping and recreating a table which the trigger references
create table t3 (i int);
create table t4 (i int);
create trigger tt2 after insert on t3 for each statement mode db2sql insert into t4 values 1;
insert into t3 values 1;
select * from t4;
drop table t4;
insert into t3 values 1;
create table t4 (i int);
insert into t3 values 1;
select * from t4;

-- dropping a function which the trigger references
create function max_value(x int, y int) returns int language java parameter style java external name 'java.lang.Math.max';
create table test(a integer);
create trigger test_trigger AFTER insert on test FOR EACH ROW MODE DB2SQL values max_value(2,4);

insert into test values(1);

--- drop function and again do inserts. these should not work as the trigger would be invalid
drop function max_value;
insert into test values(2);
insert into test values(1);


-- dropping a view which the trigger references
create table t11TriggerTest (c111 int not null primary key, c112 int);
insert into t11TriggerTest values(1,1);
insert into t11TriggerTest values(2,2);

-- create a view based on table t11TriggerTest
create view v21ViewTest as select * from t11TriggerTest;

-- get ready to create a trigger. Trigger is created on t31TriggerTest and it inserts into t32TriggerTest
create table t31TriggerTest (c311 int);
create table t32TriggerTest (c321 int);
create trigger tr31t31TriggerTest after insert on t31TriggerTest for each statement mode db2sql
   insert into t32TriggerTest values (select c111 from v21ViewTest where c112=1);

-- try an insert which will fire the trigger
insert into t31TriggerTest values(1);
select * from t31TriggerTest;
-- we know the trigger got fired if there is one row in t32TriggerTest
select * from t32TriggerTest;

-- drop the view used by the trigger.
drop view v21ViewTest;

-- try an insert which would cause insert trigger to fire. The insert trigger should have failed because view doesn't
-- exist anymore.
insert into t31TriggerTest values(1);
select * from t31TriggerTest;
select * from t32TriggerTest;

-- DERBY-1204
-- trigger causes StringIndexOutOfBoundsException
-- which half closes connection and causes rest of test to
-- fail. Enable this trigger test case to resolve 1204.
create table x (x int);
-- ok
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into x values (n.x), (999), (333);
insert into x values 1;
select * from x order by 1;
drop trigger tgood;
drop table x;

create table x (i int);
create table y (i int);
-- ok
create trigger tgood after insert on x
for each statement mode db2sql insert into y values (666), (999), (333);
drop trigger tgood;
-- ok
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into y values (n.i);
drop trigger tgood;
-- ok
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into y values (333), (999), (333);
drop trigger tgood;
-- ok.  This used to throw StringIndexOutOfBoundsException
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into y values (n.i), (999), (333); 
insert into x values (888);
select * from y;
drop trigger tgood;
delete from x;
delete from y;
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into y values (n.i), (n.i+1), (n.i+2); 
insert into x values (1), (4), (7);
select * from y;
drop trigger tgood;
drop table x;
drop table y;
create table x (i int, j varchar(10));
create table y (i int, j varchar(10));
create trigger tgood after insert on x
referencing new as n
for each row mode db2sql insert into y values (0, 'X'), (n.i, 'Y'), (0, n.j), (n.i,n.j);
insert into x values (1,'A'), (2,'B'), (3, 'C');
select * from y;
drop trigger tgood;
drop table x;
drop table y;

