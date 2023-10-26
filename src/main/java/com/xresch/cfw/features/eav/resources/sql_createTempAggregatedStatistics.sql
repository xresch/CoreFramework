INSERT INTO CFW_EAV_STATS_AGGREGATION
SELECT 
	  NEW_TIME AS "TIME"
	, (SELECT MAX(CD.PK_ID) 
			FILTER(WHERE CD.DATE <= T."NEW_TIME") 
			FROM CFW_DATE CD
		) AS NEW_DATE
	, FK_ID_ENTITY
	, FK_ID_VALUES
	, "COUNT"
	, "MIN"
	, "AVG"
	, "MAX"
	, "SUM"
	, "VAL"
	, ? /* GRANULARITY*/
FROM (
	SELECT (
			DATEADD(
				SECOND,
				DATEDIFF(SECOND, MIN(TIME), MAX(TIME)) / 2,
				MIN(TIME)
			)) AS NEW_TIME,
	FK_ID_ENTITY
	, FK_ID_VALUES
	, SUM(COUNT) AS "COUNT"
	, MIN(MIN) AS "MIN"
	, AVG(AVG) AS "AVG"
	, MAX(MAX) AS "MAX"
	, SUM(SUM) AS "SUM"
	, AVG(VAL) AS "VAL"
	FROM CFW_EAV_STATS
	WHERE TIME >= ? /* DATEADD(MS, 1686550400000, DATE '1970-01-01') */
	AND   TIME < ? /*DATEADD(MS, 1696550400000, DATE '1970-01-01') */
	AND "GRANULARITY" < ?
	GROUP BY FK_ID_ENTITY, FK_ID_VALUES
) AS T