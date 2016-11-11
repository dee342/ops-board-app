'use strict';

angular.module('OpsBoard')
  .service(
    'ReportsService',
    function() {
    	return{
    		saveVolunteerCountsOnServer: function ($resource, boardData, chartVolunteers, mandatoryChart, VacationVolunteers, successFn, errorFn ){
    			var resource = $resource(boardData.pathStart
    		            + '/SaveVolunteerCounts/:district/:date/', {
					district : boardData.boardLocation,
					date : boardData.boardDate}, {
				          save : {
				              method : 'POST',
				              headers : {
				            	  'Content-Type' : 'application/json'
				              }
				          }
					}				
				);
    			
    	        var group = {
    	                "chartVolunteers": chartVolunteers,
    	                "mandatoryChart": mandatoryChart,
    	                "vacationVolunteers": VacationVolunteers
    	              };
    	        
    	        
    	        var response = resource.save(JSON.stringify(group), function(data) {
    	            console.log('success, got data from getVolunteerCountsOnServer: ', data);
    	            successFn(data);
    	          }, function(error) {
    	            errorFn(error);
    	          });
    		},
    	
    	
    	
    	saveVolunteerCountsCommand: function (volunteerCounts, command) {
    		
    		volunteerCounts.chartVolunteers = command.commandContent.chartVolunteers;
    		volunteerCounts.mandatoryChart = command.commandContent.mandatoryChart;
    		volunteerCounts.vacationVolunteers = command.commandContent.vacationVolunteers;
    		
    		return;
    	}
    }});
    


 










