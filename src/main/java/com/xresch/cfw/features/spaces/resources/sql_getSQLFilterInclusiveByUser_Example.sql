/******************************************************
 * Example Query.
 * Filters all the Credentials that a user potentially 
 * has access too.
 * Then filters additionally based on all the spaces
 * a user can access.
 ******************************************************/	
SELECT * FROM (
SELECT 
	NAME,
	FK_ID_SPACE,
	(FK_ID_OWNER = 33) AS IS_OWNER, 
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
		AND JSON_SHARE_WITH_USERS LIKE 33
	) AS SHARED_WITH_USER,
	(JSON_EDITORS LIKE 33 ) AS IS_EDITOR
 FROM
	CFW_CREDENTIALS 
)	
 WHERE (
      IS_OWNER = TRUE
   OR SHARED_GLOBAL = TRUE
   OR SHARED_WITH_USER = TRUE
   OR IS_EDITOR = TRUE )
AND( 
	WITH USER_SPACES AS (
		WITH US AS (
			SELECT O.*
			FROM CFW_SPACES O
			-- Assigned User
			LEFT JOIN CFW_SPACES_USER_MAP U 
				ON U.FK_ID_SPACE = O.PK_ID 
			-- Admin User
			LEFT JOIN CFW_SPACES_EDITOR_MAP A 
				ON A.FK_ID_SPACE = O.PK_ID 
			-- By User Groups
			LEFT JOIN (
				SELECT FK_ID_SPACE, URM.FK_ID_USER
				FROM CFW_SPACES_USERGROUPS_MAP UGM 
				JOIN CFW_USER_ROLE_MAP URM
				  ON URM.FK_ID_ROLE = UGM.FK_ID_ROLE
			) UG 
				ON UG.FK_ID_SPACE = O.PK_ID 
			-- By Admin Groups
			LEFT JOIN (
				SELECT FK_ID_SPACE, URM.FK_ID_USER
				FROM CFW_SPACES_EDITORGROUPS_MAP AGM 
				JOIN CFW_USER_ROLE_MAP URM
				  ON URM.FK_ID_ROLE = AGM.FK_ID_ROLE
			) AG 
				ON AG.FK_ID_SPACE = O.PK_ID 
			WHERE O.IS_ENABLED IS TRUE
			  AND ( U.FK_ID_USER = 33
			     OR A.FK_ID_USER = 33
			     OR UG.FK_ID_USER = 33
			     OR AG.FK_ID_USER = 33
			  ) 
			ORDER BY LOWER(O.NAME)
		)
		-- Every space the user has directly access too
		SELECT *
			FROM US
		-- Everything Contained in Global Spaces which are in the same Parent Space
		UNION
			SELECT CS.*
				FROM US
			JOIN CFW_SPACES ZZ
			    ON ZZ.PK_ID = US.PK_ID
			JOIN CFW_SPACES CS
			    ON CS.H_ROOT = ZZ.H_ROOT
			   AND CS.H_DEPTH <= ZZ.H_DEPTH
			   AND CFW_ARRAY_CONTAINS_ALL_INT(
			           ZZ.H_LINEAGE,
			           CS.H_LINEAGE
			       )
			   AND CS.IS_GLOBAL IS TRUE
			   AND CS.IS_ENABLED IS TRUE
		-- All enabled parent spaces
		UNION	   
			SELECT CS.*
				FROM US
			JOIN CFW_SPACES CS
			    ON ARRAY_CONTAINS(US.H_LINEAGE, CS.PK_ID)
			   AND CS.IS_ENABLED IS TRUE
		-- All enabled global root spaces
		UNION
			SELECT CS.*
				FROM CFW_SPACES CS
				WHERE CS."TYPE" = 'ROOT_SPACE'
				  AND CS.IS_GLOBAL IS TRUE
				  AND CS.IS_ENABLED IS TRUE
	)
	SELECT DISTINCT TRUE
	FROM USER_SPACES
	WHERE FK_ID_SPACE = USER_SPACES.PK_ID
)