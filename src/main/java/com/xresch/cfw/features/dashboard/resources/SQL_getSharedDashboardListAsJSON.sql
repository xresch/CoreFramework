SELECT T.PK_ID
	, T.NAME
	, T.DESCRIPTION
	, T.TAGS
	, T.IS_PUBLIC
	, T.ALLOW_EDIT_SETTINGS
	, (SELECT USERNAME FROM CFW_USER U WHERE U.PK_ID = T.FK_ID_USER) AS OWNER
	, (SELECT COUNT(*) FROM CFW_DASHBOARD_FAVORITE_MAP M WHERE M.FK_ID_USER = ? AND M.FK_ID_DASHBOARD = T.PK_ID) > 0 AS IS_FAVED
FROM
	CFW_DASHBOARD T
WHERE
	T.VERSION = 0
	AND T.IS_ARCHIVED = 0
	AND
	(IS_SHARED = TRUE
	  AND ( JSON_SHARE_WITH_USERS IS NULL
	    OR JSON_SHARE_WITH_USERS = '{}'
	    OR FK_ID_USER = ? 
	  )
	  AND ( JSON_SHARE_WITH_GROUPS IS NULL
	    OR JSON_SHARE_WITH_GROUPS = '{}'
	    OR FK_ID_USER = ? 
	  )
	)
	OR (IS_SHARED = TRUE
	    AND JSON_SHARE_WITH_USERS LIKE ? )
	OR JSON_EDITORS LIKE ?  