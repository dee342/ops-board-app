'use strict';

angular.module('OpsBoard')
  .controller('EquipmentCtrl', function ($rootScope, $scope, $modal, $log, OpsBoardRepository, 
    states, binStatus, materialGroups, materialSubGroups, groups, plowDirections, EquipmentHelperService,
    BoardDataService, $filter, FilterRunner, BoardValueService) {


    angular.forEach($scope.equipment, function(value) {
      value = EquipmentHelperService.addEquipmentProperties(value);
    });

    $scope.boardQuota = OpsBoardRepository.getBoardQuota();
    $scope.groups = groups;

    $scope.equipmentLocations = $filter('sortLocationsBySequence')(OpsBoardRepository.getBoardEquipmentServiceLocations(), BoardValueService.locationRefsData);
    $scope.equipmentLocation = $scope.equipmentLocations[0];
    BoardValueService.equipmentLocations = $scope.equipmentLocations;
    BoardValueService.equipmentLocation = $scope.equipmentLocation;

    var changeEquipmentLocation = function() {
      $scope.locationEquipment = $filter('locationEquipment')($scope.equipment, $scope.equipmentLocation);
      BoardValueService.equipmentLocation = $scope.equipmentLocation;
    };

    $scope.changeEquipmentLocation = function (loc) {
      $scope.equipmentLocation = loc;
      $scope.locationEquipment = $filter('locationEquipment')($scope.equipment, $scope.equipmentLocation);
      BoardValueService.equipmentLocation = $scope.equipmentLocation;
      updateEquipmentUI();
    };

    $scope.equipmentpane = {}; // holds our filtered data
    $scope.equipment = OpsBoardRepository.getEquipment();

    $scope.locationEquipment = $scope.equipment;

    if($scope.equipmentLocations.length > 1) {
      $scope.locationEquipment = $filter('locationEquipment')($scope.equipment, $scope.equipmentLocation);
    }

    $scope.materials = BoardDataService.getMaterials();

    /* State counts */
    $scope.numDetached = 0;
    $scope.numDown = 0;
    $scope.numPendingLoad=0;
    $scope.numPendingAttach = 0;
    $scope.numPendingDetach = 0;
    $scope.numPendingRearLoaders = 0;
    $scope.numPendingDualBins = 0;
    $scope.numPendingMecBrooms = 0;
    $scope.numPendingRoRos = 0;
    $scope.numPendingEZPacks = 0;
    
    /* "Available" Group counts */
    $scope.available = {
        open: true,
        show: false,
        total: 0
    };
    $scope.alleyTrucks = {
      open: true,
      show: false,
      total: 0
    };
    $scope.dualBins = {
      open: true,
      show: false,
      total: 0
    };
    $scope.ezPacks = {
      open: true,
      show: false,
      total: 0
    };
    $scope.hoistFittedChassis = {
      open: true,
      show: false,
      total: 0
    };
    $scope.mechanicalBrooms = {
      open: true,
      show: false,
      total: 0
    };
    $scope.miscellaneous = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rearLoaders = {
      open: true,
      show: false,
      total: 0
    };
    $scope.roRos = {
      open: true,
      show: false,
      total: 0
    };
    $scope.snow = {
      open: true,
      show: false,
      total: 0
    };

    /* Pending */
    $scope.pending = {
        open: true,
        show: true
    };
    
    $scope.pendingLoad = {
            open: true,
            show: true
        };
    $scope.pendingAttach = {
        open: true,
        show: true
    };
    $scope.pendingDetach = {
        open:   true,
        show:   true
    };

    /* Relay */

    $scope.relay = {
      open: true,
      show: true,
      total: 0
    };
    $scope.relayCollection = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayRecycling = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayMiscellaneous = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relaySnow = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayRecyclingPaper = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayRecyclingMGP = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayRecyclingOrganics = {
      open: true,
      show: false,
      total: 0
    };
    $scope.relayRecyclingMisc = {
      open: true,
      show: false,
      total: 0
    };

    /* Rollover */
    $scope.rollover = {
      open: true,
      show: true,
      total: 0
    };
    $scope.rolloverCollection = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverRecycling = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverMiscellaneous = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverSnow = {
      open: true,
      show: false,
      total: 0
    };    
    $scope.rolloverRecyclingPaper = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverRecyclingMGP = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverRecyclingOrganics = {
      open: true,
      show: false,
      total: 0
    };
    $scope.rolloverRecyclingMisc = {
      open: true,
      show: false,
      total: 0
    };

    /* Detached */
    $scope.detached = {
        open: true,
        show: true,
        total: 0
    };

    /* Down */
    $scope.down = {
        open: true,
        show: true,
        total: 0
    };
    
    $scope.EquipmentSummary = {
        active: false
    };

    $scope.numAvailable = function(equipment) {
      var results = $filter('availableFilter')(equipment, $scope.equipmentLocation);
      return Object.keys(results).length;
    };

    $scope.numRelay = function(equipment) {
      var results = $filter('relayFilter')(equipment, $scope.equipmentLocation);
      return Object.keys(results).length;
    };

    $scope.numRollover = function(equipment) {
      var results = $filter('rolloverFilter')(equipment, $scope.equipmentLocation);
      return Object.keys(results).length;
    };

    var _toggleEquipmentSummary = function() {
      $scope.EquipmentSummary.active = !$scope.EquipmentSummary.active;
      $scope.equipmentSummaryActive = $scope.EquipmentSummary.active;
      BoardValueService.EquipmentSummary = $scope.EquipmentSummary.active;
    };

    $scope.toggleEquipmentSummary = function() {
      _toggleEquipmentSummary();
    };

    $rootScope.$on('SHOW-EQUIPMENT-SUMMARY', function(event, data) {
      _toggleEquipmentSummary();
    });

    $scope.showEquipmentDetails = function(event, equipmentId) {
      if (!equipmentId) {return;}
      if (event.target.className.indexOf("menu-item") > -1) {return;}
      $scope.$emit('SHOW-EQUIPMENT-DETAILS',{equipmentId: equipmentId});
    };


    function isAnyEquipmentOnLocation(equipment) {
        var onLocation = false;
        angular.forEach(equipment, function(value, key) {
          if (!onLocation && states.equipment.onLocation.indexOf(value.states[$scope.equipmentLocation]) >= 0) {
              onLocation = true;;
          }
        });

        return onLocation;
      };    


    $scope.range = function(start, stop, step) {
      if (arguments.length <= 1) {
        stop = start || 0;
        start = 0;
      }
      step = arguments[2] || 1;

      var length = Math.max(Math.ceil((stop - start) / step), 0);
      var idx = 0;
      var range = Array(length);

      while (idx < length) {
        range[idx++] = start;
        start += step;
      }

      return range;
    }


    /* "Available" group filters */

    function filterByMaterialGroup(list, group, status) {
      var results = {};

      angular.forEach(list, function(equipment, equipKey) {
        var bins = equipment.getFormattedBins($scope.materials)
        angular.forEach(bins, function(bin, binKey) {
          var material;
          if (!bin || !bin.uniqueId) return;
          if (bin.uniqueId) {
            for (var i in $scope.materials) {
              if ($scope.materials[i].uniqueId == bin.uniqueId)
                material = $scope.materials[i];
            }
          }
          if (material && material.group.toUpperCase() === group && bin.status.toUpperCase() === status)
            results[equipKey] = equipment;
        });
      });

      return results;
    };

    function filterByMaterialSubGroup(list, subGroup, status) {
      var results = {};

      angular.forEach(list, function(equipment, equipKey) {
        var bins = equipment.getFormattedBins($scope.materials)
        angular.forEach(bins, function(bin, binKey) {
          var material;
          if (!bin || !bin.uniqueId) return;
          if (bin.uniqueId) {
            for (var i in $scope.materials) {
              if ($scope.materials[i].uniqueId == bin.uniqueId)
                material = $scope.materials[i];
            }
          }
          if (material && material.group.toUpperCase() === materialGroups.equipment.recycling && material.subGroup.toUpperCase() === subGroup && bin.status.toUpperCase() === status)
            results[equipKey] = equipment;
        });
      });

      return results;
    };

    // todo: move event name to constants
    var equipmentFilters = FilterRunner();

    // invokes filters and apply data to scope
    var updateEquipmentUI = function () {
      changeEquipmentLocation();
      $scope.equipmentpane = equipmentFilters.run($scope.locationEquipment);
      $scope.equipmentpane.availableEquipment = $scope.numAvailable($scope.locationEquipment);
      $scope.equipmentpane.relayEquipment = $scope.numRelay($scope.locationEquipment);
      $scope.equipmentpane.rolloverEquipment = $scope.numRollover($scope.locationEquipment);

      return updateEquipmentUI;
    };

    // todo: these should be var instead of attached to scope

    $scope.downFilter = function (equipment, from) {
      var results = {};
      angular.forEach(equipment, function(value, key) {
        if (value.states[$scope.equipmentLocation] === states.equipment.down) {
          value = $filter('appendBinStatus')(value);
          results[key] = value;
        }
      });

      return results;
    };

    equipmentFilters.add({ down: $scope.downFilter });


    $scope.relayDownFilter = function(equipment) {
        var results = {};

        angular.forEach(equipment, function(value, equipKey) {
          var bins = value.getFormattedBins($scope.materials)
          angular.forEach(bins, function(bin, binKey) {
            if (!bin) value = $filter('appendBinStatus')(value);
            if (bin.status.toUpperCase() === binStatus.equipment.relay && value.states[$scope.equipmentLocation] === states.equipment.down && !value.assigned) {
              results[equipKey] = value;
            }
          });
        });

        return results;
    };

    $scope.numRelay = function(equipment) {
        var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
          collection = filterByMaterialGroup(relayResults, materialGroups.equipment.collection, binStatus.equipment.relay),
          miscellaneous = filterByMaterialGroup(relayResults, materialGroups.equipment.miscellaneous, binStatus.equipment.relay),
          snow = filterByMaterialGroup(relayResults, materialGroups.equipment.snow, binStatus.equipment.relay),
          recycling = $scope.numRelayRecycling(equipment),
          cnt = Object.keys(collection).length + Object.keys(miscellaneous).length + Object.keys(snow).length + recycling;

        if (cnt > 0) $scope.relay.show = true;
        return cnt;
      };

    $scope.numRelayRecycling = function(equipment) {
      $scope.relayRecycling.show = false;
      var results = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(results, materialGroups.equipment.recycling, binStatus.equipment.relay),
        cnt = 0;
      for (var i in materialSubGroups.equipment) {
        var obj = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment[i], binStatus.equipment.relay);
        cnt += Object.keys(obj)
          .length;
      }
      if (cnt > 0) $scope.relayRecycling.show = true;
      if(cnt == 0){
        var downEquipment = $scope.downFilter(equipment); // Returns all down equipment
        var relayRecyclingInDown = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay); // Returns equipment of type relay recycling that are down.
        if(Object.keys(relayRecyclingInDown).length > 0)
          $scope.relayRecycling.show = true;
      }
      return cnt;
    };

    equipmentFilters.add({ numRelayRecycling: $scope.numRelayRecycling });

    $scope.numRollover = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        collection = filterByMaterialGroup(rolloverResults, materialGroups.equipment.collection, binStatus.equipment.rollover),
        miscellaneous = filterByMaterialGroup(rolloverResults, materialGroups.equipment.miscellaneous, binStatus.equipment.rollover),
        snow = filterByMaterialGroup(rolloverResults, materialGroups.equipment.snow, binStatus.equipment.rollover),
        recycling = $scope.numRolloverRecycling(equipment),
        cnt = Object.keys(collection)
        .length + Object.keys(miscellaneous)
        .length + Object.keys(snow)
        .length + recycling;

      if (cnt > 0) $scope.rollover.show = true;
      return cnt;
    }

    $scope.numRolloverRecycling = function(equipment) {
      $scope.rolloverRecycling.show = false;
      var results = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(results, materialGroups.equipment.recycling, binStatus.equipment.rollover),
        cnt = 0;
      for (var i in materialSubGroups.equipment) {
        var obj = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment[i], binStatus.equipment.rollover);
        cnt += Object.keys(obj)
          .length;
      }
      if (cnt > 0) $scope.rolloverRecycling.show = true;
      if(cnt == 0){
        var downEquipment = $scope.downFilter(equipment); // Returns all down equipment
        var rolloverRecyclingInDown = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay); // Returns equipment of type rollover recycling that are down.
        if(Object.keys(rolloverRecyclingInDown).length > 0)
          $scope.rolloverRecycling.show = true;
      }
      return cnt;
    }

    equipmentFilters.add({ numRolloverRecycling: $scope.numRolloverRecycling });

    $scope.detachedFilter = function(equipment) {
      var results = {};
      angular.forEach(equipment, function(value, key) {

        if (value.states[$scope.equipmentLocation] === states.equipment.detached) {
          value = $filter('appendBinStatus')(value);
          results[key] = value;
        }
      });

      $scope.numDetached = Object.keys(results).length;
      return results;
    };

    equipmentFilters.add({ detached: $scope.detachedFilter });

    $scope.numDown = function(equipment) {
      var results = $scope.downFilter(equipment);

      return Object.keys(results)
        .length;
    };

    equipmentFilters.add({ numDown: $scope.numDown });

    $scope.pendingDetachFilter = function(equipment) {
      var results = {};
      angular.forEach(equipment, function(value, key) {
        if (value.states[$scope.equipmentLocation] === states.equipment.pendingDetach) {
          value = $filter('appendBinStatus')(value);
          results[key] = value;
        }
      });

      $scope.numPendingDetach = Object.keys(results).length;

      return results;
    };

    equipmentFilters.add({ pendingDetach: $scope.pendingDetachFilter });

    $scope.pendingLoadFilter = function(equipment, from) {
        var results = {};
        angular.forEach(equipment, function(value, key) {
          if (value.states[$scope.equipmentLocation] === states.equipment.pendingLoad) {
            value = $filter('appendBinStatus')(value);
            results[key] = value;
          }
        });

        $scope.numPendingLoad = Object.keys(results).length;

        return results;
    };

    equipmentFilters.add({ pendingLoad: $scope.pendingLoadFilter });

    $scope.pendingAttachFilter = function(equipment) {
      var results = {};

      angular.forEach(equipment, function(value, key) {

        if (value.states[$scope.equipmentLocation] === states.equipment.pendingAttach) {
          value = $filter('appendBinStatus')(value);
          results[key] = value;
        }
      });

      $scope.numPendingAttach = Object.keys(results).length;

      return results;
    };

    equipmentFilters.add({ pendingAttach: $scope.pendingAttachFilter });

    $scope.alleyTruckFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.alleyTruck, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.alleyTrucks.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.alleyTrucks.show = true;
          break;
        }
      }
      $scope.alleyTrucks.total = Object.keys(availableResults).length;
      if ($scope.alleyTrucks.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.alleyTrucks.show = true;
      }

      return availableResults;
    };

    equipmentFilters.add({ alleyTruck: $scope.alleyTruckFilter });

    $scope.dualBinFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.dualBins, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.dualBins.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.dualBins.show = true;
          break;
        }
      }
      $scope.dualBins.total = Object.keys(availableResults)
        .length;
      if($scope.dualBins.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.dualBins.show = true;
      }

      return availableResults;
    };

    equipmentFilters.add({ dualBin: $scope.dualBinFilter });

    $scope.ezPackFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.ezPacks, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.ezPacks.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation][$scope.equipmentLocation] != states.equipment.detached) {
          $scope.ezPacks.show = true;
          break;
        }
      }
      $scope.ezPacks.total = Object.keys(availableResults)
        .length;
      if($scope.ezPacks.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.ezPacks.show = true;
      }

      return availableResults;
    };

    equipmentFilters.add({ ezPack: $scope.ezPackFilter });

    $scope.hoistFittedChassisFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.hoistFittedChassis, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.hoistFittedChassis.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.hoistFittedChassis.show = true;
          break;
        }
      }
      $scope.hoistFittedChassis.total = Object.keys(availableResults)
        .length;
      if($scope.hoistFittedChassis.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.hoistFittedChassis.show = true;
      }

      return availableResults;
    };

    equipmentFilters.add({ hoistFittedChassis: $scope.hoistFittedChassisFilter });

    $scope.miscellaneousFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.miscellaneous, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.miscellaneous.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.miscellaneous.show = true;
          break;
        }
      }
      $scope.miscellaneous.total = Object.keys(availableResults)
        .length;
      if($scope.miscellaneous.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.miscellaneous.show = true;
      }
      return availableResults;
    };

    equipmentFilters.add({ miscellaneous: $scope.miscellaneousFilter });

    $scope.mechanicalBroomFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.mechanicalBrooms, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.mechanicalBrooms.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.mechanicalBrooms.show = true;
          break;
        }
      }
      $scope.mechanicalBrooms.total = Object.keys(availableResults)
        .length;
      if($scope.mechanicalBrooms.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.mechanicalBrooms.show = true;
      }
      return availableResults;
    };

    equipmentFilters.add({ mechanicalBroom: $scope.mechanicalBroomFilter });

    $scope.rearLoaderFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.rearLoader, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);
      $scope.rearLoaders.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.rearLoaders.show = true;
          break;
        }
      }
      $scope.rearLoaders.total = Object.keys(availableResults)  .length;
      if($scope.rearLoaders.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.rearLoaders.show = true;
      }
      return availableResults;
    };

    equipmentFilters.add({ rearLoader: $scope.rearLoaderFilter });

    $scope.roRoFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.roRo, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.roRos.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.roRos.show = true;
          break;
        }
      }
      $scope.roRos.total = Object.keys(availableResults)
        .length;
      if($scope.roRos.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.roRos.show = true;
      }
      
      return availableResults;
    };

    equipmentFilters.add({ roRo: $scope.roRoFilter });

    $scope.snowFilter = function(equipment) {
      var results = $filter('filterByGroup')(equipment, groups.equipment.snow, $scope.equipmentLocation);
      var availableResults = $filter('availableFilter')(results, $scope.equipmentLocation);

      $scope.snow.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[$scope.equipmentLocation] != states.equipment.detached) {
          $scope.snow.show = true;
          break;
        }
      }
      $scope.snow.total = Object.keys(availableResults)
        .length;
      if($scope.snow.total == 0 && isAnyEquipmentOnLocation(results)) {
          $scope.snow.show = true;
      }
      return availableResults;
    };

    equipmentFilters.add({ snow: $scope.snowFilter });

    $scope.relayCollectionFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        results = filterByMaterialGroup(relayResults, materialGroups.equipment.collection, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayCollection.show = false;
      if (cnt > 0) {
        $scope.relayCollection.show = true;
      }


      $scope.relayCollection.total = cnt;
      if($scope.relayCollection.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment, $scope.equipmentLocation);
        var relayCollectionInDown = filterByMaterialGroup(downEquipment, materialGroups.equipment.collection, binStatus.equipment.relay);
        if(Object.keys(relayCollectionInDown).length > 0)
          $scope.relayCollection.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayCollection: $scope.relayCollectionFilter });

    $scope.relayRecyclingPaperFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(relayResults, materialGroups.equipment.recycling, binStatus.equipment.relay),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.paper, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayRecyclingPaper.show = false;
      if (cnt > 0)
        $scope.relayRecyclingPaper.show = true;


      $scope.relayRecyclingPaper.total = cnt;
      if($scope.relayRecyclingPaper.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay);
          var relayRecyclingPaperInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.paper, binStatus.equipment.relay);
        if(Object.keys(relayRecyclingPaperInDown).length > 0)
          $scope.relayRecyclingPaper.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayRecycling: $scope.relayRecyclingPaperFilter });

    $scope.relayRecyclingOrganicsFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(relayResults, materialGroups.equipment.recycling, binStatus.equipment.relay),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.organics, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayRecyclingOrganics.show = false;
      if (cnt > 0)
        $scope.relayRecyclingOrganics.show = true;


      $scope.relayRecyclingOrganics.total = cnt;
      if($scope.relayRecyclingOrganics.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay);
          var relayRecyclingOrganicsInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.organics, binStatus.equipment.relay);
        if(Object.keys(relayRecyclingOrganicsInDown).length > 0)
          $scope.relayRecyclingOrganics.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayRecyclingOrganics: $scope.relayRecyclingOrganicsFilter });

    $scope.relayRecyclingMGPFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(relayResults, materialGroups.equipment.recycling, binStatus.equipment.relay),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.mgp, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayRecyclingMGP.show = false;
      if (cnt > 0)
        $scope.relayRecyclingMGP.show = true;


      $scope.relayRecyclingMGP.total = cnt;
      if($scope.relayRecyclingMGP.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay);
          var relayRecyclingMGPInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.mgp, binStatus.equipment.relay);
        if(Object.keys(relayRecyclingMGPInDown).length > 0)
          $scope.relayRecyclingMGP.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayRecyclingMGP: $scope.relayRecyclingMGPFilter });

    $scope.relayRecyclingMiscFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(relayResults, materialGroups.equipment.recycling, binStatus.equipment.relay),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.recyclingMisc, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayRecyclingMisc.show = false;
      if (cnt > 0)
        $scope.relayRecyclingMisc.show = true;


      $scope.relayRecyclingMisc.total = cnt;
      if($scope.relayRecyclingMisc.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.relay);
          var relayRecyclingMiscInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.recyclingMisc, binStatus.equipment.relay);
        if(Object.keys(relayRecyclingMiscInDown).length > 0)
          $scope.relayRecyclingMisc.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayRecyclingMisc: $scope.relayRecyclingMiscFilter });

    $scope.relayMiscellaneousFilter = function(equipment) {
      var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
        results = filterByMaterialGroup(relayResults, materialGroups.equipment.miscellaneous, binStatus.equipment.relay),
        cnt = Object.keys(results)
        .length;

      $scope.relayMiscellaneous.show = false;
      if (cnt > 0)
        $scope.relayMiscellaneous.show = true;
      else{
        var downEquipment = $scope.downFilter(results);
        if(Object.keys(downEquipment).length > 0)
          $scope.relayMiscellaneous.show = true;
      }
      $scope.relayMiscellaneous.total = cnt;
      if($scope.relayMiscellaneous.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.miscellaneous, binStatus.equipment.relay);
          if(Object.keys(temp).length > 0)
          $scope.relayMiscellaneous.show = true;
      }
      return results;
    };

    equipmentFilters.add({ relayMiscellaneous: $scope.relayMiscellaneousFilter });
    
    
    $scope.relaySnowFilter = function(equipment) {
        var relayResults = $filter('relayFilter')(equipment, $scope.equipmentLocation),
          results = filterByMaterialGroup(relayResults, materialGroups.equipment.snow, binStatus.equipment.relay),
          cnt = Object.keys(results)
          .length;

        $scope.relaySnow.show = false;
        if (cnt > 0)
          $scope.relaySnow.show = true;
        else{
          var downEquipment = $scope.downFilter(results);
          if(Object.keys(downEquipment).length > 0)
            $scope.relaySnow.show = true;
        }
        $scope.relaySnow.total = cnt;
        if($scope.relaySnow.total == 0){
          var downEquipment = $scope.relayDownFilter(equipment);
          var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.snow, binStatus.equipment.relay);
            if(Object.keys(temp).length > 0)
            $scope.relaySnow.show = true;
        }
        return results;
      };

      equipmentFilters.add({ relaySnow: $scope.relaySnowFilter });
      

    $scope.rolloverCollectionFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        results = filterByMaterialGroup(rolloverResults, materialGroups.equipment.collection, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverCollection.show = false;
      if (cnt > 0) {
        $scope.rolloverCollection.show = true;  
      }



      $scope.rolloverCollection.total = cnt;
      if($scope.rolloverCollection.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.collection, binStatus.equipment.rollover);
        if(Object.keys(temp).length > 0)
          $scope.rolloverCollection.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverCollection: $scope.rolloverCollectionFilter });

    $scope.rolloverRecyclingPaperFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(rolloverResults, materialGroups.equipment.recycling, binStatus.equipment.rollover),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.paper, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverRecyclingPaper.show = false;
      if (cnt > 0)
        $scope.rolloverRecyclingPaper.show = true;


      $scope.rolloverRecyclingPaper.total = cnt;
      if($scope.rolloverRecyclingPaper.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.rollover);
          var rolloverRecyclingPaperInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.paper, binStatus.equipment.rollover);
        if(Object.keys(rolloverRecyclingPaperInDown).length > 0)
          $scope.rolloverRecyclingPaper.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverRecyclingPaper: $scope.rolloverRecyclingPaperFilter });

    $scope.rolloverRecyclingOrganicsFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(rolloverResults, materialGroups.equipment.recycling, binStatus.equipment.rollover),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.organics, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverRecyclingOrganics.show = false;
      if (cnt > 0)
        $scope.rolloverRecyclingOrganics.show = true;


      $scope.rolloverRecyclingOrganics.total = cnt;
      if($scope.rolloverRecyclingOrganics.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.rollover);
          var rolloverRecyclingOrganicsInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.organics, binStatus.equipment.rollover);
        if(Object.keys(rolloverRecyclingOrganicsInDown).length > 0)
          $scope.rolloverRecyclingOrganics.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverRecyclingOrganics: $scope.rolloverRecyclingOrganicsFilter });

    $scope.rolloverRecyclingMGPFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(rolloverResults, materialGroups.equipment.recycling, binStatus.equipment.rollover),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.mgp, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverRecyclingMGP.show = false;
      if (cnt > 0)
        $scope.rolloverRecyclingMGP.show = true;


      $scope.rolloverRecyclingMGP.total = cnt;
      if($scope.rolloverRecyclingMGP.total == 0){
        var downEquipment = $scope.relayDownFilter(results);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.rollover);
          var rolloverRecyclingMGPInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.mgp, binStatus.equipment.rollover);
        if(Object.keys(rolloverRecyclingMGPInDown).length > 0)
          $scope.rolloverRecyclingMGP.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverRecyclingMGP: $scope.rolloverRecyclingMGPFilter });

    $scope.rolloverRecyclingMiscFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        recycleResults = filterByMaterialGroup(rolloverResults, materialGroups.equipment.recycling, binStatus.equipment.rollover),
        results = filterByMaterialSubGroup(recycleResults, materialSubGroups.equipment.recyclingMisc, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverRecyclingMisc.show = false;
      if (cnt > 0)
        $scope.rolloverRecyclingMisc.show = true;


      $scope.rolloverRecyclingMisc.total = cnt;
      if($scope.rolloverRecyclingMisc.total == 0){
        var downEquipment = $scope.relayDownFilter(results);
        var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.recycling, binStatus.equipment.rollover);
          var rolloverRecyclingMiscInDown = filterByMaterialSubGroup(temp, materialSubGroups.equipment.recyclingMisc, binStatus.equipment.rollover);
        if(Object.keys(rolloverRecyclingMiscInDown).length > 0)
          $scope.rolloverRecyclingMisc.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverRecyclingMisc: $scope.rolloverRecyclingMiscFilter });

    $scope.rolloverMiscellaneousFilter = function(equipment) {
      var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
        results = filterByMaterialGroup(rolloverResults, materialGroups.equipment.miscellaneous, binStatus.equipment.rollover),
        cnt = Object.keys(results)
        .length;

      $scope.rolloverMiscellaneous.show = false;
      if (cnt > 0)
        $scope.rolloverMiscellaneous.show = true;


      $scope.rolloverMiscellaneous.total = cnt;
      if($scope.rolloverMiscellaneous.total == 0){
        var downEquipment = $scope.relayDownFilter(equipment);
        var rolloverMiscInDown = filterByMaterialGroup(downEquipment, materialGroups.equipment.miscellaneous, binStatus.equipment.rollover);
        if(Object.keys(rolloverMiscInDown).length > 0)
          $scope.rolloverMiscellaneous.show = true;
      }
      return results;
    };

    equipmentFilters.add({ rolloverMiscellaneous: $scope.rolloverMiscellaneousFilter });
    
    $scope.rolloverSnowFilter = function(equipment) {
        var rolloverResults = $filter('rolloverFilter')(equipment, $scope.equipmentLocation),
          results = filterByMaterialGroup(rolloverResults, materialGroups.equipment.snow, binStatus.equipment.rollover),
          cnt = Object.keys(results)
          .length;

        $scope.rolloverSnow.show = false;
        if (cnt > 0)
          $scope.rolloverSnow.show = true;


        $scope.rolloverSnow.total = cnt;
        if($scope.rolloverSnow.total == 0){
          var downEquipment = $scope.relayDownFilter(equipment);
          var temp = filterByMaterialGroup(downEquipment, materialGroups.equipment.snow, binStatus.equipment.rollover);
          if(Object.keys(temp).length > 0)
            $scope.rolloverSnow.show = true;
        }
        return results;
      };

      equipmentFilters.add({ rolloverSnow: $scope.rolloverSnowFilter });

    // listen for data changes
    $scope.$on('UPDATE-EQUIPMENT-PANE', updateEquipmentUI());

    // ----------------------------------------- end of filters to update ui
    
    $rootScope.equipmentSummaryDisplay = OpsBoardRepository.getDisplayEquipmentSummaryValues();

    /*
     * Sub panel control
     *
     * Current logic is to open all top-level categories on expand,
     * close all on collapse, or, if no categories, to open/close the body.
     *
     * TODO: This is somewhat fragile - adding/removing top level categories will require modifying these functions
     *
     * */
    $scope.toggleAvailableSubpanel = function() {
        $scope.available.open = !$scope.available.open;

        $scope.rearLoaders.open = $scope.available.open;
        $scope.dualBins.open = $scope.available.open;
        $scope.alleyTrucks.open = $scope.available.open;
        $scope.mechanicalBrooms.open = $scope.available.open;
        $scope.roRos.open = $scope.available.open;
        $scope.ezPacks.open = $scope.available.open;
        $scope.miscellaneous.open = $scope.available.open;
        $scope.snow.open = $scope.available.open;
    };

    $scope.toggleRolloverSubpanel = function() {
        $scope.rollover.open = !$scope.rollover.open;

        $scope.rolloverRecycling.open = $scope.rollover.open;
        $scope.rolloverMiscellaneous.open = $scope.rollover.open;
    };
    $scope.toggleRelaySubpanel = function() {
        $scope.relay.open = !$scope.relay.open;

        $scope.relayRecycling.open = $scope.relay.open;
        $scope.relayMiscellaneous.open = $scope.relay.open;
        $scope.relaySnow.open = $scope.relay.open;
        $scope.relayCollection.open = $scope.relay.open;
    };
    $scope.togglePendingSubpanel = function() {
        $scope.pending.open = !$scope.pending.open;

        $scope.pendingLoad.open = $scope.pending.open;
        $scope.pendingAttach.open = $scope.pending.open;
        $scope.pendingDetach.open = $scope.pending.open;
    };
    $scope.toggleDetachedSubpanel = function() {
        $scope.detached.open = !$scope.detached.open;
    };
    $scope.toggleDownSubpanel = function() {
        $scope.down.open = !$scope.down.open;
    };
  });