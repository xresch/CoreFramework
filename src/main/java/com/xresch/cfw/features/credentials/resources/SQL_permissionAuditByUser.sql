SELECT * FROM (
SELECT 
	NAME,
	(FK_ID_USER = ?) AS IS_OWNER, 
	(
	  IS_SHARED = TRUE
	  AND ( JSON_SHARE_WITH_USERS IS NULL
	    OR JSON_SHARE_WITH_USERS = '{}'
	  )
	  AND ( JSON_SHARE_WITH_GROUPS IS NULL
	    OR JSON_SHARE_WITH_GROUPS = '{}'
	  )
	) AS SHARED_GLOBAL,
	(
		IS_SHARED = TRUE
		AND JSON_SHARE_WITH_USERS LIKE ?
	) AS SHARED_WITH_USER,
	(JSON_EDITORS LIKE ? ) AS IS_EDITOR

 FROM
	CFW_CREDENTIALS 
)	
 WHERE
      IS_OWNER = TRUE
   OR SHARED_GLOBAL = TRUE
   OR SHARED_WITH_USER = TRUE
   OR IS_EDITOR = TRUE