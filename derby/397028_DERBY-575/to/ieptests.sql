--table used for export
create table ex_emp(id int , name char(7) , skills varchar(200), salary decimal(10,2)) ;
--table used for import
create table imp_emp(id int , name char(7), skills varchar(200), salary decimal(10,2)) ;
--After an export from ex_emp and import to imp_emp both tables should have 
--same data.
--double delimter cases with default character delimter "
--field seperator character inside a double delimited string as first line
insert into ex_emp values(99, 'smith' , 'tennis"p,l,ayer"', 190.55) ;
-- Perform Export:

call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                 null, null, null) ;
-- Perform Import
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'IMP_EMP' , 'extinout/emp.dat' , 
                                      null, null, null, 0) ;

insert into ex_emp values(100, 'smith' , 'tennis"player"', 190.55) ;
insert into ex_emp values(101, 'smith' , 'tennis"player', 190.55) ;
insert into ex_emp values(102, 'smith' , '"tennis"player', 190.55) ;
insert into ex_emp values(103, 'smith' , '"tennis"player"', 190.55) ;
insert into ex_emp values(104, 'smith' , '"tennis"""""""""""""""""""""""""""""""""""""player"', null) ;
--empty string
insert into ex_emp values(105, 'smith' , '""', 190.55) ;
--just delimeter inside 
insert into ex_emp values(106, 'smith' , '"""""""""""""""""""', 190.55); 
--null value
insert into ex_emp values(107, 'smith"' , null, 190.55) ;
--all values are nulls
insert into ex_emp values(108, null , null, null) ;

-- Perform Export:

call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                 null, null, null) ;
-- Perform Import
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'IMP_EMP' , 'extinout/emp.dat' , 
                                      null, null, null, 0) ;

select * from ex_emp;
select * from imp_emp;
--checking query
select count(*) from imp_emp, ex_emp
      where ex_emp.id = imp_emp.id and
      (ex_emp.skills=imp_emp.skills or
      (ex_emp.skills is NULL and imp_emp.skills is NULL));



delete from imp_emp where id < 105;
--export from ex_emp using the a query only rows that got deleted in imp_emp 
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select * from ex_emp where id < 105', 
                                    'extinout/emp.dat' , null, null, null) ;

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'IMP_EMP' , 'extinout/emp.dat' , 
                                      null, null, null, 0) ;

--checking query
select count(*) from imp_emp, ex_emp
      where ex_emp.id = imp_emp.id and
      (ex_emp.skills=imp_emp.skills or
      (ex_emp.skills is NULL and imp_emp.skills is NULL));


--export the columns in different column order than in the table.
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select name , salary , skills, id from ex_emp where id < 105', 
                                    'extinout/emp.dat' , null, null, null) ;

-- import them in to a with order different than in the table;

call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'IMP_EMP' ,'NAME, SALARY, SKILLS, ID', null,
                                    'extinout/emp.dat', null, null, null, 1) ;
--check query
select count(*) from imp_emp, ex_emp
      where ex_emp.id = imp_emp.id and
      (ex_emp.skills=imp_emp.skills or
      (ex_emp.skills is NULL and imp_emp.skills is NULL));


-- do import replace into the table with table order but using column indexes
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'IMP_EMP' ,null, '4, 1, 3, 2',
                                    'extinout/emp.dat', null, null, null, 1) ;

--check query
select count(*) from imp_emp, ex_emp
      where ex_emp.id = imp_emp.id and
      (ex_emp.skills=imp_emp.skills or
      (ex_emp.skills is NULL and imp_emp.skills is NULL));

--replace using insert column names and column indexes
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'IMP_EMP' ,'SALARY, ID, SKILLS, NAME', '2, 4, 3, 1',
                                    'extinout/emp.dat', null, null, null, 1) ;

--check query
select count(*) from imp_emp, ex_emp
      where ex_emp.id = imp_emp.id and
      (ex_emp.skills=imp_emp.skills or
      (ex_emp.skills is NULL and imp_emp.skills is NULL));


