-- This script tests online backup functionality and restore. 
connect 'wombat' as c1 ;
connect 'wombat' as c2;

set connection c1;
autocommit off;
-- check backup/restore work with in place compress operation. 
create table ctest(id int primary key, name char(200)) ;
insert into ctest values(1, 'derby backup/compress test') ;
insert into ctest values(2, 'derby backup/compress test') ;
insert into ctest select id+2, name from ctest;
insert into ctest select id+4, name from ctest;
insert into ctest select id+8, name from ctest;
insert into ctest select id+16, name from ctest;
insert into ctest select id+32, name from ctest;
insert into ctest select id+64, name from ctest;
insert into ctest select id+128, name from ctest;
insert into ctest select id+256, name from ctest;

commit ;
delete from ctest where id > 2 and id < 509 and id != 300;
select * from ctest;
commit;

--start backup in a seperare thread.
set connection c2;
async bthread 
   'call SYSCS_UTIL.SYSCS_BACKUP_DATABASE(''extinout/mybackup'')';

-- start compress in seperate thread. 
set connection c1;
async cthread 
 'call SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE(''APP'' , 
                                         ''CTEST'' , 1, 1, 1)';
set connection c2;
-- wait for backup thread to finish the work.
wait for bthread;
commit;
disconnect;

set connection c1;
-- wait for compress thread to finish the work.
wait for cthread;
commit;
disconnect;

--shutdown the database
connect 'wombat;shutdown=true';

connect 'wombat;restoreFrom=extinout/mybackup/wombat';
select * from ctest;
insert into ctest values(2000, 'restore was successfil') ;

