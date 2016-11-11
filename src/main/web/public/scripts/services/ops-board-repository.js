'use strict';

angular.module('OpsBoard').service(
    'OpsBoardRepository',
    function ($rootScope, $resource, states, $filter, $timeout, $window, $interval, user, titles, 
        EquipmentModel, PersonModel, TaskModel, WorkUnits, TaskSettingsService, BoardHelperService, 
        TaskHelperService, ReferenceDataService, RecentActivityLoggingService, WebSocketService, BoardDataService,
        EquipmentHelperService, PersonnelHelperService, ReportsService, AsyncService, OpsBoardInterval, $http, $q, $log, $sce,  
        BoardValueService, CategoryDataService) {

      $log.info('data loaded %s', $window.performance.now());

      // Board key
      var boardData = BoardDataService.getBoardData(),  
        boardDate = boardData.boardDate,
        boardLocation = boardData.boardLocation,
        pathStart = BoardDataService.getPathStart();

      var uniqueId;

      // Core data
      var equipment, personnel, shifts, onlineUsers, tasks, volunteerCounts, 
        districtpersonnel, boardQuota;
      var linkedTaskMap = [];
      var actions = [];
      var boardIsLocked = false;

      // Reference data
      var locations, categories;
      var locationsRefData = {};
      var isBoro = false,
      timerset = false;
      
      /****************************************************************************************************
        Live update changes:
        Added getDisplayEquipmentSummaryValues method
      
        creates wrapper object around equipmentCountTypes and is used for both types of boards
        
      *************************************************************************************************/
      
      function getDisplayEquipmentSummaryValues(){
      var equipmentLocations = BoardValueService.equipmentLocations;
          var equipmentSummaryDisplay = [];
          /****** used if the location has more than one garage servicing themselves *******/
          if (equipmentLocations.length > 1){
            for (var i=0; i<equipmentLocations.length; i++){
              var equipmentList = $filter('filterByLocation')(equipment, equipmentLocations[i]);
              for (var j in tasks.locations){
                if (j === equipmentLocations[i]){
                  var taskLocation = {}
                  taskLocation[j] = tasks.locations[j];
                  var equipmentCountObj = EquipmentHelperService.equipmentCountTypes(equipmentList, boardQuota, taskLocation, true, false);
                  equipmentSummaryDisplay.push({
                    equipmentCountTypes: equipmentCountObj,
                    equipmentLocation: j
                  });
                }
              }
            }
            
            var equipmentCombinedList = $filter('filterCombinedLocations')(equipment, equipmentLocations);
            var equipmentCountObj = EquipmentHelperService.equipmentCountTypes(equipmentCombinedList, boardQuota, tasks.locations, false, true);
            
        equipmentSummaryDisplay.push({
          equipmentCountTypes: equipmentCountObj,
          equipmentLocation: "Combined"
        });

          }else{
            /****** used for all other types of garages *******/
            var equipmentCountObj = EquipmentHelperService.equipmentCountTypes(equipment, boardQuota, tasks.locations, false, false);
            equipmentSummaryDisplay.push({
              equipmentCountTypes: equipmentCountObj
            });
          }
        return equipmentSummaryDisplay;
      }

      function init (res) {
        var deferred = $q.defer();

        $log.info('init %s', $window.performance.now());


        var data = res[0];

        var containerObject = {};
        // Used for time point comparisons with server-side dat
        var now = new Date();

        boardData.startDate = data.shiftsStartDate;
        boardData.endDate = data.shiftsEndDate;
        
        // Store board end date
        BoardValueService.boardEndDate = data.shiftsEndDate;
        boardIsLocked = moment().isAfter(moment(BoardValueService.boardEndDate).add(2, 'days'));

        equipment = data.equipment;

        // Add equipment model functions to each piece of equipment in array
        angular.forEach(equipment, function(value, key) {
          angular.extend(value, EquipmentModel);
          value = EquipmentHelperService.addEquipmentProperties(value);
        });

        personnel = data.personnel;
        boardQuota = data.boardQuota;
        volunteerCounts = data.volunteerCounts;
        
        // Converting location data to an Object/Key reference.
        var tempLocationsRefData = BoardDataService.getRepairLocations();
        for (var i=0; i<tempLocationsRefData.length; i++){
          locationsRefData[tempLocationsRefData[i].code] = tempLocationsRefData[i];
        }
        
        // Adding location reference data to the board values service
        BoardValueService.locationRefsData = locationsRefData;

        // Add person model functions to each person in array and process partial availabilities

        var formattedNames = {};

        angular.forEach(personnel, function(value, key) {
          value = $filter('extendPerson')(value);

          if(!formattedNames[value.formattedName]) {
            formattedNames[value.formattedName] = [];
          }
          formattedNames[value.formattedName].push(value.id);
        });

        angular.forEach(formattedNames, function(name, key) {
          if(name.length > 1) {
            angular.forEach(name, function(id, key) {
              personnel[id].formattedName = $filter('getFormattedNameMI')(personnel[id]);
              if(personnel[id].formattedName.length > 12) {
                personnel[id].formattedName = personnel[id].formattedName.slice(0, 12).trim() + '.';
              }
            });
          }
       });

        isBoro = isBoroLocation(data);

        BoardValueService.isBoro = isBoroLocation(data);

        // build tasks
        tasks = TaskHelperService.marshallTasks(
          {"locations": data.taskContainers},
          BoardDataService.getCategoryData(),
          linkedTaskMap,
          BoardDataService.getShiftData(),
          BoardDataService.getRepairLocations(),
          equipment,
          personnel
        );
        
        var subcategories = {};
        var partialTasksSubcategoriesLength = 0;
        Object.keys(linkedTaskMap).forEach(function(key){
          var linkedTask = linkedTaskMap[key];
          if(!(linkedTask.subcategoryId in subcategories)){
            subcategories[linkedTask.subcategoryId] = linkedTask.subcategoryId;
            partialTasksSubcategoriesLength ++;
          }
        });
        
        Object.keys(linkedTaskMap).forEach(function(key){
          var linkedTask = linkedTaskMap[key];
          //if(linkedTask.partialTaskSequence == 1){
            linkedTask.partialTaskSubcategories = partialTasksSubcategoriesLength;
          //}
        });

        tasks.settings = {};
        tasks.settings.locations = {};
        districtpersonnel = data.personnelContainers;

        if(isBoro) {
          tasks.settings.locations[boardData.boardLocation] = tasks.locations[boardData.boardLocation];
        } else {
          tasks.settings.locations = tasks.locations;
        };
        
        // Add commands for activity log
        var commandHist = data.commandMessagesHistory;
        if(typeof commandHist != undefined && commandHist!= null){
          TaskHelperService.sortByKey(commandHist,'clientSequence');
          actions = RecentActivityLoggingService.formattedHist(commandHist, _getReferenceData());
        }

        //Add online users
        var curSessions = data.onlineSessions;
        onlineUsers = {};
        if(curSessions != undefined) {
          angular.forEach(curSessions, function (value, key) {
            if (onlineUsers[value.httpSessionId]) {
              onlineUsers[value.httpSessionId].sessions = Number(onlineUsers[value.httpSessionId].sessions) + 1;
            } else {
              if (value.httpSessionId)
                onlineUsers[value.httpSessionId] = {
                  userId: value.loggedUserId,
                  remoteAddr: value.remoteAddr,
                  sessions: '1'
              }
            }
          })
        }

        // Instruct timer to process partial availability records
        OpsBoardInterval.addToDoList(processPartialAvailabilitiesAndMdas);

        //Will generalize later
        if (!timerset) {
          timerset = true;
          $interval( function() {
            if (WebSocketService.isConnected()) {
               var resource = $resource(pathStart + '/' + boardLocation.toUpperCase() + '/heartbeat'),
                 response = resource.get();
               return response.$promise.then(function(data) {
                   $log.log('heartbeat for ' + pathStart + '/' + boardLocation.toUpperCase() + '/heartbeat')
               });
            }
          }, 15*1000*60);
        }


        $log.info('init processed (async) %s', $window.performance.now());

        BoardValueService.equipment = equipment;
        BoardValueService.personnel = personnel;
        BoardValueService.tasks = tasks;

        deferred.resolve({data: data, tasks: tasks});
        return deferred.promise;



      };


      function _getReferenceData() {
        return {
          categories: CategoryDataService.getFormattedCategories(),
          subcategories: CategoryDataService.getFormattedSubcategories(),
          shifts: BoardDataService.getShiftData(),
          equipments: equipment,
          persons: personnel,
          tasks: tasks
        }
      }

      // Command counter
      var clientSequence = 0;

      // REST connection for initial load or
      // refresh
      function receiveCommand(message, boolValue, frame) {
        var command = JSON.parse(message.body);
        var broadcast = {tasks: false, equipment: false, personnel: false};
        uniqueId = frame.headers.session;

        var shiftData = BoardDataService.getShiftData(),
          categoryData = BoardDataService.getCategoryData();

        // Disregard error messages
        if (typeof command.error !== 'undefined' && command.error > 0) {
          return;
        }



        // Handle command and update model
        switch (command.commandName) {
          /* Board Ops */
          case 'change_application_mode':

            $rootScope.$emit('MAINTENANCE-MODE',{maintenancemode: command.key});

            break;
          case 'AddSessionToBoard':
              RecentActivityLoggingService.processAddOnlineUser(onlineUsers, command);
              break;
          case 'RemoveSessionFromBoard':
            RecentActivityLoggingService.processRemoveOnlineUser(onlineUsers, command);
            break;              
          case 'ReloadBoard':
            processReloadBoard();
            break;
          case 'CommandAdminLowMemory':
            processCommandAdminLowMemory();
            break;
          case 'SaveVolunteerCounts':
            ReportsService.saveVolunteerCountsCommand(volunteerCounts, command);
            break;

          /* Equipment Ops */
          case 'AddEquipment':
            EquipmentHelperService.processAddEquipmentCommand(equipment, command, EquipmentModel);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'AttachEquipment':
            EquipmentHelperService.processEquipmentDetachmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'AutoCompleteEquipment':
            EquipmentHelperService.processUpdateEquipmentLoadCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'CancelEquipmentDetachment':
            EquipmentHelperService.processEquipmentDetachmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'DetachEquipment':
            EquipmentHelperService.processEquipmentDetachmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'DownEquipment':
            EquipmentHelperService.processUpDownEquipmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'RemoveEquipment':
            EquipmentHelperService.processRemoveEquipmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'UpdateEquipmentLoad':
            EquipmentHelperService.processUpdateEquipmentLoadCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'UpdateEquipmentSnowReadiness' :
            EquipmentHelperService.processUpdateSnowEquipmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;
          case 'UpEquipment':
            EquipmentHelperService.processUpDownEquipmentCommand(tasks.tasksMap, equipment, command);
            broadcast.equipmentdetails = true;
            broadcast.equipmentId = command.commandContent.equipmentId;
            break;

          /* Person Ops */
          case 'AddPerson':
            PersonnelHelperService.processAddPersonCommand(personnel, command, PersonModel);
            broadcast.personId = command.commandContent.personId;
            break;
          case 'AddPersonMdaStatus':
            PersonnelHelperService.processPersonMdaStatusCommand(tasks, personnel, states, command);
            break;
          case 'AddPersonUnavailability':
            PersonnelHelperService.processPersonUnavailabilityCommand(tasks, personnel, states, command);
            if (command.commandContent.tasks && command.commandContent.tasks.length > 0) {
                broadcast.tasks = true;
            }
            break;
          case 'AddSpecialPosition':
            PersonnelHelperService.processSpecialPositionCommand(tasks, personnel, states, command);
            break;
          case 'AutoCompletePersonnel':
            PersonnelHelperService.processAutoCompletePersonnelCommand(tasks, personnel, states, command);
            break;
          case 'CancelPersonUnavailability':
            PersonnelHelperService.processPersonUnavailabilityCommand(tasks, personnel, states, command);
            break;
          case 'DetachPerson':
          case 'UpdateDetachPerson':
            PersonnelHelperService.processPersonDetachmentCommand(tasks, personnel, states, command);
            if (command.commandContent.tasks && command.commandContent.tasks.length > 0) {
                broadcast.tasks = true;
            }
            break;
          case 'GroundPerson':
            PersonnelHelperService.processPersonGroundStatusCommand(tasks, personnel, states, command);
            break;
          case 'RemovePerson':
            PersonnelHelperService.processRemovePersonCommand(tasks, personnel, command);
            break;
          case 'RemovePersonMdaStatus':
            PersonnelHelperService.processPersonMdaStatusCommand(tasks, personnel, states, command);
            break;
          case 'CancelDetachPerson':
              PersonnelHelperService.processPersonDetachmentCommand(tasks, personnel, states, command);
              break;            
          case 'RemovePersonUnavailability':
            PersonnelHelperService.processPersonUnavailabilityCommand(tasks, personnel, states, command);
            break;
          case 'RemoveSpecialPosition':
            PersonnelHelperService.processSpecialPositionCommand(tasks, personnel, states, command);
            break;
          case 'ReverseCancelPersonUnavailability':
            PersonnelHelperService.processPersonUnavailabilityCommand(tasks, personnel, states, command);
            break;
          case 'UpdatePersonMdaStatus':
            PersonnelHelperService.processPersonMdaStatusCommand(tasks, personnel, states, command);
            break;
          case 'UpdatePersonUnavailability':
            PersonnelHelperService.processPersonUnavailabilityCommand(tasks, personnel, states, command);
            if (command.commandContent.tasks && command.commandContent.tasks.length > 0) {
                broadcast.tasks = true;
            }
            break;
          case 'UpdateSpecialPosition':
            PersonnelHelperService.processSpecialPositionCommand(tasks, personnel, states, command);
            break;
          case 'MassChartUpdate':
             PersonnelHelperService.processMassChartUpdate(personnel, states, command);
             break;
          /* Task Settings Ops */
          case 'AddCategory' :
            TaskSettingsService.processAddCategoryCommand(categoryData, tasks, command);
            broadcast.tasks = true;
            break;
          case 'AddSection':
            TaskSettingsService.processAddSectionCommand(tasks, command);
            broadcast.tasks = true;
            break;
          case 'AddShift':
            TaskSettingsService.processAddShiftCommand(shiftData, tasks, command);
            broadcast.tasks = true;
            break;
          case 'AddSubcategory':
            TaskSettingsService.processAddSubcategoryCommand(categoryData, tasks, command);
            broadcast.tasks = true;
            break;
          case 'AddTasks':
            TaskSettingsService.processAddTasksCommand(tasks, command);
            broadcast.tasks = true;
            break;
          case 'ClearAllLocations':
            TaskSettingsService.processClearAllLocationsCommand(tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'ClearSpecificLocation':
            TaskSettingsService.processClearSpecificLocationCommand(tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveCategory':
            TaskSettingsService.processRemoveCategoryCommand(categoryData, tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveSection':
            TaskSettingsService.processRemoveSectionCommand(tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveSingleTask':
            TaskSettingsService.processDeleteSingleTaskCommand(tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveSubcategory':
            TaskSettingsService.processRemoveSubcategoryCommand(categoryData, tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveShift':
            TaskSettingsService.processRemoveShiftCommand(shiftData, tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'RemoveTasks':
            TaskSettingsService.processRemoveTasksCommand(tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'SetPartialTask' :
            TaskSettingsService.processSetPartialTaskCommand(shiftData, tasks, command, linkedTaskMap);
            broadcast.tasks = true;
            break;
          case 'UnlinkPartialTask':
            TaskSettingsService.processUnlinkPartialTaskCommand(tasks, command, linkedTaskMap);
            broadcast.tasks = true;
            break;
          case 'UpdateCategory':
            TaskSettingsService.processUpdateCategoryCommand(categoryData, tasks, equipment, personnel, command);
            broadcast.tasks = true;
            break;
          case 'UpdateTask':
            TaskSettingsService.processUpdateTasksCommand(tasks, command);
            broadcast.tasks = true;
            break;

          /* Task Assignment Ops */
          case 'AssignEquipmentToPartialTask':
            TaskHelperService.processEquipmentToPartialTaskCommand(user, states, tasks, equipment, linkedTaskMap, command, $rootScope);
            break;
          case 'AssignEquipmentToTask':
            TaskHelperService.processEquipmentToTaskCommand(user, states, tasks, equipment, command, $rootScope);
            break;
          case 'AssignPersonToPartialTask':
            TaskHelperService.processPersonToPartialTaskCommand(user, states, tasks, personnel, command, $rootScope);
            break;
          case 'AssignPersonToTask':
            TaskHelperService.processPersonToTaskCommand(user, states, tasks, personnel, command, $rootScope);
            break;
          case 'AddDayBefore' :
            TaskHelperService.processAddDayBeforeCommand(user, states, personnel, command, $rootScope);
            break;
          case 'RemoveDayBefore' :
            TaskHelperService.processRemoveDayBeforeCommand(user, states, personnel, command, $rootScope);
            break;
          case 'UpdateDayBefore' :
            TaskHelperService.processUpdateDayBeforeCommand(user, states, personnel, command, $rootScope);
            break;
          case 'SetNextDayAvailable' : 
        TaskHelperService.processSetNextDayAvailableCommand(user, states, personnel, command, $rootScope);
        break;
          case 'RemoveNextDayAvailable' :
        TaskHelperService.processRemoveNextDayAvailableCommand(user, states, personnel, command, $rootScope);
          break;
          case 'AssignSupervisor':
            TaskHelperService.processSupervisorToTaskCommand(user, states, tasks, personnel, command, $rootScope);
            broadcast.tasks = true;
            break;
          case 'UnassignEquipmentFromPartialTask':
            TaskHelperService.processEquipmentFromPartialTaskCommand(states, tasks, equipment, linkedTaskMap, command);
            break;
          case 'UnassignEquipmentFromTask':
            TaskHelperService.processEquipmentFromTaskCommand(states, tasks, equipment, command);
            break;
          case 'UnassignPersonFromPartialTask':
            TaskHelperService.processPersonFromPartialTaskCommand(states, tasks, personnel, command);
            break;
          case 'UnassignPersonFromTask':
            TaskHelperService.processPersonFromTaskCommand(states, tasks, personnel, command);
            if (command.commandContent.tasks && command.commandContent.tasks.length > 0) {
                broadcast.tasks = true;
            }
            break;
          case 'AssignPersonFromTaskToTask':
            TaskHelperService.processPersonFromTaskToTaskCommand(tasks, personnel, command);
            broadcast.tasks = true;
            break;
          case 'AssignEquipmentFromTaskToTask':
            TaskHelperService.processEquipmentFromTaskToTaskCommand(tasks, equipment, command);
            broadcast.tasks = true;
            broadcast.equipmentdetails = false;
            break;
          case 'UnassignSupervisor':
            TaskHelperService.processUnassignSupervisorCommand(user, states, tasks, personnel, command, $rootScope);
            broadcast.tasks = true;
            break;
          case 'ErrorMessage':
            // temporary untill back end figures out a way to format
            command.commandContent.auditMessage = "[" + command.commandContent.errorCode + ":errorCode] " +  command.commandContent.errorMessage;
            command.commandContent.systemDateTime = command.commandContent.systemDateTime ? command.commandContent.systemDateTime : new Date();
            TaskHelperService.processErrorCommand(user, command, $rootScope);
            break;
        }

        // Write audit message to recent activity message service
        if(command.commandContent.auditMessage){
        	RecentActivityLoggingService.writeAuditMessage(actions, command.commandContent.systemDateTime, command.commandContent.systemUser, command.commandContent.auditMessage, _getReferenceData());
        }

        // Propagate model changes to views

        BoardValueService.personnel = personnel;
        BoardValueService.equipment = equipment;

        $rootScope.$broadcast('UPDATE-EQUIPMENT-PANE');
        $rootScope.$broadcast('UPDATE-PERSONNEL-PANE');  
        
      $rootScope.equipmentSummaryDisplay = getDisplayEquipmentSummaryValues();

        BoardValueService.tasks = tasks;

        if(broadcast.tasks === true) {
          $rootScope.$broadcast('UPDATE-TASKS', {repaint: true});
          $rootScope.$broadcast('UPDATE-EQUIPMENT-SUMMARY');
        }

        if(broadcast.equipmentdetails === true) {
          $rootScope.$broadcast('UPDATE-EQUIPMENT-DETAILS',{equipmentId: broadcast.equipmentId});
        }

        if (broadcast.personId) {
          $rootScope.$broadcast('UPDATE-PERSON-DETAILS', { personId: broadcast.personId });
        }

        $rootScope.$apply();
      }

      function processReloadBoard(){
        $rootScope.$broadcast('reload-board', "This board has been changed. Please reload it to load new tasks.");
      }

      function processCommandAdminLowMemory(){
        $rootScope.$broadcast('reload-board', "Server running on low memory. Please contact helpdesk.");
      }

      function sendCommand(command) {
        WebSocketService.send('/app/commands.' + boardLocation.toUpperCase() + '.' + boardDate, {}, JSON.stringify(command));
      }

      function sendPublishBoardCommand(command) {
        WebSocketService.send('/app/displayboard.' + boardLocation.toUpperCase(), {}, JSON.stringify(command));
      }

      function processPartialAvailabilitiesAndMdas() {
        return PersonnelHelperService.processAllPartialAvailabilitiesAndMdas(personnel, states);
      }

      function isBoroLocation(info) {
        //will change this when Dima is done with his changes
        if (!info) return false;
        return info.locationType === 'borough';
      }

     
      return {
        
      getDisplayEquipmentSummaryValues: getDisplayEquipmentSummaryValues,

        getBoardDate : function() {
          return boardDate;
        },
        
        getStartDate : function(){
          return startDate;
        },
        
        getEndDate : function(){
          return endDate;
        },

        getSessionId : function() {
          return uniqueId
        },

        getBoardLocation : function() {
          return boardLocation;
        },

        getBoardEquipmentServiceLocations : function() {


          var locationsByCode = BoardValueService.locationRefsData;

          var loc = locationsByCode[boardLocation];

          if (loc.servicesEquipmentLocations) {
            var locationCodes = [];
            var len = loc.serviceLocations.length;

            for (var i = 0; i < len; i++) {
              locationCodes.push(loc.serviceLocations[i].code);
            }

            locationCodes = locationCodes.sort(function(a, b) {
              return ((a < b) ? -1 : ((a > b) ? 1 : 0));
            });

            return locationCodes;
          } else {
            return [loc.code];
          }
        },

        getErrorMessage : function(opsBoardError) {
          var message = [];
          message
          .push('<p>' + opsBoardError.message + '.&nbsp;&nbsp;Error Code: ' + opsBoardError.code + '.</p>');
          if (opsBoardError.extendedMessages && opsBoardError.extendedMessages.length > 0) {
            message.push('<p class="server-errors left">Internal Messages:</p>');
            message.push('<ul class="server-errors">');
            for (var i = 0; i < opsBoardError.extendedMessages.length; i++) {
              message.push('<li>' + opsBoardError.extendedMessages[0] + '</li>');
            }
            message.push('</ul>');
          }
          if (opsBoardError.debugData) {
            message.push('<p class="server-errors left">Additional Debug Data:</p>');
            message.push('<ul class="server-errors">');
            for ( var key in opsBoardError.debugData) {
              if (opsBoardError.debugData.hasOwnProperty(key)) {
                var obj = JSON.parse(opsBoardError.debugData[key]);
                message.push('<li>' + key + ':<br/> ' + JSON.stringify(obj, undefined, 2) + '</li>');
              }
            }
            message.push('</ul>');
          }

          return $sce.trustAsHtml(message.join(''))
        },

        getPathStart : function() {
          return pathStart;
        },

        isBoroBoard: function() {
          return isBoro;
        },

        init: init,
        closeWebSocket: WebSocketService.closeWebSocket,
        isConnected : WebSocketService.isConnected,

        subscribe: function () {
          WebSocketService.subscribe(boardData, user, receiveCommand);
        },

        isLocked: function() {
          return boardIsLocked;
        },

        containsTasks: function() {
          return TaskHelperService.containsTasks(tasks);
        },

        getTasks: function () {
          return tasks;
        },

        getLinkedTaskMap: function () {
          return linkedTaskMap;
        },

        setTasks: function (tasksObj) {
          tasks = tasksObj;
        },

        /** Section - Load Board - End * */

        /** Section - Core board data - Start * */

        getActions : function() {
          return actions;
        },

        getOnlineUsers : function() {
          return onlineUsers;
        },

        getEquipment : function() {
          return equipment;
        },

        getPersonnel : function() {
          return personnel;
        },

        getBoardQuota : function() {
            return boardQuota;
          },

        getvolunteerCounts :function(){
          return volunteerCounts;
        },

        

        /** Section - Core board data - End * */

        /** Section - Reference data - Start * */

        getMappedTitle : function(title) {
          var tMap = ''
          if (!title)
            return '';
          Object.keys(titles).forEach(function(key) {
            var val = titles[key];
            if (title.toUpperCase() === val.toUpperCase())
              tMap = key;
          })
          return tMap;
        },

        /** Section - Reference data - End * */

        /**
         * Section - Asynchronous board
         * operations - Start *
         */

         assignEquipmentToTask : function(task, pieceOfEquipmentId) {   
           sendCommand(TaskHelperService.createAssignEquipmentToTaskCommand(states, clientSequence++, boardLocation, boardDate, task, equipment[pieceOfEquipmentId], uniqueId));
         },
         
         assignEquipmentToPartialTask : function(task, pieceOfEquipmentId, locationId) {   
           sendCommand(TaskHelperService.createAssignEquipmentToPartialTaskCommand(states, clientSequence++, boardLocation, boardDate, tasks, linkedTaskMap, task, equipment[pieceOfEquipmentId], locationId));
         },

         assignPersonToTask : function(task, personId, position) {
           sendCommand(TaskHelperService.createAssignPersonToTaskCommand(states, clientSequence++, boardLocation, boardDate, task, personnel[personId.split('_')[0]], position, uniqueId));
         },

         assignSupervisorToTask: function (task, personId, taskIndicator) {
           var command = TaskHelperService.createAssignSupervisorToTaskCommand(states, clientSequence++, boardLocation, boardDate, task, personnel[personId.split('_')[0]], taskIndicator);
           if (command) {
             sendCommand(command);
           }
         },

         assignSupervisorToSection: function (section, personId, taskIndicator) {
           var command = TaskHelperService.createAssignSupervisorToSectionCommand(states, clientSequence++, boardLocation, boardDate, section, personnel[personId.split('_')[0]], taskIndicator);
           if (command) {
             sendCommand(command);
           }
         },
         
         assignSupervisorToSubcategory: function (subcategory, personId, taskIndicator) {
           var command = TaskHelperService.createAssignSupervisorToSubcategoryCommand(states, clientSequence++, boardLocation, boardDate, subcategory, personnel[personId.split('_')[0]], taskIndicator);
           if (command) {
             sendCommand(command);
           }
         },

         assignPersonToPartialTask : function(task, personId, position, locationId) {
           sendCommand(TaskHelperService.createAssignPersonToPartialTaskCommand(states, clientSequence++, boardLocation, boardDate, tasks, linkedTaskMap, task, personnel[personId.split('_')[0]], position, locationId));
         },
         
         setPersonNextDayAvailable : function(personId){
           sendCommand(TaskHelperService.createSetPersonNextDayAvailableCommand(states, clientSequence++, boardLocation, boardDate, personId.split('_')[0]));

         },
         removePersonNextDayAvailable: function(personId){
           sendCommand(TaskHelperService.createRemovePersonNextDayAvailableCommand(states, clientSequence++, boardLocation, boardDate, personId.split('_')[0]));
         },
         unlinkPartialTasks : function(route) {   
           sendCommand(TaskSettingsService.createUnlinkPartialTasksCommand(states, clientSequence++, boardLocation, boardDate, equipment, personnel, tasks, linkedTaskMap, route));
         },

         unassignEquipmentFromTask : function(equipment, task, locationId) {
           sendCommand(TaskHelperService.createUnassignEquipmentFromTaskCommand(states, clientSequence++, boardLocation, boardDate, equipment, tasks, linkedTaskMap, task, locationId));
         },

         unassignAndAssignEquipmentFromTaskToTask : function(equipmentId, oldTask, locationId, newTask) {
           sendCommand(TaskHelperService.createUnassignAndAssignEquipmentFromTaskToTaskCommand(clientSequence++, boardLocation, boardDate, equipment, tasks, linkedTaskMap, oldTask, newTask, locationId, equipmentId));
         },

         unassignPersonFromTask : function(person, task, position, locationId) {
           sendCommand(TaskHelperService.createUnassignPersonFromTaskCommand(states, clientSequence++, boardLocation, boardDate, personnel, tasks, position, linkedTaskMap, task, locationId, person));
         },

          unassignAndAssignPersonFromTaskToTask : function(personId, oldTask, oldPosition, newPosition, locationId, newTask) {
            sendCommand(TaskHelperService.createUnassignAndAssignPersonFromTaskToTaskCommand(clientSequence++, boardLocation, boardDate, personnel, tasks, oldPosition, newPosition, linkedTaskMap, oldTask, newTask, locationId, personId));
          },

         unassignSupervisor : function(subcategory, section, task, locationId) {
            sendCommand(TaskHelperService.createUnassignSupervisorCommand(states, clientSequence++, boardLocation, boardDate, tasks, section, subcategory, task));
          },
         unassignNonBinEquipmentFromTask:function(equipment, task){
           sendCommand(TaskHelperService.createUnassignNonBinEquipmentFromTaskCommand(states, clientSequence++, boardLocation, boardDate, personnel, task, position)); 
         },
         clearAllLocations:function(){
           sendCommand(TaskSettingsService.createClearAllLocationsCommand(states, clientSequence++, boardLocation, boardDate)); 
         },
         clearSpecificLocation:function(serviceLocationId){
           sendCommand(TaskSettingsService.createClearSpecificLocationCommand(states, clientSequence++, boardLocation, boardDate,serviceLocationId)); 
         },
         addShift : function(locationShiftId, serviceLocationId, shiftId) {   
           sendCommand(TaskSettingsService.createAddShiftCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, serviceLocationId, shiftId));
         },

         removeShift : function(locationShiftId, serviceLocationId, shiftId) {   
           sendCommand(TaskSettingsService.createRemoveShiftCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, serviceLocationId, shiftId));
         },

         addCategory : function(locationShiftId, shiftCategoryId, serviceLocationId, shiftId, categoryId) {   
           sendCommand(TaskSettingsService.createAddCategoryCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, serviceLocationId, shiftId, categoryId));
         },

         removeCategory : function(locationId, locationShiftId, shiftCategoryId, categoryId, shiftId) {   
           sendCommand(TaskSettingsService.createRemoveCategoryCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, locationId, shiftId, categoryId));
         },

         updateCategory : function(locationId, shiftId, locationShiftId, shiftCategoryId, oldCategory, newCategory) {   
           sendCommand(TaskSettingsService.createUpdateCategoryCommand(states, clientSequence++, boardLocation, boardDate, locationId, shiftId, locationShiftId, shiftCategoryId, oldCategory, newCategory));
         },

         addSubcategory : function(locationShiftId, shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId, subCategoryId, containsSections, taskId) {   
           sendCommand(TaskSettingsService.createAddSubcategoryCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId, subCategoryId, containsSections, taskId));
         },

         removeSubcategory : function(locationShiftId, shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId, subCategoryId, containsSections, taskId) {   
           sendCommand(TaskSettingsService.createRemoveSubcategoryCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, serviceLocationId, shiftId, categoryId, subCategoryId, containsSections, taskId));
         },

         addSection: function (locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, sectionId) {
           sendCommand(TaskSettingsService.createAddSectionCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, sectionId));
         },

         removeSection: function (locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, serviceLocationId, shiftId, categoryId, subcategoryId, sectionId) {
           sendCommand(TaskSettingsService.createRemoveSectionCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, sectionTaskId, serviceLocationId, shiftId, categoryId, subcategoryId, sectionId));
         },

         addTask: function (locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId) {
           sendCommand(TaskSettingsService.createAddTaskCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId));
         },

         updateTask: function (task, locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId) {
           sendCommand(TaskSettingsService.createUpdateTaskCommand(states, clientSequence++, boardLocation, boardDate, task, locationShiftId, shiftCategoryId, subcategoryTaskId, taskIds, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId));
         },

         removeTasks: function (locationShiftId, shiftCategoryId, subcategoryTaskId, numOfTasks, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId) {
           sendCommand(TaskSettingsService.createRemoveTasksCommand(states, clientSequence++, boardLocation, boardDate, locationShiftId, shiftCategoryId, subcategoryTaskId, numOfTasks, serviceLocationId, shiftId, categoryId, subcategoryId, containsSections, sectionTaskId, sectionId))
         },

         deleteSingleTask: function (task, section, subcategory, category, shift, location, locationId, containsSection) {
           sendCommand(TaskSettingsService.createDeleteSingleTaskCommand(states, clientSequence++, boardLocation, boardDate, task, section, subcategory, category, shift, location, locationId, containsSection))
         },

         publishBoard: function () {
           sendPublishBoardCommand(TaskSettingsService.createPublishBoardCommand(states, clientSequence++, boardLocation, boardDate))
         },

         /**
          * Section - Asynchronous board
          * operations - End *
          */

          /**
           * Section - Synchronous board
           * operations - Start *
           */

         acceptAttachEquipment : function(pieceOfEquipment, receivedBy, receivedDate, receivedTime, remarks, successFn, errorFn) {
           EquipmentHelperService.acceptAttachEquipmentOnServer($resource, pieceOfEquipment, receivedBy, receivedDate, receivedTime, remarks, boardData, successFn, errorFn);
         },

         cancelEquipmentDetachment : function(pieceOfEquipment, successFn, errorFn) {
           EquipmentHelperService.cancelEquipmentDetachmentOnServer($resource, pieceOfEquipment, boardData, successFn, errorFn);
         },

         detachEquipment : function(pieceOfEquipment, to, time, driver, successFn, errorFn) {
           EquipmentHelperService.detachEquipmentOnServer($resource, pieceOfEquipment, to, time, driver, boardData, successFn, errorFn);
         },

         detachPerson : function(person, detachData, successFn, errorFn) {
           PersonnelHelperService.detachPersonOnServer($resource, person, detachData, boardData, successFn, errorFn);
         },

         updatePersonDetachment : function(person, detachData, successFn, errorFn) {
          PersonnelHelperService.updatePersonDetachmentOnServer($resource, person, detachData, boardData, successFn, errorFn);
         },

         downEquipment : function(pieceOfEquipment, downData, successFn, errorFn) {
           EquipmentHelperService.downEquipmentOnServer($resource, pieceOfEquipment, downData, boardData, successFn, errorFn);
         },

         updateEquipmentLoadStatus : function(pieceOfEquipment, loadData, successFn, errorFn) {
           EquipmentHelperService.updateEquipmentLoadStatusOnServer($resource, pieceOfEquipment, loadData, boardData, successFn, errorFn);
         },

         upEquipment : function(pieceOfEquipment, upData, successFn, errorFn) {
           EquipmentHelperService.upEquipmentOnServer($resource, pieceOfEquipment, upData, boardData, successFn, errorFn);
         },
         updateSnowEquipment:function(pieceOfEquipment, snowData, successFn, errorFn) {
           EquipmentHelperService.updateSnowEquipmentOnServer($resource, pieceOfEquipment, snowData, boardData, successFn, errorFn);
         },

         updatePersonMdaStatus : function(person, mdaData, successFn, errorFn) {
           PersonnelHelperService.updatePersonMdaStatusOnServer($resource, person, mdaData, boardData, successFn, errorFn);
         },

         createPersonMdaStatus : function(person, mdaData, successFn, errorFn) {
           PersonnelHelperService.createPersonMdaStatusOnServer($resource, person, mdaData, boardData, successFn, errorFn);
         },

         addSpecialPosition : function(person, specialPositionData, success, error){
           PersonnelHelperService.addSpecialPositionOnServer($resource, person, specialPositionData, boardData, success, error);
         },

         updateSpecialPosition : function(person, specialPositionData, success, error){
           PersonnelHelperService.updateSpecialPositionOnServer($resource, person, specialPositionData, boardData, success, error);
         },

         removePersonMdaStatus : function(person, mdaData, successFn, errorFn) {
           PersonnelHelperService.removePersonMdaStatusOnServer($resource, person, mdaData, boardData, successFn, errorFn);
         },
         
         cancelDetach : function(person, detachData, successFn, errorFn) {
             PersonnelHelperService.cancelPersonDetachOnServer($resource, person, detachData, boardData, successFn, errorFn);
           },         

         createPersonUnavailabilityCode : function(person, unavailabilityData, successFn, errorFn) {
           PersonnelHelperService.createPersonUnavailabilityOnServer($resource, person, unavailabilityData, boardData, successFn, errorFn);
         },

        saveGroup: function (group, successFn, errorFn) {
            TaskSettingsService.saveGroupOnServer($resource, group, boardData, successFn, errorFn)
        },

         updatePersonUnavailabilityCode : function(person, unavailabilityData, successFn, errorFn) {
           PersonnelHelperService.updatePersonUnavailabilityOnServer($resource, person, unavailabilityData, boardData, successFn, errorFn);
         },

         removePersonUnavailabilityCode : function(person, unavailabilityData, successFn, errorFn) {
           PersonnelHelperService.removePersonUnavailabilityOnServer($resource, person, unavailabilityData, boardData, successFn, errorFn);
         },

         cancelPersonUnavailabilityCode:function(person, unavailabilityData, successFn, errorFn){
           PersonnelHelperService.cancelPersonUnavailabilityOnServer($resource, person, unavailabilityData, boardData, successFn, errorFn);  
         },

         undoCancelPersonUnavailabilityCode:function(person, unavailabilityData, successFn, errorFn){
           PersonnelHelperService.undoCancelPersonUnavailabilityOnServer($resource, person, unavailabilityData, boardData, successFn, errorFn);  
         },

         removeSpecialPosition : function(person, specialPositionData, successFn, errorFn) {
           PersonnelHelperService.removeSpecialPositionOnServer($resource, person, specialPositionData, boardData, successFn, errorFn);
         },

        massChartUpdate : function(cancelledIds, reverseCancelledIds, chartDate, successFn, errorFn) {
          PersonnelHelperService.massChartUpdateOnServer($resource, cancelledIds, reverseCancelledIds, chartDate, boardData, successFn, errorFn);
        },

             isSame: function (obj1, obj2) {
                    var masterCopy = angular.copy(obj1),
                        updatedCopy = angular.copy(obj2);
                    delete masterCopy.reasonForChange;
                    delete updatedCopy.reasonForChange;
                    masterCopy.startTime = masterCopy.startTime ? { hours: masterCopy.startTime.getHours(), mins: masterCopy.startTime.getMinutes()} : '';
                    masterCopy.endTime = masterCopy.endTime ? { hours: masterCopy.endTime.getHours(), mins: masterCopy.endTime.getMinutes()} : '';
                    updatedCopy.startTime = updatedCopy.startTime ? { hours: updatedCopy.startTime.getHours(), mins: updatedCopy.startTime.getMinutes()} : '';
                    updatedCopy.endTime = updatedCopy.endTime ? { hours: updatedCopy.endTime.getHours(), mins: updatedCopy.endTime.getMinutes()} : '';
                    return _.isEqual(masterCopy, updatedCopy)
             },

         copyBoard : function(copyBoardTo, successFn, errorFn) {
           BoardHelperService.copyBoardOnServer($resource, $window, copyBoardTo, boardData, successFn, errorFn);
         },
         
         saveVolunteerCounts : function(boardId, chartVolunteers, mandatoryChart, VacationVolunteers, successFn, errorFn ){
           ReportsService.saveVolunteerCountsOnServer($resource , boardData, chartVolunteers, mandatoryChart, VacationVolunteers, successFn, errorFn )
         },



        locationtasks : function(tasksin, personnel) {
          return locationtasks(tasksin, personnel);
        }
      }
    });