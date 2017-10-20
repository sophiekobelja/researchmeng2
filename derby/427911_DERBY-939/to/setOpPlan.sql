-- DERBY-939 
-- Test union and intersect/except with runtime statistics enabled.
CREATE TABLE t1 (i int);
CREATE TABLE t2 (j int);
CREATE TABLE t3 (k int);
INSERT INTO t1 VALUES 3,2,1;
INSERT INTO t2 VALUES 1,2,3,4;
INSERT INTO t3 VALUES 5,2,3,4,1;
CALL SYSCS_UTIL.SYSCS_SET_RUNTIMESTATISTICS(1);
MaximumDisplayWidth 7000;

SELECT i FROM t1 UNION SELECT j FROM t2 INTERSECT SELECT k FROM t3;
VALUES SYSCS_UTIL.SYSCS_GET_RUNTIMESTATISTICS();

SELECT i FROM t1 UNION SELECT j FROM t2 EXCEPT SELECT k FROM t3;
VALUES SYSCS_UTIL.SYSCS_GET_RUNTIMESTATISTICS();

SELECT i FROM t1 INTERSECT SELECT j FROM t2 EXCEPT SELECT k FROM t3;
VALUES SYSCS_UTIL.SYSCS_GET_RUNTIMESTATISTICS();

CALL SYSCS_UTIL.SYSCS_SET_RUNTIMESTATISTICS(0);