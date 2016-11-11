'use strict';

angular.module('OpsBoard')
  .controller('TasksSettingsCtrl', function($scope, $filter, $modal, $log, $controller, $rootScope, OpsBoardRepository, InfoService, UUIDGenerator, ClientSideError, UtilityService, BoardValueService, BoardDataService, CategoryDataService, $timeout) {

    $scope.shiftData = BoardDataService.getShiftData();
    $scope.categoryData = BoardDataService.getCategoryData();
    $scope.equipment = OpsBoardRepository.getEquipment();
    $scope.personnel = OpsBoardRepository.getPersonnel();
    $scope.sortedCategoryData = $scope.categoryData;
    $scope.activeLocation = '';

    var taskSettingsLocations = [];
    var taskSettingsPanel = [];
    var taskSettingsPanelTemp = [];


    $scope.spinner = false;
    $scope.spinnerSelect = false;

    // locations for the tabs
   
    if (OpsBoardRepository.isBoroBoard()) {
      var loc = BoardDataService.getBoardData().boardLocation, locations = {};
      locations[loc] = $scope.tasks.locations[loc];
      taskSettingsLocations = $filter('orderDistrict')(locations, BoardDataService.getBoardData().boardLocation, 'code');
    } else {
      taskSettingsLocations = $filter('orderDistrict')($scope.tasks.locations, BoardDataService.getBoardData().boardLocation, 'code');
    }

    var _taskSettingsShiftButtons = function() {
      var taskSettingsShifts = [];
      var firstShift = {};
      var shiftId = '';
      var shifts = {};
      var sequence = 0;

      if (OpsBoardRepository.isBoroBoard()) {
        taskSettingsLocations = $filter('orderDistrict')(locations, BoardDataService.getBoardData().boardLocation, 'code');
      } else {
        taskSettingsLocations = $filter('orderDistrict')($scope.tasks.locations, BoardDataService.getBoardData().boardLocation, 'code');
      }

      _.each(taskSettingsLocations, function(location) {
        //LOCATION  **************************

        if($scope.activeLocation === '') {
          $scope.activeLocation = location.location.code;
        }

        if(location.location.code === $scope.activeLocation) {

        shifts = $filter('orderByShiftId')(location.locationShifts);

        _.each(shifts, function (shift) {

            if(!firstShift.location) {
              firstShift.location = location;
              firstShift.shift = shift;
              shiftId = shift.id;
            }

            sequence++;
            shift.sequence = sequence;
            taskSettingsShifts.push(shift);
          });

        }

      });

      return {
        shiftId: shiftId,
        taskSettingsShifts: taskSettingsShifts
      };
    };

    var _activeShift = function(shiftId) {

      _.each(taskSettingsLocations, function(location) {
        //LOCATION  **************************

        if(location.location.code === $scope.activeLocation) {
          taskSettingsPanel[0] = {};
          taskSettingsPanel[0].active = true;
          taskSettingsPanel[0].id = location.location.id;
          taskSettingsPanel[0].locationCode = location.location.code;
          taskSettingsPanel[0].location = location.location;
          taskSettingsPanel[0].locationShifts = {};
          taskSettingsPanelTemp[0] = {};
          taskSettingsPanelTemp[0].active = true;
          taskSettingsPanelTemp[0].id = location.location.id;
          taskSettingsPanelTemp[0].locationCode = location.location.code;
          taskSettingsPanelTemp[0].location = location.location;
          taskSettingsPanelTemp[0].locationShifts = {};

          _.each(location.locationShifts, function (shift) {
            if (shift.id === shiftId) {
              //SELECTED SHIFT  **************************
              taskSettingsPanel[0].locationShifts[shift.id] = shift;
            }
          });
        }
      });

      $scope.taskSettingsPanel = taskSettingsPanel;
      $scope.taskSettingsLocations = taskSettingsLocations;
      $scope.spinner = false;

    };

    $rootScope.$on('UPDATE-TASK-SETTINGS-PANE', function taskSettingsShiftButtons(event, data) {
      $scope.spinnerSelect = false;
      var shiftObject = _taskSettingsShiftButtons();
      $scope.taskSettingsShifts = shiftObject.taskSettingsShifts;

      if(data.shiftId === '') {
        data.shiftId = shiftObject.shiftId;
      }
      _setActiveShift(data.shiftId);

      if(shiftObject.taskSettingsShifts.length > 0 && data.shiftId === '') {
        $scope.spinnerSelect = true;
      }

    });

    var _setActiveLocation = function(locationId) {
      $scope.activeLocation = locationId;
      var shiftObject = _taskSettingsShiftButtons();
      var shiftId = shiftObject.shiftId;
      _setActiveShift('');
      $scope.taskSettingsShifts = shiftObject.taskSettingsShifts;
    };

    var _setActiveShift = function(shiftId) {

      $scope.taskSettingsPanel = taskSettingsPanelTemp;
      $timeout(function() {
        _activeShift(shiftId);
      }, 0);

      $scope.activeShift = shiftId;
    };


    $scope.setActiveLocation = function(locationId) {
      $scope.spinnerSelect = true;
      _setActiveLocation(locationId);
    };

    $scope.setActiveShift = function(shiftId) {
      $scope.spinner = true;
      $scope.spinnerSelect = false;
      _setActiveShift(shiftId);
    };

    var shiftObject = _taskSettingsShiftButtons();
    _setActiveShift('');
    //$scope.activeShift = shiftObject.shiftId;
    $scope.taskSettingsShifts = shiftObject.taskSettingsShifts;

    if(shiftObject.taskSettingsShifts.length > 0) {
      $scope.spinnerSelect = true;
    }


    $rootScope.$on('UPDATE-TASKS-SETTINGS', function () {
      var tasks = BoardValueService.tasks;
      var shift2 = {};
      var shiftCategories = {};
      var shiftcategory2 = {};
      var subcategorytasks = {};
      var categorytask2 = {};
      var task2 = {};
      var sections = {};
      var location2 = {};
      var tasksobject = [];
      var section2 = {};
      var taskCount = 0;

      _.each(tasks.locations, function(location) {
        //LOCATION  **************************
        _.each(location.locationShifts, function (shift) {
          //SHIFT  **************************
          _.each(shift.shiftCategories, function (shiftCategory) {
            //SHIFT CATEGORY  **************************
            _.each(shiftCategory.subcategoryTasks, function (subcategoryTask) {
              //REGULAR TASKS **************************
              taskCount = 0;
              _.each(subcategoryTask.tasks, function (task) {
                taskCount++;
              });

              subcategoryTask.numOfTasks = taskCount;

              _.each(subcategoryTask.sections, function (section) {
                //SECTION TASKS **************************
                taskCount = 0;
                _.each(section.tasks, function (task) {
                  taskCount++;
                });

                if(taskCount === 0) {
                  $scope.$emit('DELETE-SECTION',{section: section, subcategory: subcategoryTask, category: shiftCategory, shift: shift, location: location, locationId: location.location.code});
                }

                _.each(subcategoryTask.allSections, function (allTask) {
                  if(allTask.name === section.sectionName) {
                    allTask.numOfTasks = taskCount;
                  }
                });
              });
            });
          });
        });
      });
      $scope.tasks = tasks;

    });

    $scope.getCounter = function() {
      var arr = [];
      for (var i = 0; i <= 99; i++) {
        arr.push(i);
      }
      return arr;
    }

    function sorterPriceAsc(a,b) {
        return parseInt(a['sequence']) - parseInt(b['sequence']);
    }

    $scope.counter = $scope.getCounter();

    $scope.getCategory = function(category) {
      return (category.id, category);
    };

    $scope.getCategoryData = function() {
      return OpsBoardRepository.getCategoryData();
    };

    $scope.pushShift = function(locationId, selectedShift) {
      if (!selectedShift) return;
      var locationShiftId = UUIDGenerator.uuid();
      var s = $scope.shiftData[selectedShift.shiftId];
      var shift = InfoService.getShiftInfo(locationShiftId, s);
      var locationShiftIdOfExistingShift;
      var shiftExists = false;

      Object.keys($scope.tasks.locations[locationId].locationShifts).forEach(function(key){
        if($scope.tasks.locations[locationId].locationShifts[key].shift.id == shift.shift.id){
          shiftExists = true;
          locationShiftIdOfExistingShift = key;
        }
      });

      if(!shiftExists) {
        $scope.tasks.locations[locationId].locationShifts[locationShiftId] = shift;
        OpsBoardRepository.addShift(locationShiftId, locationId, selectedShift.shiftId);
      }
    };

    // resets all location shifts on active location
    $scope.clearSpecificLocation = function () {
      var locationId; // todo: move to service to get active location, see add-shift-controller.js
      angular.forEach($scope.tasks.locations, function (val) {
        if (val.active && val.locationCode) {
          locationId = val.locationCode;
        }
      });
      var showModalPopUp = true;
      
      if (showModalPopUp){
          $scope.opData = {
          titleAction : 'Delete All Tasks',
          cancelButtonText: 'No',
          submitButtonText: 'Yes',
          message: 'Are you sure you want to DELETE ALL tasks created?'
          };

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
            $scope.tasks.locations[locationId].locationShifts = {};
              OpsBoardRepository.clearSpecificLocation(locationId);
            }, function() {
              $log.info('Modal dismissed at: ' + new Date());
            });
      }
      
    };

    $scope.getMatchedCategory = function(name,categoryData){
        var category ;
        for(var i=0; i< categoryData.length; i++){
          if(categoryData[i].name == name){
            category= categoryData[i];
            break;
          }
        }
        return category;
       }

    $scope.updateCategoryModalPopUp = function(locationId, category, shiftId){

      if (UtilityService.numberOfObjectsInCollection(category.subcategoryTasks) > 0 ){
        var showModalPopUp = false;
        if (category.modalPopUp === undefined || category.modalPopUp.length == 0){
          category.modalPopUp = [];
          category.modalPopUp.push({
            locationId: locationId,
                categoryId: category.category.id,
                shiftId: shiftId,
            });
          showModalPopUp = true;
        }
        else{
          for (var i = 0; i < category.modalPopUp.length; i++) {
                var obj = category.modalPopUp[i];
                if (obj.locationId == locationId && obj.categoryId == category.category.id && obj.shiftId == shiftId ){
                  showModalPopUp = false;
                }
                else{
                category.modalPopUp.push({
                locationId: locationId,
                    categoryId: category.category.id,
                    shiftId: shiftId,
                });
              showModalPopUp = true;
                }
            }
        }

        if (showModalPopUp){
          $scope.opData = {
          titleAction : 'Replace Category',
          cancelButtonText: 'No',
          submitButtonText: 'Yes',
          headerType:  'striped'
          };

          $scope.passPreValidation = function () {
          return true
          }
          $scope.submitted = false;
          $scope.operation = function(success, error) {
              $scope.submitted = true;
              $scope.opData.clientErrors = [];
              $scope.opData.serverErrors = [];
              success();
          };
          $scope.cancelOperation = function(success, error) {
            $scope.submitted = true;
            $scope.opData.clientErrors = [];
            $scope.opData.serverErrors = [];
            for (var i = 0; i < category.modalPopUp.length; i++) {
              var obj = category.modalPopUp[i];
              if (obj.locationId == locationId && obj.categoryId == category.category.id && obj.shiftId == shiftId ) {
                category.modalPopUp.splice(i, 1)
                break;
              }
            }
            success();
          };
          var modalInstance = $modal.open({
          templateUrl : appPathStart + '/views/modals/modal-category-switches',
          controller : 'ModalCtrl',
          backdrop : 'static',
          resolve : {
              data : function() {
                return [$scope.opData];
                  }
          },
          scope : $scope
          });
          modalInstance.result.then(function(selectedItem) {
            $scope.selected = selectedItem;
            
            }, function() {
              $log.info('Modal dismissed at: ' + new Date());
            });
      }
      }
    }

    $scope.updateCategory = function(locationId, locationShiftId, shiftCategoryId, shiftId, category, newCategoryId){
        var oldCategory = $scope.tasks.locations[locationId].locationShifts[locationShiftId].addedCategories[shiftCategoryId];
        var categoryData = $scope.categoryData;
        
        var newCategory, catData;
        
        for(var i=0; i< categoryData.length; i++){
          if(categoryData[i].id == newCategoryId){
            newCategory = categoryData[i];
            catData = angular.copy(InfoService.getCategoryInfo(shiftCategoryId, categoryData[i]));
            break;
          }
        }



      $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId] = catData;
      CategoryDataService.updateReferenceCategories($scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories);
      OpsBoardRepository.updateCategory(locationId, shiftId, locationShiftId, shiftCategoryId, oldCategory, newCategory);


    };

   $scope.categoryFilter = function(data) {
    /*
    console.log('************************************************************')
    console.log($scope.sortedCategoryData)
    console.log('************************************************************')
    */
    
    return function(item) {
      //console.log('here')
       var found = false;
       if (data.category.category !== undefined){
          if (data.category.category.id == item.id) return true;


          if (data.shift.shiftCategories !== undefined){
             Object.keys(data.shift.shiftCategories).forEach(function(key) {

               if (data.shift.shiftCategories[key].category !== undefined){
                  if (item.id == data.shift.shiftCategories[key].category.id) found = true;
               }
          });
        }
        if (!found) return true;
        return false;
       }
     };
   };

    $scope.updateSubCategory = function(subcategory, locationId, locationShiftId, shiftCategoryId, categoryId, shiftId) {

      var subcategories = CategoryDataService.getFormattedSubcategories();
      var allSubcategories = CategoryDataService.getAllFormattedSubcategories();
      var found = false;
      var shiftCategory = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId];
      var allSubcategoryTasks = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].allsubcategoryTasks;

      if(shiftCategory.category.id !== categoryId || subcategories[subcategory.id].id !== subcategory.id) {
        _.each(allSubcategoryTasks, function(subcat) {
          if(subcategory && subcategory.id === subcat.id) {
            subcat.selected = false;
          }
        });

        $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: ''});
        return;
      }

      for (var i = 0; i < $scope.categoryData.length && !found; i++) {
        if ($scope.categoryData[i].id == categoryId && subcategory) {
          for (var j = 0; j < $scope.categoryData[i].subcategories.length && !found; j++) {
            if ($scope.categoryData[i].subcategories[j].id == subcategory.id) {
              if (subcategory.selected) {
                var subcategoryTaskId = UUIDGenerator.uuid(), subcategoryTasks = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks;
                subcategoryTasks[subcategoryTaskId] = angular.copy(InfoService.getSubcategoryInfo(subcategoryTaskId, $scope.categoryData[i].subcategories[j], $scope.tasks.locations[locationId].location.sectionIds)); //angular.copy($scope.categoryData[i].subcategories[j]);

                if (subcategoryTasks[subcategoryTaskId].subcategory.containsSections) {
                  OpsBoardRepository.addSubcategory(locationShiftId, shiftCategoryId, subcategoryTaskId, locationId, shiftId, categoryId, subcategory.id, true, null);
                } else {
                  var taskId = UUIDGenerator.uuid();
                  subcategoryTasks[subcategoryTaskId].tasks = {}
                  subcategoryTasks[subcategoryTaskId].tasks[taskId] = angular.copy(InfoService.getTaskInfo(taskId));
                  OpsBoardRepository.addSubcategory(locationShiftId, shiftCategoryId, subcategoryTaskId, locationId, shiftId, categoryId, subcategory.id, false, taskId);
                }
                found = true;
              } else { // When subcategory is unselected
                var subcategoryTaskId = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].addedSubcategories[subcategory.id.toString()];

                //TODO Sometimes subcategoryTaskId is undefined. WHY!!???

                if(subcategoryTaskId) {
                  if (!subcategory.containsSections) {
                    delete $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];
                    OpsBoardRepository.removeSubcategory(locationShiftId, shiftCategoryId, subcategoryTaskId, locationId, shiftId, categoryId, subcategory.id, true, null);
                  } else{
                    delete $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];
                    OpsBoardRepository.removeSubcategory(locationShiftId, shiftCategoryId, subcategoryTaskId, locationId, shiftId, categoryId, subcategory.id, false, taskId);
                  }
                } else {
                  subcategory.selected = true;
                }
              }
            }
          }
        }
      }
      $scope.updateScroll()
    }