---testing with different delimiters
-- single quote(') as character delimiter
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                    null, '''', null) ;

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'IMP_EMP' , 'extinout/emp.dat' , 
                                    null, '''', null, 1) ;

select * from imp_emp ;

-- single quote(') as column delimiter
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                    '''',null, null) ;
delete from imp_emp ;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'IMP_EMP' , 'extinout/emp.dat' , 
                                    '''', null, null, 0) ;
select * from imp_emp;

call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                 '*', '%', null) ;

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'EX_EMP' , 'extinout/emp.dat' , 
                                 '*', '%', null, 1) ;

select * from imp_emp ;

--cases for identity columns
--create table emp1(id int generated always as identity (start with 100), name char(7), 
--              skills varchar(200), salary decimal(10,2),skills varchar(200));


--check import export with real and double that can not be explictitly
--casted from VARCHAR type .

create table noncast(c1 double , c2 real ) ;
insert into noncast values(1.5 , 6.7 ) ;
insert into noncast values(2.5 , 8.999) ;
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('APP' , 'NONCAST' , 'extinout/noncast.dat'  , null , null , null) ;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'NONCAST' , 'extinout/noncast.dat'  , null , null , null , 0) ;
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'NONCAST', 'C2 , C1' , '2, 1' , 
                                   'extinout/noncast.dat'  , null , null , null , 0) ;
select * from noncast ;

--check import/export of time types

CREATE TABLE   TTYPES(DATETYPE DATE, TIMETYPE TIME, TSTAMPTYPE TIMESTAMP );
insert into ttypes values('1999-09-09' , '12:15:19' , '1999-09-09 11:11:11' );
insert into ttypes values('2999-12-01' , '13:16:10' , '2999-09-09 11:12:11' );
insert into ttypes values('3000-11-02' , '14:17:21' , '4999-09-09 11:13:11' );
insert into ttypes values('2004-04-03' , '15:18:31' , '2004-09-09 11:14:11' );
insert into ttypes values(null , null , null);

call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'TTYPES' , 'extinout/ttypes.del' , 
                                 null, null, null) ;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'TTYPES' , 'extinout/ttypes.del' , 
                                 null, null, null, 0) ;

select * from ttypes;




---Import should commit on success and rollback on any failures
autocommit off ;
create table t1(a int ) ;
insert into t1 values(1) ;
insert into t1 values(2) ;
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'T1' , 'extinout/t1.del' , 
                                 null, null, null) ;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1' , 'extinout/t1.del' , 
                                 null, null, null, 0) ;
--above import should have committed , following rollback should be a noop.
rollback;
select * from t1;
insert into t1 values(3) ;
insert into t1 values(4) ;
--file not found error should rollback 
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1' , 'extinout/nofile.del' , 
                                 null, null, null, 0) ;
commit;
select * from t1 ;
insert into t1 values(3) ;
insert into t1 values(4) ;
--table not found error should issue a implicit rollback 
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'NOTABLE' , 'extinout/t1.del' , 
                                 null, null, null, 0) ;
commit ;
select * from t1 ;
delete from t1;
---check commit/rollback with replace options using 
insert into t1 values(1) ;
insert into t1 values(2) ;
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'T1' , 'extinout/t1.del' , 
                                 null, null, null) ;
--above export should have a commit.rollback below should be a noop
rollback;
select * from t1;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1' , 'extinout/t1.del' , 
                                 null, null, null, 1) ;
--above import should have committed , following rollback should be a noop.
rollback;
select * from t1;
insert into t1 values(3) ;
insert into t1 values(4) ;
--file not found error should rollback 
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'T1' , 'extinout/nofile.del' , 
                                 null, null, null, 1) ;
commit;
select * from t1 ;
insert into t1 values(3) ;
insert into t1 values(4) ;
--table not found error should issue a implicit rollback 
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'NOTABLE' , 'extinout/t1.del' , 
                                 null, null, null, 1) ;
commit ;
---check IMPORT_DATA calls commit/rollback
select * from t1 ;
delete from t1;
---check commit/rollback with replace options using 
insert into t1 values(1) ;
insert into t1 values(2) ;
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'T1' , 'extinout/t1.del' , 
                                 null, null, null) ;
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'T1' , 'A' , '1' , 'extinout/t1.del' , 
                                 null, null, null, 0) ;
