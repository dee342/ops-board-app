'use strict';

angular
  .module('OpsBoard')
  .service('UnavailabilityCodeService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var unavailabilityCodeList;

    var _getPersonnelUnavailabilityCodes = function(){
      if (!unavailabilityCodeList) {
        return ReferenceDataService.loadPersonnelUnavailabilityCodes().then(function(data){
        	unavailabilityCodeList = data;
        	return data;
        });
      } else {
      	return unavailabilityCodeList;
      }
    }

    return {
      getPersonnelUnavailabilityCodes: _getPersonnelUnavailabilityCodes
    }
  }
])