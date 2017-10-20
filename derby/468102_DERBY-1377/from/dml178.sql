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
AUTOCOMMIT OFF;

-- MODULE  DML178  

-- SQL Test Suite, V6.0, Interactive SQL, dml178.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0891 FIPS sizing, 250 columns, 4000 char data statement!

   CREATE TABLE L1 (
C1 INT, C2 INT, C3 INT, C4 INT, C5 INT, C6 INT, 
C7 INT, C8 INT, C9 INT, C10 INT, C11 INT, C12 INT, 
C13 INT, C14 INT, C15 INT, C16 INT, C17 INT, C18 INT, 
C19 INT, C20 INT, C21 INT, C22 INT, C23 INT, C24 INT, 
C25 INT, C26 INT, C27 INT, C28 INT, C29 INT, C30 INT, 
C31 INT, C32 INT, C33 INT, C34 INT, C35 INT, C36 INT, 
C37 INT, C38 INT, C39 INT, C40 INT, C41 INT, C42 INT, 
C43 INT, C44 INT, C45 INT, C46 INT, C47 INT, C48 INT, 
C49 INT, C50 INT, C51 INT, C52 INT, C53 INT, C54 INT, 
C55 INT, C56 INT, C57 INT, C58 INT, C59 INT, C60 INT, 
C61 INT, C62 INT, C63 INT, C64 INT, C65 INT, C66 INT, 
C67 INT, C68 INT, C69 INT, C70 INT, C71 INT, C72 INT, 
C73 INT, C74 INT, C75 INT, C76 INT, C77 INT, C78 INT, 
C79 INT, C80 INT, C81 INT, C82 INT, C83 INT, C84 INT, 
C85 INT, C86 INT, C87 INT, C88 INT, C89 INT, C90 INT, 
C91 INT, C92 INT, C93 INT, C94 INT, C95 INT, C96 INT, 
C97 INT, C98 INT, C99 INT, C100 INT, C101 INT, C102 INT, 
C103 INT, C104 INT, C105 INT, C106 INT, C107 INT, C108 INT, 
C109 INT, C110 INT, C111 INT, C112 INT, C113 INT, C114 INT, 
C115 INT, C116 INT, C117 INT, C118 INT, C119 INT, C120 INT, 
C121 INT, C122 INT, C123 INT, C124 INT, C125 INT, C126 INT, 
C127 INT, C128 INT, C129 INT, C130 INT, C131 INT, C132 INT, 
C133 INT, C134 INT, C135 INT, C136 INT, C137 INT, C138 INT, 
C139 INT, C140 INT, C141 INT, C142 INT, C143 INT, C144 INT, 
C145 INT, C146 INT, C147 INT, C148 INT, C149 INT, C150 INT, 
C151 INT, C152 INT, C153 INT, C154 INT, C155 INT, C156 INT, 
C157 INT, C158 INT, C159 INT, C160 INT, C161 INT, C162 INT, 
C163 INT, C164 INT, C165 INT, C166 INT, C167 INT, C168 INT, 
C169 INT, C170 INT, C171 INT, C172 INT, C173 INT, C174 INT, 
C175 INT, C176 INT, C177 INT, C178 INT, C179 INT, C180 INT, 
C181 INT, C182 INT, C183 INT, C184 INT, C185 INT, C186 INT, 
C187 INT, C188 INT, C189 INT, C190 INT, C191 INT, C192 INT, 
C193 INT, C194 INT, C195 INT, C196 INT, C197 INT, C198 INT, 
C199 INT, C200 INT, C201 INT, C202 INT, C203 INT, C204 INT, 
C205 INT, C206 INT, C207 INT, C208 INT, C209 INT, C210 INT, 
C211 INT, C212 INT, C213 INT, C214 INT, C215 INT, C216 INT, 
C217 INT, C218 INT, C219 INT, C220 INT, C221 INT, C222 INT, 
C223 INT, C224 INT, C225 INT, C226 INT, C227 INT, C228 INT, 
C229 INT, C230 INT, C231 INT, C232 INT, C233 INT, C234 INT, 
C235 INT, C236 INT, C237 INT, C238 INT, C239 INT, C240 INT, 
C241 INT, C242 INT, C243 INT, C244 INT, C245 INT, C246 INT, 
C247 INT, C248 INT, C249 INT, C250 INT);
-- PASS:0891 If table created successfully?

   COMMIT WORK;

   INSERT INTO L1 VALUES (
1, 2, 3, 4, 5, 6, 
7, 8, 9, 10, 11, 12, 
13, 14, 15, 16, 17, 18, 
19, 20, 21, 22, 23, 24, 
25, 26, 27, 28, 29, 30, 
31, 32, 33, 34, 35, 36, 
37, 38, 39, 40, 41, 42, 
43, 44, 45, 46, 47, 48, 
49, 50, 51, 52, 53, 54, 
55, 56, 57, 58, 59, 60, 
61, 62, 63, 64, 65, 66, 
67, 68, 69, 70, 71, 72, 
73, 74, 75, 76, 77, 78, 
79, 80, 81, 82, 83, 84, 
85, 86, 87, 88, 89, 90, 
91, 92, 93, 94, 95, 96, 
97, 98, 99, 100, 101, 102, 
103, 104, 105, 106, 107, 108, 
109, 110, 111, 112, 113, 114, 
115, 116, 117, 118, 119, 120, 
121, 122, 123, 124, 125, 126, 
127, 128, 129, 130, 131, 132, 
133, 134, 135, 136, 137, 138, 
139, 140, 141, 142, 143, 144, 
145, 146, 147, 148, 149, 150, 
151, 152, 153, 154, 155, 156, 
157, 158, 159, 160, 161, 162, 
163, 164, 165, 166, 167, 168, 
169, 170, 171, 172, 173, 174, 
175, 176, 177, 178, 179, 180, 
181, 182, 183, 184, 185, 186, 
187, 188, 189, 190, 191, 192, 
193, 194, 195, 196, 197, 198, 
199, 200, 201, 202, 203, 204, 
205, 206, 207, 208, 209, 210, 
211, 212, 213, 214, 215, 216, 
217, 218, 219, 220, 221, 222, 
223, 224, 225, 226, 227, 228, 
229, 230, 231, 232, 233, 234, 
235, 236, 237, 238, 239, 240, 
241, 242, 243, 244, 245, 246, 
247, 248, 249, 250);
-- PASS:0891 If 1 row inserted successfully?

   UPDATE L1 SET
