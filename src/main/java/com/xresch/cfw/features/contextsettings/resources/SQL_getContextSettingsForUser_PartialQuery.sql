SELECT PK_ID, CFW_CTXSETTINGS_NAME, CFW_CTXSETTINGS_DESCRIPTION
FROM
	CFW_CONTEXT_SETTINGS AS T
WHERE CFW_CTXSETTINGS_TYPE = ?
AND
(
	( 
	  (JSON_RESTRICTED_TO_USERS IS NULL
	    OR JSON_RESTRICTED_TO_USERS = '{}'
	  )
	  AND ( JSON_RESTRICTED_TO_GROUPS IS NULL
	    OR JSON_RESTRICTED_TO_GROUPS = '{}'
	  )
	)
	OR JSON_RESTRICTED_TO_USERS LIKE ?
	/* Added Programatically: OR JSON_RESTRICTED_TO_ROLES LIKE '%"2":%' [...]
)*/