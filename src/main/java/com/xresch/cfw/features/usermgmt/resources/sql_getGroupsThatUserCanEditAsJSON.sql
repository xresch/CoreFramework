SELECT DISTINCT
	T.*
	, (SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_GROUPOWNER) AS OWNER
FROM CFW_ROLE T
LEFT JOIN CFW_ROLE_EDITORS_MAP M ON T.PK_ID = M.FK_ID_ROLE 
WHERE T.IS_GROUP = TRUE
	AND (
		T.FK_ID_GROUPOWNER = ?
		OR M.FK_ID_USER = ?
	)