/*
    $scope.updateSection = function (subcategory, section) {
      if(section.selected) {
        subcategory.sections[section.id] = InfoService.getSectionInfo(section);
      } else {
        delete subcategory.sections[section.id]
      }
    }
*/

    $scope.updateTasks = function (numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section) {

      //ADD SECTION TASKS
      if (subcategory.subcategory.containsSections) {
        var oldTasks = 0, originalSection;
        Object.keys(subcategory.sections).forEach(function (sectionId) {
          if (subcategory.sections[sectionId] && subcategory.sections[sectionId].sectionId === section.id) {
            oldTasks = Object.keys(subcategory.sections[sectionId].tasks).length;
            originalSection = subcategory.sections[sectionId];
          }
        });

        if (!originalSection) {
          $scope.addSection(numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section);
        } else {
          var sectionPartialTaskNum=0;

          var subSection=subcategory.sections[originalSection.id];
          Object.keys(subSection.tasks).forEach(function (taskId) {
            var sectionSubTask=subSection.tasks[taskId];
            if((sectionSubTask.linkedTaskChildId&&sectionSubTask.linkedTaskChildId!=null)||
              (sectionSubTask.linkedTaskParentId&&sectionSubTask.linkedTaskParentId!=null)){
              sectionPartialTaskNum++;

            }
          });


          if (numOfTasks === 0 && sectionPartialTaskNum===0) {
            $scope.removeSection(subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection);
          } else {
            if (numOfTasks > oldTasks) {
              $scope.addTasksToSection(numOfTasks-oldTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection);
            } else if (numOfTasks < oldTasks) {
              if(numOfTasks<sectionPartialTaskNum){
                numOfTasks=sectionPartialTaskNum;
              }
              $scope.removeTasksFromSection(oldTasks, numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection);
            }
          }
        }
      } else {

        //ADD CATEGORY (REGULAR) TASKS

        var oldTasks = Object.keys(subcategory.tasks).length;
        subcategory.numOfTasks = numOfTasks;
        if (numOfTasks > oldTasks) {
          $scope.addTasks(numOfTasks - oldTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId);
        } else if (numOfTasks < oldTasks) {
          var partialTaskNum=0;
          Object.keys(subcategory.tasks).forEach(function (taskId) {
            var subTask=subcategory.tasks[taskId];
            if((subTask.linkedTaskChildId&&subTask.linkedTaskChildId!=null)||
              (subTask.linkedTaskParentId&&subTask.linkedTaskParentId!=null)){
              partialTaskNum++;

            }
          })
          if(numOfTasks < partialTaskNum){
            numOfTasks = partialTaskNum;
          }
          $scope.removeTasks(oldTasks, numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId);
        }
      }

      $rootScope.$emit('UPDATE-TASKS-SETTINGS');

      $scope.updateScroll();
    };


    $scope.$on('UPDATE-TASKS-COUNT', function (event, data) {
      $scope.updateTasks(0, data.subcategory, data.location.locationCode, data.shift.id, data.category.id, data.subcategory.id, data.category.category.id, data.shift.shiftId, data.subcategory.subcategory.id, null);
    });

    $scope.removeSection = function (subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection) {
      var sectionTaskId = originalSection.id;
      Object.keys(originalSection.tasks).forEach(function (taskId) {
        var e = originalSection.tasks[taskId].assignedEquipment,
          p1 = originalSection.tasks[taskId].assignedPerson1,
          p2 = originalSection.tasks[taskId].assignedPerson2;
        if (e && e.id) {
          if($scope.equipment[e.id] != null){
          $scope.equipment[e.id].states[locationId] = 'Available';
          }
        }
        if (p1 && p1.id){
          if($scope.personnel[p1.id] != null){
            $scope.personnel[p1.id].states[locationId] = 'Available';
          }
        }
        if (p2 && p2.id){
          if($scope.personnel[p2.id] != null){
            $scope.personnel[p2.id].states[locationId] = 'Available';
          }
        }
      });
      delete subcategory.sections[originalSection.id]
      OpsBoardRepository.removeSection(locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, locationId, shiftId, categoryId, subcategoryId, section.id)

    }

    $scope.addSection = function (numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section) {
      var sectionExists = false;
      var sectionTaskIdOfExistingSection;
      var sectionTaskId = UUIDGenerator.uuid();
      var sectionTask = InfoService.getSectionInfo(sectionTaskId, numOfTasks, section);
      var taskIds = [];
      Object.keys($scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections)
        .forEach(function (key){
          if ($scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[key].sectionId == section.id){
            sectionExists = true;
            sectionTaskIdOfExistingSection = key;
          }
        });
      if(!sectionExists) {
        subcategory.sections[sectionTaskId] = sectionTask;
        for (var i = 0, taskId; i < numOfTasks; i++) {
          taskId = UUIDGenerator.uuid();
          taskIds.push(taskId);
          subcategory.sections[sectionTaskId].tasks[taskId] = InfoService.getTaskInfo(taskId);
          subcategory.sections[sectionTaskId].tasks[taskId].sequence = i+1;
        }
        OpsBoardRepository.addSection(locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, taskIds, locationId, shiftId, categoryId, subcategoryId, section.id);
      }
    };

    $scope.removeTasksFromSection = function (oldTasks, numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection) {
      var bottomArr = [],
        taskIdsToBeDeleted = [],
        sectionTaskId = originalSection.id,
        toBeDeleted = oldTasks - numOfTasks;
      Object.keys(originalSection.tasks).forEach(function (taskId) {
        var filterTask=originalSection.tasks[taskId];
        if(!((filterTask.linkedTaskChildId&&filterTask.linkedTaskChildId!=null)||
           (filterTask.linkedTaskParentId&&filterTask.linkedTaskParentId!=null))){
        var seq = originalSection.tasks[taskId].sequence;
        if (bottomArr.length < toBeDeleted) {
          taskIdsToBeDeleted.push(taskId);
          bottomArr.push(seq);
        } else {
          var k = bottomArr.indexOf(Math.min.apply(Math, bottomArr));
          if (bottomArr[k] < seq)  {
            bottomArr[k] = seq;
            taskIdsToBeDeleted[k] = taskId;
          }
        }
      }
      })
      for (var i = 0; i < taskIdsToBeDeleted.length; i++) {
        var taskIdToBeDeleted = taskIdsToBeDeleted[i];
        var e = originalSection.tasks[taskIdsToBeDeleted[i]].assignedEquipment,
          p1 = originalSection.tasks[taskIdsToBeDeleted[i]].assignedPerson1,
          p2 = originalSection.tasks[taskIdsToBeDeleted[i]].assignedPerson2;
        if (e && e.id) {
          if($scope.equipment[e.id] != null){
          $scope.equipment[e.id].states[locationId] = 'Available';
          }
        }
        if (p1 && p1.id){
          if($scope.personnel[p1.id] != null){
            $scope.personnel[p1.id].states[locationId] = 'Available';
          }
        }
        if (p2 && p2.id){
          if($scope.personnel[p2.id] != null){
            $scope.personnel[p2.id].states[locationId] = 'Available';
          }
        }
        delete section.tasks[taskIdToBeDeleted]
      }
      OpsBoardRepository.removeTasks(locationShiftId, shiftCategoryId, subcategoryTaskId, numOfTasks, locationId, shiftId, categoryId, subcategoryId, true, sectionTaskId, section.id)
    }

     $scope.addTasksToSection = function (numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId, section, originalSection) {
      if (!subcategory.sections[originalSection.id].tasks) subcategory.sections[originalSection.id].tasks = {};
      var sectionTaskId = originalSection.id;
      var sequence = 1,key;
    for(key in subcategory.sections[sectionTaskId].tasks){
      if(!subcategory.sections[sectionTaskId].tasks[key]['sequence']){
        subcategory.sections[sectionTaskId].tasks[key]['sequence'] = sequence;
      }else{
        sequence = subcategory.sections[sectionTaskId].tasks[key]['sequence'];
        subcategory.sections[sectionTaskId].tasks[key].sequence = sequence;
      }
      sequence++;
    }
      var taskIds = [];
      for (var i = 0; i < numOfTasks; i++) {
        var taskId = UUIDGenerator.uuid();
        taskIds.push(taskId);
        subcategory.sections[sectionTaskId].tasks[taskId] = InfoService.getTaskInfo(taskId);
        subcategory.sections[sectionTaskId].tasks[taskId]['sequence'] = sequence;
        sequence++;
      }
      OpsBoardRepository.addTask(locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, locationId, shiftId, categoryId, subcategoryId, true, sectionTaskId, section.id);
    }

    $scope.addTasks = function (numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId) {
      var sequence = 1, key;
      for(key in subcategory.tasks){
        if(!subcategory.tasks[key]['sequence']){
          subcategory.tasks[key]['sequence'] = sequence;
        } else {
          sequence = subcategory.tasks[key]['sequence'];
          subcategory.tasks[key].sequence = sequence;
        }
        sequence++;
      }

      var taskIds = [];

      for (var i = 0; i < numOfTasks; i++) {
        var taskId = UUIDGenerator.uuid();
        taskIds.push(taskId);
        subcategory.tasks[taskId] = InfoService.getTaskInfo(taskId);
        subcategory.tasks[taskId]['sequence'] = sequence;
        sequence++;
      }

      OpsBoardRepository.addTask(locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, locationId, shiftId, categoryId, subcategoryId, false, null, null);
    };

      $scope.removeTasks = function (oldTasks, numOfTasks, subcategory, locationId, locationShiftId, shiftCategoryId, subcategoryTaskId, categoryId, shiftId, subcategoryId) {
        var bottomArr = [],
          taskIdsToBeDeleted = [],
          toBeDeleted = oldTasks - numOfTasks;
        Object.keys(subcategory.tasks).forEach(function (taskId) {
          var filterTask=subcategory.tasks[taskId];
          if(!((filterTask.linkedTaskChildId&&filterTask.linkedTaskChildId!=null)||
            (filterTask.linkedTaskParentId&&filterTask.linkedTaskParentId!=null))){
          var seq = subcategory.tasks[taskId].sequence;
          if (bottomArr.length < toBeDeleted) {
            taskIdsToBeDeleted.push(taskId);
            bottomArr.push(seq);
          } else {
            var k = bottomArr.indexOf(Math.min.apply(Math, bottomArr));
            if (bottomArr[k] < seq)  {
              bottomArr[k] = seq;
              taskIdsToBeDeleted[k] = taskId;
            }
          }
        }
      });

        for (var i = 0; i < taskIdsToBeDeleted.length; i++) {
            var taskIdToBeDeleted = taskIdsToBeDeleted[i];
          var e = subcategory.tasks[taskIdToBeDeleted].assignedEquipment,
            p1 = subcategory.tasks[taskIdToBeDeleted].assignedPerson1,
            p2 = subcategory.tasks[taskIdToBeDeleted].assignedPerson2;
          if (e && e.id) {
            if($scope.equipment[e.id] != null){
            $scope.equipment[e.id].states[locationId] = 'Available';
            }
          }
          if (p1 && p1.id){
            if($scope.personnel[p1.id] != null){
              $scope.personnel[p1.id].states[locationId] = 'Available';
            }
          }
          if (p2 && p2.id){
            if($scope.personnel[p2.id] != null){
              $scope.personnel[p2.id].states[locationId] = 'Available';
            }
          }
          delete subcategory.tasks[taskIdToBeDeleted]
        }
        OpsBoardRepository.removeTasks(locationShiftId, shiftCategoryId, subcategoryTaskId, numOfTasks, locationId, shiftId, categoryId, subcategoryId, false, null, null);
      };


      $scope.addCategory = function (shift, locationId, locationShiftId) {

        if (!shift) {
          return;
        }
        var catData, found = false, shiftCategoryId = UUIDGenerator.uuid();

        for (var j = 0; j < $scope.categoryData.length; j++) {
          if (!$scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories) {
            catData = angular.copy(InfoService.getCategoryInfo(shiftCategoryId, $scope.categoryData[j])); //angular.copy($scope.categoryData[j], catData);
            break;
          } else {
            var shiftCategories = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories, found = false;

            Object.keys(shiftCategories).forEach(function (key) {
              if (shiftCategories[key].category.id == $scope.categoryData[j].id) {
                found = true;
              }
            });

            if (!found) {
              catData = angular.copy(InfoService.getCategoryInfo(shiftCategoryId, $scope.categoryData[j])); //angular.copy($scope.categoryData[j], catData);
              break;
            }
          }
        }

        if (!catData) {
          return; //Will disable add category button here;
        }

        var shiftCategories = $scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories;

        if (!shiftCategories) {
          shiftCategories = {};
        }

        var categoryExists = false,
        idOfExistingCategory;

        Object.keys(shiftCategories).forEach(function(key){
          if(shiftCategories[key].category.id == catData.id){
            categoryExists = true;
            idOfExistingCategory = key;
          }

        });

        if(!categoryExists){
          shiftCategories[shiftCategoryId] = catData;
          CategoryDataService.updateReferenceCategories($scope.tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories);
          OpsBoardRepository.addCategory(locationShiftId, shiftCategoryId, locationId, shift.shift.id, catData.category.id);
        }

        $scope.updateScroll();
        
      };

      $scope.updateScroll = function () {
        $('.scroller').perfectScrollbar('update')
      };

      var locals = {
        $scope: $scope,
        $filter: $filter,
        $modal: $modal,
        $log: $log,
        OpsBoardRepository: OpsBoardRepository,
        ClientSideError: ClientSideError
      }

      $controller('AddShift', locals);

    var locals = {
      $scope: $scope,
      $filter: $filter,
      $modal: $modal,
      $log: $log,
      OpsBoardRepository: OpsBoardRepository,
      ClientSideError: ClientSideError
    }

      $controller('MultiRoutes', locals);
  });