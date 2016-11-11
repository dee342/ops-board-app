'use strict';

angular
  .module('OpsBoard')
  .service('MaterialService', ['$log', 'ReferenceDataService', function ($log, ReferenceDataService) {

    var materialList;

    var _getMaterials = function() {
      if (!materialList) {
      return ReferenceDataService.loadMaterialList().then(function (data) {
        materialList = data;
        return data;
      });
      } else {
      return materialList;
      }
    };

    var _getMaterialsObject = function() {
      var materialsObject = {};
      if (!materialList) {
        return ReferenceDataService.loadMaterialList().then(function (data) {
          materialList = data;
        });
      }

      for (var i = 0; i < materialList.length; i++) {
        materialsObject[materialList[i].materialType] = materialList[i];
      }

      return materialsObject;

    };

    return {
      getMaterials: _getMaterials,
      getMaterialsObject: _getMaterialsObject
    }
  }
  ])

