SELECT  P.NAME AS PERMISSION, G.NAME AS ROLE_OR_GROUP, FROM CFW_USER U
LEFT JOIN CFW_USER_ROLE_MAP AS UG ON UG.FK_ID_USER = U.PK_ID 
LEFT JOIN CFW_ROLE AS G ON UG.FK_ID_ROLE = G.PK_ID 
LEFT JOIN CFW_ROLE_PERMISSION_MAP AS GP ON GP.FK_ID_ROLE = G.PK_ID 
LEFT JOIN CFW_PERMISSION AS P ON GP.FK_ID_PERMISSION = P.PK_ID 
WHERE U.PK_ID = ?
  AND P.NAME NOT IS NULL
ORDER BY LOWER(P.NAME), LOWER(G.NAME)