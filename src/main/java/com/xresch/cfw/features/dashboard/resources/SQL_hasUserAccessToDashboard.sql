SELECT COUNT(*) AS COUNT
FROM
	CFW_DASHBOARD
WHERE
	PK_ID = ?
	AND (
		FK_ID_USER = ?
		OR (
		  IS_SHARED = TRUE
		  AND ( JSON_SHARE_WITH_USERS IS NULL
		    OR JSON_SHARE_WITH_USERS = '{}'
		  )
		  AND ( JSON_SHARE_WITH_ROLES IS NULL
		    OR JSON_SHARE_WITH_ROLES = '{}'
		  )
		)
		OR (IS_SHARED = TRUE
		  AND JSON_SHARE_WITH_USERS LIKE ? 
		)
		OR JSON_EDITORS LIKE ?
	);