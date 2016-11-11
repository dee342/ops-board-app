'use strict';

/**
 * This file contains all the logic for working with Tasks, including creating commands, processing server commands,
 * formatting and manipulating task data.
 */

/** Section - Create Commands for Server - Start */

angular
    .module('OpsBoard')
    .service(
        'TaskHelperService',
        function(TaskModel, InfoService, $filter, $window, BoardValueService, BoardDataService, CategoryDataService, TaskIndicatorService, $log, $rootScope) {
          var findTask = function(taskKey, data, onlyKeyRing) {
            var task, keyRing = {};
              var boardEndDate;
              // data doesn't have property shiftsEndDate.
              if(data.hasOwnProperty('shiftsEndDate')) {
                  boardEndDate = data.shiftsEndDate;
              }
              else {
                  boardEndDate = BoardDataService.getBoardData().endDate;
              }

            Object
                .keys(data.locations)
                .forEach(
                    function(key) {
                      if (data.locations[key].locationShifts) {
                        Object
                            .keys(data.locations[key].locationShifts)
                            .forEach(
                                function(shiftId) {
                                  if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                    Object
                                        .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                        .forEach(
                                            function(categoryId) {
                                              if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                Object
                                                    .keys(subs)
                                                    .forEach(
                                                        function(subId) {
                                                          if (subs[subId].subcategory.containsSections) {
                                                            Object
                                                                .keys(subs[subId].sections)
                                                                .forEach(
                                                                    function(sectionId) {
                                                                      if (subs[subId].sections[sectionId]) {
                                                                        Object
                                                                            .keys(subs[subId].sections[sectionId].tasks)
                                                                            .forEach(
                                                                                function(taskId) {
                                                                                  if (taskId == taskKey) {
                                                                                    task = subs[subId].sections[sectionId].tasks[taskId];
                                                                                    if (onlyKeyRing) {
                                                                                      keyRing = {
                                                                                        locationId : key,
                                                                                        locationShiftId : shiftId,
                                                                                        shiftCategoryId : categoryId,
                                                                                        subcategoryTaskId : subId,
                                                                                        sectionId : sectionId,
                                                                                        containsSections : true,
                                                                                        peoplePerTask : subs[subId].subcategory.peoplePerTask,
                                                                                        taskId : taskId,
                                                                                        linkedTaskChildId : task.linkedTaskChildId,
                                                                                        linkedTaskParentId : task.linkedTaskParentId
                                                                                      }
                                                                                    }
                                                                                  }
                                                                                })
                                                                      }
                                                                    })
                                                          } else {
                                                            Object
                                                                .keys(subs[subId].tasks)
                                                                .forEach(
                                                                    function(taskId) {
                                                                      if (taskId == taskKey) {
                                                                        task = subs[subId].tasks[taskId];
                                                                        if (onlyKeyRing) {
                                                                          keyRing = {
                                                                            locationId : key,
                                                                            locationShiftId : shiftId,
                                                                            shiftCategoryId : categoryId,
                                                                            subcategoryTaskId : subId,
                                                                            containsSections : false,
                                                                            peoplePerTask : subs[subId].subcategory.peoplePerTask,
                                                                            taskId : taskId,
                                                                            linkedTaskChildId : task.linkedTaskChildId,
                                                                            linkedTaskParentId : task.linkedTaskParentId
                                                                          }
                                                                        }
                                                                      }
                                                                    })
                                                          }
                                                        })
                                              }
                                            })
                                  }
                                })
                      }
                    })
            if (onlyKeyRing)
              return keyRing
            return task;
          }

          var getPersonAssignedShifts = function (personId) {
            //will be called only when person already assigned to a task get assigned again so not generalizing/precalculating -  will do if required
            var task, keyRing = {},
              data = BoardValueService.tasks,
              boardEndDate = data.shiftsEndDate,
              retShiftId = [];

            Object
                .keys(data.locations)
                .forEach(
                    function(key) {
                      if (data.locations[key].locationShifts) {
                        Object
                            .keys(data.locations[key].locationShifts)
                            .forEach(
                                function(shiftId) {
                                  if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                    Object
                                        .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                        .forEach(
                                            function(categoryId) {
                                              if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                Object
                                                    .keys(subs)
                                                    .forEach(
                                                        function(subId) {
                                                          if (subs[subId].subcategory.containsSections) {
                                                            Object
                                                                .keys(subs[subId].sections)
                                                                .forEach(
                                                                    function(sectionId) {
                                                                      if (subs[subId].sections[sectionId]) {
                                                                        Object
                                                                            .keys(subs[subId].sections[sectionId].tasks)
                                                                            .forEach(
                                                                                function(taskId) {
                                                                                  if (subs[subId].sections[sectionId].tasks[taskId].assignedPerson1.personId === personId || subs[subId].sections[sectionId].tasks[taskId].assignedPerson2.personId === personId) {
                                                                                    retShiftId.push(data.locations[key].locationShifts[shiftId].shift.id);
                                                                                  }
                                                                                })
                                                                      }
                                                                    })
                                                          } else {
                                                            Object
                                                                .keys(subs[subId].tasks)
                                                                .forEach(
                                                                    function(taskId) {
                                                                      if (subs[subId].tasks[taskId].assignedPerson1.personId === personId || subs[subId].tasks[taskId].assignedPerson2.personId === personId) {
                                                                        retShiftId.push(data.locations[key].locationShifts[shiftId].shift.id);
                                                                      }
                                                                    })
                                                          }
                                                        })
                                              }
                                            })
                                  }
                                })
                      }
                    })

            return retShiftId;
          }


          var findSection = function(sectionKey, data) {
            var section;
            Object
                .keys(data.locations)
                .forEach(
                    function(key) {
                      if (data.locations[key].locationShifts) {
                        Object
                            .keys(data.locations[key].locationShifts)
                            .forEach(
                                function(shiftId) {
                                  if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                    Object
                                        .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                        .forEach(
                                            function(categoryId) {
                                              if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                Object.keys(subs).forEach(function(subId) {
                                                  if (subs[subId].subcategory.containsSections) {
                                                    Object.keys(subs[subId].sections).forEach(function(sectionId) {
                                                      if (sectionId === sectionKey) {
                                                        section = subs[subId].sections[sectionId];
                                                      }
                                                    })
                                                  }
                                                })
                                              }
                                            })
                                  }
                                })
                      }
                    })
            return section;
          }
          
          var findSubcategory = function(subcategoryKey, data) {
              var subcategory;
              Object
                  .keys(data.locations)
                  .forEach(
                      function(key) {
                        if (data.locations[key].locationShifts) {
                          Object
                              .keys(data.locations[key].locationShifts)
                              .forEach(
                                  function(shiftId) {
                                    if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                      Object
                                          .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                          .forEach(
                                              function(categoryId) {
                                                if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                  var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                  Object.keys(subs).forEach(function(subId) {
                                                      if (subId === subcategoryKey) {
                                                          subcategory = subs[subId]
                                                      }
                                                  })
                                                    }
                                                  })
                                    }
                                  })
                        }
                      })
              return subcategory;
            }

          var hasTasks = function(data) {
            var contains = false;
            Object
                .keys(data.locations)
                .forEach(
                    function(key) {
                      if (data.locations[key].locationShifts) {
                        Object
                            .keys(data.locations[key].locationShifts)
                            .forEach(
                                function(shiftId) {
                                  if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                    Object
                                        .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                        .forEach(
                                            function(categoryId) {
                                              if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                Object
                                                    .keys(subs)
                                                    .forEach(
                                                        function(subId) {
                                                          if (subs[subId].subcategory.containsSections) {
                                                            Object
                                                                .keys(subs[subId].sections)
                                                                .forEach(
                                                                    function(sectionId) {
                                                                      if (subs[subId].sections[sectionId]) {
                                                                        if (Object
                                                                            .keys(subs[subId].sections[sectionId].tasks).length > 0) {
                                                                          contains = true;
                                                                          return;
                                                                        }
                                                                      }
                                                                    })
                                                          } else {
                                                            if (Object.keys(subs[subId].tasks).length > 0) {
                                                              contains = true;
                                                              return;
                                                            }
                                                          }
                                                        })
                                              }
                                            })
                                  }
                                })
                      }
                    })

            return contains;
          }

          var processErrorInEquipmentToTaskCommand = function(user, command, root_scope) {
              $log.error('Error assigning equipment:', command.commandContent.errorMessage);
              var equipmentId = command.commandContent.command.equipmentId;
              var pieceOfEquipment = BoardValueService.equipment[equipmentId];
              var task = findTask(command.commandContent.command.taskId, BoardValueService.tasks);
              switch (command.commandContent.errorCode) {
                case 30029: // Task not found
                case 30001: // Equipment not found
                case 30003: // Equipment is not available for assignment
                  if (pieceOfEquipment) {
                    pieceOfEquipment.assigned = false;
                  }
                  if (task) {
                    task.clearEquipmentAssignment();
                  }
                  break;
                default:
                if (pieceOfEquipment) {
                  pieceOfEquipment.assigned = false;
                }
              }
              
              if(task)
            	  task = $filter('formatTask')(task);
              
              if (user.username !== command.user) {
                rootScope.pushedId = equipmentId;
              }
            };

            var processErrorInPersonToTaskCommand = function(user, command, root_scope) {
              $log.error('Error assigning personnel:', command.commandContent.errorMessage);
              var personId = command.commandContent.command.personId;
              var position = command.commandContent.command.position;
              var person = BoardValueService.personnel[personId];
              var task = findTask(command.commandContent.command.taskId, BoardValueService.tasks);
              switch (command.commandContent.errorCode) {
                case 30029: // Task not found
                case 30002: // Person not found
                  if (person) {
                    person.assigned = false;
                    person.assignedAnywhere = false;
                  }
                  if (task) {
                    task.clearPersonnelAssignment(position);
                  }
                  break;
                case 30006: // Task has person already assigned to it
                  if (person) {
                    person.assigned = command.commandContent.command.assigned;
                    person.assignedAnywhere = command.commandContent.command.assignedAnywhere;
                  }
                  break;
                case 30039: // Person is not available for next day assignment
                  if (person && person.availableNextDay) {
                    person.assigned = command.commandContent.command.assigned;  //false
                    person.assignedAnywhere = command.commandContent.command.assignedAnywhere; //false
                  }
                  if (task) {
                    task.clearPersonnelAssignment(position);
                  }
                  break;
                default:
                  if (person) {
                	  person.assigned = command.commandContent.command.assigned;
                      person.assignedAnywhere = command.commandContent.command.assignedAnywhere;
                  }
              }
              
              if(task){
            	  task = $filter('formatTask')(task);
              }
              
              if(person){
            	  person = $filter('extendPerson')(person);
            	  root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
              }
              
              if (user.username !== command.user) {
                root_scope.pushedId = personId;
              }
            };

          var padEndofArray = function(array, desiredSize, element) {
            var l = array.length;

            for (var i = l; i < desiredSize - l; i++) {
              array[i] = element;
            }

            return;
          };


          function checkRowcount(itemsheight, itemheight, locationscount) {

            var windowHeight = angular.element($window).height();
            var colheight = windowHeight - 90;

            if(locationscount > 1) {
              colheight = windowHeight - 120;
            }

            itemsheight = itemsheight + itemheight;
            if(itemsheight > colheight) {
              itemsheight = 0;
            }

            return itemsheight;
          }

          function columnCounts(columnlocs, boroBoard, isDistrict) {
            if(isDistrict) {
              columnlocs.districtcolumncount = columnlocs.districtcolumncount + 1;

            } else {
              columnlocs.columncount = columnlocs.columncount + 1;
            }

            return columnlocs;
          }

          function buildPersonnelBlock(finalobject, personneldistricts, filter, title, itemsheight, personnelblock, personnelblockcount, column, columnname, locationscount, headerclass, code, mode, realBoardDate) {
            var filterType = '';
            var builtpersonnelblock = {};

            var results = $filter(filter)(personneldistricts, filterType, code, realBoardDate);

            if(results.results.length > 0) {
              finalobject.cols[columnname].taskcells.push({type: 'personnelheader', title: title, column: column, headerclass: headerclass, count: results.total});
            }

            for (var person in results.results) {
              personnelblockcount++;

              results.results[person].formattedname = $filter('getFormattedName')(results.results[person]);
              results.results[person].titlemap2 = $filter('getMappedTitle')(results.results[person].civilServiceTitle);

              personnelblock.push(results.results[person]);

              if (personnelblockcount > 1) {
                finalobject.cols[columnname].taskcells.push({type: 'personnelblock', personnelblock: personnelblock, column: column, headerclass: headerclass, title: title});
                itemsheight = checkRowcount(itemsheight, 21, locationscount);
                if(itemsheight === 0) {
                  column = column + .1;
                  columnname = 'col' + column;
                  finalobject.cols[columnname] = {};
                  finalobject.cols[columnname].taskcells = [];
                }
                personnelblockcount = 0;
                personnelblock = [];
              }
            }

            if(personnelblockcount > 0) {
              finalobject.cols[columnname].taskcells.push({type: 'personnelblock', personnelblock: personnelblock, column: column, title: title});
              itemsheight = checkRowcount(itemsheight, 21, locationscount);
              if(itemsheight === 0) {
                column = column + .1;
                columnname = 'col' + column;
                finalobject.cols[columnname] = {};
                finalobject.cols[columnname].taskcells = [];
              }
            }

            if(results.results.length > 0) {
              finalobject.cols[columnname].taskcells.push({type: 'personnelfooter', title: title, column: column});
            }

            builtpersonnelblock.finalobject = finalobject;
            builtpersonnelblock.itemsheight = itemsheight;
            builtpersonnelblock.personnelblock = personnelblock;
            builtpersonnelblock.personnelblockcount = personnelblockcount;
            builtpersonnelblock.column = column;
            builtpersonnelblock.columnname = columnname;

            return builtpersonnelblock;
          };
          
          function removeSupervisorsFromTask(task, supervisorIds) {
              var removedSupervisorIds = [];
              
              if (!task || !task.taskSupervisorAssignments || task.taskSupervisorAssignments.length == 0 || !supervisorIds || supervisorIds.length == 0)
                  return;
              var i =  task.taskSupervisorAssignments.length;
              while (i--) {
                  if (_.indexOf(supervisorIds, task.taskSupervisorAssignments[i].personId) != -1) {
                      removedSupervisorIds.push(task.taskSupervisorAssignments[i].personId);
                      task.taskSupervisorAssignments.splice(i, 1);
                  }
              }
              
              return removedSupervisorIds;                              
          }
          
        
          function renderTaskShifts(finalobject, column, columnname, shift2, itemsheight, columncounts, shiftCategories, shiftcategory2, subcategorytasks, categorytask2, tasksobject, tasksequence, task2, sections, hassections, section2, canRemoveSectionSupervisor, canRemoveSubcategorySupervisor, supervisorTitle, supObject, titlemapSup, shifts, locationId, locationscount, location2, districtEquipments, districtPersons, shiftData, categoryData, subcategoryData, boroBoard, isDistrict, titlemap1, titlemap2, isLocked, personnel) {
            for (var shift in shifts) {
              shift2 = shifts[shift];

              if(!shift2.shift) {
                shift2.shift = {};
                shift2.shift.id = shift2.shiftId;
                shift2.shift.name = shiftData[shift2.shiftId].name;
              }

              shift2.shift.type = (shiftData[shift2.shift.id].sequence > 6 && shiftData[shift2.shift.id].sequence < 14) ? 'day' : 'night';

              finalobject.cols[columnname].taskcells.push({type:'shift',shift: shift2, locationId: locationId, column: column});

              itemsheight = checkRowcount(itemsheight, 39, locationscount);
              if(itemsheight === 0) {
                column = column + .1;
                columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                columnname = 'col' + column;
                finalobject.cols[columnname] = {};
                finalobject.cols[columnname].taskcells = [];
              }

              shiftCategories = $filter('orderByCategoryId')(shift2.shiftCategories, null, categoryData);

              // SHIFT CATEGORY **************************

              for (var shiftcategory in shiftCategories) {
                shiftcategory2 = shiftCategories[shiftcategory];
                // extend shift category with base model

                if(!shiftcategory2.category) {
                  shiftcategory2.category = categoryData[shiftcategory2.categoryId]
                }

                // shiftcategory2.category.subcategories = undefined;

                subcategorytasks = $filter('orderBySubcategorySequence')(shiftcategory2.subcategoryTasks, null, subcategoryData);

                for (var categorytask in subcategorytasks) {
                  categorytask2 = subcategorytasks[categorytask];
                  categorytask2.partialTaskCount = $filter('getPartialTasks')(categorytask2, true);

                  canRemoveSubcategorySupervisor = $filter('canRemoveSubcategorySupervisor')(categorytask2);
                  
                  if(categorytask2.supervisorAssignments && categorytask2.supervisorAssignments.length > 0) {
                      supervisorTitle = $filter('getSupervisorTitle')('subcategory', categorytask2);
                      supObject = personnel[categorytask2.supervisorAssignments[0].personId];

                      if(supObject) {
                          titlemapSup = $filter('getMappedTitle')(supObject.civilServiceTitle);
                      } else {
                          titlemapSup = '';
                      }

                  } else {
                      supervisorTitle = 'S';
                      supObject = {};
                      titlemapSup = '';
                  } 
                  
                  finalobject.cols[columnname].taskcells.push({
                    type:'subcategory',
                    categorynamelowercase: shiftcategory2.category.name.toLowerCase(),
                    category: shiftcategory2,
                    subcategory: categorytask2,
                    shift: shift2,
                    canNotBeDeleted: categorytask2.canNotBeDeleted,
                    location: location2,
                    locationId: locationId,
                    context: 'supervisor-assignment', 
                    column: column,
                    taskIndicator: (categorytask2.subcategory.taskIndicator ? categorytask2.subcategory.taskIndicator : ''),
                    titlemapSup: titlemapSup, 
                    supervisorTitle: supervisorTitle,
                    canRemoveSubcategorySupervisor: canRemoveSubcategorySupervisor,
                    hideSupervisorAssignment: (shiftcategory2.category.id === 1 || (!(typeof categorytask2.sections === "undefined") && Object.keys(categorytask2.sections).length > 0))
                  });
                 
                  itemsheight = checkRowcount(itemsheight, 36, locationscount);

                  if(itemsheight === 0) {
                    column = column + .1;
                    columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                    columnname = 'col' + column;
                    finalobject.cols[columnname] = {};
                    finalobject.cols[columnname].taskcells = [];
                  }

                  tasksobject = $filter('orderByTaskId')(categorytask2.tasks);
                  tasksequence = 0;

                  // REGULAR TASKS **************************

                  for (var task in tasksobject) {
                    tasksequence++;
                    task2 = tasksobject[task];
                    task2 = $filter('formatTask')(task2, null, shiftcategory2, districtEquipments, districtPersons);
                    finalobject.cols[columnname].taskcells.push({type:'task',task: task2, category: shiftcategory2, subcategory: categorytask2, shift: shift2, location: location2, locationId: locationId, column: column, tasksequence: tasksequence, tasksection: 0});
                    itemsheight = checkRowcount(itemsheight, 36, locationscount);
                    if(itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject.cols[columnname] = {};
                      finalobject.cols[columnname].taskcells = [];
                    }
                  }

                  sections = $filter('orderBySection')(categorytask2.sections);
                  hassections = false;

                  for (var section in sections) {

                    // SECTION HEADING **************************
                    section2 = sections[section];
                    canRemoveSectionSupervisor = $filter('canRemoveSectionSupervisor')(section2);

                    if(section2.sectionSupervisorAssignments && section2.sectionSupervisorAssignments.length > 0) {
                      supervisorTitle = $filter('getSupervisorTitle')('section', null, section2);
                      supObject = personnel[section2.sectionSupervisorAssignments[0].personId];

                      if(supObject) {
                        titlemapSup = $filter('getMappedTitle')(supObject.civilServiceTitle);
                      } else {
                        titlemapSup = '';
                      }

                    } else {
                      supervisorTitle = 'S';
                      supObject = {};
                      titlemapSup = '';
                    }

                    finalobject.cols[columnname].taskcells.push({
                        type:'section', 
                        section: section2, 
                        categorynamelowercase: shiftcategory2.category.name.toLowerCase(),
                        category: shiftcategory2, 
                        subcategory: categorytask2, 
                        shift: shift2, 
                        location: location2, 
                        locationId: locationId, 
                        context: 'supervisor-assignment', 
                        canNotBeDeleted: section2.canNotBeDeleted, 
                        taskIndicator: (categorytask2.subcategory.taskIndicator?categorytask2.subcategory.taskIndicator:''), 
                        column: column,
                        titlemapSup: titlemapSup, 
                        titlemap1: titlemap1, 
                        titlemap2: titlemap2, 
                        isLocked: isLocked, 
                        supObject: supObject, 
                        supervisorTitle: supervisorTitle, 
                        canRemoveSectionSupervisor:canRemoveSectionSupervisor,
                        hideSupervisorAssignment: (shiftcategory2.category.id === 1)
                        });
                    itemsheight = checkRowcount(itemsheight, 25, locationscount);
                    if(itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject.cols[columnname] = {};
                      finalobject.cols[columnname].taskcells = [];
                    }

                    tasksobject = $filter('orderByTaskId')(section2.tasks);
                    tasksequence = 0;

                    // SECTION TASKS **************************
                    for (var task in tasksobject) {
                      tasksequence++;
                      task2 = tasksobject[task];
                      task2 = $filter('formatTask')(task2, section2, shiftcategory2, districtEquipments, districtPersons);
                      finalobject.cols[columnname].taskcells.push({type:'task',task: task2, category: shiftcategory2, subcategory: categorytask2, shift: shift2, location: location2, section: section2, locationId: locationId, context: 'supervisor-assignment', tasksequence: tasksequence, tasksection: section2.sectionName});
                      itemsheight = checkRowcount(itemsheight, 35, locationscount);
                      if(itemsheight === 0) {
                        column = column + .1;
                        columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                        columnname = 'col' + column;
                        finalobject.cols[columnname] = {};
                        finalobject.cols[columnname].taskcells = [];
                      }
                    }

                    if(itemsheight > 0) {
                      finalobject.cols[columnname].taskcells.push({
                        type: 'lastsectiontask',
                        category: shiftcategory2,
                        column: column
                      });
                      itemsheight = checkRowcount(itemsheight, 9, locationscount);
                      if (itemsheight === 0) {
                        column = column + .1;
                        columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                        columnname = 'col' + column;
                        finalobject.cols[columnname] = {};
                        finalobject.cols[columnname].taskcells = [];
                      }
                    }
                  }

                  if(itemsheight > 0) {
                    finalobject.cols[columnname].taskcells.push({type: 'lasttask', column: column});
                    itemsheight = checkRowcount(itemsheight, 9, locationscount);
                    if (itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject.cols[columnname] = {};
                      finalobject.cols[columnname].taskcells = [];
                    }
                  }
                }
              }
            }

            return {finalobject: finalobject, column: column, columnname: columnname, itemsheight: itemsheight, columncounts: columncounts};

          }

          return {
            getPersonAssignedShifts: getPersonAssignedShifts,
            constructTaskCommand : function(commandName, clientSequence, boardLocation, boardDate, taskKey) {

              var command = {
                'commandName' : commandName,
                'clientSequence' : clientSequence++,
                'commandContent' : {
                  'taskId' : taskKey
                },
                'date' : boardDate,
                'location' : boardLocation
              }

              return command;
            },

            containsTasks : function(tasks) {
              return hasTasks(tasks);
            },

            /** Section - Perform operations on server - Start */

            createAssignEquipmentToPartialTaskCommand : function(states, clientSequence, boardLocation, boardDate, tasks, taskMap, task, pieceOfEquipment, locationId) {

              var subcategoryTaskId, index = -1;
              for (var i = 0; i < taskMap.length; i++) {
                if (taskMap[i].taskId === task.id) {
                  subcategoryTaskId = taskMap[i].subcategoryTaskId;
                  index = i;
                }
              }
              var homogeneous = true;
              for (var i = 0; i < taskMap.length; i++) {
                if (taskMap[i].groupId === task.groupId)
                  if (taskMap[i].subcategoryTaskId !== subcategoryTaskId)
                    homogeneous = false;
              }

              if (homogeneous) {
                for (var i = 0; i < taskMap.length; i++) {
                  if (taskMap[i].groupId === task.groupId) { // will change
                    // this late to
                    // push it to
                    // array
                    var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId], subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId], task;
                    if (subcategory.subcategory.containsSections)
                      task = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                    else
                      task = subcategory.tasks[taskMap[i].taskId]
                    task.assignedEquipment.equipment = pieceOfEquipment;
                    task.homogeneous = true;
                  }
                }
              } else {
                var category = tasks.locations[taskMap[index].locationId].locationShifts[taskMap[index].locationShiftId].shiftCategories[taskMap[index].shiftCategoryId], subcategory = category.subcategoryTasks[taskMap[index].subcategoryTaskId], task;
                if (subcategory.subcategory.containsSections)
                  task = subcategory.sections[taskMap[index].sectionId].tasks[taskMap[index].taskId]
                else
                  task = subcategory.tasks[taskMap[index].taskId]
                task.assignedEquipment.equipment = pieceOfEquipment;
              }

              // Change equipment status
              pieceOfEquipment.assigned = true;
              task = $filter('formatTask')(task);

              // Construct command and send
              var command = this.constructTaskCommand('AssignEquipmentToPartialTask', clientSequence, boardLocation, boardDate, task.id);
              command.commandContent.equipmentId = pieceOfEquipment.id;
              command.commandContent.locationId = locationId;

              return command;
            },

            createAssignEquipmentToTaskCommand : function(states, clientSequence, boardLocation, boardDate, task, pieceOfEquipment, uniqueId) {

              // Change equipment status
              pieceOfEquipment.assigned = true;

              // Assign equipment to task
              task.assignedEquipment.equipmentId = pieceOfEquipment.id;
              task = $filter('formatTask')(task);

              // Construct command and send
              var command = this.constructTaskCommand('AssignEquipmentToTask', clientSequence, boardLocation, boardDate, task.id);
              command.commandContent.equipmentId = pieceOfEquipment.id;
              command.commandContent.uniqueId = uniqueId;

              return command;
            },

            createAssignPersonToPartialTaskCommand : function(states, clientSequence, boardLocation, boardDate, tasks, taskMap, task, person, position, locationId) {

              var subcategoryTaskId, index = -1;
              var taskType= ((person.availableNextDay && !task["assignedPerson" + position].type) ? 'NEXT_DAY' : task["assignedPerson" + position].type);
              for (var i = 0; i < taskMap.length; i++) {
                if (taskMap[i].taskId === task.id) {
                  subcategoryTaskId = taskMap[i].subcategoryTaskId;
                  index = i;
                }
              }

              for (var i = 0; i < taskMap.length; i++) {
                if (taskMap[i].groupId === task.groupId) { // will change this
                  // late to push it
                  // to array
                  var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId], subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId], task;
                  if (subcategory.subcategory.containsSections)
                    task = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                  else
                    task = subcategory.tasks[taskMap[i].taskId]
                  if (position == 1)
                    task.assignedPerson1.person = person;
                  else if (position == 2)
                    task.assignedPerson2.person = person;
                }
              }

              // Change person status
              person.assigned = true;
              task['assignedPerson' + position].personId = person.id;
              task = $filter('formatTask')(task);

              // Construct command and send
              var command = this.constructTaskCommand('AssignPersonToPartialTask', clientSequence, boardLocation, boardDate, task.id);
              command.commandContent.personId = person.id;
              command.commandContent.locationId = locationId;
              command.commandContent.assignType = taskType;
              command.commandContent.position = position;

              return command;

            },

            createAssignPersonToTaskCommand : function(states, clientSequence, boardLocation, boardDate, task, person, position, uniqueId, taskIndicator) {

              // Change person status

              person.assigned = true;
              person.taskIndicator = taskIndicator;
              // Assign person to task
              task['assignedPerson' + position].personId = person.id;

              task = $filter('formatTask')(task);

              // Construct command and send
              var command = this.constructTaskCommand('AssignPersonToTask', clientSequence, boardLocation, boardDate, task.id);
              command.commandContent.personId = person.id;
              command.commandContent.position = position;
              command.commandContent.assignType = ((person.availableNextDay && !task["assignedPerson" + position].type) ? 'NEXT_DAY' : task["assignedPerson" + position].type);              
            	  
              command.commandContent.uniqueId = uniqueId;

              return command;
            },

            createAssignSupervisorToSectionCommand : function(states, clientSequence, boardLocation, boardDate, section, person, taskIndicator) {
              
              if (!person.assigned)
                return null; // supervisor must be already assigned

              var tasks = section.tasks;
              var duplicate = false;
              Object.keys(tasks).forEach(function(taskId) {
                var task = tasks[taskId];
                /* introduce sae logic as back end to filter duplicate assignment */
                if(task["taskSupervisorAssignments"].length>0)
                  {
                   for(var i=0 ; i<task["taskSupervisorAssignments"].length ; i++){
                     var assignment=task["taskSupervisorAssignments"][i];
                     if(assignment.personId == person.id && assignment.taskIndicator == taskIndicator){
                       duplicate=true;
                       }
                     }
                    }
                if(!duplicate){
                   if (!task["taskSupervisorAssignments"])
                     task["taskSupervisorAssignments"] = [];
                   var sequenceNumber = task["taskSupervisorAssignments"].length + 1;
                   task["taskSupervisorAssignments"].push({
                     sequenceNumber : sequenceNumber,
                     taskIndicator : taskIndicator,
                     personId : person.id,
                     personFullName : person.fullName
                     });
                   }
                duplicate = false;
              })

              if (!section.sectionSupervisorAssignments)
                section.sectionSupervisorAssignments = [];
              section.sectionSupervisorAssignments.push({
                personId : person.id,
                taskIndicator : taskIndicator
              });

              var command = this.constructTaskCommand('AssignSupervisor', clientSequence, boardLocation, boardDate,
                  section.id);

              command.commandContent.personId = person.id;
              command.commandContent.sectionId = section.id;
              command.commandContent.taskId = null;
              command.commandContent.sequenceNumber = null;
              command.commandContent.taskIndicator = taskIndicator;

              return command;
            },
            
            createAssignSupervisorToSubcategoryCommand : function(states, clientSequence, boardLocation, boardDate,
                subcategory, person, taskIndicator) {
              
              if (!person.assigned)
                return null; // supervisor must be already assigned

              var tasks = subcategory.tasks;
              var duplicate = false;
              Object.keys(tasks).forEach(function(taskId) {
                var task = tasks[taskId];
                if(task["taskSupervisorAssignments"].length>0)
                  {
                   for(var i=0 ; i<task["taskSupervisorAssignments"].length ; i++){
                     var assignment=task["taskSupervisorAssignments"][i];
                     if(assignment.personId == person.id && assignment.taskIndicator == taskIndicator){
                       duplicate=true;
                       }
                     }
                    }
                if(!duplicate){
                   if (!task["taskSupervisorAssignments"])
                     task["taskSupervisorAssignments"] = [];
                   var sequenceNumber = task["taskSupervisorAssignments"].length + 1;
                   task["taskSupervisorAssignments"].push({
                     sequenceNumber : sequenceNumber,
                     taskIndicator : taskIndicator,
                     personId : person.id,
                     personFullName : person.fullName
                     });
                   }
                duplicate = false;
              })

              if (!subcategory.supervisorAssignments)
                  subcategory.supervisorAssignments = [];
              subcategory.supervisorAssignments.push({
                personId : person.id,
                taskIndicator : taskIndicator
              });

              var command = this.constructTaskCommand('AssignSupervisor', clientSequence, boardLocation, boardDate,
                  subcategory.id);

              command.commandContent.personId = person.id;
              command.commandContent.subcategoryId = subcategory.id;
              command.commandContent.taskId = null;
              command.commandContent.sequenceNumber = null;
              command.commandContent.taskIndicator = taskIndicator;

              return command;
            },

            createAssignSupervisorToTaskCommand : function(states, clientSequence, boardLocation, boardDate, task, person, taskIndicator) {
              
              if (!person.assigned)
                return null; // supervisor must be already assigned

              person.assigned = true;
              if (!task["taskSupervisorAssignments"])
                task["taskSupervisorAssignments"] = [];

              var sequenceNumber = task["taskSupervisorAssignments"].length + 1;

              task["taskSupervisorAssignments"].push({
                sequenceNumber : sequenceNumber,
                taskIndicator : taskIndicator,
                personId : person.id,
                personFullName : person.fullName
              });
              var command = this.constructTaskCommand('AssignSupervisor', clientSequence, boardLocation, boardDate,
                  null);

              command.commandContent.personId = person.id;
              command.commandContent.sectionId = null;
              command.commandContent.taskId = task.id;
              command.commandContent.sequenceNumber = task["taskSupervisorAssignments"].length;
              command.commandContent.taskIndicator = taskIndicator;

              return command;
            },
            
            createSetPersonNextDayAvailableCommand : function(states, clientSequence, boardLocation, boardDate, personId){
            	var command = {
                     'commandName' : "SetNextDayAvailable",
                     'clientSequence' : clientSequence,
                     'date' : boardDate,
                     'location' : boardLocation,
                     'commandContent' : {
                    	 'personId' : personId
                     }
                   }
                   return command;
            },
            
            createRemovePersonNextDayAvailableCommand : function(states, clientSequence, boardLocation, boardDate, personId){
            	var command = {
                     'commandName' : "RemoveNextDayAvailable",
                     'clientSequence' : clientSequence,
                     'date' : boardDate,
                     'location' : boardLocation,
                     'commandContent' : {
                    	 'personId' : personId
                     }
                   }
                   return command;
            },


            createUnassignEquipmentFromTaskCommand : function(states, clientSequence, boardLocation, boardDate, equipment, tasks, taskMap, taskObject, locationId) {

              var task = taskObject;
              
              if (typeof taskObject !== 'object') {
                task = BoardValueService.tasks.tasksMap[task];
              }

              // Unassign equipment from task
              var equipmentId = task.assignedEquipment.equipmentId;

              if (task.groupId) {
                var subcategoryTaskId, index = -1;
                for (var i = 0; i < taskMap.length; i++) {
                  if (taskMap[i].taskId === task.id) {
                    subcategoryTaskId = taskMap[i].subcategoryTaskId;
                    index = i;
                  }
                }
                var homogeneous = true;
                for (var i = 0; i < taskMap.length; i++) {
                  if (taskMap[i].groupId === task.groupId)
                    if (taskMap[i].subcategoryTaskId !== subcategoryTaskId)
                      homogeneous = false;
                }

                if (homogeneous) {
                  for (var i = 0; i < taskMap.length; i++) {
                    if (taskMap[i].groupId === task.groupId) { // will change
                      // this late to
                      // push it to
                      // array
                      var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId], subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId], taskU;
                      if (subcategory.subcategory.containsSections)
                        taskU = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                      else
                        taskU = subcategory.tasks[taskMap[i].taskId]
                      taskU.assignedEquipment.equipmentId = null;
                    }
                  }
                } else {
                  task.assignedEquipment.equipmentId = null;
                }
              } else {
                task.assignedEquipment.equipmentId = null;
              }

              task = $filter('formatTask')(task);

              // Construct command and send
              var command;
              if (!task.groupId) {
                command = this.constructTaskCommand('UnassignEquipmentFromTask', clientSequence, boardLocation, boardDate, task.id);
              } else {
                command = this.constructTaskCommand('UnassignEquipmentFromPartialTask', clientSequence, boardLocation, boardDate, task.id);
                command.commandContent.locationId = locationId;
              }
              command.commandContent.equipmentId = equipmentId;

              return command;
            },

            createUnassignPersonFromTaskCommand : function(states, clientSequence, boardLocation, boardDate, personnel, tasks, position, taskMap, taskObject, locationId, person) {

              var task = taskObject;

              if (typeof taskObject !== 'object') {
                task = BoardValueService.tasks.tasksMap[task];
              }

              // Unassign person from task
              var personId;
              if (position == 1)
                personId = task.assignedPerson1.personId;
              else if (position == 2)
                personId = task.assignedPerson2.personId;

              for (var i = 0; i < taskMap.length; i++) {
                if (taskMap[i].groupId === task.groupId) {
                  var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId], subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId], taskU;
                  if (subcategory.subcategory.containsSections)
                    taskU = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                  else
                    taskU = subcategory.tasks[taskMap[i].taskId]
                  task.clearPersonnelAssignment(position);
                }
              }

              task.clearPersonnelAssignment(position);
              task = $filter('formatTask')(task);
              
              // Construct command and send
              var command;
              if (!task.groupId) {
                command = this.constructTaskCommand('UnassignPersonFromTask', clientSequence, boardLocation, boardDate,
                    task.id);
              } else {
                command = this.constructTaskCommand('UnassignPersonFromPartialTask', clientSequence, boardLocation,
                    boardDate, task.id);
                command.commandContent.locationId = locationId;
              }
              command.commandContent.personId = personId;
              command.commandContent.position = position;

              return command;
            },

            createUnassignAndAssignPersonFromTaskToTaskCommand: function (clientSequence, boardLocation, boardDate, personnel,tasks, oldPosition, newPosition, taskMap, oldTaskObject, newTaskObject, locationId, personId) {

                var oldTask = typeof oldTaskObject !== 'object' ? BoardValueService.tasks.tasksMap[oldTaskObject] : oldTaskObject;
                var newTask = typeof newTaskObject !== 'object' ? BoardValueService.tasks.tasksMap[newTaskObject] : newTaskObject;
                var command;

                for (var i = 0, len = taskMap.length; i < len; i ++) {
                    if (taskMap[i].groupId === oldTask.groupId) {
                        var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId];
                        var subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId];
                        var taskU;
                        if (subcategory.subcategory.containsSections) {
                            taskU = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                        } else {
                            taskU = subcategory.tasks[taskMap[i].taskId]
                        }
                        taskU.clearPersonnelAssignment(oldPosition);
                    }
                }

                // Construct command and send
                command = this.constructTaskCommand('AssignPersonFromTaskToTask', clientSequence, boardLocation, boardDate, oldTask.id);
                command.commandContent.personId = personId;
                command.commandContent.position = oldPosition;
                command.commandContent.newPosition = newPosition;
                command.commandContent.newTaskId = newTask.id;
                command.commandContent.assignType = newTask['assignedPerson' + oldPosition].type;

                return command;
            },

            createUnassignSupervisorCommand : function(states, clientSequence, boardLocation, boardDate, tasks, section, subcategory, task) {
                var taskId, command, taskRemoval = [];
                if (!task) {

                    if (!subcategory) {
                        var section = findSection(section.id, tasks);
                        command = this.constructTaskCommand('UnassignSupervisor', clientSequence, boardLocation, boardDate,
                            null);
                        command.commandContent.sectionId = section.id;
                        command.commandContent.supervisorAssignments = angular
                        .copy(section.sectionSupervisorAssignments);
                        Object
                        .keys(section.tasks)
                        .forEach(
                            function(taskId) {
                                taskRemoval=[];
                                var task = section.tasks[taskId]
                                for (var j = 0; j < section.sectionSupervisorAssignments.length; j++) {
                                    for (var i = 0; i < task.taskSupervisorAssignments.length; i++) {
                                        if (task.taskSupervisorAssignments[i].taskIndicator === section.sectionSupervisorAssignments[j].taskIndicator
                                            && task.taskSupervisorAssignments[i].personId === section.sectionSupervisorAssignments[j].personId) {
                                            if(taskRemoval.length<section.sectionSupervisorAssignments.length)
                                                taskRemoval.push(i);
                                        }
                                    }
                                }

                                for (var k = 0; k < taskRemoval.length; k++) {
                                    task.taskSupervisorAssignments.splice(taskRemoval[k]-k,taskRemoval[k]-k+1);                         
                                }
                            })                       
                            section.sectionSupervisorAssignments=[];
                    } else {
                        var subcategory = findSubcategory(subcategory.id, tasks);
                        command = this.constructTaskCommand('UnassignSupervisor', clientSequence, boardLocation, boardDate,
                            null);
                        command.commandContent.subcategoryId = subcategory.id;
                        command.commandContent.supervisorAssignments = angular
                        .copy(subcategory.supervisorAssignments);
                        Object
                        .keys(subcategory.tasks)
                        .forEach(
                            function(taskId) {
                                taskRemoval=[];
                                var task = subcategory.tasks[taskId]
                                for (var j = 0; j < subcategory.supervisorAssignments.length; j++) {
                                    for (var i = 0; i < task.taskSupervisorAssignments.length; i++) {
                                        if (task.taskSupervisorAssignments[i].taskIndicator === subcategory.supervisorAssignments[j].taskIndicator
                                            && task.taskSupervisorAssignments[i].personId === subcategory.supervisorAssignments[j].personId) {
                                            if(taskRemoval.length < subcategory.supervisorAssignments.length)
                                                taskRemoval.push(i);
                                        }
                                    }
                                }

                                for (var k = 0; k < taskRemoval.length; k++) {
                                    task.taskSupervisorAssignments.splice(taskRemoval[k]-k,taskRemoval[k]-k+1);                         
                                }
                            })                       
                            subcategory.supervisorAssignments=[];
                    }

                } else {
                    var task = findTask(task.id, tasks);
                    command = this.constructTaskCommand('UnassignSupervisor', clientSequence, boardLocation, boardDate,
                        task.id);
                    command.commandContent.supervisorAssignments =  [];
                    for (var i = 0; i < task.taskSupervisorAssignments.length; i++) {
                        var temp = {
                            personId: task.taskSupervisorAssignments[i].personId,
                            taskIndicator:  task.taskSupervisorAssignments[i].taskIndicator
                        };
                        command.commandContent.supervisorAssignments.push(temp);
                    }
                    
                    if (section && section != 'null') {
                        var supRemoval = [];
                        var section = findSection(section.id, tasks);                  
                        for (var j = 0; j < task.taskSupervisorAssignments.length; j++) {
                            for (var i = 0; section.sectionSupervisorAssignments
                            && i < section.sectionSupervisorAssignments.length; i++) {
                                if (task.taskSupervisorAssignments[j].taskIndicator === section.sectionSupervisorAssignments[i].taskIndicator
                                    && task.taskSupervisorAssignments[j].personId === section.sectionSupervisorAssignments[i].personId) {
                                    supRemoval.push(i);
                                }
                            }
                        }
                        if (supRemoval.length > 0) {
                            for (var k = 0; k < supRemoval.length; k++) {
                                section.sectionSupervisorAssignments.splice(supRemoval[k]-k, 1);
                            }
                        }
                    } else if (subcategory && subcategory != 'null') {
                        var supRemoval = [];
                        var subcategory = findSubcategory(subcategory.id, tasks);                 
                        for (var j = 0; j < task.taskSupervisorAssignments.length; j++) {
                            for (var i = 0; subcategory.supervisorAssignments
                            && i < subcategory.supervisorAssignments.length; i++) {
                                if (task.taskSupervisorAssignments[j].taskIndicator === subcategory.supervisorAssignments[i].taskIndicator
                                    && task.taskSupervisorAssignments[j].personId === subcategory.supervisorAssignments[i].personId) {
                                    supRemoval.push(i);
                                }
                            }
                        }
                        if (supRemoval.length > 0) {
                            for (var k = 0; k < supRemoval.length; k++) {
                                subcategory.supervisorAssignments.splice(supRemoval[k]-k, 1);
                            }
                        }
                    }

                    task.taskSupervisorAssignments.length = 0;
                }

                return command;
            },

            createUnassignAndAssignEquipmentFromTaskToTaskCommand: function (clientSequence, boardLocation, boardDate, personnel, tasks, taskMap, oldTaskObject, newTaskObject, locationId, equipmentId) {

              var oldTask = typeof oldTaskObject !== 'object' ? BoardValueService.tasks.tasksMap[oldTaskObject] : oldTaskObject;
              var newTask = typeof newTaskObject !== 'object' ? BoardValueService.tasks.tasksMap[newTaskObject] : newTaskObject;
              var command;

              // refactor
              for (var i = 0, len = taskMap.length; i < len; i ++) {
                if (taskMap[i].groupId === oldTask.groupId) {
                  var category = tasks.locations[taskMap[i].locationId].locationShifts[taskMap[i].locationShiftId].shiftCategories[taskMap[i].shiftCategoryId];
                  var subcategory = category.subcategoryTasks[taskMap[i].subcategoryTaskId];
                  var taskU;
                  if (subcategory.subcategory.containsSections) {
                    taskU = subcategory.sections[taskMap[i].sectionId].tasks[taskMap[i].taskId]
                  } else {
                    taskU = subcategory.tasks[taskMap[i].taskId]
                  }
                  taskU.clearEquipmentAssignment();
                }
              }

              // clear task
              oldTask.clearEquipmentAssignment();
              $filter('formatTask')(oldTask);

              // update task now, process command will set other data later
              newTask.assignedEquipment.equipmentId = equipmentId;
              newTask = $filter('formatTask')(newTask);

              command = this.constructTaskCommand('AssignEquipmentFromTaskToTask', clientSequence, boardLocation, boardDate, oldTask.id);
              command.commandContent.equipmentId = equipmentId;
              command.commandContent.newTaskId = newTask.id;
              return command;
            },

            /** Section - Perform operations on server - End */

            /** Section - Create Commands for Server - End */

            processEquipmentFromPartialTaskCommand : function(states, tasks, equipment, linkedTaskMap, command) {
              var e = equipment[command.commandContent.equipmentId];
              e.assigned = command.commandContent.assigned;
              e.states = command.commandContent.states;

              var task;
              var assignments = command.commandContent.assignments;
              for (var i = 0; i < assignments.length; i++) {
                task = findTask(assignments[i].taskId, tasks);
                task.assignedEquipment.equipment = null;
                task.assignedEquipment.equipmentId = null;
                task.assignedEquipment.assignmentTime = null;
                task = $filter('formatTask')(task);
              }          
              
              TaskIndicatorService.applyTasksIndicators(linkedTaskMap, tasks);

            },

            processEquipmentFromTaskCommand : function(states, tasks, equipment, command) {

              var equipment = BoardValueService.equipment;
              var tasks = BoardValueService.tasks;

              var equipmentId = command.commandContent.equipmentId;
              var task = findTask(command.commandContent.taskId, tasks);

              equipment[equipmentId].assigned = command.commandContent.assigned;
              equipment[equipmentId].states = command.commandContent.states;

              task.assignedEquipment.equipmentId = null;
              task.assignedEquipment.equipment = null;
              task.assignedEquipment.assignmentTime = null;
              task = $filter('formatTask')(task);

            },

            processEquipmentToPartialTaskCommand : function(user, states, tasks, equipment, linkedTaskMap, command, root_scope) {
              var equipmentId = command.commandContent.equipmentId;
              var assignments = command.commandContent.assignments;

              // Update equipment assignment flag and state
              equipment[equipmentId].assigned = command.commandContent.assigned;
              equipment[equipmentId].states = command.commandContent.states;

              var task;
              for (var i = 0; i < assignments.length; i++) {
                task = findTask(assignments[i].taskId, tasks);
                task.assignedEquipment.assignmentTime = assignments[i].assignmentTime;
                task.assignedEquipment.completed = assignments[i].completed;
                task.assignedEquipment.equipmentId = equipmentId; // override from server
                task = $filter('formatTask')(task);
              }

              TaskIndicatorService.applyTasksIndicators(linkedTaskMap, tasks);

              if (user.username !== command.user)
                root_scope.pushedId = equipmentId;
            },

            processEquipmentToTaskCommand : function(user, states, tasks, equipment, command, root_scope) {
              var equipmentId = command.commandContent.equipmentId;
              var task = findTask(command.commandContent.taskId, tasks);
              // Update equipment assignment flag and state
              equipment[equipmentId].assigned = command.commandContent.assigned;
              equipment[equipmentId].states = command.commandContent.states;

              // Set task assignment details
              task.assignedEquipment.assignmentTime = command.commandContent.assignmentTime;
              task.assignedEquipment.completed = command.commandContent.completed;
              task.assignedEquipment.equipmentId = equipmentId; // override from server

              task = $filter('formatTask')(task);

              if (user.username !== command.user)
                root_scope.pushedId = equipmentId;
            },

            processErrorCommand : function(user, command, root_scope) {
                switch (command.commandContent.command.name) {
                  case 'AssignEquipmentToTask':
                  case 'AssignEquipmentToPartialTask':
                    processErrorInEquipmentToTaskCommand(user, command, root_scope);
                    break;
                  case 'AssignPersonToTask':
                  case 'AssignPersonToPartialTask':
                    processErrorInPersonToTaskCommand(user, command, root_scope);
                    break;
                  default:
                    //
                }
              },

            processPersonFromPartialTaskCommand : function(states, tasks, personnel, command) {
              var personId = command.commandContent.personId;
              var assignments = command.commandContent.assignments;
              
              // Update person assignment flag and state
              personnel[personId].assigned = command.commandContent.assigned;
              personnel[personId].assignedAnywhere = command.commandContent.assignedAnywhere;
              personnel[personId].state = command.commandContent.state;
              $filter('processPersonState')(personnel[personId]);
              
              var position = command.commandContent.position;
              var task;
              for (var i = 0; i < assignments.length; i++) {
                task = findTask(assignments[i].taskId, tasks);
                task['assignedPerson' + position].assignmentTime = null;
                task['assignedPerson' + position].personId = null;
                task['assignedPerson' + position].type = null;
                task = $filter('formatTask')(task);
              }
            },

            processPersonFromTaskCommand : function(states, tasks, personnel, command) {
              var personId = command.commandContent.personId, position = command.commandContent.position;
              personnel[personId].assigned = command.commandContent.assigned;
              personnel[personId].assignedAnywhere = command.commandContent.assignedAnywhere;
              personnel[personId].state = command.commandContent.state;
              
              if(command.commandContent.shift){
            	  var found = _.findWhere(personnel[personId].assignedNextDayShifts, {id: command.commandContent.shift.id});
    			  if(found)
    				  personnel[personId].assignedNextDayShifts = _.without(personnel[personId].assignedNextDayShifts, found);
              }            	 
              
              $filter('processPersonState')(personnel[personId]);

              var task = findTask(command.commandContent.taskId, tasks);
              task['assignedPerson' + position].assignmentTime = null;
              task['assignedPerson' + position].personId = null;    
              task['assignedPerson' + position].type = null;

              task = $filter('formatTask')(task);
                  
              if (command.commandContent.tasks) {
                  for (var i = 0; i < command.commandContent.tasks.length; i++) {
                      var tCommand = command.commandContent.tasks[i];
                      var t = tasks.tasksMap[tCommand.taskId];
                      if (tCommand.supervisorId) {
                          // Supervisor assignment 
                          t.clearSupervisorAssignment(tCommand.supervisorId);
                          
                          // Supervisor section assignment
                          var supervisorIds = [tCommand.supervisorId];
                          if (t.sectionId) {
                              this.removeSupervisorsFromSection(tasks, t.sectionId, supervisorIds);
                          } else if (t.subcategoryId) {
                              this.removeSupervisorsFromSubcategory(tasks, t.subcategoryId, supervisorIds);
                          }
                      } 
                      
                      t = $filter('formatTask')(t);
                  }
              }
            },

            processPersonFromTaskToTaskCommand : function(tasks, personnel, command) {
              var person = personnel[command.commandContent.personId];
              var newPosition = command.commandContent.newPosition;

              // update person
              person.updateState(command.commandContent.assigned, command.commandContent.assignedAnywhere, command.commandContent.state);

              // update new task
              var newTask = findTask(command.commandContent.newTaskId, tasks);
              var taskPerson = newTask['assignedPerson' + newPosition];
              var oldTask = findTask(command.commandContent.taskId, tasks);
              taskPerson.type = command.commandContent.assignType;
              newTask.setPersonnelAssignment(command.commandContent.assignmentTime, command.commandContent.completed, command.commandContent.newPosition, { id: command.commandContent.personId });

              // update old task
              var oldAssignedPerson = oldTask['assignedPerson' + command.commandContent.position];

              // assign person to task
              newTask['assignedPerson' + newPosition].personId = person.id;
              newTask['assignedPerson' + newPosition].completed = oldAssignedPerson.completed;
              newTask = $filter('formatTask')(newTask);

              // clear old task of person
              oldTask.clearPersonnelAssignment(command.commandContent.position);
              $filter('formatTask')(oldTask);
            },

            processEquipmentFromTaskToTaskCommand : function(tasks, equipment, command) {
              var equipment = equipment[command.commandContent.equipmentId];
              // update states
              equipment.updateState(command.commandContent.assigned, command.commandContent.states);
              // update new task
              var newTask = findTask(command.commandContent.newTaskId, tasks);
              newTask.setEquipmentAssignment(command.commandContent.assignmentTime, command.commandContent.completed, { id: command.commandContent.equipmentId });
              // update old task
              var oldTask = findTask(command.commandContent.taskId, tasks);
              oldTask.clearEquipmentAssignment();
            },

            processPersonToPartialTaskCommand : function(user, states, tasks, personnel, command, root_scope) {
              var personId = command.commandContent.personId;
              var assignments = command.commandContent.assignments;
                
              // Update person assignment flag and state
              personnel[personId].assigned = command.commandContent.assigned;
              personnel[personId].assignedAnywhere = command.commandContent.assignedAnywhere;
              personnel[personId].state = command.commandContent.state;
              $filter('processPersonState')(personnel[personId]);
              
              var position = command.commandContent.position;
              var task;
              for (var i = 0; i < assignments.length; i++) {
                task = findTask(assignments[i].taskId, tasks);
                task['assignedPerson' + position].assignmentTime = assignments[i].assignmentTime;
                task['assignedPerson' + position].completed = assignments[i].completed;
                task['assignedPerson' + position].type=command.commandContent.assignType;
                task['assignedPerson' + position].personId = personId;
                task = $filter('formatTask')(task);
              }

              if (user.username !== command.user)
                root_scope.pushedId = personId;
            },
            
            processAddDayBeforeCommand : function(user, states, personnel, command, root_scope) {
            	var shifts = command.commandContent.shifts;
            	var person = personnel[command.commandContent.personId];
            	
            	if(shifts){
            		shifts.forEach(function(s, i){
            		  var found = _.findWhere(person.dayBeforeShifts, {id: s.id});
           			  if(!found)
           				person.dayBeforeShifts.push(s);
            		});
            	}        
            	root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
            },
            
            processRemoveDayBeforeCommand : function(user, states, personnel, command, root_scope) {
            	var shifts = command.commandContent.shifts;
            	var person = personnel[command.commandContent.personId];
            	
            	if(shifts){
            		shifts.forEach(function(s, i){
            		  var found = _.findWhere(person.dayBeforeShifts, {id: s.id});
           			  if(found)
           				person.dayBeforeShifts = _.without(person.dayBeforeShifts, found);
           			  
           			
            		});
            	}
            	person.availableNextDay = command.commandContent.nextDayAvailable;
       			person = $filter('extendPerson')(person);
            	root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
            },
            
            processUpdateDayBeforeCommand : function(user, states, personnel, command, root_scope) {
            	var remove = command.commandContent.remove;
            	var add = command.commandContent.add;
            	var person = personnel[command.commandContent.personId];
            	
            	if(add && remove){
            		remove.forEach(function(s, i){
           			 var found = _.findWhere(person.dayBeforeShifts, {id: s.id});
             			  if(found)
             				person.dayBeforeShifts = _.without(person.dayBeforeShifts, found);
             		});
            		
            		add.forEach(function(s, i){
            		  var found = _.findWhere(person.dayBeforeShifts, {id: s.id});
           			  if(!found)
           				person.dayBeforeShifts.push(s);
            		});
            		            		
            	}        
            	root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
            },

            processSetNextDayAvailableCommand : function(user, states, personnel, command, root_scope) {
            	var person = personnel[command.commandContent.personId];
            	person.availableNextDay = command.commandContent.nextDayAvailable;
            	person = $filter('extendPerson')(person);
            	root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
            	
            },
            processRemoveNextDayAvailableCommand : function(user, states, personnel, command, root_scope) {
            	var person = personnel[command.commandContent.personId];
            	person.availableNextDay = command.commandContent.nextDayAvailable;
            	person = $filter('extendPerson')(person);
            	root_scope.$broadcast('UPDATE-PERSONNEL-PANE');
            	
            },
            processPersonToTaskCommand : function(user, states, tasks, personnel, command, root_scope) {
              var personId = command.commandContent.personId;
              
              // Update person assignment flag and state
              personnel[personId].assigned = command.commandContent.assigned;
              personnel[personId].assignedAnywhere = command.commandContent.assignedAnywhere;
              
              if(command.commandContent.shift){
            	  var found = _.findWhere(personnel[personId].assignedNextDayShifts, {id: command.commandContent.shift.id});
            	  if(!found)
            		  personnel[personId].assignedNextDayShifts.push(command.commandContent.shift);
              }
            	  
              
              personnel[personId].state = command.commandContent.state;
              $filter('processPersonState')(personnel[personId]);
              
              var position = command.commandContent.position;
              var task = findTask(command.commandContent.taskId, tasks);
              task['assignedPerson' + position].assignmentTime = command.commandContent.assignmentTime;
              task['assignedPerson' + position].completed = command.commandContent.completed;
              task['assignedPerson' + position].personId = personId;
              task['assignedPerson' + position].type=command.commandContent.assignType;
              task = $filter('formatTask')(task);
              
              if (user.username !== command.user)
                root_scope.pushedId = personId;
            },
            
            processSupervisorToTaskCommand : function(user, states, tasks, personnel, command, root_scope) {
                var personId = command.commandContent.personId,
                sequenceNumber = command.commandContent.sequenceNumber,
                position = sequenceNumber - 1,
                taskIndicator = command.commandContent.taskIndicator;
                var person = personnel[personId];

                var task;
                var section;
                var subcategory;
                if (command.commandContent.taskId) {
                    task = findTask(command.commandContent.taskId, tasks);
                    person.assigned = true;
                    $filter('processPersonState')(person);

                    if (!task["taskSupervisorAssignments"]) {
                        task["taskSupervisorAssignments"] = [];
                    }

                    task["taskSupervisorAssignments"][position] = {
                        personId : personId,
                        personFullName : person.fullName,
                        sequenceNumber : sequenceNumber,
                        taskIndicator : taskIndicator
                    }
                    task = $filter('formatTask')(task);

                    if (task.sectionId)
                        section = findSection(task.sectionId, tasks);
                    else if (task.subcategoryId) 
                        subcategory = findSubcategory(task.subcategoryId, tasks);

                } else {            
                    var taskMap = command.commandContent.taskSequenceNumberMap;

                    Object.keys(taskMap).forEach(function(taskId) {
                        var pos = taskMap[taskId] - 1;
                        task = findTask(taskId, tasks);

                        if (!task["taskSupervisorAssignments"]) {
                            task["taskSupervisorAssignments"] = [];
                        }

                        if (!task["taskSupervisorAssignments"][pos]) {
                            task["taskSupervisorAssignments"][pos] = {
                                personId : personId,
                                personFullName : person.fullName,
                                sequenceNumber : taskMap[taskId],
                                taskIndicator : taskIndicator
                            };
                        }

                        task = $filter('formatTask')(task);

                    });

                    if (task.sectionId)
                        section = findSection(task.sectionId, tasks);
                    else if (task.subcategoryId) 
                        subcategory = findSubcategory(task.subcategoryId, tasks);
                }

                // Update section or subcategory
                if (section) {
                    var assignedToAllTasks = true;
                    Object.keys(section.tasks).forEach(function(taskId) {
                        var task = section.tasks[taskId];
                        var found = false;
                        for (var j = 0; j < task.taskSupervisorAssignments.length; j++) {
                            if (task.taskSupervisorAssignments[j].personId == personId &&
                                task.taskSupervisorAssignments[j].taskIndicator == taskIndicator) {
                                found = true;
                                break;
                            }
                        }    
                        if (!found) {
                            assignedToAllTasks = false;  
                        }
                    });
                    
                    if (assignedToAllTasks) {
                        if (!section.sectionSupervisorAssignments || section.sectionSupervisorAssignments.length === 0) {
                            section.sectionSupervisorAssignments = [];
                        }
                        var exists = false;
                        for (var i = 0; i < section.sectionSupervisorAssignments.length; i++) {
                            if (section.sectionSupervisorAssignments[i].personId == personId &&
                                section.sectionSupervisorAssignments[i].taskIndicator == taskIndicator) {
                                exists = true;                            
                            }
                        }
                        if (!exists) {
                            section.sectionSupervisorAssignments.push({
                                personId : personId,
                                taskIndicator : taskIndicator
                            })
                        }
                    }
                } else if (subcategory) {                  
                    var assignedToAllTasks = true;
                    Object.keys(subcategory.tasks).forEach(function(taskId) {
                        var task = subcategory.tasks[taskId];
                        var found = false;
                        for (var j = 0; j < task.taskSupervisorAssignments.length; j++) {
                            if (task.taskSupervisorAssignments[j].personId == personId &&
                                task.taskSupervisorAssignments[j].taskIndicator == taskIndicator) {
                                found = true;
                                break;
                            }
                        }                  
                        if (!found) {
                            assignedToAllTasks = false;  
                        }
                            
                    });                    
                    if (assignedToAllTasks) {
                        if (!subcategory.supervisorAssignments || subcategory.supervisorAssignments.length === 0) {
                            subcategory.supervisorAssignments = [];
                        }
                        var exists = false;
                        for (var i = 0; i < subcategory.supervisorAssignments.length; i++) {
                            if (subcategory.supervisorAssignments[i].personId == personId &&
                                subcategory.supervisorAssignments[i].taskIndicator == taskIndicator) {
                                exists = true;                            
                            }
                        }
                        if (!exists) {
                            subcategory.supervisorAssignments.push({
                                personId : personId,
                                taskIndicator : taskIndicator
                            })
                        }
                    }
                }  
            },
       
            processUnassignSupervisorCommand : function(user, states, tasks, personnel, command) {

                // Get supervisors to remove
                if (!command.commandContent.supervisorAssignments || command.commandContent.supervisorAssignments.length == 0) {
                    return;
                }
                var supervisorIds = [];
                for (var i = 0; i < command.commandContent.supervisorAssignments.length; i++) {
                    supervisorIds.push(command.commandContent.supervisorAssignments[i].personId);
                }

                if (command.commandContent.taskId) {

                    // Remove from task
                    var task = findTask(command.commandContent.taskId, tasks);     
                    var removedSupervisorIds = removeSupervisorsFromTask(task, supervisorIds);

                    // Remove from section                    
                    this.removeSupervisorsFromSection(tasks, task.sectionId, removedSupervisorIds);

                    // Remove from subcategory
                    if (!task.sectionId) 
                        this.removeSupervisorsFromSubcategory(tasks, task.subcategoryId, removedSupervisorIds);

                } else if (command.commandContent.sectionId) {
                    // Remove from section
                    var section = this.removeSupervisorsFromSection(tasks, command.commandContent.sectionId, supervisorIds);

                    // Remove from task
                    Object
                    .keys(section.tasks)
                    .forEach(
                        function(taskId) {
                            var task = section.tasks[taskId];
                            removeSupervisorsFromTask(task, supervisorIds);
                        })
                } else if (command.commandContent.subcategoryId) {
                    // Remove from subcategory
                    var subcategory = this.removeSupervisorsFromSubcategory(tasks, command.commandContent.subcategoryId, supervisorIds);

                    // Remove from task
                    Object
                    .keys(subcategory.tasks)
                    .forEach(
                        function(taskId) {
                            var task = subcategory.tasks[taskId];
                            removeSupervisorsFromTask(task, supervisorIds);
                        })
                }
            },

            /** Section - Handle Server Commands - End */

            sortByKey : function(array, key) {
              return array.sort(function(a, b) {
                var x = a[key];
                var y = b[key];

                if (typeof x == "string") {
                  x = x.toLowerCase();
                  y = y.toLowerCase();
                }

                return ((x < y) ? -1 : ((x > y) ? 1 : 0));
              });
            },

            updateTaskMap : function(tasks) {
              tasks.tasksMap = {};
              tasks.sectionMap = {};

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

              _.each(tasks.locations, function(location) {
                location2 = location;
                var shifts = location2.locationShifts;
                // SHIFT **************************

                for (var shift in shifts) {
                  shift2 = shifts[shift];
                  shiftCategories = shift2.shiftCategories;

                  // SHIFT CATEGORY **************************

                  for (var shiftcategory in shiftCategories) {
                    shiftcategory2 = shiftCategories[shiftcategory];
                    subcategorytasks = shiftcategory2.subcategoryTasks;

                    for (var categorytask in subcategorytasks) {
                      categorytask2 = subcategorytasks[categorytask];
                      tasksobject = categorytask2.tasks;
                      // REGULAR TASKS **************************

                      for (var task in tasksobject) {
                        task2 = tasksobject[task];
                        task2.subcategoryId = categorytask;
                        angular.extend(task2, TaskModel);
                        tasks.tasksMap[task] = task2;
                      }

                      sections = categorytask2.sections ;

                      for (var section in sections) {
                        // SECTION HEADING **************************
                        section2 = sections[section];
                        tasksobject = section2.tasks;
                        tasks.sectionMap[section2.id] = {
                          id: section2.id,
                          sectionName: section2.sectionName
                        }

                        // SECTION TASKS **************************

                        for (var task in tasksobject) {
                          task2 = tasksobject[task];
                          task2.sectionId = section2.id;
                          angular.extend(task2, TaskModel);
                          tasks.tasksMap[task] = task2;
                        }
                      }
                    }
                  }
                }
              });

              return tasks;
            },

            marshallTasks : function(data, categories, linkedTaskMap, shiftData, locationData, equipmentData, personnelData) {
              var allSubcategories = CategoryDataService.getAllFormattedSubcategories();

              data.tasksMap = {};
              data.sectionMap = {};
              data.deletedTasksMap = {};
              linkedTaskMap.length = 0;
              Object
                  .keys(data.locations)
                  .forEach(
                      function(key) {
                        if (data.locations[key].locationShifts) {
                          // copy location model to location
                          var locationModel = locationData.filter(function(val) {
                            return val.code === data.locations[key].locationCode;
                          });

                          data.locations[key].location = locationModel[0];
                          Object
                              .keys(data.locations[key].locationShifts)
                              .forEach(
                                  function(shiftId) {
                                    // copy shift model to location shift
                                    var locationShift = data.locations[key].locationShifts[shiftId];
                                    locationShift.shift = shiftData[locationShift.shiftId];
                                    locationShift.shift.type = InfoService.getShiftType(locationShift.shiftId);
                                    if (data.locations[key].locationShifts[shiftId].shiftCategories) {
                                      CategoryDataService.updateReferenceCategories(data.locations[key].locationShifts[shiftId].shiftCategories);
                                      Object
                                          .keys(data.locations[key].locationShifts[shiftId].shiftCategories)
                                          .forEach(
                                              function(categoryId) {
                                                if (categories != null)
                                                  for (var i = 0, locationShiftItem; i < categories.length; i++) {
                                                    locationShiftItem = data.locations[key].locationShifts[shiftId];
                                                    if (locationShiftItem.shiftCategories[categoryId].categoryId === categories[i].id) {
                                                      // copy category model to
                                                      // location shift if
                                                      // matched
                                                      var shiftModel = categories.filter(function(val) {
                                                        return val.id === categories[i].id;
                                                      });
                                                      locationShiftItem.shiftCategories[categoryId].category = angular.copy(shiftModel[0]);
                                                      locationShiftItem.shiftCategories[categoryId].allsubcategoryTasks = angular.copy(categories[i].subcategories);
                                                      if (data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks) {
                                                        var subs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].subcategoryTasks;
                                                        var allSubs = data.locations[key].locationShifts[shiftId].shiftCategories[categoryId].allsubcategoryTasks;
                                                        Object
                                                            .keys(subs)
                                                            .forEach(
                                                                function(subId) {
                                                                  for (var k = 0; k < allSubs.length; k++) {
                                                                    if (subs[subId].subcategoryId === allSubs[k].id) {
                                                                      // copy subcategory to current shift subcategory
                                                                        // tasks
                                                                      subs[subId].subcategory = allSubs[k];
                                                                      allSubs[k].selected = true;
                                                                    }
                                                                  }

                                                                  if(!subs[subId].subcategory) {
                                                                    subs[subId].subcategory = allSubcategories[subs[subId].subcategoryId];
                                                                  }

                                                                  if (subs[subId].subcategory.containsSections) {
                                                                    subs[subId].allSections = [];
                                                                    for (var l = 0; l < data.locations[key].location.sectionIds.length; l++) {
                                                                      var selected = false, cnt = 0, id = 0, name = 0;
                                                                      Object
                                                                          .keys(subs[subId].sections)
                                                                          .forEach(
                                                                              function(sectionId) {
                                                                                data.sectionMap[sectionId] = {
                                                                                  id: sectionId,
                                                                                  sectionName: subs[subId].sections[sectionId].sectionName
                                                                                }
                                                                                if (l === 0) {
                                                                                  Object
                                                                                      .keys(
                                                                                          subs[subId].sections[sectionId].tasks)
                                                                                      .forEach(
                                                                                          function(taskId) {
                                                                                            var taskObject = subs[subId].sections[sectionId].tasks[taskId];

                                                                                            if (taskObject.assignedEquipment && taskObject.assignedEquipment.eqipmentId) {
                                                                                              taskObject.assignedEquipment.equipment = angular.copy(equipmentData[taskObject.assignedEquipment.eqipmentId]);
                                                                                            }

                                                                                            if (taskObject.assignedPerson1 && taskObject.assignedPerson1.personId) {

                                                                                              try{
                                                                                                taskObject.assignedPerson1.person = angular.copy(personnelData[taskObject.assignedPerson1.personId]);
                                                                                              } catch(e) {
                                                                                                console.info(e);
                                                                                                console.info(personnelData);
                                                                                              }
                                                                                            }

                                                                                            if (taskObject.assignedPerson2 && taskObject.assignedPerson2.personId) {
                                                                                              taskObject.assignedPerson2.person = angular.copy(personnelData[taskObject.assignedPerson2.personId]);
                                                                                            }

                                                                                            taskObject.sectionId = sectionId;

                                                                                            // Add model to task and
                                                                                            // then map the task to its
                                                                                            // id
                                                                                            angular.extend(taskObject, TaskModel);
                                                                                            data.tasksMap[taskObject.id] = taskObject;
                                                                                            
                                                                                            // Populate linked task map
                                                                                            if (taskObject.linkedTaskChildId
                                                                                                || taskObject.linkedTaskParentId) {
                                                                                              linkedTaskMap
                                                                                                  .push({
                                                                                                    locationId : key,
                                                                                                    locationShiftId : shiftId,
                                                                                                    shiftCategoryId : categoryId,
                                                                                                    subcategoryTaskId : subId,
                                                                                                    sectionId : sectionId,
                                                                                                    containsSections : true,
                                                                                                    peoplePerTask : subs[subId].subcategory.peoplePerTask,
                                                                                                    taskId : taskId,
                                                                                                    groupId: taskObject.groupId,
                                                                                                    subcategoryId: subs[subId].subcategory.id,
                                                                                                    partialTaskSequence: taskObject.partialTaskSequence,
                                                                                                    sequence : taskObject.sequence,
                                                                                                    linkedTaskChildId : taskObject.linkedTaskChildId,
                                                                                                    linkedTaskParentId : taskObject.linkedTaskParentId,
                                                                                                  })
                                                                                            }
                                                                                          })
                                                                                }
                                                                                var sectionLinkedId = subs[subId].sections[sectionId].sectionId;
                                                                                if (sectionLinkedId && (sectionLinkedId == l + 1)) {
                                                                                  selected = true;
                                                                                  cnt = Object.keys(subs[subId].sections[sectionId].tasks).length;
                                                                                  id = sectionLinkedId;
                                                                                  name = subs[subId].sections[sectionId].sectionName;
                                                                                }
                                                                              })
                                                                      var locationSection = data.locations[key].location;
                                                                      subs[subId].allSections.push({
                                                                        id: (locationSection && locationSection.sectionIds[l]) || id,
                                                                        name: (locationSection && locationSection.sectionIds[l]) || name,
                                                                        selected: selected,
                                                                        numOfTasks: cnt,
                                                                        tasks: []
                                                                      });
                                                                    }
                                                                  } else {
                                                                    Object
                                                                    .keys(subs[subId].tasks)
                                                                    .forEach(
                                                                        function(taskId) {
                                                                          var taskObject = subs[subId].tasks[taskId];
                                                                          taskObject.subcategoryId = subId;
                                                                          angular.extend(taskObject, TaskModel);
                                                                          data.tasksMap[taskObject.id] = taskObject;

                                                                        if (taskObject.linkedTaskChildId
                                                                            || taskObject.linkedTaskParentId) {
                                                                          linkedTaskMap
                                                                          .push({
                                                                            locationId : key,
                                                                            locationShiftId : shiftId,
                                                                            shiftCategoryId : categoryId,
                                                                            subcategoryTaskId : subId,
                                                                            containsSections : false,
                                                                            peoplePerTask : subs[subId].subcategory.peoplePerTask,
                                                                            taskId : taskId,
                                                                            subcategoryId : subs[subId].subcategory.id,
                                                                            groupId: taskObject.groupId,
                                                                            partialTaskSequence: taskObject.partialTaskSequence,
                                                                            sequence : taskObject.sequence,
                                                                            linkedTaskChildId : taskObject.linkedTaskChildId,
                                                                            linkedTaskParentId : taskObject.linkedTaskParentId,
                                                                          })
                                                                        }
                                                                  })
                                                                  subs[subId].numOfTasks = Object
                                                                  .keys(subs[subId].tasks).length;
                                                                    subs[subId].hasTasks = true;
                                                                }
                                                      })
                                                      }
                                                      break;
                                                    }
                                                  }
                                              })
                                    }
                                  })
                        }
                      });

              TaskIndicatorService.applyTasksIndicators(linkedTaskMap, data);

              return data;
            },

            getPathStart: function() {
              return BoardDataService.getPathStart();
            },

            renderTasks: function() {
              var tasks = BoardValueService.tasks;
              var shiftData = BoardValueService.shiftData;
              var categoryData = CategoryDataService.getAllFormattedCategories();
              var subcategoryData = CategoryDataService.getAllFormattedSubcategories();
              var equipment = BoardValueService.equipment;
              var districtequipment = BoardValueService.equipmentdistricts;
              var districtEquipments = BoardValueService.districtEquipments;
              var districtPersons = BoardValueService.districtPersons;
              var personnel = BoardValueService.personnel;
              var personneldistricts = BoardValueService.personneldistricts;
              var locationscount =  Object.keys(tasks.locations).length;
              var taskcells = [];
              var shift2 = {};
              var shiftCategories = {};
              var shiftcategory2 = {};
              var subcategorytasks = {};
              var categorytask2 = {};
              var task2 = {};
              var sections = {};
              var section2 = {};
              var location2 = {};
              var tasksobject = [];
              var locationId = '';
              var hassections = false;
              var finalobject = {};
              var personnelblock = [];
              var personnelblockcount = 0;
              var tempcount = 0;
              var builtpersonnelblock = {};
              var itemsheight = 0;
              var cols = {};
              var column = 1;
              var columnname = 'col' + column;
              var results = [];
              var unavailablegroup = [];
              var unavailablecount = 0;
              var tasksequence = 0;
              var titlemapSup = '';
              var titlemap1 = '';
              var titlemap2 = '';
              var binLoadUpdated = false;
              var windowHeight = angular.element($window).height();
              var isLocked = $filter('isLocked');
              var supervisorTitle = '';
              var categorycontext = '';
              var canRemove = [];
              var canRemoveSectionSupervisor = '';
              var canRemoveSubcategorySupervisor = '';
              var locationsSorted = $filter('orderByLocation')(tasks.locations);
              var alreadyDetached1 = false;
              var alreadyDetached2 = false;

              var supObject = {};
              var EquipObj = {};
              var PersonnelObj1 = {};
              var PersonnelObj2 = {};
              var loccount = 1;
              var districtloccount = 1;

              var columncounts = {
                columncount: 0,
                districtcolumncount:0
              };

              var boardLocation = BoardDataService.getBoardData().boardLocation;
              var boroBoard = BoardValueService.isBoro;
              var realBoardDate = BoardDataService.getFormattedBoardDate();
              var isDistrict = false;
              var shifts = {};
              var finalobjectrender = {};

              // LOCATION **************************
              _.each(locationsSorted, function(location) {
                column = 0;
                itemsheight = 0;
                columnname = 'col' + column;
                location2 = location;
                location2.locationShiftsCount = location2.locationShifts ? Object.keys(location2.locationShifts).length : 0;
                locationId = location2.location.code.trim();
                finalobject[locationId] = {};
                finalobject[locationId].personnel = {};

                if(boardLocation === locationId || !boroBoard) {
                  isDistrict = false;
                  loccount = loccount + 1;
                } else {
                  isDistrict = true;
                  districtloccount = districtloccount + 1;
                }

                columncounts = columnCounts(columncounts, boroBoard, isDistrict);

                if(personneldistricts) {
                  finalobject[locationId].personnel = personneldistricts[locationId];
                }

                finalobject[locationId].locationId = locationId;
                finalobject[locationId].failedToLoad = location2.failedToLoad;
                finalobject[locationId].cols = {};
                finalobject[locationId].cols[columnname] = {};
                finalobject[locationId].cols[columnname].taskcells = [];
                finalobject[locationId].locationcount = Object.keys(locationsSorted).length;

                shifts = location2.locationShifts;
                shifts = $filter('orderByShiftId')(shifts);



                //SHIFT  **************************


                finalobjectrender = renderTaskShifts(finalobject[locationId], column, columnname, shift2, itemsheight, columncounts, shiftCategories, shiftcategory2, subcategorytasks, categorytask2, tasksobject, tasksequence, task2, sections, hassections, section2, canRemoveSectionSupervisor, canRemoveSubcategorySupervisor, supervisorTitle, supObject, titlemapSup, shifts, locationId, locationscount, location2, districtEquipments, districtPersons, shiftData, categoryData, subcategoryData, boroBoard, isDistrict, titlemap1, titlemap2, isLocked, personnel);

                finalobject[locationId] = finalobjectrender.finalobject;

                column = finalobjectrender.column;
                columnname = finalobjectrender.columnname;
                itemsheight = finalobjectrender.itemsheight;
                columncounts =  finalobjectrender.columncounts;

                if(location.servicelocations) {
                  _.each(location.servicelocations, function(serviceLocation) {
                    location2 = serviceLocation;

                    finalobject[locationId].cols[columnname].taskcells.push({type:'serviceloc',code: location2.locationCode, locationId: location2.locationCode, column: column});

                    itemsheight = checkRowcount(itemsheight, 39, locationscount);

                    if(itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject[locationId].cols[columnname] = {};
                      finalobject[locationId].cols[columnname].taskcells = [];
                    }

                    shifts = $filter('orderByShiftId')(location2.locationShifts);


                   finalobjectrender = renderTaskShifts(finalobject[locationId], column, columnname, shift2, itemsheight, columncounts, shiftCategories, shiftcategory2, subcategorytasks, categorytask2, tasksobject, tasksequence, task2, sections, hassections, section2, canRemoveSectionSupervisor, canRemoveSubcategorySupervisor, supervisorTitle, supObject, titlemapSup, shifts, locationId, locationscount, location2, districtEquipments, districtPersons, shiftData, categoryData, subcategoryData, boroBoard, isDistrict, titlemap1, titlemap2, isLocked, personnel);


                    finalobject[locationId] = finalobjectrender.finalobject;
                    column = finalobjectrender.column;
                    columnname = finalobjectrender.columnname;
                    itemsheight = finalobjectrender.itemsheight;
                    columncounts =  finalobjectrender.columncounts;

                  });
                }



                personnelblock = [];
                personnelblockcount = 0;

                if(personneldistricts){

                  builtpersonnelblock = buildPersonnelBlock(finalobject[locationId], personneldistricts[locationId], 'availablePersonnelFilter', 'Available/Unassigned', itemsheight, personnelblock, personnelblockcount, column, columnname, locationscount, 'available', 1, realBoardDate);
                  finalobject[locationId] = builtpersonnelblock.finalobject;
                  personnelblockcount =  builtpersonnelblock.personnelblockcount;
                  itemsheight = builtpersonnelblock.itemsheight;
                  personnelblock = builtpersonnelblock.personnelblock;
                  personnelblockcount = builtpersonnelblock.personnelblockcount;
                  column = builtpersonnelblock.column;
                  columnname = builtpersonnelblock.columnname;

                  personnelblock = [];
                  personnelblockcount = 0;

                  builtpersonnelblock = buildPersonnelBlock(finalobject[locationId], personneldistricts[locationId], 'mdaPersonnelFilter', 'MDA', itemsheight, personnelblock, personnelblockcount, column, columnname, locationscount, 'mda', realBoardDate);
                  finalobject[locationId] = builtpersonnelblock.finalobject;
                  personnelblockcount =  builtpersonnelblock.personnelblockcount;
                  itemsheight = builtpersonnelblock.itemsheight;
                  personnelblock = builtpersonnelblock.personnelblock;
                  personnelblockcount = builtpersonnelblock.personnelblockcount;
                  column = builtpersonnelblock.column;
                  columnname = builtpersonnelblock.columnname;

                  var unavailableCodes = [{code: 'VACATION/CHART', title:  'Vacation/Chart'}, {code: 'CHART', title:  'Charts'}, {code: 'XWP', title:  'XWP'}, {code: 'XWOP', title:  'XWOP'}, {code: 'SICK', title:  'Sick'}, {code: 'VACATION', title:  'Vacation'}, {code: 'LODI', title:  'LODI'}, {code: 'APP', title:  'APP'}, {code: 'AWOL', title:  'AWOL'}, {code: 'DIF', title:  'DIF'}, {code: 'SUSPENDED', title:  'Suspended'}, {code: 'TERMINAL LEAVE', title:  'Terminal Leave'}, {code: 'JURY DUTY', title:  'Jury Duty'}, {code: 'Mil Dty w/Pay', title:  'Mil Dty w/Pay'}, {code: 'Mil Dty w/o Pay', title:  'Mil Dty w/o Pay'}, {code: 'HONOR GUARD', title:  'Honor Guard'}, {code: 'FMLA', title:  'FMLA'}, {code: 'MATERNITY LEAVE', title:  'Maternity Leave'}, {code: 'MULTIPLE', title:  'Multiple'}];

                  unavailablecount = 0;
                  unavailablegroup = [];
                  _.each(unavailableCodes, function(codeobject) {

                    personnelblock = [];
                    personnelblockcount = 0;

                    results = $filter('unavailableCodeDistrictFilter')(personneldistricts[locationId], codeobject.code);
                    results = results.results;

                    if(results.length > 0){
                      unavailablecount = unavailablecount + results.length;
                      unavailablegroup.push({type: 'unavailablegroupheader', title: codeobject.title, column: column, count: results.length});

                      for (var result in results) {
                        personnelblockcount++;

                        results[result].formattedname = $filter('getFormattedName')(results[result]);
                        personnelblock.push(results[result]);

                        if (personnelblockcount > 1) {
                          unavailablegroup.push({type: 'personnelblock', personnelblock: personnelblock, column: column});
                          itemsheight = checkRowcount(itemsheight, 21, locationscount);
                          if(itemsheight === 0) {
                            column = column + .1;
                            columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                            columnname = 'col' + column;
                            finalobject[locationId].cols[columnname] = {};
                            finalobject[locationId].cols[columnname].taskcells = [];
                          }
                          personnelblockcount = 0;
                          personnelblock = [];
                        }
                      }

                      if (personnelblockcount > 0) {
                        unavailablegroup.push({type: 'personnelblock', personnelblock: personnelblock, column: column});
                        itemsheight = checkRowcount(itemsheight, 21, locationscount);
                        if(itemsheight === 0) {
                          column = column + .1;
                          columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                          columnname = 'col' + column;
                          finalobject[locationId].cols[columnname] = {};
                          finalobject[locationId].cols[columnname].taskcells = [];
                        }
                      }

                    }
                  });

                  if(unavailablecount > 0) {
                    finalobject[locationId].cols[columnname].taskcells.push({type: 'personnelheader', title: 'Unavailable', column: column, headerclass: 'unavailable', count: unavailablecount});
                    itemsheight = checkRowcount(itemsheight, 38, locationscount);
                    if(itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject[locationId].cols[columnname] = {};
                      finalobject[locationId].cols[columnname].taskcells = [];
                    }

                    finalobject[locationId].cols[columnname].taskcells = finalobject[locationId].cols[columnname].taskcells.concat(unavailablegroup);

                    finalobject[locationId].cols[columnname].taskcells.push({type: 'personnelfooter', title: '', column: column});
                    itemsheight = checkRowcount(itemsheight, 38, locationscount);
                    if(itemsheight === 0) {
                      column = column + .1;
                      columncounts = columnCounts(columncounts, boroBoard, isDistrict);
                      columnname = 'col' + column;
                      finalobject[locationId].cols[columnname] = {};
                      finalobject[locationId].cols[columnname].taskcells = [];
                    }
                  }
                }

              });

              finalobject = $filter('orderDistrict')(finalobject, BoardDataService.getBoardData().boardLocation);
              finalobject.columncount = columncounts.columncount;
              finalobject.loccount = loccount;
              finalobject.districtcolumncount = columncounts.districtcolumncount;
              finalobject.districtloccount = districtloccount;
              return finalobject;
            },
            
            removeSupervisorsFromSection: function(tasks, sectionKey, supervisorIds) {              
                var section = findSection(sectionKey, tasks);
                
                if (!section || !section.sectionSupervisorAssignments || section.sectionSupervisorAssignments.length == 0 || !supervisorIds || supervisorIds.length == 0)
                    return section;
                var i =  section.sectionSupervisorAssignments.length;
                while (i--) {
                    if (_.indexOf(supervisorIds, section.sectionSupervisorAssignments[i].personId) != -1) {
                        section.sectionSupervisorAssignments.splice(i, 1);
                    }
                }
                
                return section;
            },
            
            removeSupervisorsFromSubcategory: function(tasks, subcategoryKey, supervisorIds) {              
                var subcategory = findSubcategory(subcategoryKey, tasks);
                
                if (!subcategory || !subcategory.supervisorAssignments || subcategory.supervisorAssignments.length == 0 || !supervisorIds || supervisorIds.length == 0)
                    return subcategory;
                var i =  subcategory.supervisorAssignments.length;
                while (i--) {
                    if (_.indexOf(supervisorIds, subcategory.supervisorAssignments[i].personId) != -1) {
                        subcategory.supervisorAssignments.splice(i, 1);
                    }
                }
                
                return subcategory;
            }
          }
        });
