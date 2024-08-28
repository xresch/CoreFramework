(function (){
	
	CFW.dashboard.registerWidget(
			"emp_postgresquerychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_postgresquerychart', "Postgres Query Chart")) 
		);
	
})();