C1 = C1 + 1, C2 = C2 + 1, C3 = C3 + 1, 
C4 = C4 + 1, C5 = C5 + 1, C6 = C6 + 1, 
C7 = C7 + 1, C8 = C8 + 1, C9 = C9 + 1, 
C10 = C10 + 1, C11 = C11 + 1, C12 = C12 + 1, 
C13 = C13 + 1, C14 = C14 + 1, C15 = C15 + 1, 
C16 = C16 + 1, C17 = C17 + 1, C18 = C18 + 1, 
C19 = C19 + 1, C20 = C20 + 1, C21 = C21 + 1, 
C22 = C22 + 1, C23 = C23 + 1, C24 = C24 + 1, 
C25 = C25 + 1, C26 = C26 + 1, C27 = C27 + 1, 
C28 = C28 + 1, C29 = C29 + 1, C30 = C30 + 1, 
C31 = C31 + 1, C32 = C32 + 1, C33 = C33 + 1, 
C34 = C34 + 1, C35 = C35 + 1, C36 = C36 + 1, 
C37 = C37 + 1, C38 = C38 + 1, C39 = C39 + 1, 
C40 = C40 + 1, C41 = C41 + 1, C42 = C42 + 1, 
C43 = C43 + 1, C44 = C44 + 1, C45 = C45 + 1, 
C46 = C46 + 1, C47 = C47 + 1, C48 = C48 + 1, 
C49 = C49 + 1, C50 = C50 + 1, C51 = C51 + 1, 
C52 = C52 + 1, C53 = C53 + 1, C54 = C54 + 1, 
C55 = C55 + 1, C56 = C56 + 1, C57 = C57 + 1, 
C58 = C58 + 1, C59 = C59 + 1, C60 = C60 + 1, 
C61 = C61 + 1, C62 = C62 + 1, C63 = C63 + 1, 
C64 = C64 + 1, C65 = C65 + 1, C66 = C66 + 1, 
C67 = C67 + 1, C68 = C68 + 1, C69 = C69 + 1, 
C70 = C70 + 1, C71 = C71 + 1, C72 = C72 + 1, 
C73 = C73 + 1, C74 = C74 + 1, C75 = C75 + 1, 
C76 = C76 + 1, C77 = C77 + 1, C78 = C78 + 1, 
C79 = C79 + 1, C80 = C80 + 1, C81 = C81 + 1, 
C82 = C82 + 1, C83 = C83 + 1, C84 = C84 + 1, 
C85 = C85 + 1, C86 = C86 + 1, C87 = C87 + 1, 
C88 = C88 + 1, C89 = C89 + 1, C90 = C90 + 1, 
C91 = C91 + 1, C92 = C92 + 1, C93 = C93 + 1, 
C94 = C94 + 1, C95 = C95 + 1, C96 = C96 + 1, 
C97 = C97 + 1, C98 = C98 + 1, C99 = C99 + 1, 
C100 = C100 + 1, C101 = C101 + 1, C102 = C102 + 1, 
C103 = C103 + 1, C104 = C104 + 1, C105 = C105 + 1, 
C106 = C106 + 1, C107 = C107 + 1, C108 = C108 + 1, 
C109 = C109 + 1, C110 = C110 + 1, C111 = C111 +1, 
C112 = C112 +1, C113 = C113 +1, C114 = C114 +1, 
C115 = C115 +1, C116 = C116 +1, C117 = C117 +1, 
C118 = C118 +1, C119 = C119 +1, C120 = C120 +1, 
C121 = C121 +1, C122 = C122 +1, C123 = C123 +1, 
C124 = C124 +1, C125 = C125 +1, C126 = C126 +1, 
C127 = C127 +1, C128 = C128 +1, C129 = C129 +1, 
C130 = C130 +1, C131 = C131 +1, C132 = C132 +1, 
C133 = C133 +1, C134 = C134 +1, C135 = C135 +1, 
C136 = C136 +1, C137 = C137 +1, C138 = C138 +1, 
C139 = C139 +1, C140 = C140 +1, C141 = C141 +1, 
C142 = C142 +1, C143 = C143 +1, C144 = C144 +1, 
C145 = C145 +1, C146 = C146 +1, C147 = C147 +1, 
C148 = C148 +1, C149 = C149 +1, C150 = C150 +1, 
C151 = C151 +1, C152 = C152 +1, C153 = C153 +1, 
C154 = C154 +1, C155 = C155 +1, C156 = C156 +1, 
C157 = C157 +1, C158 = C158 +1, C159 = C159 +1, 
C160 = C160 +1, C161 = C161 +1, C162 = C162 +1, 
C163 = C163 +1, C164 = C164 +1, C165 = C165 +1, 
C166 = C166 +1, C167 = C167 +1, C168 = C168 +1, 
C169 = C169 +1, C170 = C170 +1, C171 = C171 +1, 
C172 = C172 +1, C173 = C173 +1, C174 = C174 +1, 
C175 = C175 +1, C176 = C176 +1, C177 = C177 +1, 
C178 = C178 +1, C179 = C179 +1, C180 = C180 +1, 
C181 = C181 +1, C182 = C182 +1, C183 = C183 +1, 
C184 = C184 +1, C185 = C185 +1, C186 = C186 +1, 
C187 = C187 +1, C188 = C188 +1, C189 = C189 +1, 
C190 = C190 +1, C191 = C191 +1, C192 = C192 +1, 
C193 = C193 +1, C194 = C194 +1, C195 = C195 +1, 
C196 = C196 +1, C197 = C197 +1, C198 = C198 +1, 
C199 = C199 +1, C200 = C200 +1, C201 = C201 +1, 
C202 = C202 +1, C203 = C203 +1, C204 = C204 +1, 
C205 = C205 +1, C206 = C206 +1, C207 = C207 +1, 
C208 = C208 +1, C209 = C209 +1, C210 = C210 +1, 
C211 = C211 +1, C212 = C212 +1, C213 = C213 +1, 
C214 = C214 +1, C215 = C215 +1, C216 = C216 +1, 
C217 = C217 +1, C218 = C218 +1, C219 = C219 +1, 
C220 = C220 +1, C221 = C221 +1, C222 = C222 +1, 
C223 = C223 +1, C224 = C224 +1, C225 = C225 +1, 
C226 = C226 +1, C227 = C227 +1, C228 = C228 +1, 
C229 = C229 +1, C230 = C230 +1, C231 = C231 +1, 
C232 = C232 +1, C233 = C233 +1, C234 = C234 +1, 
C235 = C235 +1, C236 = C236 +1, C237 = C237 +1, 
C238 = C238 +1, C239 = C239 +1, C240 = C240 +1, 
C241 = C241 +1, C242 = C242 +1, C243 = C243 +1, 
C244 = C244 +1, C245 = C245 +1, C246 = C246 +1, 
C247 = C247 +1, C248 = C248 +1, C249 = C249 +1, 
C250 = C250 +1;
-- PASS:0891 If update completed successfully?

