-- Very basic single user testing of update row locking.
run resource 'createTestProcedures.subsql';
call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.storage.pageSize', '4096');

autocommit off;
run resource 'LockTableQuery.subsql';
commit;

-- READ COMMITTED TEST
set current isolation read committed;
commit;

-- run each test with rows on one page in the interesting conglomerate (heap in
-- the non-index tests, and in the index in the index based tests).

-- cursor, no index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
    
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
    
    create table a(a int, b int);
    alter table a add column c varchar(1900);
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
    
    create table a(a int, b int);
    alter table a add column c varchar(1900);
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
    
    create table a(a int, b int);
    alter table a add column c varchar(1900);
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- run each test with rows across multiple pages in the interesting 
-- conglomerate (heap in the non-index tests, and in the index in the index 
-- based tests).

-- cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(600) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',600));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',600));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',600));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',600));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',600));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',600));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',600));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(700) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',700));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',700));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',700));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',700));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',700));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',700));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',700));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(800) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',800));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',800));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',800));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',800));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',800));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',800));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',800));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(900) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',900));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',900));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',900));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',900));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',900));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',900));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',900));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';

commit;

-- REPEATABLE READ TEST
--   should be the same as SERIALIZABLE results except no previous key locks.

set isolation rs;
commit;

-- run each test with rows on one page in the interesting conglomerate (heap in
-- the non-index tests, and in the index in the index based tests).

-- cursor, no index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
        -- create table a (a int, b int, c varchar(1900));
            
    create table a(a int, b int);
    alter table a add column c varchar(1900); 

    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- run each test with rows across multiple pages in the interesting 
-- conglomerate (heap in the non-index tests, and in the index in the index 
-- based tests).

-- cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(600) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',600));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',600));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',600));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',600));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',600));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',600));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',600));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(700) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',700));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',700));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',700));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',700));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',700));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',700));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',700));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(800) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',800));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',800));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',800));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',800));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',800));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',800));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',800));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(900) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',900));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',900));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',900));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',900));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',900));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',900));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',900));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';


commit;

-- SERIALIZABLE TEST
set isolation serializable;
commit;

-- run each test with rows on one page in the interesting conglomerate (heap in
-- the non-index tests, and in the index in the index based tests).

-- cursor, no index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
    	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
        -- to create tables of page size 4k and still keep the following tbl 
	-- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- run each test with rows across multiple pages in the interesting 
-- conglomerate (heap in the non-index tests, and in the index in the index 
-- based tests).

-- cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(600) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',600));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',600));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',600));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',600));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',600));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',600));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',600));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(700) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',700));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',700));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',700));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',700));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',700));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',700));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',700));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(800) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',800));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',800));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',800));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',800));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',800));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',800));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',800));
   create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(900) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',900));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',900));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',900));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',900));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',900));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',900));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',900));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';


commit;

-- READ UNCOMMITTED TEST (for update tests should act just like read committed).
set isolation read uncommitted;
commit;

-- run each test with rows on one page in the interesting conglomerate (heap in
-- the non-index tests, and in the index in the index based tests).

-- cursor, no index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create unique index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    -- to create tables of page size 4k and still keep the following tbl 
    -- create table a (a int, b int, c varchar(1900));
        
    create table a(a int, b int);
    alter table a add column c varchar(1900); 
    insert into a values (1, 10, 'one');
    insert into a values (2, 20, 'two');
    insert into a values (3, 30, 'three');
    insert into a values (4, 40, 'four');
    insert into a values (5, 50, 'five');
    insert into a values (6, 60, 'six');
    insert into a values (7, 70, 'seven');
    create index a_idx on a (a);
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- run each test with rows across multiple pages in the interesting 
-- conglomerate (heap in the non-index tests, and in the index in the index 
-- based tests).

-- cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatecursorlocks.subsql';

-- non cursor, no index run
    drop table a;
    create table a (a int, b int, c varchar(1900)) ;
    insert into a values (1, 10, PADSTRING('one',1900));
    insert into a values (2, 20, PADSTRING('two',1900));
    insert into a values (3, 30, PADSTRING('three',1900));
    insert into a values (4, 40, PADSTRING('four',1900));
    insert into a values (5, 50, PADSTRING('five',1900));
    insert into a values (6, 60, PADSTRING('six',1900));
    insert into a values (7, 70, PADSTRING('seven',1900));
    commit;
    run resource 'updatesetlocks.subsql';

-- cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(600) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',600));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',600));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',600));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',600));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',600));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',600));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',600));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(700) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',700));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',700));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',700));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',700));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',700));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',700));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',700));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeCursorLocks.subsql';

-- non cursor, unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(800) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',800));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',800));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',800));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',800));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',800));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',800));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',800));
    create unique index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';

-- non cursor, non-unique index run
    drop table a;
    create table a (a int, b int, c varchar(1900), index_pad varchar(900) ) ;
    insert into a values (1, 10, PADSTRING('one',1900), PADSTRING('index pad 1',900));
    insert into a values (2, 20, PADSTRING('two',1900), PADSTRING('index pad 2',900));
    insert into a values (3, 30, PADSTRING('three',1900), PADSTRING('index pad 3',900));
    insert into a values (4, 40, PADSTRING('four',1900), PADSTRING('index pad 4',900));
    insert into a values (5, 50, PADSTRING('five',1900), PADSTRING('index pad 5',900));
    insert into a values (6, 60, PADSTRING('six',1900), PADSTRING('index pad 6',900));
    insert into a values (7, 70, PADSTRING('seven',1900), PADSTRING('index pad 7',900));
    create index a_idx on a (a, index_pad) ;
    commit;
    run resource 'updateBtreeSetLocks.subsql';


commit;
exit;
