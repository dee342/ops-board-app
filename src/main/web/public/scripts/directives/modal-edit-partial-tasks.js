'use strict';

angular
  .module('OpsBoard')
  .directive(
    'modalEditPartialTasks', ['$window', '$modal', 'durations', 'OpsBoardRepository', 'BoardValueService',
      function($window, $modal, durations, OpsBoardRepository, BoardValueService) {
        return {
          restrict: 'A',
          scope: {
            linkedtaskmap: '=',
            groupid: '=',
            tasks: '='
          },
          
          link: function(scope, element, attrs) {
            var self = this;
            scope.showWarning = false;
            scope.opData = {
              titleAction: 'Partial Tasks',
              resetButtonText: 'Reset',
              cancelButtonText: 'Cancel',
              submitButtonText: 'Confirm',
              warning: 'Partial tasks will be unlinked. Please confirm.',
              routes: [],
              hasCompletedTask: false
            };

            function getRoutes () {
              scope.opData.routes = [];
              var linkedtaskmap = OpsBoardRepository.getLinkedTaskMap();
              var tasks = BoardValueService.tasks;

              for (var i = 0; i < linkedtaskmap.length; i++) {
                if (linkedtaskmap[i].groupId === scope.groupid) { // will change this late to push it to array
                  var mappedTask = linkedtaskmap[i];
                  
                  var category = tasks.locations[linkedtaskmap[i].locationId].locationShifts[linkedtaskmap[i].locationShiftId].shiftCategories[linkedtaskmap[i].shiftCategoryId],
                    subcategory = category.subcategoryTasks[linkedtaskmap[i].subcategoryTaskId],
                    task;
                  var label = category.category.name + " - " + subcategory.subcategory.name + "/ ";

                  if (subcategory.subcategory.containsSections) {
                    task = subcategory.sections[linkedtaskmap[i].sectionId].tasks[linkedtaskmap[i].taskId]
                    label = label + subcategory.sections[linkedtaskmap[i].sectionId].sectionName + " / ";
                  } else {
                    task = subcategory.tasks[linkedtaskmap[i].taskId]
                  }
                  label = label + task.taskName

                  var duration;
                  for (var j = 0; j < durations.length; j++) {
                    if (durations[j].duration == task.hours) {
                      duration = durations[j].label + " (" + durations[j].duration + "h)"
                      break;
                    }
                  }

                  // disable unlinking when linked task has been completed
                  if (task.isCompleted()) {
                    scope.opData.hasCompletedTask = true;
                  }

                  scope.opData.routes.push({
                    taskId: mappedTask.taskId,
                    sequence: task.partialTaskSequence,
                    label: label,
                    duration: duration,
                    groupId: mappedTask.groupId,
                    locationId: mappedTask.locationId,
                    locationShiftId: mappedTask.locationShiftId,
                    shiftCategoryId: mappedTask.shiftCategoryId,
                    subcategoryTaskId: mappedTask.subcategoryTaskId,
                    containsSections: mappedTask.containsSections,
                    sectionId: mappedTask.sectionId,
                    linkedTaskChildId: mappedTask.linkedTaskChildId,
                    linkedTaskParentId: mappedTask.linkedTaskParentId
                  })
                }
              }
            }

            element.on('click', function (event) {
              scope.showWarning = false;
              getRoutes();
              var modalInstance = $modal.open({
                templateUrl : appPathStart + '/views/modals/modal-edit-partial-tasks',
                controller : function ($scope, $modalInstance) {
                  $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                  }
                  
                  $scope.reset = function () {
                    scope.showWarning = true;
                  };

                  $scope.confirm = function () {
                    OpsBoardRepository.unlinkPartialTasks($scope.opData.routes)
                    $modalInstance.close();
                  };
            
                },
                windowClass: 'multi-routes-modal',
                backdrop : 'static',
                resolve : {
                  data : function() {
                    return []
                    }
                  },
                scope : scope
              });
            })
          }
        };
      }
    ]);