'use strict';

angular
  .module('OpsBoard')
  .service('ShiftDataService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var shiftData;

    var _getShiftData = function() {
      if (!shiftData) {
        return ReferenceDataService.loadShifts().then(function(data){
          shiftData = data;
          return data;
        });
      } else {
        return shiftData;
      }
    }


    return {
      getShiftData: _getShiftData
    }
  }
])