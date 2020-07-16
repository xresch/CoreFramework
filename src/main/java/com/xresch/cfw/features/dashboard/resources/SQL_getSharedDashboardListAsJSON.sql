SELECT PK_ID, NAME, DESCRIPTION , (SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER) AS OWNER
FROM
	CFW_DASHBOARD
WHERE
	(IS_SHARED = TRUE
	  AND ( JSON_SHARE_WITH_USERS IS NULL
	    OR JSON_SHARE_WITH_USERS = '{}'
	    OR FK_ID_USER = ? 
	  )
	  AND ( JSON_SHARE_WITH_ROLES IS NULL
	    OR JSON_SHARE_WITH_ROLES = '{}'
	    OR FK_ID_USER = ? 
	  )
	)
	OR (IS_SHARED = TRUE
	AND JSON_SHARE_WITH_USERS LIKE ? )
	OR JSON_EDITORS LIKE ? 