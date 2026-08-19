SELECT NAME, FK_ID_SPACE 
FROM CFW_CREDENTIALS
WHERE (
	( 
		FK_ID_SPACE = 385 -- is selected selectedSpace
		OR 0 = 1 -- selectedSpace is "All" SPACE
		OR ( 
			SELECT DISTINCT TRUE  
			FROM CFW_SPACES CS 
			JOIN CFW_SPACES ZZ -- SELF Join
			WHERE 
			   -- Everything contained in parent spaces of selected space
				  ( CS.PK_ID = 385 -- selectedSpace
			        AND ARRAY_CONTAINS(CS.H_LINEAGE, FK_ID_SPACE) -- SPACE IS IN LINEAGE
			      ) 
			   -- Everything directly contained in Global Root Spaces 
			   OR ( CS.PK_ID = FK_ID_SPACE -- selectedSpace
			        AND CS."TYPE" = 'ROOT_SPACE' 
			        AND CS.IS_GLOBAL IS TRUE 
			      )
			   -- Everything Contained in Global Spaces which are in the same Root Space
			   OR ( CS.PK_ID = FK_ID_SPACE -- selectedSpace
			        AND CS."TYPE" = 'SPACE' 
			        AND CS.H_ROOT = ZZ.H_ROOT
			        AND CS.IS_GLOBAL IS TRUE 
			      )
		)
	) 
	-- only from spaces that are enabled
	AND ( 
		SELECT CS99.IS_ENABLED 
		FROM CFW_SPACES CS99 
		WHERE CS99.PK_ID = FK_ID_SPACE 
	)
);