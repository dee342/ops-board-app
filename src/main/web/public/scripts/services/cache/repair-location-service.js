'use strict';

angular
  .module('OpsBoard')
  .service('RepairLocationsService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var repairLocations, locationsByCode;

    var _getRepairLocations = function () {
      if (!repairLocations) {
        return ReferenceDataService.loadRepairLocations().then(function (data) {
          repairLocations = data;
          locationsByCode = _.object(_.map(repairLocations, function(item) {
            return [item.code, item]
          }));
          return data;
        });
      } else {
        return repairLocations;
      }
    }


    return {
      getRepairLocations: _getRepairLocations
    }
  }
])




        