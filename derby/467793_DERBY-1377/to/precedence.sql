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

-- this tests precedence of operators.


-- test precedence of operators other than and, or, and not
-- that return boolean.

-- expect 'true' row:
create table t1(c11 int);
insert into t1 values(1);

select c11 from t1 where 1 in (1,2,3) = (1=1);

--
select c11 from t1 where 'acme widgets' like 'acme%' in (1=1);

select c11 from t1 where 1 between -100 and 100 is not null;

--
select c11 from t1 where exists(select * from (values 1) as t) not in (1=2);