--above import should have committed , following rollback should be a noop.
rollback;
select * from t1;
insert into t1 values(3) ;
insert into t1 values(4) ;
--file not found error should rollback 
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'T1', 'A' , '1'  , 'extinout/nofile.del' , 
                                 null, null, null, 0) ;
commit;
select * from t1 ;
insert into t1 values(3) ;
insert into t1 values(4) ;
--table not found error should issue a implicit rollback 
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'NOTABLE' , 'A' , '1', 'extinout/t1.del' , 
                                 null, null, null, 1) ;
commit ;
select * from t1 ;

autocommit on ;
--make sure commit import code is ok in autcommit mode.
insert into t1 values(3) ;
insert into t1 values(4) ;
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'T1' , 'A' , '1' , 'extinout/t1.del' , 
                                 null, null, null, 0) ;
select * from t1 ;
insert into t1 values(5) ;
insert into t1 values(6) ;
--following import will back , but should not have any impact on inserts
call SYSCS_UTIL.SYSCS_IMPORT_DATA(null, 'T1', 'A' , '1'  , 'extinout/nofile.del' , 
                                 null, null, null, 0) ;
select * from t1 ;
--END IMPORT COMMIT/ROLLBACK TESTSING

---all types supported by Derby import/export
create table alltypes(chartype char(20) , 
	          biginttype bigint , 
		  datetype date , 
		  decimaltype decimal(10,5) , 
		  doubletype double , 
		  inttype integer , 
		  lvartype long varchar , 
		  realtype real , 
		  sminttype smallint , 
		  timetype time , 
		  tstamptype timestamp , 
		  vartype varchar(50));

insert into  alltypes values('chartype string' ,
                          9223372036854775807,
                         '1993-10-29' ,
                          12345.54321,
                          10E307,
                          2147483647,
                          'long varchar testing',
                          10E3,
                          32767,
                          '09.39.43',
                          '2004-09-09 11:14:11',
                          'varchar testing');

insert into  alltypes values('chartype string' ,
                          -9223372036854775808,
                         '1993-10-29' ,
                          0.0,
                          -10E307,
                          -2147483647,
                          'long varchar testing',
                          -10E3,
                          32767,
                          '09.39.43',
                          '2004-09-09 11:14:11',
                          'varchar testing');

insert into  alltypes values('"chartype" string' , 
                              9223372036854775807,
                             '1993-10-29' , 
                              -12345.54321,
                              10E307,
                              2147483647,
                              'long "varchar" testing',
                              10E3,
                              32767,
                              '09.39.43',
                              '2004-09-09 11:14:11',
                              '"varchar" testing');
                              
call SYSCS_UTIL.SYSCS_EXPORT_TABLE (null, 'ALLTYPES' , 'extinout/alltypes.del' , 
                                 null, null, null) ;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'ALLTYPES' , 'extinout/alltypes.del' , 
                                 null, null, null, 0) ;
select * from alltypes ;                          
delete from alltypes;

--import should work with trigger enabled on append and should not work on replace
create table test1(a char(20)) ;
create trigger trig_import after INSERT on alltypes
referencing new as newrow
for each  row mode db2sql
insert into test1 values(newrow.chartype);

call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'ALLTYPES' , 'extinout/alltypes.del' , 
                                 null, null, null, 0) ;
select count(*) from alltypes ;
select * from test1;
delete from alltypes;
call SYSCS_UTIL.SYSCS_IMPORT_TABLE (null, 'ALLTYPES' , 'extinout/alltypes.del' , 
                                 null, null, null, 1) ;
select count(*) from alltypes;

drop trigger trig_import;
drop table test1;

--test importing to identity columns
create table table1(c1 char(30), 
       c2 int generated always as identity,
       c3 real,
       c4 char(1));

create table table2(c1 char(30), 
       c2 int,
       c3 real,
       c4 char(1));

insert into table2 values('Robert',100, 45.2, 'J');
insert into table2 values('Mike',101, 76.9, 'K');
insert into table2 values('Leo',102, 23.4, 'I');

call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select c1,c3,c4 from table2' , 'extinout/import.del' , 
                                 null, null, null) ;
CALL SYSCS_UTIL.SYSCS_IMPORT_DATA(NULL,'TABLE1', 'C1,C3,C4' , null, 'extinout/import.del',null, null,null,0);

