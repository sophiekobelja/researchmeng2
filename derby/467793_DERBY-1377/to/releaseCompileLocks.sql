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
autocommit off;

-- move static initializer tests to front, hoping to avoid class garbage
-- collection in jdk18.  Sometimes the static initializer in the 
-- DMLInStaticInitializer and InsertInStaticInitializer classes gets called
-- twice in jdk118 - causing a diff.  This can happen if for some reason the
-- JVM decides to garbage collect the class between references to the class
-- in the course of executing the query.

-- static initializers.
create table t1 (s int);
commit;

create function dmlstatic() returns INT
parameter style java language java
external name 'org.apache.derbyTesting.functionTests.util.StaticInitializers.DMLInStaticInitializer.getANumber'
no sql;

create function insertstatic() returns INT
parameter style java language java
external name 'org.apache.derbyTesting.functionTests.util.StaticInitializers.InsertInStaticInitializer.getANumber'
no sql;

commit;


-- the static initializer in DMLInStaticInitializer will select from t1 
-- the DML will be executed within a nested query-- however all locks 
-- on system tables which the static initializer gets should be released.
select 
(dmlstatic()) 
from sys.systables where tablename = 'SYSCONGLOMERATES';

select TYPE, MODE, TABLENAME, LOCKNAME, STATE from syscs_diag.lock_table
order by 1;

commit;

drop table t1;
create table t1 (s int);
commit;
select 
(insertstatic()) 
from sys.systables where tablename = 'SYSCONGLOMERATES';

-- only two locks!
select TYPE, MODE, TABLENAME, LOCKNAME, STATE from syscs_diag.lock_table
order by 1;

-- verify that the row went into t1.
select * from t1;
drop table t1;
commit;

select TYPE, MODE, TABLENAME, LOCKNAME, STATE from syscs_diag.lock_table
order by 1;

commit;


-- some really simple tests to start off.
create table test_tab (x int);
insert into test_tab values (1);
commit;

-- earlier we would get a bunch of locks on the system catalogs 
-- when trying to resolve the method alias.
select count(*) from syscs_diag.lock_table;

-- select from a system catalog.
select count(*) from sys.sysviews;
-- look ma, no locks.
select count(*) from syscs_diag.lock_table;

insert into test_tab values (2);
-- only see locks on test_tab, none on system catalogs
-- 

select TYPE, MODE, TABLENAME, LOCKNAME, STATE from syscs_diag.lock_table
order by 1;

-- bugid 3214, atlas case: 962505
-- selecting from a table would hold locks which would disallow creating
-- another table.
drop table t1;
create table t1 (x int);
commit;
select * from t1;

connect 'wombat' as conn1;
-- this should not time out waiting for locks.
create table t2 (x int);
drop table t2;
set connection connection0;
disconnect conn1;
commit;

show connections;

-- create table again to force scanning system catalogs.
drop table test_tab;
create table test_tab (x int);
insert into test_tab values (1);
commit;

-- prepare a statement-- no locks.
prepare cursor1 as 'update test_tab set x=2 where x=?';
select count(*) from syscs_diag.lock_table;

-- now execute it-- should see locks on test_tab
execute cursor1 using 'values (1)';
select TYPE, MODE, TABLENAME, LOCKNAME, STATE from syscs_diag.lock_table
order by 1;
commit;


-- problem with backing index scans.

create table t (c1 int not null primary key, c2 int references t);
insert into t values (1,1);
insert into t values (2,1);

commit;

prepare ps as 'select * from t where c1 = ? and c2 = ?';

-- no locks, no locks at all.
select * from syscs_diag.lock_table;

-- clear DataDictionary cache
create table x(c1 int);
drop table x;
commit;

-- try inserting into the table; no locks on system catalogs.
prepare pi as 'insert into t values (3,2)';
select * from syscs_diag.lock_table;

commit;

-- clear DataDictionary cache
create table x(c1 int);
drop table x;
commit;

-- try updating the table; no locks on system catalogs.
prepare p1 as 'update t set c2 = c1, c1 = c2';
select * from syscs_diag.lock_table;

commit;

-- clear DataDictionary cache
create table x(c1 int);
drop table x;
commit;

-- try deleting from the table; no locks on system catalogs.
prepare p1 as 'delete from t';
select * from syscs_diag.lock_table;

commit;

-- create some triggers.
create trigger update_of_t after update on t for each row mode db2sql values 2;

create trigger insert_of_t after insert on t for each row mode db2sql values 3;

commit;

-- t has (1,1) (2,1) (3,2)
prepare pu as 'update t set c2=2 where c1=2';
select * from syscs_diag.lock_table;
commit;
