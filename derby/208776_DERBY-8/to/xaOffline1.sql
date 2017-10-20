xa_datasource 'wombat' create;
-- xa_datasource 'wombat';

---------------------------------------------
-- a single connection, prepare transaction and crash.
---------------------------------------------
xa_connect ;
xa_start xa_noflags 0;
xa_getconnection;
drop table foo;
create table foo (a int);
insert into foo values (0);
select * from foo;
run resource 'global_xactTable.view';
run resource 'LockTableQuery.subsql';
xa_end xa_success 0;
xa_commit xa_1phase 0;


---------------------------------------------
-- a single connection, prepare transaction and crash.
---------------------------------------------

xa_start xa_noflags 1;
insert into foo values (0);
select * from global_xactTable where gxid is not null order by gxid;


select global_xid, username, type, status, cast(sql_text as varchar(512)) sql_text from tran_table where global_xid is not null order by global_xid, username, type, status, sql_text;
xa_end xa_success 1;

xa_prepare 1;

xa_start xa_noflags 2;
select * from global_xactTable where gxid is not null order by gxid;
select * from lock_table order by tabname, type desc, mode, cnt, lockname;
xa_end xa_success 2;
xa_commit xa_1phase 2;

xa_recover xa_startrscan;


--- xa_datasource 'wombat' shutdown;

---------------------------------------------
-- shutdown the database.
---------------------------------------------
connect 'jdbc:derby:;shutdown=true';


---------------------------------------------
-- restart the system, should find the prepared transaction
---------------------------------------------

xa_datasource 'wombat';

---------------------------------------------
-- a single connection, verify the prepared xact has come back.
---------------------------------------------

xa_connect ;

xa_start xa_noflags 1;

xa_start xa_noflags 4;
xa_getconnection;
select * from global_xactTable where gxid is not null order by gxid;
select * from lock_table order by tabname, type desc, mode, cnt, lockname;
xa_recover xa_startrscan;
xa_end xa_success 4;
xa_commit xa_1phase 4;

---------------------------------------------
-- a single connection, now make sure after the commit that it is gone.
---------------------------------------------
xa_start xa_noflags 4;
xa_commit xa_2phase 1;
select * from global_xactTable where gxid is not null order by gxid;
select * from lock_table order by tabname, type desc, mode, cnt, lockname;
xa_recover xa_startrscan;
xa_end xa_success 4;
xa_commit xa_1phase 4;

---------------------------------------------
-- a single connection, verify the prepared xact has come back - should
-- get an error trying to start another with same global xact.
---------------------------------------------

exit;