-- modified select to avoid a problem with sed in processing test result
   SELECT
C1, C2, C3, C4, C5, C6, 
C7, C8, C9, C10, C11, C12, 
C13, C14, C15, C16, C17, C18, 
C19, C20, C21, C22, C23, C24, 
C25, C26, C27, C28, C29, C30, 
C31, C32, C33, C34, C35, C36, 
C37, C38, C39, C40, C41, C42, 
C43, C44, C45, C46, C47, C48, 
C49, C50, C51, C52, C53, C54, 
C55, C56, C57, C58, C59, C60, 
C61, C62, C63, C64, C65, C66, 
C67, C68, C69, C70, C71, C72, 
C73, C74, C75, C76, C77, C78, 
C79, C80, C81, C82, C83, C84, 
C85, C86, C87, C88, C89, C90, 
C91, C92, C93, C94, C95, C96, 
C97, C98, C99, C100, C101, C102, 
C103, C104, C105, C106, C107, C108, 
C109, C110, C111, C112, C113, C114, 
C115, C116, C117, C118, C119, C120, 
--O C121, C122, C123, C124, C125, C126, 
 C121, C122, C123, C124, C125, C126
from l1;
   SELECT
C127, C128, C129, C130, C131, C132, 
C133, C134, C135, C136, C137, C138, 
C139, C140, C141, C142, C143, C144, 
C145, C146, C147, C148, C149, C150, 
C151, C152, C153, C154, C155, C156, 
C157, C158, C159, C160, C161, C162, 
C163, C164, C165, C166, C167, C168, 
C169, C170, C171, C172, C173, C174, 
C175, C176, C177, C178, C179, C180, 
C181, C182, C183, C184, C185, C186, 
C187, C188, C189, C190, C191, C192, 
C193, C194, C195, C196, C197, C198, 
C199, C200, C201, C202, C203, C204, 
C205, C206, C207, C208, C209, C210, 
C211, C212, C213, C214, C215, C216, 
C217, C218, C219, C220, C221, C222, 
C223, C224, C225, C226, C227, C228, 
C229, C230, C231, C232, C233, C234, 
C235, C236, C237, C238, C239, C240, 
C241, C242, C243, C244, C245, C246, 
C247, C248, C249, C250
FROM L1;
-- PASS:0891 If 250 values are returned with values from 2 thru 251?

   COMMIT WORK;

--O   DROP TABLE L1 CASCADE;
   DROP TABLE L1 ;
-- PASS:0891 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0891 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
