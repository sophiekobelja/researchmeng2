autocommit off;
create table a (x int);
insert into a values (1);
insert into a values (1);
insert into a values (1);
insert into a values (1);
insert into a values (1);
drop table a;
commit;
disconnect;
