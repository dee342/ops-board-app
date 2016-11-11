'use strict';

angular
  .module('OpsBoard')
  .service('DownCodesService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var equipmentDownCodes;

    var _getEquipmentDownCodes = function() {
      if (!equipmentDownCodes) {
        return ReferenceDataService.loadEquipmentDownCodes().then(function(data) {
          equipmentDownCodes = data;
          return data;
        });
      } else {
        return equipmentDownCodes;
      }
    }

    return {
      getEquipmentDownCodes: _getEquipmentDownCodes
    }
  }
])