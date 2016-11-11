'use strict';

angular.module('taskFilters', [])
  .filter('isBinLoadUpdated',
  function($filter) {
    return function(task, equipment) {
      var code = '';
      if (equipment && task && task.assignedEquipment) {
        if (task.assignedEquipment.completed) {
          code = 'G';
        } else  if ($filter('isEquipmentDressed')(equipment)) {
          code = 'S';
        }
      }
      return code;
    }
  }).filter('getSupervisorTitle',
  function($filter) {
    return function(type, subcategory, section, task) {
      if (!type) {
        return;
      }

      if (!task && !section && !subcategory) {return;}

      if (type === 'subcategory') {
          if (!subcategory || !subcategory.supervisorAssignments || subcategory.supervisorAssignments.length === 0) {return;}
          if (subcategory.supervisorAssignments.length > 1) {return 'MULT';}
          return subcategory.supervisorAssignments[0].taskIndicator;
        }
      
      if (type === 'section') {
        if (!section || !section.sectionSupervisorAssignments || section.sectionSupervisorAssignments.length === 0) {return;}
        if (section.sectionSupervisorAssignments.length > 1) {return 'MULT';}
        return section.sectionSupervisorAssignments[0].taskIndicator;
      }

      if (type === 'task') {
        var indicator;
        if (!task || !task.taskSupervisorAssignments || task.taskSupervisorAssignments.length === 0) {
          return ;
        }
        if (subcategory && subcategory.supervisorAssignments && subcategory.supervisorAssignments.length !== 0) {
            if (subcategory.supervisorAssignments.length !== task.taskSupervisorAssignments.length) {
              indicator = 'MULT'
            } else {
              for (var i = 0; i < task.taskSupervisorAssignments.length; i++) {
                var found = false;
                for (var j = 0; j < subcategory.supervisorAssignments.length; j++) {
                  if (task.taskSupervisorAssignments[i].taskIndicator === subcategory.supervisorAssignments[j].taskIndicator) {
                    found = true;
                  }
                }
                if (found) {
                  indicator = task.taskSupervisorAssignments[i].taskIndicator;
                }
              }
            }
            return indicator;
        } else if (section && section.sectionSupervisorAssignments && section.sectionSupervisorAssignments.length !== 0) {
          if (section.sectionSupervisorAssignments.length !== task.taskSupervisorAssignments.length) {
            indicator = 'MULT'
          } else {
            for (var i = 0; i < task.taskSupervisorAssignments.length; i++) {
              var found = false;
              for (var j = 0; j < section.sectionSupervisorAssignments.length; j++) {
                if (task.taskSupervisorAssignments[i].taskIndicator === section.sectionSupervisorAssignments[j].taskIndicator) {
                  found = true;
                }
              }
              if (found) {
                indicator = task.taskSupervisorAssignments[i].taskIndicator;
              }
            }
          }
          return indicator;
        } else {
          if (task.taskSupervisorAssignments.length > 1) {
            return "MULT";
          }
          return task.taskSupervisorAssignments[0].taskIndicator;
        }
        return indicator;
      }
    }
  }).filter('getContext',
  function() {
    return function(id) {
      if (id === 1) {
        return 'supervisor-assignment';
      } else {
        return 'task-assignment';
      }
      return;
    }
  }).filter('canRemovePerson',
  function() {
    return function(task, position) {
      if (!task) return;
      if(position === 1)
        if (!task.assignedPerson1 || !task.assignedPerson1.person) return "";
      if(position === 2)
        if (!task.assignedPerson2 || !task.assignedPerson2.person) return "";
      if(position === 3)
        if (!task.taskSupervisorAssignments || task.taskSupervisorAssignments.length === 0) return "";
      if (task.groupId && task.partialTaskSequence !== 1 && position !== 3) return "";
      return true;
    }
  }).filter('canRemoveEquipment',
  function() {
    return function(task) {
      if (!task || !task.assignedEquipment || !task.assignedEquipment.equipmentId) {
        return "";
      }

      if (task.homogeneous && task.partialTaskSequence !== 1) {
        return "";
      }

      return true;
    }
  }).filter('canRemoveSectionSupervisor',
  function() {
    return function(section) {
      if (!section || !section.sectionSupervisorAssignments || section.sectionSupervisorAssignments.length === 0) return "";
      return true;
    }
  }).filter('canRemoveSubcategorySupervisor',
      function() {
        return function(subcategory) {
          if (!subcategory || !subcategory.supervisorAssignments || subcategory.supervisorAssignments.length === 0) return "";
          return true;
        }  
  }).filter('alreadyDetachedFilter',
  function() {
    return function(person) {
      if(person && person.state=='Detached') {
        return true;
      }
      return false;
    }
  }).filter('canDeleteLocation',
  function() {
    return function(location) {
      if(location && location.locationShifts) {
        return (location && Object.keys(location.locationShifts).length > 0);
      }
      return;
    }
  }).filter('formatTask',
  function($filter, BoardValueService) {
    return function(task2, section2, shiftcategory2, equipment, personnel) {
      var tasks = BoardValueService.tasks;
      var equipment, personnel;
      if (Object.prototype.toString.call(equipment) === "[object Object]" && Object.keys(equipment).length > 0) {
        equipment = equipment
      } else {
        equipment = BoardValueService.equipment;
      }

      if (Object.prototype.toString.call(personnel) === "[object Object]" && Object.keys(personnel).length > 0) {
        personnel = personnel
      } else {
        personnel = BoardValueService.personnel;
      }

      var locationscount =  Object.keys(tasks.locations).length;
      var canRemove = ['','','',''];
      var alreadyDetached1 = false;
      var alreadyDetached2 = false;
      var supObject = {};
      var EquipObj = {};
      var PersonnelObj1 = {};
      var PersonnelObj2 = {};
      var titlemapSup = '';
      var titlemap1 = '';
      var titlemap2 = '';
      var completedPersonAssignment1 = '';
      var completedPersonAssignment2 = '';
      var binLoadUpdated = false;
      var supervisorTitle = '';
      var categorycontext = '';

      if(task2 && task2.taskSupervisorAssignments && task2.taskSupervisorAssignments.length > 0) {
        supObject = personnel[task2.taskSupervisorAssignments[0].personId];

        supervisorTitle = $filter('getSupervisorTitle')('task', null, null, task2);
      } else {
        supObject = {};
        supervisorTitle = 'S';
      }

      if(task2.assignedEquipment && task2.assignedEquipment.equipmentId) {
        canRemove[0] = true;
        EquipObj = equipment[task2.assignedEquipment.equipmentId];
      } else {
        EquipObj = {name: 'E'};
      }

      if(task2.taskSupervisorAssignments && task2.taskSupervisorAssignments.length > 0) {
        titlemapSup = $filter('getMappedTitle')(personnel[task2.taskSupervisorAssignments[0].personId].civilServiceTitle);
        canRemove[3] = true;
      } else {
        titlemapSup = '';
      }

      if (task2.assignedPerson1.personId) {
        if (task2.assignedPerson1.completed) {
          completedPersonAssignment1 = 'completed';
        }
        titlemap1 = $filter('getMappedTitle')(personnel[task2.assignedPerson1.personId].civilServiceTitle);
        canRemove[1] = true;
        alreadyDetached1 = $filter('alreadyDetachedFilter')(personnel[task2.assignedPerson1.personId]);
        PersonnelObj1 = personnel[task2.assignedPerson1.personId];

      } else {
        titlemap1 = '';
        PersonnelObj1 = {formattedName: 'P'};
      }

      if(task2.assignedPerson2.personId) {
        if (task2.assignedPerson2.completed) {
          completedPersonAssignment2 = 'completed';
        }
        titlemap2 = $filter('getMappedTitle')(personnel[task2.assignedPerson2.personId].civilServiceTitle);
        canRemove[2] = true;
        alreadyDetached2 = $filter('alreadyDetachedFilter')(personnel[task2.assignedPerson2.personId]);
        PersonnelObj2 = personnel[task2.assignedPerson2.personId];
      } else {
        titlemap2 = '';
        PersonnelObj2 = {formattedName: 'P'};
      }

      binLoadUpdated = $filter('isBinLoadUpdated')(task2, equipment[task2.assignedEquipment.equipmentId]);

      task2.isLocked = $filter('isLocked')();
      task2.isEquipLocked = $filter('isEquipLocked')(task2.endDate);
      task2.supObject = supObject;
      task2.supervisorTitle = supervisorTitle;
      task2.EquipObj = EquipObj;
      task2.titlemapSup = titlemapSup;
      task2.canRemove = canRemove;
      task2.titlemap1 = titlemap1;
      task2.alreadyDetached1 = alreadyDetached1;
      task2.PersonnelObj1 = PersonnelObj1;
      task2.titlemap2 = titlemap2;
      task2.alreadyDetached2 = alreadyDetached2;
      task2.PersonnelObj2 = PersonnelObj2;
      task2.binLoadUpdated = binLoadUpdated;
      task2.completedPersonAssignment1 = completedPersonAssignment1;
      task2.completedPersonAssignment2 = completedPersonAssignment2;

      if(shiftcategory2) {
        task2.categorycontext = $filter('getContext')(shiftcategory2.category.id);
        task2.categorynamelowercase = shiftcategory2.category.name.toLowerCase();
      }
      return task2;
    }
  })
  .filter('getPartialTasks', function () {
    return function (task, count) {
      var results = _.toArray(task.tasks).filter(function (val) {
        return val.groupId;
      });
      return count ? results.length : results;
    };
  })
  .filter('isLocked',
  function(BoardValueService) {
    return function() {
      if (moment().isAfter(moment(BoardValueService.boardEndDate).add(2, 'days'))) {
        return true;
      } else {
        return false;
      }
    }
  })
  .filter('isEquipLocked',
  function(BoardValueService) {
	    return function(taskEnd) {
	      if (moment().isAfter(moment(BoardValueService.boardEndDate).add(2, 'days')) || (taskEnd < (new Date()).getTime() + 3600*1000)) {
	        return true;
	      } else {
	        return false;
	      }
	    }
	  });