select * from table1;
delete from table1;
call SYSCS_UTIL.SYSCS_EXPORT_TABLE(null , 'TABLE2' , 'extinout/import.del',  null, null, null) ;

--following import should fail becuase of inserting into identity column.
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(NULL, 'TABLE1', 'extinout/import.del',null, null, null,1);

--following import should be succesful
CALL SYSCS_UTIL.SYSCS_IMPORT_DATA(NULL, 'TABLE1', 'C1,C3,C4' , '1,3,4', 'extinout/import.del',null, null, null,1);
select * from table1;

update table2 set c2=null;
--check null values import to identity columns should also fail
call SYSCS_UTIL.SYSCS_EXPORT_TABLE(null , 'TABLE2' , 'extinout/import.del' , 
                                 null, null, null) ;
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(NULL, 'TABLE1', 'extinout/import.del',null, null, null,1);
select * from table1;

--check that replace fails when there dependents and replaced data 
--does not violate foreign key constraints.
create table parent(a int not null primary key);
insert into parent values (1) , (2) , (3) , (4) ;
create table child(b int references parent(a));
insert into child values (1) , (2) , (3) , (4) ;
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select * from parent where a < 3' , 'extinout/parent.del' , 
                                 null, null, null) ;
--replace should fail because of dependent table
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(NULL, 'PARENT', 'extinout/parent.del',null, null, null,1);
select * from parent;

---test with a file which has a differen records seperators (\n, \r , \r\n)
create table nt1( a int , b char(30));
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(NULL, 'NT1', 'extin/mixednl.del',null, null, 'UTF-8',0);
select * from nt1;
drop table nt1 ;

--test case for bug 5977;(with lot of text data)
create table position_info
    (
       position_code varchar(10) not null ,
       literal_no int not null ,
       job_category_code varchar(10),
       summary_description long varchar,
       detail_description long varchar,
       web_flag varchar(1)
    );
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP', 'POSITION_INFO', 'extin/position_info.del',
                                    null, null, 'US-ASCII', 1);
select count(*) from position_info ;
select detail_description from position_info where position_code='AG1000';
CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE ('APP', 'POSITION_INFO', 'extinout/pinfo.del',
                                    null, null, null);
delete from position_info;
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('APP', 'POSITION_INFO', 'extinout/pinfo.del',
                                    null, null, null, 1);
select count(*) from position_info ;
select detail_description from position_info where position_code='AG1000';


--test for autoincrement values
CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY('values(1),(2),(3)','extinout/autoinc.dat',null,null,null);


create table dest_always(i int generated always as identity);
create table dest_by_default(i int generated by default as identity);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('APP','DEST_ALWAYS','extinout/autoinc.dat',null,null,null,0);
select * from dest_always;
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('APP','DEST_BY_DEFAULT','extinout/autoinc.dat',null,null,null,0);
select * from dest_by_default;

drop table dest_always;
drop table dest_by_default;


create table dest_always(i int generated always as identity);
create table dest_by_default(i int generated by default as identity);

CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('APP','DEST_ALWAYS','extinout/autoinc.dat',null,null,null,1);
select * from dest_always;
CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE('APP','DEST_BY_DEFAULT','extinout/autoinc.dat',null,null,null,1);
select * from dest_by_default;

drop table dest_always;
drop table dest_by_default;
--test case for bug (DERBY-390)
--test import/export with reserved words as table Name, column Names ..etc.
create schema "Group";
create table "Group"."Order"("select" int, "delete" int, itemName char(20)) ;
insert into "Group"."Order" values(1, 2, 'memory') ;
insert into "Group"."Order" values(3, 4, 'disk') ;
insert into "Group"."Order" values(5, 6, 'mouse') ;

--following export should fail because schema name is not matching the way it is defined using delimited quotes.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('GROUP', 'Order' , 'extinout/order.dat', null, null, null) ;
--following export should fail because table name is not matching the way it is defined in the quotes.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('Group', 'ORDER' , 'extinout/order.dat', null, null, null) ;
--following export should fail because of unquoted table name that is a reserved word.
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select * from "Group".Order' , 'extinout/order.dat' ,    null , null , null ) ;
--following exports should pass.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('Group', 'Order' , 'extinout/order.dat', null, null, null) ;
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select * from "Group"."Order"' , 'extinout/order.dat' ,    null , null , null ) ;
call SYSCS_UTIL.SYSCS_EXPORT_QUERY('select "select" , "delete" , itemName from "Group"."Order"' , 
                                                 'extinout/order.dat' ,    null , null , null ) ;

