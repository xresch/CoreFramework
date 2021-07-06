SELECT DISTINCT * FROM (
	SELECT C.CFW_CTXSETTINGS_TYPE AS TYPE, C.CFW_CTXSETTINGS_NAME AS NAME,
		( 
		  (JSON_RESTRICTED_TO_USERS IS NULL
		    OR JSON_RESTRICTED_TO_USERS = '{}'
		  )
		  AND ( JSON_RESTRICTED_TO_GROUPS IS NULL
		    OR JSON_RESTRICTED_TO_GROUPS = '{}'
		  )
		) AS UNRESTRICTED,
		(JSON_RESTRICTED_TO_USERS LIKE '%354%') AS RESTRICTED_TO_USER,
		(
			0 < (
				SELECT COUNT(*) FROM CFW_CONTEXT_SETTINGS X
				WHERE C.JSON_RESTRICTED_TO_GROUPS LIKE CONCAT('%', G.PK_ID, '%')
			)
		) AS RESTRICTRED_TO_GROUP,
		( CASE WHEN (
				SELECT COUNT(*) FROM CFW_CONTEXT_SETTINGS X
				WHERE C.JSON_RESTRICTED_TO_GROUPS LIKE CONCAT('%', G.PK_ID, '%')
			) 
			THEN 
				G.NAME 
			ELSE 
				'' 
			END
		) AS GROUPNAME
	FROM CFW_CONTEXT_SETTINGS AS C, CFW_USER AS U
	LEFT JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_USER = U.PK_ID 
	LEFT JOIN CFW_ROLE AS G ON UG.FK_ID_ROLE = G.PK_ID 
	--LEFT JOIN CFW_DASHBOARD AS D ON 1 = 1
	WHERE U.PK_ID = ?
) WHERE 
     UNRESTRICTED = TRUE
  OR RESTRICTED_TO_USER = TRUE
  OR RESTRICTRED_TO_GROUP = TRUE