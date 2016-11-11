'use strict';

angular
  .module('OpsBoard')
  .service('PersonnelStatusService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var personnelStatusList;

    var _getPersonnelStatusList = function() {      
      if (!personnelStatusList) {
        return ReferenceDataService.loadPersonnelStatusList().then(function (data) {
          personnelStatusList = data;
          return data;
        });
      } else {
        return personnelStatusList;
      }
  	}

    return {
      getPersonnelStatusList: _getPersonnelStatusList
    }
  }
])
