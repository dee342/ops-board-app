'use strict';

angular
    .module('OpsBoard')
    .service(
        'TaskSettingsService',
        function(InfoService, UUIDGenerator, TaskHelperService, TaskModel,  $filter, CategoryDataService, EquipmentHelperService, TaskIndicatorService, $rootScope) {

          var constructTaskSettingsCommand = function(commandName, clientSequence, boardLocation, boardDate,
              serviceLocationId, locationShiftId) {

            var command = {
              'commandName' : commandName,
              'clientSequence' : clientSequence++,
              'commandContent' : {
                'serviceLocationId' : serviceLocationId,
                'locationShiftId' : locationShiftId
              },
              'date' : boardDate,
              'location' : boardLocation
            }

            return command;
          };
          
          var constructTaskCommand = function(commandName, clientSequence, boardLocation, boardDate, taskKey) {

            var command = {
              'commandName' : commandName,
              'clientSequence' : clientSequence++,
              'commandContent' : {
                'taskId' : taskKey
              },
              'date' : boardDate,
              'location' : boardLocation
            };

            return command;
          };

          var getPersonKey = function(id) {
            if (id == null || typeof id == undefined || id == '')
              return;
            if (id.indexOf('_') < 0)
              return id;
            return id.split('_')[0];
          }

          var getEquipmentKey = function(id) {
            if (id == null || typeof id == undefined || id == '')
              return;
            if (id.indexOf('_') < 0)
              return id;
            return id.split('_')[0];
          }

          var unassignPersons = function(personnel, personnelIds, personnelAssignedStates) {
            if (personnelIds != null) {
              for (var k = 0; k < personnelIds.length; k++) {
                var personId = personnelIds[k].substring(0, personnelIds[k].indexOf("_"));
                var person = personnel[getPersonKey(personId)];
                if (person) {
                  person.assigned = personnelAssignedStates[personnelIds[k]].assigned;
                  person.assignedAnywhere = personnelAssignedStates[personnelIds[k]].assignedAnywhere;          
                  person.state = personnelAssignedStates[personnelIds[k]].state;
                  person = $filter('extendPerson')(person);
                }
              }
            }
          };

          var unassignEquipment = function(equipment, equipmentIds, equipmentAssignedStates) {
            var equipmentId = '';
            var e = {};
            if (equipmentIds != null) {
              _.each(equipmentIds, function(key) {
                equipmentId = key.substring(0, key.indexOf("_"));
                e = equipment[equipmentId];
                if (e) {
                  e.assigned = equipmentAssignedStates[key].assigned;
                  e.states[e.currentLocation] = equipmentAssignedStates[key].states[e.currentLocation];
                  e = EquipmentHelperService.addEquipmentProperties(e);
                }
             });
            }
          };

          var checkIfPersonAlreadyAssigned = function(personnel, personnelIds, assignedPersonMap) {
            if (personnelIds != null)
              for (var k = 0; k < personnelIds.length; k++) {
                var p = personnel[getPersonKey(personnelIds[k])];
                if (!assignedPersonMap[personnelIds[k]]) {
                  if (p && p.state)
                    p.state = 'Available'
                } else {
                  if (p && p.state)
                    p.state = 'Assigned'
                }
              }
          };

          return {

            createClearAllLocationsCommand : function(states, clientSequence, boardLocation, boardDate) {
              var command = {
                'commandName' : "ClearAllLocations",
                'clientSequence' : clientSequence++,
                'date' : boardDate,
                'location' : boardLocation
              }
              return command;
            },

            createPublishBoardCommand : function(states, clientSequence, boardLocation, boardDate) {
              var command = {
                'commandName' : "PublishBoard",
                'clientSequence' : clientSequence++,
                'date' : boardDate,
                'location' : boardLocation
              }
              return command;
            },
            createClearSpecificLocationCommand : function(states, clientSequence, boardLocation, boardDate,
                serviceLocationId) {
              var command = {
                'commandName' : "ClearSpecificLocation",
                'clientSequence' : clientSequence++,
                'commandContent' : {
                  'serviceLocationId' : serviceLocationId
                },
                'date' : boardDate,
                'location' : boardLocation
              }
              return command;
            },

            createAddShiftCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                serviceLocationId, shiftId) {
              var command = constructTaskSettingsCommand('AddShift', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftId = shiftId.toString();
              return command;
            },
          
            createRemoveShiftCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                serviceLocationId, shiftId) {
              var command = constructTaskSettingsCommand('RemoveShift', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftId = shiftId.toString();
              return command;
            },

            createAddCategoryCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                shiftCategoryId, serviceLocationId, shiftId, categoryId) {
              var command = constructTaskSettingsCommand('AddCategory', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              return command;
            },

            createRemoveCategoryCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                shiftCategoryId, serviceLocationId, shiftId, categoryId) {
              var command = constructTaskSettingsCommand('RemoveCategory', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.shiftId = shiftId;
              command.commandContent.categoryId = categoryId.toString();
              return command;
            },

            createUpdateCategoryCommand : function(states, clientSequence, boardLocation, boardDate, locationId,
                shiftId, locationShiftId, shiftCategoryId, oldCategory, newCategory) {
              var command = constructTaskSettingsCommand('UpdateCategory', clientSequence, boardLocation, boardDate,
                  locationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = newCategory.id.toString();
              command.commandContent.oldCategoryId = oldCategory.id.toString();
              return command;
            },

            createAddSubcategoryCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId, subcategoryId,
                containsSections, taskId) {
              var command = constructTaskSettingsCommand('AddSubcategory', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              if (!containsSections) {
                command.commandContent.taskId = taskId.toString();
              }
              return command;
            },

            createRemoveSubcategoryCommand : function(states, clientSequence, boardLocation, boardDate,
                locationShiftId, shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId,
                subcategoryId, containsSections, taskId) {
              var command = constructTaskSettingsCommand('RemoveSubcategory', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              return command;
            },

            createAddSectionCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                shiftCategoryId, subcategoryTaskId, sectionTaskId, taskIds, serviceLocationId, shiftId, categoryId,
                subcategoryId, sectionId) {
              var command = constructTaskSettingsCommand('AddSection', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.sectionTaskId = sectionTaskId;
              command.commandContent.taskIds = taskIds;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              command.commandContent.sectionId = sectionId.toString();
              return command;
            },

            createRemoveSectionCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, serviceLocationId, shiftId, categoryId, subcategoryId, sectionId) {
              var command = constructTaskSettingsCommand('RemoveSection', clientSequence, boardLocation, boardDate,serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.sectionTaskId = sectionTaskId;
              command.commandContent.shiftId = shiftId;
              command.commandContent.categoryId = categoryId;
              command.commandContent.subcategoryId = subcategoryId;
              command.commandContent.sectionId = sectionId.toString();
              return command;
            },

            createAddTaskCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId) {
              var command = constructTaskSettingsCommand('AddTasks', clientSequence, boardLocation, boardDate, serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.taskIds = taskIds;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              if (containsSections) {
                command.commandContent.sectionId = sectionId.toString();
                command.commandContent.sectionTaskId = sectionTaskId;
              }
              return command;
            },

            createUpdateTaskCommand : function(states, clientSequence, boardLocation, boardDate, task, locationShiftId,
                shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId,
                containsSections, sectionTaskId, sectionId) {

              var command = constructTaskSettingsCommand('UpdateTask', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);

              command.commandContent.taskId = task.id;
              command.commandContent.taskName = task.taskName;
              command.commandContent.taskComments = task.comments;
              command.commandContent.taskStartDate = task.startDate;
              command.commandContent.taskEndDate = task.endDate;

              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              if (containsSections) {
                command.commandContent.sectionId = sectionId.toString();
                command.commandContent.sectionTaskId = sectionTaskId;
              }

              return command;
            },

            createRemoveTasksCommand : function(states, clientSequence, boardLocation, boardDate, locationShiftId,
                shiftCategoryId, subcategoryTaskId, size, serviceLocationId, shiftId, categoryId, subcategoryId,
                containsSections, sectionTaskId, sectionId) {
              var command = constructTaskSettingsCommand('RemoveTasks', clientSequence, boardLocation, boardDate,
                  serviceLocationId, locationShiftId);
              command.commandContent.shiftCategoryId = shiftCategoryId;
              command.commandContent.subcategoryTaskId = subcategoryTaskId;
              command.commandContent.size = size;
              command.commandContent.shiftId = shiftId.toString();
              command.commandContent.categoryId = categoryId.toString();
              command.commandContent.subcategoryId = subcategoryId.toString();
              if (containsSections) {
                command.commandContent.sectionId = sectionId.toString();
                command.commandContent.sectionTaskId = sectionTaskId;
              }
              return command;
            },

            createDeleteSingleTaskCommand : function(states, clientSequence, boardLocation, boardDate, task, section,
                subcategory, category, shift, location, locationId, containsSections) {
              var command = constructTaskSettingsCommand('RemoveSingleTask', clientSequence, boardLocation, boardDate,
                  locationId, shift.id);
              command.commandContent.shiftCategoryId = category.id;
              command.commandContent.subcategoryTaskId = subcategory.id;
              command.commandContent.shiftId = shift.shift.id.toString();
              command.commandContent.categoryId = category.category.id.toString();
              command.commandContent.subcategoryId = subcategory.subcategory.id.toString();
              command.commandContent.taskId = task.id;
              if (containsSections) {
                command.commandContent.sectionId = section.sectionName;
                command.commandContent.sectionTaskId = section.id;
              }
              return command;
            },
            
            createUnlinkPartialTasksCommand : function(states, clientSequence, boardLocation, boardDate, equipment,
                personnel, tasks, taskMap, route) {
              var groupId = route[0].groupId, subcategoryTaskId = route[0].subcategoryTaskId, locationId = route[0].locationId, taskId = route[0].taskId, homogeneous = true;
              for (var i = 0; i < route.length; i++) {
                if (route[i].subcategoryTaskId !== subcategoryTaskId)
                  homogeneous = false;
              }

              for (var i = 0; i < route.length; i++) {

                var category = tasks.locations[route[i].locationId].locationShifts[route[i].locationShiftId].shiftCategories[route[i].shiftCategoryId], subcategory = category.subcategoryTasks[route[i].subcategoryTaskId], task;
                if (subcategory.subcategory.containsSections)
                  task = subcategory.sections[route[i].sectionId].tasks[route[i].taskId]
                else
                  task = subcategory.tasks[route[i].taskId]

                for (var j = 0; j < taskMap; j++)
                  if (taskMap[j].taskId === route[i].taskId)
                    delete taskMap[j]

                delete task.linkedTaskChildId
                delete task.linkedTaskParentId
                delete task.partialTaskSequence
                delete task.groupId
                delete task.homogeneous

                if (route[i].sequence != 1) {
                  task.assignedPerson1.person = null;
                  task.assignedPerson1.personId = null;
                  task.assignedPerson2.person = null;
                  task.assignedPerson2.personId = null;

                  if (homogeneous) {
                    task.assignedEquipment.equipment = null;
                    task.assignedEquipment.equipmentId = null;
                  }
                }
                
                task = $filter('formatTask')(task);
              }

              // Construct command and send
              var command = constructTaskCommand('UnlinkPartialTask', clientSequence, boardLocation, boardDate,
                  taskId);             
              command.commandContent.locationId = locationId;
              return command;
            },

            processClearAllLocationsCommand : function(tasks, equipment, personnel, command) {
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;

              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }
              
              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              var locationIds = Object.keys(tasks.locations);
              for (var i = 0; i < locationIds.length; i++) {
                tasks.locations[locationIds[i]].locationShifts = {};
              }

              tasks = TaskHelperService.updateTaskMap(tasks);
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: ''});

            },

            processClearSpecificLocationCommand : function(tasks, equipment, personnel, command) {
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;

              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }
              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);

              var locationIds = Object.keys(tasks.locations);
              for (var i = 0; i < locationIds.length; i++) {
                if (locationIds[i] == serviceLocationId) {
                  tasks.locations[locationIds[i]].locationShifts = {};
                }
              }

              tasks = TaskHelperService.updateTaskMap(tasks);
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: ''});

            },
            processAddShiftCommand : function(shifts, tasks, command) {
              var locationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shift = InfoService.getShiftInfo(locationShiftId, shifts[command.commandContent.shiftId]);
              var locationShiftIdOfExistingShift;
              var shiftExists = false;
              Object.keys(tasks.locations[locationId].locationShifts).forEach(function(key) { // check
                // if shift exists, to prevent duplicate shfits from getting added on front end
                if (tasks.locations[locationId].locationShifts[key].shift.id == shift.shift.id) {
                  shiftExists = true;
                  locationShiftIdOfExistingShift = key;
                }
              });
              if (shiftExists) {
                delete tasks.locations[locationId].locationShifts[locationShiftIdOfExistingShift]; // delete
                // shift
                // which
                // does
                // not
                // exist
                // in
                // backend
              }
              tasks.locations[locationId].locationShifts[locationShiftId] = shift; // overwrite
              // shift
              // received
              // from
              // server

              tasks = TaskHelperService.updateTaskMap(tasks);
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: shift.id});

            },

            processRemoveShiftCommand : function(shifts, tasks, equipment, personnel, command) {
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              var locationShiftId = command.commandContent.locationShiftId;
              var shift = InfoService.getShiftInfo(locationShiftId, shifts[command.commandContent.shiftId]);
              delete tasks.locations[serviceLocationId].locationShifts[locationShiftId];

              tasks = TaskHelperService.updateTaskMap(tasks);
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: ''});

            },

            processAddCategoryCommand : function(categories, tasks, command) {
              var locationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var category;
              for (var i = 0; i < categories.length; i++) {
                if (categories[i].id == command.commandContent.categoryId) {
                  category = categories[i];
                  break;
                }
              }
              category = InfoService.getCategoryInfo(shiftCategoryId, category);
              var categoryExists = false;
              var idOfExistingCategory;

              Object
                  .keys(tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories)
                  .forEach(
                      function(key) {
                        if (tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[key].category.id == category.category.id) {
                          categoryExists = true;
                          idOfExistingCategory = key;
                        }
                      });
              if (categoryExists) {
                delete tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[idOfExistingCategory];
              }
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId] = category;
              if (tasks.locations[locationId].locationShifts[locationShiftId].addedCategories == null
                  || tasks.locations[locationId].locationShifts[locationShiftId].addedCategories == undefined)
                tasks.locations[locationId].locationShifts[locationShiftId].addedCategories = {};
              tasks.locations[locationId].locationShifts[locationShiftId].addedCategories[shiftCategoryId] = category.category;

              CategoryDataService.updateReferenceCategories(tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories);
              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processRemoveCategoryCommand : function(categories, tasks, equipment, personnel, command) {
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }

              var category;

              for (var i = 0; i < categories.length; i++) {
                if (categories[i].id == command.commandContent.categoryId) {
                  category = categories[i];
                  break;
                }
              }
              category = InfoService.getCategoryInfo(shiftCategoryId, category);

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              delete tasks.locations[serviceLocationId].locationShifts[locationShiftId].addedCategories[shiftCategoryId];
              delete tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId];

              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processUpdateCategoryCommand : function(categories, tasks, equipment, personnel, command) {
              var locationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              var category;

              for (var i = 0; i < categories.length; i++) {
                if (categories[i].id == command.commandContent.categoryId) {
                  category = categories[i];
                  break;
                }
              }
              category = InfoService.getCategoryInfo(shiftCategoryId, category);         
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId] = category;

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              if (tasks.locations[locationId].locationShifts[locationShiftId].addedCategories == null
                  || tasks.locations[locationId].locationShifts[locationShiftId].addedCategories == undefined)
                tasks.locations[locationId].locationShifts[locationShiftId].addedCategories = {};
              tasks.locations[locationId].locationShifts[locationShiftId].addedCategories[shiftCategoryId] = angular.copy(category.category);

              CategoryDataService.updateReferenceCategories(tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories);
              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processAddSubcategoryCommand : function(categories, tasks, command) {
              var locationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var category, subcategory;
              for (var i = 0; i < categories.length; i++) {
                if (categories[i].id == command.commandContent.categoryId) {
                  category = categories[i];
                  for (var j = 0; j < category.subcategories.length; j++) {
                    if (category.subcategories[j].id == command.commandContent.subcategoryId) {
                      subcategory = category.subcategories[j];
                      break;
                    }
                  }
                  break;
                }
              }
              subcategory = InfoService.getSubcategoryInfo(subcategoryTaskId, subcategory, tasks.locations[locationId].location.sectionIds);
              var subcategoryExists = false;
              var idOfExistingSubcategory;

              if (!subcategory.subcategory.containsSections) {
                Object
                    .keys(
                        tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks)
                    .forEach(
                        function(key) {
                          if (tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[key].subcategory.id == subcategory.subcategory.id) {
                            subcategoryExists = true;
                            idOfExistingSubcategory = key;
                          }
                        });
              }
              if (subcategoryExists) {
                delete tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[idOfExistingSubcategory];
              }
              if (!subcategory.subcategory.containsSections) {
                if (!subcategory.tasks)
                  subcategory.tasks = {}
                subcategory.tasks[command.commandContent.taskId] = command.commandContent.task;
              }
              var allSubcategoryTasks = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].allsubcategoryTasks;

              for (var i = 0; i < Object.keys(allSubcategoryTasks).length; i++) {
                if (allSubcategoryTasks[i].id == subcategory.subcategory.id)
                  allSubcategoryTasks[i].selected = true;
              }
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].allsubcategoryTasks = allSubcategoryTasks;
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId] = subcategory;
              if (tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].addedSubcategories == null
                  || tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].addedSubcategories == undefined)
                tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].addedSubcategories = {};
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].addedSubcategories[subcategory.subcategory.id] = subcategory.id;

              tasks = TaskHelperService.updateTaskMap(tasks);


            },

            processRemoveSubcategoryCommand : function(categories, tasks, equipment, personnel, command) {
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              var category, subcategory;
              
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }

              for (var i = 0; i < categories.length; i++) {
                if (categories[i].id == command.commandContent.categoryId) {
                  category = categories[i];
                  for (var j = 0; j < category.subcategories.length; j++) {
                    if (category.subcategories[j].id == command.commandContent.subcategoryId) {
                      subcategory = category.subcategories[j];
                      break;
                    }
                  }
                  break;
                }
              }
              subcategory = InfoService.getSubcategoryInfo(subcategoryTaskId, subcategory, tasks.locations[serviceLocationId].location.sectionIds);             
              var allSubcategoryTasks;

              if (!subcategory.subcategory.containsSections) {
                if (!subcategory.tasks) {
                  subcategory.tasks = {}
                }
                subcategory.tasks[command.commandContent.taskId] = command.commandContent.task;
              }

              if(allSubcategoryTasks = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].allsubcategoryTasks) {

                allSubcategoryTasks = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].allsubcategoryTasks

                for (var i = 0; i < Object.keys(allSubcategoryTasks).length; i++) {
                  if (allSubcategoryTasks[i].id == subcategory.subcategory.id) {
                    allSubcategoryTasks[i].selected = false;
                  }
                }
              }

              delete tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processAddSectionCommand : function(tasks, command) {
              var locationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var sectionTaskId = command.commandContent.sectionTaskId;
              var sectionExists = false;
              var sectionTaskIdOfExistingSection;
              var section = InfoService.getSectionInfo(
                sectionTaskId,
                Object.keys(command.commandContent.tasksMap).length,
                command.commandContent.section
              );
              section.tasks = command.commandContent.tasksMap;
              Object.keys(
                      tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections)
                  .forEach(
                      function(key) {
                        if (tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[key].sectionId == section.sectionId) {
                          sectionExists = true;
                          sectionTaskIdOfExistingSection = key;
                        }
                      });
              if (sectionExists) {
                delete tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[sectionTaskIdOfExistingSection];
              }
              var allSections = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections;
              for (var j = 0; j < allSections.length; j++) {
                if (allSections[j].id == section.sectionId) {
                  allSections[j].numOfTasks = Object.keys(command.commandContent.tasksMap).length;
                }
              }


              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[sectionTaskId] = section;
              tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections = allSections;

              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processAddTasksCommand : function(tasks, command) {
              var locationId = command.commandContent.serviceLocationCode, locationShiftId = command.commandContent.locationShiftId, shiftCategoryId = command.commandContent.shiftCategoryId, subcategoryTaskId = command.commandContent.subcategoryTaskId, sectionTaskId = command.commandContent.sectionTaskId, numOfTasks = command.commandContent.numOfTasks;
              var subcategory = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];
              var sequence = 0;

              if (!subcategory.subcategory.containsSections) {

                //REGULAR CATEGORY TASKS

                /*
                if (!subcategory.tasks) {
                }

               var lastSequence = 1, key;
                for (key in subcategory.tasks) {
                  if (subcategory.tasks[key]['sequence']) {
                    if (lastSequence < subcategory.tasks[key]['sequence'])
                      lastSequence = subcategory.tasks[key]['sequence'];
                  }
                }
                for (key in subcategory.tasks) {
                  if (!subcategory.tasks[key]['sequence']) {
                    subcategory.tasks[key].sequence = ++lastSequence;
                  }
                }*/

                // Re-add task to map as tasks from server have additional
                // fields

                Object.keys(command.commandContent.tasksMap).forEach(function(taskId) {
                  var task = InfoService.getTaskInfo(taskId);
                  subcategory.tasks[taskId] = command.commandContent.tasksMap[taskId];
                  subcategory.tasks[taskId].sequence = command.commandContent.tasksMap[taskId].sequence;
                  angular.extend(command.commandContent.tasksMap[taskId], TaskModel);
                  tasks.tasksMap[taskId] = command.commandContent.tasksMap[taskId];
                });

                subcategory.numOfTasks = Number(numOfTasks);

              } else {

                //SECTION TASKS

                var section = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[sectionTaskId];
                var sequence = 1, offset, key;

                /*if (command.commandContent.numOfTasks != Object.keys(section.tasks).length) {
                  offset = Math.abs(Object.keys(section.tasks).length - command.commandContent.numOfTasks);
                  if (Object.keys(section.tasks).length < command.commandContent.numOfTasks) {
                    sequence = Object.keys(section.tasks).length;
                    sequence++;
                  } else if (Object.keys(section.tasks).length = command.commandContent.numOfTasks) {

                  }
                }*/

                // Re-add task to map as tasks from server have additional
                // fields
                Object.keys(command.commandContent.tasksMap).forEach(function(taskId) {
                  var task = InfoService.getTaskInfo(taskId);
                  section.tasks[taskId] = command.commandContent.tasksMap[taskId];
                  sequence = (command.commandContent.tasksMap[taskId].sequence.seconds * 1e+9) + command.commandContent.tasksMap[taskId].sequence.nano;
                  section.tasks[taskId].sequence = command.commandContent.tasksMap[taskId].sequence;
                  angular.extend(command.commandContent.tasksMap[taskId], TaskModel);
                  tasks.tasksMap[taskId] = command.commandContent.tasksMap[taskId];


                });

                var allSections = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections;
                for (var j = 0; j < allSections.length; j++) {
                  if (allSections[j].id == section.sectionId) {
                    allSections[j].numOfTasks = Number(numOfTasks);
                  }
                }

                tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections = allSections;
                tasks = TaskHelperService.updateTaskMap(tasks);

              }
            },

            processUpdateTasksCommand : function(tasks, command) {
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var taskId = command.commandContent.taskId;
              var taskObj = null;
              var mapTask = tasks.tasksMap[taskId];
              var taskSectionId = mapTask.sectionId;

              if (taskSectionId) {
                var sectionId = command.commandContent.section.id;
                taskObj = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[taskSectionId].tasks[taskId];
              } else {
                taskObj = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].tasks[taskId];
              }

              taskObj.taskName = command.commandContent.taskName;
              taskObj.startDate = command.commandContent.taskStartDate;
              taskObj.endDate = command.commandContent.taskEndDate;
              taskObj.comments = command.commandContent.taskComments;
              tasks = TaskHelperService.updateTaskMap(tasks);
            },

            processRemoveTasksCommand : function(tasks, equipment, personnel, command) {
              var serviceLocationId = command.commandContent.serviceLocationCode;


              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var sectionTaskId = command.commandContent.sectionTaskId;
              var size = command.commandContent.size;
              var taskIds = command.commandContent.taskIds;
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              var subcategory = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }
              
              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              if (!subcategory.subcategory.containsSections) {
                if (subcategory.numOfTasks != Number(size)) {
                  subcategory.numOfTasks = Number(size);
                  for (var i = 0; i < taskIds.length; i++) {
                    delete subcategory.tasks[taskIds[i]]
                  }
                }
              } else {
                var section = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[sectionTaskId];
                for (var i = 0; i < taskIds.length; i++) {
                  delete section.tasks[taskIds[i]]
                }
                var allSections = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections;
                for (var l = 0; l < allSections.length; l++) {

                  
                  if (allSections[l].id == section.sectionId) {
                    allSections[l].numOfTasks = Number(size);
                  }
                }
              }

              tasks = TaskHelperService.updateTaskMap(tasks);

            },

            processDeleteSingleTaskCommand : function(tasks, equipment, personnel, command) {
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId;
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var sectionTaskId = command.commandContent.sectionTaskId;
              var size = command.commandContent.size;
              var taskId = command.commandContent.taskId;
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var assignedPersonMap = command.commandContent.assignedPersonMap;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var subcategory = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId];
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              checkIfPersonAlreadyAssigned(personnel, personnelIds, assignedPersonMap);

              if (!subcategory.subcategory.containsSections) {
                  delete subcategory.tasks[taskId];
                subcategory.numOfTasks = subcategory.numOfTasks - 1;
              } else {
                var section = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections[sectionTaskId];

                if(section) {
                  delete section.tasks[taskId];
                  var allSections = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections;

                  for (var l = 0; l < allSections.length; l++) {
                    var id;
                    if (section.sectionName != null)
                      id = section.sectionName;
                    else if (section.section != null)
                      id = section.sectionId;
                    if (allSections[l].id == id) {
                      allSections[l].numOfTasks = allSections[l].numOfTasks - 1;
                    }
                  }
                }
              }
              tasks = TaskHelperService.updateTaskMap(tasks);
            },

            processRemoveSectionCommand : function(tasks, equipment, personnel, command) {
              var serviceLocationId = command.commandContent.serviceLocationCode;
              var locationShiftId = command.commandContent.locationShiftId;
              var shiftCategoryId = command.commandContent.shiftCategoryId; 
              var subcategoryTaskId = command.commandContent.subcategoryTaskId;
              var sectionTaskId = command.commandContent.sectionTaskId;
              var equipmentIds = command.commandContent.equipmentIds;
              var personnelIds = command.commandContent.personnelIds;
              var personnelAssignedStates = command.commandContent.personnelAssignedStates;
              var equipmentAssignedStates = command.commandContent.equipmentAssignedStates;
              
              var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
              
              if(nextDayUnassignedShifts){
            	  for(var id in nextDayUnassignedShifts){
            		  if(nextDayUnassignedShifts.hasOwnProperty(id)){
            			  nextDayUnassignedShifts[id].forEach(function(shift, index){
            				  var found = _.findWhere(personnel[id].assignedNextDayShifts, {id: shift.id});
                			  if(found)
                				  personnel[id].assignedNextDayShifts = _.without(personnel[id].assignedNextDayShifts, found);

            			  });
            			  
               		  }
            	  }
              }

              unassignPersons(personnel, personnelIds, personnelAssignedStates);
              unassignEquipment(equipment, equipmentIds, equipmentAssignedStates);
              
              var sections = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].sections;
              var allSections = tasks.locations[serviceLocationId].locationShifts[locationShiftId].shiftCategories[shiftCategoryId].subcategoryTasks[subcategoryTaskId].allSections;
              if (!sections[sectionTaskId])
                return;
              Object
                  .keys(sections[sectionTaskId].tasks)
                  .forEach(
                      function(taskId) {
                        var e = sections[sectionTaskId].tasks[taskId].assignedEquipment, p1 = sections[sectionTaskId].tasks[taskId].assignedPerson1, p2 = sections[sectionTaskId].tasks[taskId].assignedPerson2;
                        if (e && e.id && equipment[e.id])
                          equipment[e.id].state = 'Available';
                        if (p1 && p1.id && personnel[p1.id])
                          personnel[p1.id].state = 'Available';
                        if (p2 && p2.id && personnel[p2.id])
                          personnel[p2.id].state = 'Available';
                      });

              for (var l = 0; l < allSections.length; l++) {
                if (sections[sectionTaskId] && allSections[l].id === sections[sectionTaskId].sectionId)
                  allSections[l].numOfTasks = 0;
              }

              delete sections[sectionTaskId];
              tasks = TaskHelperService.updateTaskMap(tasks);

            },
            
            processSetPartialTaskCommand : function(shifts, tasks, command, linkedTaskMap) {
              var locationId = command.commandContent.serviceLocationCode,
                locationShiftId = command.commandContent.locationShiftId,              
                shift = tasks.locations[locationId].locationShifts[locationShiftId],
                catId = command.commandContent.partialTasks[0].categoryId,
                subcategoryTaskId = command.commandContent.partialTasks[0].subCategoryId,
                sectionId = command.commandContent.partialTasks[0].sectionId,
                homogeneous = true,
                sameSection = true,
                sameCat = true;

              for (var i = 0; i < command.commandContent.partialTasks.length; i++) {
                if (command.commandContent.partialTasks[i].subCategoryId !== subcategoryTaskId) {
                  homogeneous = false;
                }
                if (command.commandContent.partialTasks[i].sectionId !== sectionId) {
                  sameSection = false;
                }
                if (command.commandContent.partialTasks[i].categoryId !== catId) {
                  sameCat = false;
                }
              }

              for (var i = 0; i < command.commandContent.partialTasks.length; i++) {
                var task = command.commandContent.partialTasks[i];
                var taskId = task.id;
                var sequence = task.sequence;
                var taskSequence = tasks.tasksMap[task.id].sequence;
                var subcategoryId = task.subCategoryId;
                var categoryId = task.categoryId;
                var duration = task.hours;
                var groupId = task.groupId;
                var category = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[categoryId];
                var subcategory = category.subcategoryTasks[subcategoryId];
                var peoplePerTask = subcategory.peoplePerTask;

                var label = category.category.name + " - " + subcategory.subcategory.name + "/ ";
                if (subcategory.subcategory.containsSections) {
                  if (!sameSection) {
                    subcategory.sections[task.sectionId].canNotBeDeleted = true;
                  }
                  label = label + subcategory.sections[task.sectionId].sectionName + " / ";
                }
                label = label + task.taskName;

                if (!sameCat) {
                  category.canNotBeDeleted = true;
                }

                if (!homogeneous) {
                  subcategory.canNotBeDeleted = true;
                  for (var x = 0; x < category.allsubcategoryTasks.length; x++) {
                    if (subcategory.subcategory.id === category.allsubcategoryTasks[x].id) {
                      category.allsubcategoryTasks[x].disabled = true;
                    }
                  }
                }

                var found = false;
                for (var j = 0; linkedTaskMap && j < linkedTaskMap.length; j++)
                  if (linkedTaskMap[j].taskId === task.id)
                    found = true;
                if (!found) {
                  linkedTaskMap.push({
                    locationId : locationId,
                    locationShiftId : locationShiftId,
                    shiftCategoryId : categoryId,
                    subcategoryTaskId : subcategoryId,
                    sectionId : task.sectionId,
                    containsSections : task.containsSections,
                    peoplePerTask : peoplePerTask,
                    taskId : taskId,
                    sequence : taskSequence,
                    label : label,
                    duration : duration,
                    groupId : groupId,
                    homogeneous: homogeneous,
                    linkedTaskChildId : task.linkedTaskChildId,
                    linkedTaskParentId : task.linkedTaskParentId,
                    partialTaskSubcategories: task.partialTaskSubcategories
                  })
                }
                
                var t;
                if (subcategory.subcategory.containsSections)
                  t = subcategory.sections[task.sectionId].tasks[taskId];
                else
                  t = subcategory.tasks[taskId];

                t.groupId = groupId;
                t.homogeneous = homogeneous;
                t.partialTaskSequence = sequence;
                t.linkedTaskParentId = task.linkedTaskParentId;
                t.linkedTaskChildId = task.linkedTaskChildId;
                t.partialTaskSubcategories = task.partialTaskSubcategories;
                t.startDate = task.startDate;
                t.endDate = task.endDate;
               
              }
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: shift.id});
            },
            
            processUnlinkPartialTaskCommand : function(tasks, command, linkedTaskMap) {
              var locationId = command.commandContent.serviceLocationCode; 
              var locationShiftId = command.commandContent.locationShiftId;
              var shift = tasks.locations[locationId].locationShifts[locationShiftId];
              var subcategoryTaskId = command.commandContent.partialTasks[0].subCategoryId;
              var homogeneous = true;

              for (var i = 0; i < command.commandContent.partialTasks.length; i++)
                if (command.commandContent.partialTasks[i].subCategoryId !== subcategoryTaskId)
                  homogeneous = false;

              for (var i = 0; i < command.commandContent.partialTasks.length; i++) {
                var task = command.commandContent.partialTasks[i];
                var taskId = task.id;
                var subcategoryId = task.subCategoryId;
                var categoryId = task.categoryId;
                var duration = task.hours;
                var category = tasks.locations[locationId].locationShifts[locationShiftId].shiftCategories[categoryId];
                var subcategory = category.subcategoryTasks[subcategoryId];
                var peoplePerTask = subcategory.peoplePerTask;
                var unassignEquipId = task.unassignEquipId;
                var unassignPersonId1 = task.unassignPersonId1;
                var unassignPersonId2 = task.unassignPersonId2;

                for (var j = 0; j < linkedTaskMap.length; j++)
                  if (linkedTaskMap[j].taskId === task.id)
                    linkedTaskMap.splice(j, 1)

                category.canNotBeDeleted = false;
                subcategory.canNotBeDeleted = false;
                for (var x = 0; x < category.allsubcategoryTasks.length; x++) {
                  if (subcategory.subcategory.id === category.allsubcategoryTasks[x].id) {
                    category.allsubcategoryTasks[x].disabled = false;
                  }
                }
                var actualTask;
                if (subcategory.subcategory.containsSections) {
                  subcategory.sections[task.sectionId].canNotBeDeleted = false;
                  actualTask = subcategory.sections[task.sectionId].tasks[taskId]

                } else {
                  actualTask = subcategory.tasks[taskId];
                }

                delete actualTask.linkedTaskChildId;
                delete actualTask.linkedTaskParentId;
                delete actualTask.partialTaskSequence;
                delete actualTask.groupId;
                delete actualTask.homogeneous;
                
                if(unassignEquipId){
                	actualTask.clearEquipmentAssignment();
                }
                
                if(unassignPersonId1){
                	actualTask.clearPersonnelAssignment(1);
                }
                
                if(unassignPersonId2){
                	actualTask.clearPersonnelAssignment(2);
                }                
                
              }

              TaskIndicatorService.applyTasksIndicators(linkedTaskMap, tasks);
              $rootScope.$broadcast('UPDATE-TASK-SETTINGS-PANE', {shiftId: shift.id});
            },

            saveGroupOnServer : function($resource, group, boardData, successFn, errorFn) {

              var locationId;
              for (var i = 0; i < group.length; i++) {
                locationId = group[i].locationId;
                break;
              }

              var resource = $resource(boardData.pathStart + '/SetPartialTask/:district/:date/', {
                district : boardData.boardLocation,
                date : boardData.boardDate
              }, {
                save : {
                  method : 'POST',
                  headers : {
                    'Content-Type' : 'application/json'
                  }
                }
              });

              var partialTasks = [];

              for (var i = 0; i < group.length; i++) {
                partialTasks.push({
                  "id" : group[i].taskId,
                  "sequence" : group[i].sequence,
                  "hours" : group[i].duration,
                  "subCategoryId" : group[i].subcategoryTaskId,
                  "sectionId" : group[i].sectionId,
                  "categoryId" : group[i].shiftCategoryId,
                })
              }

              var group = {
                "serviceLocationId" : locationId,
                "locationShiftId" : group[0].locationShiftId,
                "shiftId" : group[0].shiftId,
                "boardId" : boardData.boardLocation + "_" + boardData.boardDate,
                "partialTasks" : partialTasks
              }

              var response = resource.save(JSON.stringify(group), function(data) {
                console.log('success, got data: ', data);
                successFn(data);
              }, function(error) {
                errorFn(error);
              });
            }
          }

        });