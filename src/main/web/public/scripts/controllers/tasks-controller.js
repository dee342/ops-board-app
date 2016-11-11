'use strict';

angular.module('OpsBoard')
  .controller('TasksCtrl', function($rootScope, $scope, $filter, $controller, states, OpsBoardRepository, $document, ClientSideError, UtilityService, $modal, $log, durations, BoardValueService, TaskHelperService, $window, DistrictService, $timeout) {

    var _renderTaskPanel = function(renderDistrict) {
      var tasksForDom = TaskHelperService.renderTasks();
      $scope.taskPaneWidth = ((tasksForDom.columncount) * 464) + (20 * tasksForDom.loccount);
      $scope.taskPaneWidthDistrict = ((tasksForDom.districtcolumncount) * 270) + 10;

      $timeout(function() {
        $scope.locationtasks = tasksForDom;
      },0);

      if(renderDistrict) {
        $timeout(function() {
          $scope.locationtasksdistrict = tasksForDom;
        },0);
      }
    };

    var _updateTaskPane = function(event, data) {
      if(data && data.repaint) {
        _renderTaskPanel(data.repaintdistrict);
      }
    };

    _renderTaskPanel(true);


    $scope.tasks = OpsBoardRepository.getTasks();
    $scope.renderDistrict = function (locationId) {
      DistrictService.renderDistrict(locationId, $scope.tasks)
    };

    $rootScope.$on('UPDATE-TASKS', function onUpdateTasks(event, data) {
      _updateTaskPane(event, data);
    });


    var locals = {
      $scope: $scope,
      $filter: $filter,
      OpsBoardRepository: OpsBoardRepository,
      ClientSideError: ClientSideError
    }

    $scope.taskDescMaxLength = 10;

    $scope.isLocked = function() {
      return OpsBoardRepository.isLocked();
    };
    
    $controller('ClearAllLocations', locals);
    
    $scope.toggleTask = function (task) {
      task.editMode = !task.editMode;
    };
    
    $scope.linkedTaskMap = OpsBoardRepository.getLinkedTaskMap();

    $scope.saveChanges = function (task) {
      task.editMode = false;
    };

    $scope.clearSpecificLocation = function(locationId){
      $scope.tasks.locations[locationId].locationShifts={};
      OpsBoardRepository.clearSpecificLocation(locationId);
    };

    var _removeShift = function(data) {
      var locationId = data.locationId,
        locationShiftId = data.locationShiftId,
        shiftId = data.shiftId;

      delete $scope.tasks.locations[locationId].locationShifts[locationShiftId];
      _updateTaskPane(null, {repaint: true});
      OpsBoardRepository.removeShift(locationShiftId, locationId, shiftId);
    }
    
    var _showConfirmPopUp = function(data, type) {

        $scope.passPreValidation = function () {
            return true;
          }
          $scope.submitted = false;
          $scope.operation = function(success, error) {
              $scope.submitted = true;
              $scope.opData.clientErrors = [];
              $scope.opData.serverErrors = [];
              success();
            };
        
        var titleAction, message, methodName;
        if (type == 'SHIFT'){
        	titleAction = 'Delete Shift';
            message = 'Are you sure you want to DELETE this shift?';
        }else if (type == 'SUBCATEGORY'){
        	titleAction = 'Delete Subcategory';
            message = 'Are you sure you want to DELETE this subcategory?';
        }
    	//if (type == 'SHIFT'){
            $scope.opData = {
            titleAction : titleAction,
            cancelButtonText: 'No',
            submitButtonText: 'Yes',
            message: message,
            };

            var modalInstance = $modal.open({
            templateUrl : appPathStart + '/views/modals/modal-delete-confirm',
            controller : 'ModalCtrl',
            backdrop : 'static',
            resolve : {
                data : function() {
                  return [$scope.opData];
                }
            },
            scope : $scope
            });
            modalInstance.result.then(function() {
            	if(type == 'SHIFT'){
            		_removeShift(data);
            	}
            	else if (type == 'SUBCATEGORY'){
            		_deleteSubcategory(data);
            	}
              }, function() {
                $log.info('Modal dismissed at: ' + new Date());
              });
        //}
    }

    $rootScope.$on('DELETE-SHIFT', function onUpdateTasks(event, data) {
      //_removeShift(data);
    	_showConfirmPopUp(data, 'SHIFT');
    });

    var _deleteTaskFromSection = function (data){
       var task = data.task,
           section = data.section,
           subcategory = data.subcategory,
           category = data.category,
           shift = data.shift,
           location = data.location,
           locationId = data.locationId;


      $scope.tasks.deletedTasksMap[task.id] = $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].sections[section.id].tasks[task.id];
      delete $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].sections[section.id].tasks[task.id];

      OpsBoardRepository.deleteSingleTask(task, section, subcategory, category, shift, location, locationId, true);
      _updateTaskPane(null, {repaint: true});
    };

    $rootScope.$on('DELETE-TASK-FROM-SECTION', function onUpdateTasks(event, data) {
        _deleteTaskFromSection(data);
    });

    var _deleteTaskFromSubCategory = function (data){

      var task = data.task,
        subcategory = data.subcategory,
        category = data.category,
        shift = data.shift,
        location = data.location,
        locationId = data.locationId;

      $scope.tasks.deletedTasksMap[task.id] = $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].tasks[task.id];
      delete $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].tasks[task.id];
      OpsBoardRepository.deleteSingleTask(task, null, subcategory, category, shift, location, locationId, false);
        _updateTaskPane(null, {repaint: true});
    };

    $rootScope.$on('DELETE-TASK-FROM-SUBCATEGORY', function onUpdateTasks(event, data) {
        _deleteTaskFromSubCategory(data);
    });


    var _addToGroup = function (data) {
      var task = data.task,
          subcategory = data.subcategory,
          category = data.category,
          shift = data.shift,
          location = data.location,
          locationId = data.locationId,
          section = data.section;

      if (!($scope.layout.screen1.visible && $scope.panes.taskSettings.active && $scope.panes.showMultiRoutes.active)) {
        return;
      }

      if (task.groupId) {
        return;
      }

      if ((task.assignedEquipment.equipment && task.assignedEquipment.equipment.id) || (task.assignedPerson1.person && task.assignedPerson1.person.id) || (task.assignedPerson2.person && task.assignedPerson2.person.id)) {
        return;
      }

      if ($scope.group.length !== 0) {
        if ($scope.group[0].locationShiftId !== shift.id) {
          return;
        }
        if ($scope.group[0].peoplePerTask !== subcategory.subcategory.peoplePerTask) {
          return;
        }
      }

      for (var i = 0; i < $scope.group.length; i++) {
        if ($scope.group[i].taskId === task.id) {
          return;
        }
      }

      var label = category.category.name + " - " + subcategory.subcategory.name + " / ";

      if (subcategory.subcategory.containsSections) {
        label += section.sectionName + ' / ';
      }
      
      if (category && category.category && category.category.id === 1) {
    	  return;
      }
      
      $scope.group.push({
        locationId: locationId,
        taskId: task.id,
        locationShiftId: shift.id,
        shiftId: shift.shift.id,
        shiftCategoryId: category.id,
        subcategoryTaskId: subcategory.id,
        containsSections: subcategory.subcategory.containsSections,
        peoplePerTask: subcategory.subcategory.peoplePerTask,
        sectionId: subcategory.subcategory.containsSections ? section.id : null,
        sequence: $scope.group.length + 1,
        label: label,
        duration: '',
        linkedTaskChildId: task.linkedTaskChildId,
        linkedTaskParentId: task.linkedTaskParentId
      });
    };

    $scope.$on('ADD-TASK-TO GROUP', function(event, data) {
      _addToGroup(data)
    });


    var _deleteSubcategory = function (data){
      var subcategory = data.subcategory,
          category = data.category,
          shift = data.shift,
          location = data.location,
          locationId = data.locationId,
          deletesubcategory = true;

      delete $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id];

      var location2 = {}, shift2 = {}, shiftCategories = {}, shiftcategory2 = {}, subcategorytasks = {};

      //todo don't need this loop
      _.each($scope.tasks.locations, function(location) {
        location2 = location;
        var shifts = location2.locationShifts;
        //SHIFT  **************************
        for (var shift in shifts) {
          shift2 = shifts[shift];
          shiftCategories = shift2.shiftCategories;
          //SHIFT CATEGORY **************************
          for (var shiftcategory in shiftCategories) {
            shiftcategory2 = shiftCategories[shiftcategory];
            subcategorytasks = shiftcategory2.subcategoryTasks;
            if(_.keys(subcategorytasks).length=== 0 && shiftcategory2.category.id === category.category.id) {
              //locationId, locationShiftId, shiftCategoryId, categoryId, shiftId
              OpsBoardRepository.removeCategory(location2.locationCode, shift2.id, shiftcategory2.id, shiftcategory2.category.id, shift2.shiftId);
              deletesubcategory = false;
            }
          }
        }
      });

      if(deletesubcategory === true) {
        OpsBoardRepository.removeSubcategory(shift.id, category.id, subcategory.id, locationId, shift.shift.id.toString(), category.category.id.toString(), subcategory.subcategory.id.toString());
      }

      _updateTaskPane(null, {repaint: true});

    }

    var deleteSubcategory = function (event, data) {
        _showConfirmPopUp(data, 'SUBCATEGORY');
    	//_deleteSubcategory(data);
    };

    $rootScope.$on('DELETE-SUBCATEGORY', deleteSubcategory);

    var _deleteSection = function (data) {
      var section = data.section,
        subcategory = data.subcategory,
        category = data.category,
        shift = data.shift,
        location = data.location,
        locationId = data.locationId;

      var allSections = $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].allSections;
      for(var l = 0; l < allSections.length; l++) {
        if (allSections[l].id == $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].sections[section.id].sectionId) {
          allSections[l].numOfTasks = 0;
        }
      }
      delete $scope.tasks.locations[locationId].locationShifts[shift.id].shiftCategories[category.id].subcategoryTasks[subcategory.id].sections[section.id];

      _updateTaskPane(null, {repaint: true});
      OpsBoardRepository.removeSection(shift.id, category.id, subcategory.id, section.id, locationId, shift.shiftId, category.categoryId, subcategory.subcategoryId, section.sectionId);
    };

    $rootScope.$on('DELETE-SECTION', function onUpdateTasks(event, data) {
      _deleteSection(data);
    });

    $scope.equipment = OpsBoardRepository.getEquipment();
    $scope.personnel = OpsBoardRepository.getPersonnel();
    $scope.defaultOpen = true;
    $scope.recentlyPushedId = 'default';

    $rootScope.$watch('pushedId', function(newV, oldV) {
      if (oldV === undefined && oldV === newV) return;
      $scope.recentlyPushedId = newV;
    });

    $scope.items = ['Reset'];

    $scope.status = {
      isopen: false
    };
    
    $scope.getEquipmentKey = function(assignedEquipment) {
        if (!assignedEquipment || !assignedEquipment.equipment)
          return;
        return assignedEquipment.equipment.id;
      };


  $scope.isBinLoadUpdated = function(task, equipment) {
     if (equipment && task && task.assignedEquipment) {

      if (task.assignedEquipment.completed)
        return 'G';

      if ($filter('isEquipmentDressed')(equipment))
        return 'S';
     }
  };

  $scope.getFormattedEndHour = function(shiftStartHour, shiftEndHour){
    var formattedEndHour;
    if(shiftStartHour >= 16 && shiftStartHour < 23 ){
      switch (shiftEndHour) {
      case 0:
        formattedEndHour = 24;
        break;
      case 1:
        formattedEndHour = 25;
        break;
      case 2:
        formattedEndHour = 26;
        break;
      case 3:
        formattedEndHour = 27;
        break;
      case 4:
        formattedEndHour = 28;
        break;
      case 5:
        formattedEndHour = 29;
        break;
      case 6:
        formattedEndHour = 30;
        break;
      }
    }
    return formattedEndHour;
  };


  $scope.getContext = function(id) {
    if (id === 1) {
      return 'supervisor-assignment';
    } else {
      return 'task-assignment';
    }
    return;
  };

    $scope.clearAllLocations = function() {
      var locationIds = Object.keys($scope.tasks.locations);
      for(var i=0; i< locationIds.length; i++){
        $scope.tasks.locations[locationIds[i]].locationShifts = {};
      }
      return OpsBoardRepository.clearAllLocations();
      }



    $scope.toggleDropdown = function($event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.status.isopen = !$scope.status.isopen;
    };

    $scope.titleMap = function(title) {
      var results = OpsBoardRepository.getMappedTitle(title);
      return results;
    };
    
    
    $scope.isShowLocationId = function (locations, locationShifts) {
      if(UtilityService.numberOfObjectsInCollection(locations) > 1 && UtilityService.numberOfObjectsInCollection(locationShifts) > 0 ){
        return true;
        }
        else{
          return false;
        }
      };


    });
