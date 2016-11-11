'use strict';

angular.module('OpsBoard')
  .service(
    'DistrictService',
    function($rootScope, $q, $filter, $http, $timeout, states, BoardValueService, 
      PersonModel, EquipmentModel, PersonnelHelperService, TaskHelperService, 
      BoardDataService, OpsBoardRepository, UtilityService) {

      var boardData = BoardDataService.getBoardData(),
        boardDate = boardData.boardDate,
        boardLocation = boardData.boardLocation,
        pathStart = BoardDataService.getPathStart();

        BoardValueService.districtPersons = {};
        BoardValueService.districtEquipments = {};

      var updateDistrictData = function (data, locationId, now, teaser) {
        var deferred = $q.defer();
        var districtData = {};
        var fitleredtaskContainers = {};
        var location = {};
        var personObject = {};



        $http.get(pathStart + '/' + locationId + '/' + boardDate + '/district')
          .success(function (jsondata) {
            if ((jsondata.taskContainers[jsondata.location] || BoardValueService.locationRefsData[jsondata.location].servicesEquipmentLocations === true) && BoardValueService.locationRefsData[jsondata.location].type !== 'district-garage') {
              var locationOverride = jsondata.location;
              fitleredtaskContainers[locationId] = jsondata.taskContainers[jsondata.location];
              if(BoardValueService.locationRefsData[jsondata.location].servicesEquipmentLocations === true) {
                fitleredtaskContainers[locationId] = {};
                fitleredtaskContainers[locationId].servicelocations = jsondata.taskContainers;
              }

              districtData = $filter('filterDistrictObjects')(fitleredtaskContainers[locationId]);
              data.taskContainers[jsondata.location] = fitleredtaskContainers[locationId];
              data.taskContainers[jsondata.location].failedToLoad = false;
            }

            BoardValueService.personneldistricts[jsondata.location] = jsondata.personnel;

            for (var person in jsondata.personnel) {
              angular.extend(jsondata.personnel[person], PersonModel);
              PersonnelHelperService.processPartialAvailabilityAndMda(jsondata.personnel[person], now, states);
              jsondata.personnel[person] = $filter('extendPerson')(jsondata.personnel[person]);
              BoardValueService.districtPersons[person] = jsondata.personnel[person];
            }

            for (var equipment in jsondata.equipment) {
              angular.extend(jsondata.equipment[equipment], EquipmentModel);
              BoardValueService.districtEquipments[equipment] = jsondata.equipment[equipment];
              jsondata.equipment[equipment].activeGroundedStatus = [];
            }

            BoardValueService.equipmentdistricts[jsondata.location] = jsondata.equipment;
            deferred.resolve();
          }
        ).error(function (jsondata, status, headers, config) {
            data.taskContainers[locationId] = {location: {code: locationId}, failedToLoad: true};
            deferred.resolve({'error': locationId});
        });

        return deferred.promise;
      };

      var updateAllDistricts = function (data, now) {
        var boardLocation = boardData.boardLocation,
          pathStart = BoardDataService.getPathStart();
        data.personnelContainers = {};
        BoardValueService.personneldistricts = {};
        BoardValueService.equipmentdistricts = {};

        var locations = BoardDataService.getRepairLocations();


        var districtscount = 0;
        var districtsloaded = 0;
        var districtLoads = [];

        var districtLoads = locations.map(function(loc) {
          if (loc.boroughCode === boardLocation && loc.type !== 'district-garage') {
            return updateDistrictData(data, loc.code, now);
          }
        });

        var deferred = $q.defer();

        $q.all(districtLoads).then(function (res) {
          deferred.resolve(res);
        });
        return deferred.promise;
      }

      var renderDistricts = function (res) {
        var data = res.data,
          tasks = res.tasks,
          now = new Date(),
          deferred = $q.defer();

        if(data.locationType === 'borough' && !$rootScope.renderDistricts) {
          updateAllDistricts(data, now).then(function(){
            // build tasks
            tasks = TaskHelperService.marshallTasks(
              {"locations": data.taskContainers},
              BoardDataService.getAllCategoryData(),
              OpsBoardRepository.getLinkedTaskMap(),
              BoardDataService.getShiftData(),
              BoardDataService.getRepairLocations(),
              data.equipment,
              data.personnel
            );
            Object.keys(data.personnel).forEach(function(person){
              BoardValueService.districtPersons[person] = data.personnel[person]
            });
            Object.keys(data.equipment).forEach(function(equipment){
              BoardValueService.districtEquipments[equipment] = data.equipment[equipment]
            });
            deferred.resolve(data);
           
          });
          $rootScope.renderDistricts = true;
        } else {
          deferred.resolve(data);
        }
        return deferred.promise;
      }

      var renderDistrict = function (locationId, tasks) {
        var now = new Date();
        var districtdata = {taskContainers:{}};
        var locId = locationId;
        updateDistrictData(districtdata, locationId, now, true).then(function(){

          var task = TaskHelperService.marshallTasks(
            {"locations": districtdata.taskContainers},
            BoardDataService.getAllCategoryData(),
            OpsBoardRepository.getLinkedTaskMap(),
            BoardDataService.getShiftData(),
            BoardDataService.getRepairLocations(),
            BoardValueService.districtEquipments,
            BoardValueService.districtPersons
          );

          tasks = UtilityService.mergeRecursive(tasks, task);
          tasks.locations[locId] = task.locations[locId];
          $rootScope.$broadcast('UPDATE-TASKS', {repaint: true, repaintdistrict: true});
        })
      }


      return {
        renderDistricts: renderDistricts,
        renderDistrict: renderDistrict
      }
    });