--following import should fail because schema name is not matching the way it is defined using delimited quotes.
call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('GROUP', 'Order' , 'extinout/order.dat', null, null, null, 0) ;
--following import should fail because table name is not matching the way it is defined in the quotes.
call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('Group', 'ORDER' , 'extinout/order.dat', null, null, null, 0) ;
--following import should fail because table name is not matching the way it is defined in the quotes.
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'ORDER' , null , null ,   'extinout/order.dat'   , null , null , null, 1) ; 
--following import should fail because column name is not matching the way it is defined in the quotes.
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'Order' , 'DELETE, ITEMNAME' , '2, 3' ,   'extinout/order.dat'   , null , null , null, 1) ; 
--following import should fail because undelimited column name is not in upper case.
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'Order' , 'delete, itemName' , '2, 3' ,   'extinout/order.dat'   , null , null , null, 1) ; 

--following imports should pass
call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('Group', 'Order' , 'extinout/order.dat', null, null, null, 0) ;
select * from "Group"."Order";
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'Order' , null , null ,   'extinout/order.dat'   , null , null , null, 1) ; 
select * from "Group"."Order";
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'Order' , 'delete' , '2' ,   'extinout/order.dat'   , null , null , null, 1) ; 
select * from "Group"."Order";
call SYSCS_UTIL.SYSCS_IMPORT_DATA('Group', 'Order' , 'ITEMNAME, select, delete' , '3,2,1' ,   'extinout/order.dat'   , null , null , null, 1) ; 
select * from "Group"."Order";
drop table "Group"."Order";

---test undelimited names( All unquoted SQL identfiers should be passed in upper case). 
create schema inventory;
create table inventory.orderTable(id int, amount int, itemName char(20)) ;
insert into inventory.orderTable values(101, 5, 'pizza') ;
insert into inventory.orderTable values(102, 6, 'coke') ;
insert into inventory.orderTable values(103, 7, 'break sticks') ;
insert into inventory.orderTable values(104, 8, 'buffolo wings') ;

--following export should fail because schema name is not in upper case.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('inventory', 'ORDERTABLE' , 'extinout/order.dat', null, null, null) ;
--following export should fail because table name is not in upper case.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('INVENTORY', 'ordertable' , 'extinout/order.dat', null, null, null) ;

--following export should pass.
call SYSCS_UTIL.SYSCS_EXPORT_TABLE ('INVENTORY', 'ORDERTABLE' , 'extinout/order.dat', null, null, null) ;

--following import should fail because schema name is not in upper case
call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('inventory', 'ORDERTABLE' , 'extinout/order.dat', null, null, null, 0) ;
--following import should fail because table name is not in upper case.
call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('INVENTORY', 'ordertable' , 'extinout/order.dat', null, null, null, 0) ;
--following import should fail because table name is not in upper case .
call SYSCS_UTIL.SYSCS_IMPORT_DATA('INVENTORY', 'ordertable' , null , null ,   'extinout/order.dat'   , null , null , null, 1) ; 
--following import should fail because column name is not in upper case.
call SYSCS_UTIL.SYSCS_IMPORT_DATA('INVENTORY', 'ORDERTABLE' , 'amount, ITEMNAME' , '2, 3' ,   'extinout/order.dat'   , null , null , null, 1) ; 


call SYSCS_UTIL.SYSCS_IMPORT_DATA('INVENTORY', 'ORDERTABLE' , null , null ,   'extinout/order.dat'   , null , null , null, 1) ; 
select * from inventory.orderTable;
call SYSCS_UTIL.SYSCS_IMPORT_DATA('INVENTORY', 'ORDERTABLE' , 'ITEMNAME, ID, AMOUNT' , '3,2,1' ,   'extinout/order.dat'   , null , null , null, 1) ; 
select * from inventory.orderTable;

drop table inventory.orderTable;
--end derby-390 related test cases.
