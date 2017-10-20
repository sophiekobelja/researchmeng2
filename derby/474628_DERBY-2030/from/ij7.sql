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

-- This test will cover SHOW TABLES, SHOW SCHEMAS, etc.
-- and the DESCRIBE command.

connect 'jdbc:derby:wombat;create=true';

SET SCHEMA = APP;
CREATE TABLE t1 (i int generated always as identity, d DECIMAL(5,2), test VARCHAR(20));

CREATE SCHEMA USER1;
SET SCHEMA = USER1;
CREATE TABLE t2 (i int);

CREATE SYNONYM USER1.T3 FOR USER1.T2;
CREATE VIEW v1 AS SELECT * from app.t1;
CREATE INDEX idx1 ON APP.t1 (test ASC);
CREATE PROCEDURE APP.PROCTEST(IN A INTEGER, OUT B DECIMAL(10,2))
PARAMETER STYLE JAVA READS SQL DATA LANGUAGE JAVA 
EXTERNAL NAME 'a.b.c.d.e';

-- first display all tables, then display tables in one schema
SHOW TABLES;
SHOW TABLES IN APP;

-- 'describe t1' will give error, as not in current schema
DESCRIBE t1;
DESCRIBE APP.t1;
DESCRIBE v1;

SHOW SCHEMAS;
SHOW VIEWS IN USER1;
SHOW PROCEDURES IN APP;
SHOW SYNONYMS IN USER1;
SHOW INDEXES FROM APP.t1;

