'use strict';

angular
  .module('OpsBoard')
  .controller('TaskController', function ($rootScope, $scope, $filter, $controller, states, OpsBoardRepository, $document, ClientSideError, UtilityService, $modal, $log, durations, BoardValueService, TaskHelperService, $window, BoardDataService, groups, BoardHelperService) {
    var persons = BoardValueService.personnel;
    var equipments = OpsBoardRepository.getEquipment();
    var tasks = OpsBoardRepository.getTasks().tasksMap;

    /* Handle Drag & Drop */
    $scope.assignToTask = function (task, type, position, shiftId, elementId, locationId, context, taskIndicator, fromTask, fromPosition) {

      if (OpsBoardRepository.isLocked()) {
        return false;
      }
      tasks = OpsBoardRepository.getTasks().tasksMap; // we need to refresh tasks, newly added tasks do not reflect without this step
      var originalTask = null;
      if (fromTask) {
        originalTask = tasks[fromTask];
      }

      if (originalTask && originalTask.isCompleted()) {
        return false;
      }

      /* if(fromPosition == 1 && originalTask && originalTask.completedPersonAssignment1 == 'completed'){
       return false;
       }else if(fromPosition == 2 && originalTask && originalTask.completedPersonAssignment2 == 'completed'){
       return false;
       }*/

      if (originalTask && originalTask.EquipObj && originalTask.EquipObj.state == 'Pending Load') {
        return false;
      }

      if (originalTask && (originalTask.linkedTaskChildId || originalTask.linkedTaskParentId)) {
        return false;
      }

      // check if equipment eixsts

      if (type === 'equipment' && (equipments[elementId] === undefined)) {
        return false;
      }

      var personReference = persons[elementId];

      // check if person exists
      if (type === 'personnel' && personReference == undefined) {
        return false;
      }

      if (type === 'personnel' && (type === 'supervisor-task' || type === 'supervisor-section' || type === 'supervisor-subcategory' || context === 'supervisor-assignment' || context === 'task-assignment')) {
        return false;
      }

      if ((type === 'supervisor-task' || type === 'supervisor-section' || type === 'supervisor-subcategory') && personReference == undefined) {
        return false;
      }

      // target person must be a supervisor
      if ((type === 'supervisor-task' || type === 'supervisor-section' || type === 'supervisor-subcategory') && personReference.civilServiceTitle !== groups.personnel.supervisors) {
        return false;
      }

      if ((type === 'supervisor-task' || type === 'supervisor-section' || type === 'supervisor-subcategory') && context !== 'supervisor-assignment') {
        return false;
      }

      /* method to find out the taskIndicator from supervision category */

      if (context !== 'supervisor-assignment') {
        taskIndicator = '';
      } else {
        if (taskIndicator === '') {
          return false;
        }
      }

      if (context === 'supervisor-assignment') {
        if (type === 'supervisor-task' && task.taskSupervisorAssignments && task.taskSupervisorAssignments.length !== 0) {
          var found = false;
          for (var i = 0; i < task.taskSupervisorAssignments.length && !found; i++) {
            if (task.taskSupervisorAssignments[i].taskIndicator === taskIndicator && elementId === task.taskSupervisorAssignments[i].personId) {
              found = true;
            }
          }
          if (found) return;
        }
      }

      if (context === 'supervisor-assignment') {
        if (taskIndicator === 'undefined') {
          return false;
        }
        if (type === 'supervisor-section' && task.sectionSupervisorAssignments && task.sectionSupervisorAssignments.length !== 0) {

          console.info('supervisor-section');

          var found = false;

          for (var i = 0; i < task.sectionSupervisorAssignments.length && !found; i++) {
            if (task.sectionSupervisorAssignments[i].taskIndicator === taskIndicator && elementId === task.sectionSupervisorAssignments[i].personId) {
              found = true;
            }
          }
          if (found) return;
        }

        if (type === 'supervisor-subcategory' && task.supervisorAssignments && task.supervisorAssignments.length !== 0) {

          console.info('supervisor-subcategory');

          var found = false;

          for (var i = 0; i < task.supervisorAssignments.length && !found; i++) {
            if (task.supervisorAssignments[i].taskIndicator === taskIndicator && elementId === task.supervisorAssignments[i].personId) {
              found = true;
            }
          }
          if (found) return;
        }

      }


      if (fromTask && task.groupId) {
        return $log.warn('dnd : move : drop to partial task');
      }

      if (type === 'personnel' && personReference.state === 'Unavailable' && !BoardHelperService.isCurrentOrPastBoard()) {
        if (!personReference.getIsFutureMda()) {
          return false;
        }
      }

      if (type === 'personnel' && personReference.state === 'Available' && (personReference.assigned || personReference.assignedAnywhere) && !fromTask) {
        // show selection if already assigned
        $scope.decideAssignmentType(task, type, position, shiftId, elementId, locationId);
      } else if (type === 'personnel' && ((personReference.state === 'Unavailable' && personReference.activeUnavailabilityReasons.length > 0) || (!personReference.isActiveMDA() && personReference.assigned)) && BoardHelperService.isCurrentOrPastBoard() && !fromTask) {
        $scope.decideAssignmentType(task, type, position, shiftId, elementId, locationId);
      } else if(type === 'personnel' &&  !BoardHelperService.isCurrentOrPastBoard() && personReference.getIsFutureMda() && personReference.assigned) {
        $scope.decideAssignmentType(task, type, position, shiftId, elementId, locationId);
      } else {
        assignToTaskByDragAndDrop($scope, OpsBoardRepository, task, type, position, elementId, locationId, context, taskIndicator, fromTask, fromPosition);
      }
    };

    $scope.assginmentTypeToTask = function(task, type, position, elementId, locationId){
      assignToTaskByDragAndDrop($scope, OpsBoardRepository, task, type, position, elementId, locationId, null, null, null);
    };

    $scope.showPersonDetails = function(event, personId) {
      if (!personId) {return;}
      if (event.target.className.indexOf("menu-item") > -1) {return;}
      $scope.$emit('SHOW-PERSON-DETAILS',{personId: personId});
    };

    $scope.showEquipmentDetails = function(event, equipmentId) {
      if (!equipmentId) {return;}
      if (event.target.className.indexOf("menu-item") > -1) {return;}
      $scope.$emit('SHOW-EQUIPMENT-DETAILS',{equipmentId: equipmentId});
    };

    $scope.unassignPerson = function(personnel, task, position, locationId) {
      return OpsBoardRepository.unassignPersonFromTask(personnel, task, position, locationId);
    };

    $scope.unassignSupervisor = function(subcategory, section, task, locationId) {
      return OpsBoardRepository.unassignSupervisor(subcategory, section, task, locationId);
    };

  $scope.editTask = function (task, subcategory, category, shift, location, locationId, section) {
    if ($scope.layout.screen1.visible && $scope.panes.taskSettings.active && $scope.panes.showMultiRoutes.active) return;

    $scope.passPreValidation = function() {
      return true
    };

    $scope.shift = shift;
    $scope.category = category;
    $scope.subcategory = subcategory;
    $scope.linkedTaskMap = OpsBoardRepository.getLinkedTaskMap();
    $scope.tasks = OpsBoardRepository.getTasks();
     
    var typeIcon1 = false;
    var typeTitle1 = '';
    var typeIconClass1 = '';
    var typeClass1 = '';
    var typeIcon2 = false;
    var typeTitle2 = '';
    var typeIconClass2 = '';
    var typeClass2 = '';
    var mutliroutes = [];
    // unique supervisors using id as comparator
    var supervisors = _.unique(task.taskSupervisorAssignments, true, function (val) {
      return val.personId;
    });

    if(task.assignedPerson1 && task.assignedPerson1.type) {
      if (task.assignedPerson1.type.match(/OVERTIME/gi)) {
        typeIcon1 = true;
        typeTitle1 = 'Overtime';
        typeIconClass1 = 'fa-clock-o';
        typeClass1 = 'edit-task-personnel1-overnext overtime';
      }

      if (task.assignedPerson1.type.match(/NEXT_DAY/gi)) {
        typeIcon1 = true;
        typeTitle1 = 'Next Day';
        typeIconClass1 = 'fa-calendar-o';
        typeClass1 = 'edit-task-personnel1-overnext nextday';
      }

      if (task.assignedPerson1.type.match(/OTHER/gi)) {
        typeIcon1 = true;
        typeTitle1 = 'Diversion';
        typeIconClass1 = 'fa-share-square-o';
        typeClass1 = 'edit-task-personnel1-overnext diversion';
      }
    }

    if(task.assignedPerson2 && task.assignedPerson2.type) {
      if (task.assignedPerson2.type.match(/OVERTIME/gi)) {
        typeIcon2 = true;
        typeTitle2 = 'Overtime';
        typeIconClass2 = 'fa-clock-o';
        typeClass2 = 'edit-task-personnel1-overnext overtime';
      }

      if (task.assignedPerson2.type.match(/NEXT_DAY/gi) || task.assignedPerson2.type.match(/the next day/gi)) {
        typeIcon2 = true;
        typeTitle2 = 'Next Day';
        typeIconClass2 = 'fa-calendar-o';
        typeClass2 = 'edit-task-personnel1-overnext nextday';
      }

      if (task.assignedPerson2.type.match(/OTHER/gi)) {
        typeIcon2 = true;
        typeTitle2 = 'Diversion';
        typeIconClass2 = 'fa-share-square-o';
        typeClass2 = 'edit-task-personnel1-overnext diversion';
      }
    }

    mutliroutes = $filter('multiRouteGroup')($scope.linkedTaskMap, task.groupId, $scope.tasks, task, durations);

    $scope.opData = {
      titleAction : 'Task Details',
      cancelButtonText : 'Cancel',
      submitButtonText : 'Save',
      clientErrors : [],
      errors : [],
      required : [],
      taskNameMaxLength: 10,
      taskCommentMaxLength: 150,
      progress : 100,
      taskdata: task,
      taskname: task.taskName,
      comments: task.comments,
      startdate: new Date(parseInt(task.startDate)),
      enddate: new Date(parseInt(task.endDate)),
      section: section,
      typeIcon1: typeIcon1,
      typeTitle1: typeTitle1,
      typeIconClass1: typeIconClass1,
      typeClass1: typeClass1,
      typeIcon2: typeIcon2,
      typeTitle2: typeTitle2,
      typeIconClass2: typeIconClass2,
      typeClass2: typeClass2,
      mutliroutes: mutliroutes,
      assigned: {
        supervisors: supervisors,
        equipment: equipments[task.assignedEquipment.equipmentId || (task.assignedEquipment.equipment ? task.assignedEquipment.equipment.name:null)],
        person1: persons[task.assignedPerson1.personId],
        person2: persons[task.assignedPerson2.personId]
      }

    };
   
    
    var containsSections = false;
    var sectionid = null;

    if(section) {
      $scope.opData.sectionname = 'Section ' + section.sectionName;
      $scope.opData.sectionmarker = true;
      containsSections = true;
      sectionid = section.sectionId;
    } else {
      $scope.opData.sectionname = '';
      $scope.opData.sectionmarker = false;
    }

    var modalInstance = $modal.open({
      templateUrl : appPathStart + '/views/modals/modal-edit-task',
      controller : 'ModalCtrl',
      backdrop : 'static',
      size : 'lg',
      resolve : {
        data : function() {
          return task;
        }
      },
      scope : $scope
    });

    $scope.operation = function(success, error) {

      $scope.submitted = true;
      $scope.opData.clientErrors = []; // clear errors
      $scope.opData.required = [];

      if (_.isEmpty($scope.opData.taskname)) {
        $scope.opData.clientErrors.push({ message: 'Task Description cannot be blank.' });
      }

      // Task end time must be greater than start time
      if ($scope.opData.startdate.getTime() >= $scope.opData.enddate.getTime()) {
        $scope.opData.clientErrors.push({ message: 'Task end time must be greater than start time.'});
      }
      
      if ($scope.opData.clientErrors.length > 0) {
        return error(new ClientSideError('ValidationError'));
      }

      task.taskName = $scope.opData.taskname;
      task.comments = $scope.opData.comments;
      task.startDate = $scope.opData.startdate.getTime();
      task.endDate = $scope.opData.enddate.getTime();

      // Save edited task

      var actions = new Array();


      // task, locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId

      OpsBoardRepository.updateTask(task, shift.id, category.id, subcategory.id, null, location.location.code, shift.shift.id, category.category.id, subcategory.subcategory.id, containsSections, task.id, sectionid, success, error);

      modalInstance.close();
    };

    modalInstance.result.then(function(selectedItem) {
      $scope.selected = selectedItem;
    }, function() {
      $log.info('Modal dismissed at: ' + new Date());
    });
  };

  $scope.unassignEquipment = function(equipment, task, locationId) {
    return OpsBoardRepository.unassignEquipmentFromTask(equipment, task, locationId);
  };

  $scope.removeShift = function(locationId, locationShiftId, shiftId){
    $scope.$emit('DELETE-SHIFT',{locationId: locationId, locationShiftId: locationShiftId, shiftId: shiftId});
  };

  $scope.deleteTaskFromSection = function (task, section, subcategory, category, shift, location, locationId){
    $scope.$emit('DELETE-TASK-FROM-SECTION',{task: task, section: section, subcategory: subcategory, category: category, shift: shift, location: location, locationId: locationId});
  };

  $scope.deleteTaskFromSubCategory = function (task, subcategory, category, shift, location, locationId){
    $scope.$emit('DELETE-TASK-FROM-SUBCATEGORY',{task: task, subcategory: subcategory, category: category, shift: shift, location: location, locationId: locationId});

  };

  $scope.deleteSubcategory = function (subcategory, category, shift, location, locationId){
    $scope.$emit('DELETE-SUBCATEGORY',{subcategory: subcategory, category: category, shift: shift, location: location, locationId: locationId});
  };

  $scope.deleteSection = function (section, subcategory, category, shift, location, locationId) {
    $scope.$emit('DELETE-SECTION',{section: section, subcategory: subcategory, category: category, shift: shift, location: location, locationId: locationId});

  };

  $scope.addToGroup = function (task, subcategory, category, shift, location, locationId, section) {
    $scope.$emit('ADD-TASK-TO GROUP',{task: task, subcategory: subcategory, category: category, shift: shift, location: location, locationId: locationId, section: section});
  }

  $scope.decideAssignmentType = function(task, type, position, shiftId, elementId, locationId){
    $scope.$emit('DECIDE-ASSIGNMENT-TYPE',{task: task, type: type, position: position,  shiftId: shiftId, elementId: elementId, locationId: locationId});
  };

});
