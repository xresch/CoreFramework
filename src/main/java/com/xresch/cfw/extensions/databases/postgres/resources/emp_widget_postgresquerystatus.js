(function (){
	
	CFW.dashboard.registerWidget(
			"emp_postgresquerystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_postgresquerystatus', "Postgres Query Status")) 
		);
	
})();