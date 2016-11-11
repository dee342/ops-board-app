'use strict';

angular
  .module('OpsBoard')
  .service('SpecialPositionService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var personnelSpecialPositionsList;

    var _getPersonnelSpecialPositionsList = function(){
      if (!personnelSpecialPositionsList) {
        return ReferenceDataService.loadPersonnelSpecialPositionsList().then(function(data){
        	personnelSpecialPositionsList = data;
        	return data;
        });
      } else {
      	return personnelSpecialPositionsList;
      }
    }

    return {
      getPersonnelSpecialPositionsList: _getPersonnelSpecialPositionsList
    }
  }
])