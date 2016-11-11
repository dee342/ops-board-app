(function() {

    'use strict';

    // checks if a person/equipment is already in the target spot
    function dropTargetIsEmpty (obj) {
        return !obj;
    };

    function assignToTaskByDragAndDrop ($scope, OpsBoardRepository, newTask, type, newPosition, elementId, locationId, context, taskIndicator, oldTask, oldPosition) {

        var applyFn = false;

        oldPosition = parseInt(oldPosition, 10);

        if (newTask) {
            if (type === 'equipment') {
                if (dropTargetIsEmpty(newTask.assignedEquipment.equipmentId)) {
                    applyFn = function () {
                        if (!newTask.groupId) {
                            // move when an old task exists
                            if (oldTask) {
                                OpsBoardRepository.unassignAndAssignEquipmentFromTaskToTask(elementId, oldTask, locationId, newTask);
                            } else {
                                OpsBoardRepository.assignEquipmentToTask(newTask, elementId);
                            }
                        } else if (!oldTask) {
                            // equipment from tasks to partial tasks is not supported
                            OpsBoardRepository.assignEquipmentToPartialTask(newTask, elementId, locationId);
                        }
                    }
                }
            } else if (type === 'supervisor-task') {
                applyFn = function () {
                    if (!oldTask) {
                        OpsBoardRepository.assignSupervisorToTask(newTask, elementId, taskIndicator);
                    }
                }
            } else if (type === 'supervisor-section') {
                applyFn = function () {
                    OpsBoardRepository.assignSupervisorToSection(newTask, elementId, taskIndicator);
                }                
            } else if (type === 'supervisor-subcategory') {
                applyFn = function () {
                    OpsBoardRepository.assignSupervisorToSubcategory(newTask, elementId, taskIndicator);
                }
            } else if (dropTargetIsEmpty(newTask['assignedPerson' + newPosition].personId)) {
                applyFn = function () {
                    if (!newTask.groupId) {
                        // move when an old task exists
                        if (oldTask) {
                            OpsBoardRepository.unassignAndAssignPersonFromTaskToTask(elementId, oldTask, oldPosition, newPosition, locationId, newTask);
                        } else {
                            OpsBoardRepository.assignPersonToTask(newTask, elementId, newPosition);
                        }
                    } else {
                        OpsBoardRepository.assignPersonToPartialTask(newTask, elementId, newPosition, locationId);
                    }
                }
            }

            if (typeof applyFn === 'function') {
                if ($scope.$$phase) { // most of the time it is "$digest"
                    applyFn();
                } else {
                    $scope.$apply(applyFn);
                }
            }

        }
    };

    window.assignToTaskByDragAndDrop = assignToTaskByDragAndDrop;

}());