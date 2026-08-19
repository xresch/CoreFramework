 (
	( 
		FK_ID_SPACE = ? -- is selected selectedSpace
		OR 0 = ? -- selectedSpace is "All" SPACE
		OR ( 
			SELECT DISTINCT TRUE  
			FROM CFW_SPACES CS 
			WHERE 
			   -- Everything contained in parent spaces of selected space
				  ( CS.PK_ID = ? -- selectedSpace
			        AND ARRAY_CONTAINS(CS.H_LINEAGE, FK_ID_SPACE) -- SPACE IS IN LINEAGE
			      ) 
			   -- Everything directly contained in Global Root Spaces 
			   OR ( CS.PK_ID = FK_ID_SPACE -- selectedSpace
			        AND CS."TYPE" = 'ROOT_SPACE' 
			        AND CS.IS_GLOBAL IS TRUE 
			      )
		)
		-- Everything Contained in Global Spaces which are in the same Parent Space
		OR ( 
			SELECT DISTINCT TRUE  
			FROM CFW_SPACES CS 
			JOIN CFW_SPACES ZZ -- SELF JOIN
			  ON ZZ.PK_ID = ?
			WHERE CS.PK_ID = FK_ID_SPACE -- selectedSpace
			  AND CS.H_ROOT = ZZ.H_ROOT
			  AND CS.H_DEPTH <= ZZ.H_DEPTH
			  AND ZZ.PK_ID != FK_ID_SPACE
			 -- AND CS."TYPE" = ZZ."TYPE"
			  AND CFW_ARRAY_CONTAINS_ALL_INT(ZZ.H_LINEAGE, CS.H_LINEAGE)
			  AND CS.IS_GLOBAL IS TRUE       
		)
	) 
	-- only from spaces that are enabled
	AND ( 
		SELECT CS.IS_ENABLED 
		FROM CFW_SPACES CS 
		WHERE CS.PK_ID = FK_ID_SPACE 
	)
) 