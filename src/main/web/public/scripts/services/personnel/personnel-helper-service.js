'use strict';

/**
 * This file contains all the logic for working with Personnel, including creating commands, processing server commands, formatting and manipulating task data.
 */

angular.module('OpsBoard')
.service(
    'PersonnelHelperService',
    function($rootScope, InfoService, TaskHelperService, itemsPerPage, $filter) {
      var _getPersonKey = function (id) {
        if (id == null || typeof id == undefined || id == '') return;
        if (id.indexOf('_') < 0) return id;
        return id.split('_')[0];
      }

      var _addPersonProperties = function(value) {
        //TODO what needs to be here? @see _addEquipmentProperties
        return value;
      };

      var _processGeneralPersonnelCommand = function(tasks, personnel, command) {
         
        var p = personnel[command.commandContent.personId];
        if (p) {
          p.updateState(command.commandContent.assigned, command.commandContent.assignedAnywhere, command.commandContent.state);          
          if (command.commandContent.tasks) {
            for (var i = 0; i < command.commandContent.tasks.length; i++) {
              var tCommand = command.commandContent.tasks[i];
              var t = tasks.tasksMap[tCommand.taskId];
              if (tCommand.supervisorId) {
                  // Supervisor assignment 
                  t.clearSupervisorAssignment(tCommand.supervisorId);
                  
                  // Remove from section
                  var supervisorIds = [tCommand.supervisorId];
                  if (t.sectionId) {                      
                      TaskHelperService.removeSupervisorsFromSection(tasks, t.sectionId, supervisorIds);
                  } else if (t.subcategoryId) {
                      TaskHelperService.removeSupervisorsFromSubcategory(tasks, t.subcategoryId, supervisorIds);
                  }

              } else if (tCommand.assignmentTime == null) {
                 // Person assignment 
                if (tCommand.position)
                t.clearPersonnelAssignment(tCommand.position);    
                else
                  t.clearPersonnelAssignmentWhenIndexNotKnown(command.commandContent.personId, tCommand);
              } else {
                t.setPersonnelAssignment(tCommand.assignmentTime, tCommand.completed, tCommand.position, p);
              }

              t = $filter('formatTask')(t);
            }
          }
        }

        return p;
      };

      var _processPersonUnavailability = function (p, states, content) {
        if (p) {
          if(p.state !== states.personnel.partiallyAvailable){
            p.partialAvailability = false; // reset based on server state
          }
          
          var processed = false;
          for(var i =0; i < p.unavailabilityHistory.length; i++){
            if (content.unavailableReason.replaces && content.unavailableReason.replaces == p.unavailabilityHistory[i].id) {
              processed = true;
              p.unavailabilityHistory[i] = content.unavailableReason;
            }
          }

          if (!processed)
            p.unavailabilityHistory.push(content.unavailableReason);

          p.state = content.state
          p.activeUnavailabilityReasons = content.activeUnavailabilityReasons;
          p.formattedUnavailableReasons =p.getFormattedUnavailableReasons();
          p.showPaginationLinksForUnavailable =p.formattedUnavailableReasons.length > 0;
          p.unavailabilityPaginationModel = p.formattedUnavailableReasons.slice(0, itemsPerPage);
          
          
          _processPartialAvailabilityAndMda(p, new Date(), states);
          $rootScope.$broadcast('UNAVAILABILITY-PERSON', p);

        }
      }
      
      var _processAutoPersonnelCommand = function(tasks, personnel, command,position) {
        var p;
        if(command.commandContent.opsBoardPerson1MetaData.personId && position =='1'){
          p = personnel[command.commandContent.opsBoardPerson1MetaData.personId];
          if (p) {
            p.updateState(command.commandContent.opsBoardPerson1MetaData.assigned, command.commandContent.opsBoardPerson1MetaData.state);          
            if (command.commandContent.opsBoardPerson1MetaData.tasks) {
              for (var i = 0; i < command.commandContent.opsBoardPerson1MetaData.tasks.length; i++) {
                var tCommand = command.commandContent.opsBoardPerson1MetaData.tasks[i];
                var t = tasks[tCommand.taskId];
                if (tCommand.assignmentTime == null) {
                  if(tCommand.position)
                    t.clearPersonnelAssignment(tCommand.position);    
                  else
                    t.clearPersonnelAssignmentWhenIndexNotKnown(command.commandContent.opsBoardPerson1MetaData.personId, tCommand);
                } else {
                  t.setPersonnelAssignment(tCommand.assignmentTime, tCommand.completed, tCommand.position, p);
                }

                t = $filter('formatTask')(t);
              }
            }
          }


        }else{
          p = personnel[command.commandContent.opsBoardPerson2MetaData.personId];
          if (p) {
            p.updateState(command.commandContent.opsBoardPerson2MetaData.assigned, command.commandContent.opsBoardPerson2MetaData.state);          
            if (command.commandContent.opsBoardPerson2MetaData.tasks) {
              for (var i = 0; i < command.commandContent.opsBoardPerson2MetaData.tasks.length; i++) {
                var tCommand = command.commandContent.opsBoardPerson2MetaData.tasks[i];
                var t = tasks[tCommand.taskId];
                if (tCommand.assignmentTime == null) {
                  if(tCommand.position)
                    t.clearPersonnelAssignment(tCommand.position);    
                  else
                    t.clearPersonnelAssignmentWhenIndexNotKnown(command.commandContent.opsBoardPerson2MetaData.personId, tCommand);
                } else {
                  t.setPersonnelAssignment(tCommand.assignmentTime, tCommand.completed, tCommand.position, p);
                }

                t = $filter('formatTask')(t);
              }
            }
          }
        }
        return p;
    };

      var _processPartialAvailabilityAndMda = function (person, now, states) {
        var changed = false;

        if(person.grounded && person.groundingHistory.length > 0) {

          var lastGround = person.groundingHistory[person.groundingHistory.length - 1];
          var foundgrounded = false;

          for(var g=0; g<person.groundingHistory.length; g++) {
            lastGround = person.groundingHistory[g];
            if (!lastGround.endDate || moment(lastGround.endDate).diff(now, 'minutes') > 0) {
              foundgrounded = true
            }
          }
        }

        if (person.grounded && foundgrounded === false) {
          person.grounded = false;
          person.indicatorText = person.getIndicatorText();
          person.indicatorBox = person.getIndicatorBox();
          person.activeMDA = person.isActiveMDA();
          $rootScope.$broadcast('UPDATE-PERSONNEL-PANE');
        }

        if (person.state == states.personnel.unavailable) {
          var activeRecords = person.activeUnavailabilityReasons;
          for (var i = 0; i < activeRecords.length; i++) {
            if (activeRecords[i].status === "A" && (activeRecords[i].start == null || (moment(activeRecords[i].start).diff(now, 'days') <= 0 && moment(activeRecords[i].start).diff(now, 'minutes') <= 0))) {
              // record start date <= now
              if ((moment(activeRecords[i].end).diff(now, 'days') == 0 && moment(activeRecords[i].end).diff(now, 'minutes') == 0) && !activeRecords[i].completed) {
                activeRecords[i].completed = true;
                $rootScope.$broadcast('UNAVAILABILITY-PERSON', person);
              }
            }
          }
        }

        if (person.state !== states.personnel.detached) {
          if (!person.isPartiallyAvailable())
            return changed;
          var available = true;
          if (person.partialAvailability) {
            var activeRecords = person.activeUnavailabilityReasons;
            for (var i = 0; i < activeRecords.length; i++) {
              if (activeRecords[i].status === "A" && (activeRecords[i].start == null || (moment(activeRecords[i].start).diff(now, 'days') <= 0 && moment(activeRecords[i].start).diff(now, 'minutes') <= 0))) {
                // record start date <= now
                if ((moment(activeRecords[i].end).diff(now, 'days') == 0 && moment(activeRecords[i].end).diff(now, 'minutes') == 0) && !activeRecords[i].completed) {
                  activeRecords[i].completed = true;
                  $rootScope.$broadcast('UNAVAILABILITY-PERSON', person);
                }
                if (activeRecords[i].end == null ||
                    (moment(activeRecords[i].end).diff(now, 'days') >= 0 && moment(activeRecords[i].end).diff(now, 'minutes') > 0)) {
                  // record end date > now
                  available = false;
                  //break;
                }
                  // record end date > now
              }
            }
            var clearMda = false;
            if (available) {
              var activeMdaCodes = person.activeMdaCodes;
              for (var i = 0; i < activeMdaCodes.length; i++) {
                if (activeMdaCodes[i].status == "A" && activeMdaCodes[i].subType != "1L" && (activeMdaCodes[i].startDate == null ||
                    (moment(activeMdaCodes[i].startDate).diff(now, 'days') <= 0 && moment(activeMdaCodes[i].startDate).diff(now, 'minutes') <= 0))) {
                  // record start date <= now
                  if (activeMdaCodes[i].endDate == null ||
                      (moment(activeMdaCodes[i].endDate).diff(now, 'days') >= 0 && moment(activeMdaCodes[i].endDate).diff(now, 'minutes') > 0)) {
                    // record end date > now
                    available = false;
                    break;
                  }
                  if (activeMdaCodes[i].endDate !== null &&
                          (moment(activeMdaCodes[i].endDate).diff(now, 'days') <= 0 && moment(activeMdaCodes[i].endDate).diff(now, 'minutes') <= 0)) {
                        // record end date < now
                        available = true;
                        clearMda = true;
                        break;
                      }
                }
              }
            }
          }       
          if (available) {
            if (person.state == states.personnel.partiallyAvailable || person.state == states.personnel.unavailable) {
              person.state = states.personnel.available;
              changed = true;             
            }
            if(clearMda){ person.activeMdaCodes = []; }
          } else { // when person is not available and has MDA that expires on todays board at some point, MDA indicator needs to be cleared.
            
              var activeMdaCodes = person.activeMdaCodes;
              for (var i = 0; i < activeMdaCodes.length; i++) {
                if (activeMdaCodes[i].status == "A" && activeMdaCodes[i].subType != "1L" && (activeMdaCodes[i].startDate == null ||
                    (moment(activeMdaCodes[i].startDate).diff(now, 'days') <= 0 && moment(activeMdaCodes[i].startDate).diff(now, 'minutes') <= 0))) {
                  if (activeMdaCodes[i].endDate !== null &&
                          (moment(activeMdaCodes[i].endDate).diff(now, 'days') <= 0 && moment(activeMdaCodes[i].endDate).diff(now, 'minutes') <= 0)) {
                        // record end date < now
                        clearMda = true;
                        break;
                      }
                }
              }
            
            if (person.state == states.personnel.partiallyAvailable || person.state == states.personnel.available) {
              person.state = states.personnel.unavailable;
              changed = true;
            }
            if(clearMda){ person.activeMdaCodes = []; }
          }
        }

        //$rootScope.$broadcast('UNAVAILABILITY-PERSON', person);

        $rootScope.$broadcast('UPDATE-PERSONNEL-PANE');
        person.indicatorText = person.getIndicatorText();
        person.indicatorBox = person.getIndicatorBox();
        person.activeMDA = person.isActiveMDA();

        return changed;
      };

      return {
        addPersonProperties: function(value){
          return _addPersonProperties(value);  
        },
        /* Partial Availability Logic */

        processPartialAvailabilityAndMda: function (person, now, states) {
          return _processPartialAvailabilityAndMda(person, now, states);
        },

        processAllPartialAvailabilitiesAndMdas: function (personnel, states) {
          var check = false;
          if (personnel) {
            var now = new Date();
            Object.keys(personnel).forEach(
                function(key) {
                  check = _processPartialAvailabilityAndMda(personnel[key], now, states);
                });
          }
          return check;
        },



        /** Section - Perform synchronous operation on server - Start */

         updatePersonDetachmentOnServer: function ($resource, person, detachData, boardData, successFn, errorFn) {

          var resource = $resource(boardData.pathStart + '/UpdateDetachPerson/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: person.id
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });
          resource.save({
            id: detachData.detachmentId,
            endDate: new Date(detachData.endDate)
          }, successFn, errorFn);

        },
        
        detachPersonOnServer: function ($resource, person, detachData, boardData, successFn, errorFn) {
          var personId = person.id;
          
          var resource = $resource(boardData.pathStart + '/DetachPerson/:district/:date/:personId', {
              district: boardData.boardLocation,
              date: boardData.boardDate,
              personId: personId
            }, {
              save: {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json'
                }
              }
            });            
          
          var response = resource.save({
              to: detachData.to,
              from: detachData.from,
              startDate: new Date(detachData.startDate),
              endDate: new Date(detachData.endDate),
              shift: detachData.shift,
              comments: detachData.remarks
            }, function(data) {
              console.log('success, got data: ', data);
              successFn(data);
            }, function(error) {
              errorFn(error);
            });
          },          

            
        updatePersonMdaStatusOnServer: function ($resource, person, mdaData, boardData, successFn, errorFn) {
          //using person.sorId instead of id - Sriram Vasantha Jan 02 2015  
          var personId = person.id;
          var resource = $resource(boardData.pathStart + '/UpdatePersonMdaStatus/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });

          var endDate = null;
          if (mdaData.endDate) {
            var edt = new Date(mdaData.endDate);
            endDate = new Date(edt.getFullYear(), edt.getMonth(), edt.getDate(), mdaData.endTime.getHours(), mdaData.endTime.getMinutes(), 0, 0);
          }

          var response = resource.save({
            type: mdaData.type,
            subType: mdaData.subType,
            // changed from sorid to id
            id: mdaData.id,
            startDate: mdaData.startDate ? new Date(mdaData.startDate) : '',
                endDate: endDate,
                appointmentDate: mdaData.appointmentDate ? new Date(mdaData.appointmentDate) : '',
                    comments: mdaData.remarks
          }, function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        removePersonMdaStatusOnServer: function ($resource, person, mdaData, boardData, successFn, errorFn) {
          //using person.sorId instead of id - Sriram Vasantha Jan 02 2015  
          var personId = person.id;
          var resource = $resource(boardData.pathStart + '/RemovePersonMdaStatus/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,  
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });

          var record = {
              subType: mdaData.subType,
              // changed from sorid to id
              id: mdaData.id,
              startDate: Date.parse(mdaData.startDate),
              reasonForChange: mdaData.reasonForChange
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },
        
        cancelPersonDetachOnServer: function ($resource, person, detachData, boardData, successFn, errorFn) {
          
          var personId = person.id;
          
          var resource = $resource(boardData.pathStart + '/CancelDetachPerson/:district/:date/:personId', {
              district: boardData.boardLocation,
              date: boardData.boardDate,
              personId: personId
            }, {
              save: {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json'
                }
              }
            });            
          
          var response = resource.save({
              id: detachData.id,
              reasonForChange: detachData.reasonForChange,
              to: detachData.to,
              from: detachData.from,
              startDate: new Date(detachData.startDate),
              endDate: new Date(detachData.endDate),
              shift: detachData.shift,
              comments: detachData.remarks
            }, function(data) {
              console.log('success, got data: ', data);
              successFn(data);
            }, function(error) {
              errorFn(error);
            });
          },  

        createPersonMdaStatusOnServer: function ($resource, person, mdaData, boardData, successFn, errorFn) {
          //using person.sorId instead of id - Sriram Vasantha Dec 29 2014  
          var personId = person.id;
          var resource = $resource(boardData.pathStart + '/AddPersonMdaStatus/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });                  

          var endDate;

          // set end date
          if (mdaData.endDate && mdaData.endTime) {
            endDate = InfoService.getDateFormat(mdaData.endDate, mdaData.endTime);
          }      

          var response = resource.save({
            type: mdaData.type,
            subType: mdaData.subType,
            startDate: new Date(mdaData.startDate),
            endDate: endDate,
            appointmentDate: new Date(mdaData.appointmentDate),
            comments: mdaData.remarks
          }, function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        createPersonUnavailabilityOnServer: function ($resource, person, unavailabilityData, boardData, successFn, errorFn) {

          var personId = person.id;          

          var resource = $resource(boardData.pathStart + '/AddPersonUnavailabilityReason/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });         

          var endDate;
          var startDate;

          // set start date
          if (unavailabilityData.startDate && unavailabilityData.startTime) {
            startDate = InfoService.getDateFormat(unavailabilityData.startDate, unavailabilityData.startTime);
          }      

          // set end date
          if (unavailabilityData.endDate && unavailabilityData.endTime) {
            endDate = InfoService.getDateFormat(unavailabilityData.endDate, unavailabilityData.endTime);
          }            

          var record = {
              code: unavailabilityData.code,
              start: startDate,
              end: endDate,
              comments: unavailabilityData.remarks
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        addSpecialPositionOnServer: function ($resource, person, specialPositionData, boardData, successFn, errorFn) {

          var personId = person.id;
          var now = new Date();

          var resource = $resource(boardData.pathStart + '/AddSpecialPosition/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });


          var endDate = null;
          var startDate = null;
          if (specialPositionData.endDate)
            endDate = new Date(specialPositionData.endDate.getFullYear(), specialPositionData.endDate.getMonth(), specialPositionData.endDate.getDate(), now.getHours(), now.getMinutes(), 0, 0);

          if (specialPositionData.startDate)
            startDate = new Date(specialPositionData.startDate.getFullYear(), specialPositionData.startDate.getMonth(), specialPositionData.startDate.getDate(), now.getHours(), now.getMinutes(), 0, 0);

          var record = {
              code: specialPositionData.code,
              description: specialPositionData.description,
              startDate: startDate,
              endDate: endDate,
              comments: specialPositionData.comments
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        updateSpecialPositionOnServer: function ($resource, person, specialPositionData, boardData, successFn, errorFn) {

          var personId = person.id;
          var now = new Date();

          var resource = $resource(boardData.pathStart + '/UpdateSpecialPosition/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });


          var record = {
              code: specialPositionData.code,
              description: specialPositionData.description,
              startDate: specialPositionData.startDate ? new Date(specialPositionData.startDate) : '', 
                  endDate: specialPositionData.endDate ? new Date(specialPositionData.endDate) : '',
                      //changed to id
                      id: specialPositionData.id,
                      comments: specialPositionData.comments
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });

        },
        updatePersonUnavailabilityOnServer: function ($resource, person, unavailabilityData, boardData, successFn, errorFn) {

          var personId = person.id;

          var resource = $resource(boardData.pathStart + '/UpdatePersonUnavailabilityReason/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });


          var endDate;
          var startDate;

          // set start date
          if (unavailabilityData.startDate && unavailabilityData.startTime) {
            startDate = InfoService.getDateFormat(unavailabilityData.startDate, unavailabilityData.startTime);
          }      

          // set end date
          if (unavailabilityData.endDate && unavailabilityData.endTime) {
            endDate = InfoService.getDateFormat(unavailabilityData.endDate, unavailabilityData.endTime);
          }

          var record = {
              code: unavailabilityData.code,
              start: startDate,
              end: endDate,
              //changed from sorid to id
              id: unavailabilityData.id,
              reasonForChange: unavailabilityData.reasonForChange,
              comments: unavailabilityData.remarks
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });

        },


        cancelPersonUnavailabilityOnServer:function($resource, person, unavailabilityData, boardData, successFn, errorFn){


          var personId = person.id;

          var resource = $resource(boardData.pathStart + '/CancelPersonUnavailability/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });


          var endDate = null;
          if (unavailabilityData.endDate)              
            var endDate = new Date(unavailabilityData.endTime.getFullYear(), unavailabilityData.endTime.getMonth(), unavailabilityData.endTime.getDate(), unavailabilityData.endTime.getHours(), unavailabilityData.endTime.getMinutes(), 0, 0);
          var startDate=new Date(unavailabilityData.endTime.getFullYear(), unavailabilityData.endTime.getMonth(), unavailabilityData.endTime.getDate(), unavailabilityData.endTime.getHours(), unavailabilityData.endTime.getMinutes(), 0, 0);
          startDate.setHours(unavailabilityData.startTime.substring(0,2));
          startDate.setMinutes(unavailabilityData.startTime.substring(3,5));
          var systemDate=new Date();
          if(startDate.getFullYear()==systemDate.getFullYear() && startDate.getMonth()==systemDate.getMonth() && startDate.getDate()!=systemDate.getDate()){
            startDate.setHours(0);
            startDate.setMinutes(0);
            endDate.setHours(23);
            endDate.setMinutes(59);
          }

          var record = {
              code: unavailabilityData.code,
              start: startDate,
              end: endDate,
              id:unavailabilityData.id,
              comments: unavailabilityData.remarks

          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });


        },

        undoCancelPersonUnavailabilityOnServer:function($resource, person, unavailabilityData, boardData, successFn, errorFn){


          var personId = person.id;

          var resource = $resource(boardData.pathStart + '/ReverseCancelPersonUnavailability/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });


          var endDate = null;
          if (unavailabilityData.endDate)              
            var endDate = new Date(unavailabilityData.startDate.getFullYear(), unavailabilityData.startDate.getMonth(), unavailabilityData.startDate.getDate(), unavailabilityData.startDate.getHours(), unavailabilityData.startDate.getMinutes(), 0, 0);
          var startDate=new Date(unavailabilityData.startDate.getFullYear(), unavailabilityData.startDate.getMonth(), unavailabilityData.startDate.getDate(), unavailabilityData.startDate.getHours(), unavailabilityData.startDate.getMinutes(), 0, 0);
          startDate.setHours(0);
          startDate.setMinutes(0);
          endDate.setHours(23);
          endDate.setMinutes(59);
          /*
              var systemDate=new Date();
              if(startDate.getFullYear()==systemDate.getFullYear() && startDate.getMonth()==systemDate.getMonth() && startDate.getDate()!=systemDate.getDate()){
                  startDate.setHours(unavailabilityData.startDate.substring(0,2));
                  startDate.setMinutes(unavailabilityData.startDate.substring(3,5));
              }*/
          var record = {
              code: unavailabilityData.code,
              start: startDate,
              end: endDate,
              id:unavailabilityData.id,
              comments: unavailabilityData.remarks

          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });


        },
        removePersonUnavailabilityOnServer: function ($resource, person, unavailabilityData, boardData, successFn, errorFn) {

          var personId = person.id;

          var resource = $resource(boardData.pathStart + '/RemovePersonUnavailabilityReason/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });

          var record = {
              code: unavailabilityData.code,
              start: Date.parse(unavailabilityData.startDate),
              //changed from sorid to id
              id: unavailabilityData.id,
              reasonForChange: unavailabilityData.reasonForChange
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        removeSpecialPositionOnServer: function ($resource, person, specialPositionData, boardData, successFn, errorFn) {

          var personId = person.id;

          var resource = $resource(boardData.pathStart + '/RemoveSpecialPosition/:district/:date/:personId', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            personId: personId
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });

          var record = {
              code: specialPositionData.code,
              //changed to id
              id: specialPositionData.id,
              startDate: Date.parse(specialPositionData.startDate),
              reasonForChange: specialPositionData.reasonForChange
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        massChartUpdateOnServer: function($resource, cancelledIds, reverseCancelledIds, chartDate, boardData, successFn, errorFn) {

          var resource = $resource(boardData.pathStart + '/massChartUpdate/:district/:date/:chartDate', {
            district: boardData.boardLocation,
            date: boardData.boardDate,
            chartDate: chartDate
          }, {
            save: {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              }
            }
          });

          var record = {
            cancelPersonIds: cancelledIds,
            reverseCancelPersonIds: reverseCancelledIds
          }

          var response = resource.save(JSON.stringify(record), function(data) {
            console.log('success, got data: ', data);
            successFn(data);
          }, function(error) {
            errorFn(error);
          });
        },

        /** Section - Perform synchronous operation on server - End */

        /** Section - Handle Server Commands - Start */

        processAddPersonCommand: function (personnel, command, PersonModel) {
            var personId = command.commandContent.personId      
            var p = $filter('extendPerson')(command.commandContent.person);
            p.updateState(command.commandContent.assigned, command.commandContent.assignedAnywhere, command.commandContent.state);    
            personnel[personId] = p;
            $rootScope.$broadcast('UPDATE-PERSON-DETAILS', { personId: personId });
        },

        processAutoCompletePersonnelCommand : function(tasks, personnel, states, command) {
          var p1; 
          var p2; 
          if(command.commandContent.opsBoardPerson1MetaData.personId){  
            p1 = _processAutoPersonnelCommand(tasks.tasksMap, personnel, command,1);
          }
          if(command.commandContent.opsBoardPerson2MetaData.personId){  
            p2 = _processAutoPersonnelCommand(tasks.tasksMap, personnel, command,2);
          }
          if (p1) {
            if (p1.state !== states.personnel.partiallyAvailable) {
              p1.partialAvailability = false; // reset based on server state
            }
            p1 = $filter('extendPerson')(p1);
          }
          if (p2) {
            if (p2.state !== states.personnel.partiallyAvailable) {
              p2.partialAvailability = false; // reset based on server state
            }
            p2 = $filter('extendPerson')(p2);
          }
        },

        processMassChartUpdate: function (personnel, states, command) {
          var i,
            chartCancelled = command.commandContent.cancelledPersonsMetaData,
            reverseCancelled = command.commandContent.reverseCancelledPersonsMetaData;
            
          for(i = 0; i < chartCancelled.length; i++) {
            var p = personnel[chartCancelled[i].personId];
            var content = chartCancelled[i];
            content.unavailableReason = command.commandContent.cancelledReasons[chartCancelled[i].personId];
            _processPersonUnavailability(p, states, content);
          }
          for(i = 0; i < reverseCancelled.length; i++) {
            var p = personnel[reverseCancelled[i].personId];
            var content = reverseCancelled[i];
            content.unavailableReason = command.commandContent.reverseCancelledReasons[reverseCancelled[i].personId];
            _processPersonUnavailability(p, states, content);
          }
          
        },

        processPersonUnavailabilityCommand: function (tasks, personnel, states, command) {
          var p = _processGeneralPersonnelCommand(tasks, personnel, command);
          _processPersonUnavailability(p, states, command.commandContent);
        },

        processPersonGroundStatusCommand: function (tasks, personnel, states, command) {
            var p = _processGeneralPersonnelCommand(tasks, personnel, command);
            if(p) {       
                p.groundingHistory.unshift(command.commandContent.historicalGroundingStatus);
                p.grounded = command.commandContent.grounded;
                p = $filter('extendPerson')(p);
                $rootScope.$broadcast('UPDATE-PERSON-DETAILS',{personId: p.id});
                $rootScope.$broadcast('GROUND-PERSON', p);
            }
        },

        processSpecialPositionCommand: function (tasks, personnel, states, command) {
          var p = _processGeneralPersonnelCommand(tasks, personnel, command);
          if (p) {

            var processed = false;
            for (var i = 0; i < p.specialPositionsHistory.length; i++) {
              if (command.commandContent.specialPosition.replaces && command.commandContent.specialPosition.replaces == p.specialPositionsHistory[i].id) {
                processed = true;
                p.specialPositionsHistory[i] = command.commandContent.specialPosition;
              }
            }            

            if (!processed)
              p.specialPositionsHistory.push(command.commandContent.specialPosition);

            p.activeSpecialPositions = command.commandContent.activeSpecialPositions;
            p.formattedSpecialPositions = p.getFormattedSpecialPositions();
            p.showPaginationLinksForSpecialPositions = p.formattedSpecialPositions.length > 0;
            p.specialPositionsPaginationModel = p.formattedSpecialPositions.slice(0, itemsPerPage);

            $rootScope.$broadcast('SPECIAL-POSITION-PERSON', p);
          }
        },

        processPersonDetachmentCommand: function (tasks, personnel, states, command) {

          var p = _processGeneralPersonnelCommand(tasks, personnel, command);
          if(p){
            p.currentLocation = command.commandContent.currentLocation;
            
            if (p.state !== states.personnel.partiallyAvailable) {
                p.partialAvailability = false; // reset based on server state
             }  
            //if already existing update fields
            var processed = false;
            for (var i = 0; i < p.detachmentHistory.length; i++) {
                if (command.commandContent.detachment.replaces && command.commandContent.detachment.replaces == p.detachmentHistory[i].id) {
                    processed = true;
                    p.detachmentHistory[i] = command.commandContent.detachment;
                  }             
            }  
            
            if (!processed){
               p.detachmentHistory.push(command.commandContent.detachment);         
            }
            
            var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
            
            if(nextDayUnassignedShifts){
              nextDayUnassignedShifts.forEach(function(shift, index){
                var found = _.findWhere(p.assignedNextDayShifts, {id: shift.id});
                  if(found)
                    p.assignedNextDayShifts = _.without(p.assignedNextDayShifts, found);

              });                                   
            }
            
            p.formattedDetachments = p.getFormattedDetachmentHistory();
            p.showPaginationLinksForDetachments = p.formattedDetachments.length > 0;
            p.personDetachPaginationModel = p.formattedDetachments.slice(0, itemsPerPage);
            p.activeDetachment = p.detachmentHistory[0];
            $rootScope.$broadcast('DETACH-PERSON', p);
          }
        },

        processRemovePersonCommand: function (tasks, personnel, command) {
           var p = _processGeneralPersonnelCommand(tasks, personnel, command);   
           
           var nextDayUnassignedShifts = command.commandContent.nextDayUnassignedShifts;
             
             if(nextDayUnassignedShifts){
              nextDayUnassignedShifts.forEach(function(shift, index){
                var found = _.findWhere(p.assignedNextDayShifts, {id: shift.id});
                  if(found)
                    p.assignedNextDayShifts = _.without(p.assignedNextDayShifts, found);

              });                                   
             }
        },

        processPersonMdaStatusCommand: function (tasks, personnel, states, command) {
          var p = _processGeneralPersonnelCommand(tasks, personnel, command);
          if(p){
            p.state = command.commandContent.state;
            if (p.state !== states.personnel.partiallyAvailable) {
              p.partialAvailability = false; // reset based on server state
            }

            // Set any previous/historic recored to a status of updated
            var processed = false;
            for (var i = 0; i < p.mdaStatusHistory.length; i++) {
              if (command.commandContent.mdaStatus.replaces && command.commandContent.mdaStatus.replaces == p.mdaStatusHistory[i].id) {
                processed = true;
                p.mdaStatusHistory[i] = command.commandContent.mdaStatus;
              }
            }

            if (!processed)
              p.mdaStatusHistory.push(command.commandContent.mdaStatus);

            p.activeMdaCodes = command.commandContent.activeMdaStatus;
            p.formattedMdaStatus = p.getFormattedMdaStatus();
            p.showPaginationLinksForMda = p.formattedMdaStatus.length > 0;
            p.personMDAPaginationModel = p.formattedMdaStatus.slice(0, itemsPerPage);
            p = $filter('extendPerson')(p);
            _processPartialAvailabilityAndMda(p, new Date(), states);
            $rootScope.$broadcast('MDA-PERSON', p);
          }
        },

        personnelCountTypes: function(personnel, boardQuota, numAssigned, personnelpane, boroBoard, groups) {
          var personnelCountTypes = [];
         
          if(!boroBoard) {
            var sanitationWorkers = $filter('filterPersonnelByGroup')(personnel,  groups.personnel.sanitationWorkers); // This filter counts SWs payroll assigned to a location and attached to this location
            var sanitationWorkersUnavailable = $filter('unavailablePersonnelFilter')(sanitationWorkers); // This filter counts SWs under unavailable and MDA
            var sanitationWorkersHidden = $filter('hiddenPersonnelFilter')(sanitationWorkers); // This filter counts SWs that are hidden due to activity or detachment history
            var sanitationWorkersDetached = $filter('detachedPersonnelFilter')(sanitationWorkers);
            var sanitationWorkersAssignedNextDay = $filter('assignedNextDayFilter')(sanitationWorkers);
            var sanitationWorkersAssignedDayBefore = $filter('dayBeforeShiftsFilter')(sanitationWorkers);
            personnelCountTypes.push({type: 'Sanitation Workers', availablecount: (Object.keys(sanitationWorkers).length + sanitationWorkersAssignedNextDay.total - sanitationWorkersAssignedDayBefore.total) - sanitationWorkersUnavailable.total - sanitationWorkersDetached.total - sanitationWorkersHidden.total, quota: boardQuota.sanitationWorkers, actual: $filter('getActualSanitationWorkers')()});
            
          }
          var supervisors = $filter('filterPersonnelByGroup')(personnel,  groups.personnel.supervisors); // This filter counts SUPs payroll assigned to a location and attached to this location
          var supervisorsUnavailable = $filter('unavailablePersonnelFilter')(supervisors); // This filter counts SUPs under unavailable and MDA
          var supervisorsDetached = $filter('detachedPersonnelFilter')(supervisors);
          var supervisorsAssignedNextDay = $filter('assignedNextDayFilter')(supervisors);
          var supervisorsHidden = $filter('hiddenPersonnelFilter')(supervisors);
          personnelCountTypes.push({type: 'Supervisors', availablecount: (Object.keys(supervisors).length + supervisorsAssignedNextDay.total) - supervisorsUnavailable.total - supervisorsDetached.total - supervisorsHidden.total, quota: boardQuota.supervisors, actual: $filter('getActualSup')()});

          if(boroBoard) {
            var superintendents = $filter('filterPersonnelByGroup')(personnel,  groups.personnel.superintendents);
            var superintendentsUnavailable = $filter('unavailablePersonnelFilter')(superintendents);
            var superintendentsDetached = $filter('detachedPersonnelFilter')(superintendents);
            var superintendentsAssignedNextDay = $filter('assignedNextDayFilter')(superintendents);
            personnelCountTypes.push({type: 'Superintendents', availablecount: (Object.keys(superintendents).length + superintendentsAssignedNextDay.total) - superintendentsUnavailable.total - superintendentsDetached.total, quota: boardQuota.superintendents, actual: $filter('getActualSuperintendents')()});
          }

          for (var i = 0, len = personnelCountTypes.length, typeObject, delta; i < len; i ++) {
            typeObject = personnelCountTypes[i];
            delta = typeObject.availablecount - typeObject.quota;
            personnelCountTypes[i].quotadeltaflag = delta > 0 ? 'positive' : (!delta ? 0 : 'negative');
            personnelCountTypes[i].quotadelta = delta || 'E';
            delta = typeObject.availablecount - typeObject.actual;
            personnelCountTypes[i].actualdeltaflag = delta > 0 ? 'positive' : (!delta ? 0 : 'negative');
            personnelCountTypes[i].actualdelta = delta || 'E'; // neutral is marked as E with no color
          }
          return personnelCountTypes;
        },

        buildAssigned: function (cachedLocations, cachedLocationsKeys) {
          var locations = cachedLocations,
          locationsKeys = cachedLocationsKeys,
          assigned = [],
          nextDayAssigned = [],
          shifts = {},
          len = 0,
          nextDayLen = 0,
          numAssigned = 0,
          nextDayNumAssigned = 0;
          for (var locationsCount = 0, locationsLength = locationsKeys.length; locationsCount < locationsLength; locationsCount ++) {
            var location = locations[locationsKeys[locationsCount]], locationShifts = location.locationShifts ? Object.keys(location.locationShifts) : [];
            for (var lsCount = 0, lsLength = locationShifts.length; lsCount < lsLength; lsCount ++) {
              var val = locationShifts[lsCount],categories = Object.keys(location.locationShifts[val].shiftCategories);
              for (var catCount = 0, catLength = categories.length; catCount < catLength; catCount ++) {
                var categoryKey = categories[catCount];
                // assign shift model to shift tracking object by id
                var shift = location.locationShifts[val].shift, subCats = location.locationShifts[val].shiftCategories[categoryKey].subcategoryTasks;
                if (typeof subCats !== 'object') break;
                var shiftPersonnel = [];
                shift._personnel = [];
                shift._nextDayPersonnel = [];
                shifts[shift.id] = shift;
                subCats = Object.keys(subCats);
                // need to check for sections (household refuse for example)
                for (var subCatCount = 0, subCatLength = subCats.length; subCatCount < subCatLength; subCatCount ++) {
                  var taskId = subCats[subCatCount],
                  task = location.locationShifts[val].shiftCategories[categoryKey].subcategoryTasks[taskId],
                  sections = task.sections,
                  sectionsKeys, taskKeys;
                  if (typeof sections === 'object' && Object.keys(sections).length) {
                    sectionsKeys = Object.keys(sections);
                    // iterate section rows
                    for (var sectionCount = 0, sectionLength = sectionsKeys.length; sectionCount < sectionLength; sectionCount ++) {
                      var sectionTasks = sections[sectionsKeys[sectionCount]].tasks, sectionTasksKeys = Object.keys(sectionTasks);
                      // traverse SECTION TASKS in sections and grab personnel
                      for (var taskCount = 0, taskLength = sectionTasksKeys.length; taskCount < taskLength; taskCount ++) {
                        var taskEntry = sectionTasks[sectionTasksKeys[taskCount]],
                        persons = $filter('getPersonnelFromTask')(taskEntry, shift.id, assigned),                       
                        tmp = assigned.concat(persons);                        
                        assigned = tmp;    
                        
                        persons = $filter('getNextDayPersonnelFromTask')(taskEntry, shift.id, nextDayAssigned),                       
                        tmp = nextDayAssigned.concat(persons);                        
                        nextDayAssigned = tmp;  
                      }
                    }
                  } else if (task.tasks) {
                    taskKeys = Object.keys(task.tasks);
                    // traverse REGULAR TASKS and grab personnel
                    for (var taskCount = 0, taskLength = taskKeys.length; taskCount < taskLength; taskCount ++) {
                      var taskEntry = taskKeys[taskCount],
                      persons = $filter('getPersonnelFromTask')(task.tasks[taskEntry], shift.id, assigned),                  
                      tmp = assigned.concat(persons);
                      assigned = tmp;
                      
                      persons = $filter('getNextDayPersonnelFromTask')(task.tasks[taskEntry], shift.id, nextDayAssigned),                  
                      tmp = nextDayAssigned.concat(persons);
                      nextDayAssigned = tmp;
                    }
                  }
                }
              }
            }
          }          
          
          len = assigned.length;
          for (var i = 0, duplicate; i < len; i ++) {
            duplicate = false;
            // remove personnel already in list on same shift (multi routes)
            shifts[assigned[i]._shiftId]._personnel.forEach(function (val, index) {
              if (val.id === assigned[i].id) {
                duplicate = true;
              }
            });
            // link persons to shifts by shift id for each assigned person
            if (!duplicate) {
              shifts[assigned[i]._shiftId]._personnel.push(assigned[i]);
              numAssigned ++;
            }
          }
          
          nextDayLen = nextDayAssigned.length;
          for (var i = 0, duplicate; i < nextDayLen; i ++) {
            duplicate = false;
            // remove personnel already in list on same shift (multi routes)
            shifts[nextDayAssigned[i]._shiftId]._nextDayPersonnel.forEach(function (val, index) {
              if (val.id === nextDayAssigned[i].id) {
                duplicate = true;
              }
            });
            // link persons to shifts by shift id for each assigned person
            if (!duplicate) {
              shifts[nextDayAssigned[i]._shiftId]._nextDayPersonnel.push(nextDayAssigned[i]);
              nextDayNumAssigned ++;
            }
          }
          
          for(var id in shifts){
            if(shifts.hasOwnProperty(id)){
              shifts[id]._nextDayPersonnel.forEach(function(p, i){
                var found = _.findWhere(shifts[id]._personnel, {id: p.id, _assignmentType: undefined});
                if(found)
                  shifts[id]._personnel = _.without(shifts[id]._personnel, found);
              });
            }
          }
          
          return {shifts: shifts, numAssigned: numAssigned, nextDayNumAssigned: nextDayNumAssigned};
        }
        /** Section - Handle Server Commands - End */
      }
    });