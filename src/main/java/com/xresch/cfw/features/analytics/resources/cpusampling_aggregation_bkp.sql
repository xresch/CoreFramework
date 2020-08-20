CREATE TEMP TABLE IF NOT EXISTS CFW_STATS_CPUSAMPLE_AGGREGATION (TIME TIMESTAMP, FK_ID_SIGNATURE INT, FK_ID_PARENT INT , COUNT INT, MIN INT, AVG INT, MAX INT, GRANULARITY INT);

SET @startTime = ?;
SET @endTime = ?;
SET @newGranularity = ?;

INSERT INTO CFW_STATS_CPUSAMPLE_AGGREGATION
SELECT (
		DATEADD(
			SECOND, 
			DATEDIFF(SECOND, MIN(TIME), MAX(TIME)) / 2,
			MIN(TIME)
		)) AS NewTime, 
FK_ID_SIGNATURE, FK_ID_PARENT, SUM(COUNT), MIN(MIN), AVG(AVG), MAX(MAX),  @newGranularity
FROM CFW_STATS_CPUSAMPLE
WHERE TIME > @startTime
AND TIME < @endTime
AND GRANULARITY <  @newGranularity
GROUP BY FK_ID_SIGNATURE, FK_ID_PARENT;

DELETE FROM CFW_STATS_CPUSAMPLE
WHERE TIME > @startTime
AND TIME < @endTime
AND GRANULARITY < @newGranularity;

INSERT INTO CFW_STATS_CPUSAMPLE (TIME, FK_ID_SIGNATURE, FK_ID_PARENT,  COUNT, MIN, AVG, MAX, GRANULARITY)
SELECT * FROM CFW_STATS_CPUSAMPLE_AGGREGATION;

DELETE FROM CFW_STATS_CPUSAMPLE_AGGREGATION WHERE 1=1;