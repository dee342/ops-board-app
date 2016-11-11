'use strict';

/**
 * This file contains all the logic for working with Equipment, including
 * creating commands, processing server commands, formatting and manipulating
 * task data.
 */
angular
    .module('OpsBoard')
    .service(
        'EquipmentHelperService',
        function($filter, $rootScope, groups, BoardDataService, MaterialService, BoardValueService) {

          var _addEquipmentProperties = function(value) {
            value.materialTypes = [''];
            value.materialTypeClass = $filter('materialTypeClassFilter')(1, value);
            value.loadType = $filter('loadTypeFilter')(value);
            value.snowReadinessProperty = $filter('snowReadinessFilter')(value);
            value.snowReadinessIndicator = $filter('snowReadinessIndicatorFilter')(value);
            value.snowReadinessOneIndicator = $filter('snowReadinessOneIndicator')(value);
            value.plowType = $filter('plowTypeFilter')(value);
            value.secondIndicator = $filter('secondIndicator')(value);
            value.isLoadStatusUpdatable = $filter('isLoadStatusUpdatable')(value, value.currentLocation);
            value.isEquipmentDressable = $filter('isEquipmentDressable')(value);
            value.isEquipmentDressed = $filter('isEquipmentDressed')(value);

            var materials = MaterialService.getMaterialsObject();
            var color = '';

            for (var i = 0; i < value.bins.length; i++) {
              color = '';
              if(value.bins[i].material) {
                color = materials[value.bins[i].material].color
              }

              value.materialTypes[i] = {material: $filter('materialTypeFilter')(i, value), color: color};
            };

            return value;
          };
          
          var _processGeneralEquipmentCommand = function(tasks, equipment, command) {
            var e = equipment[command.commandContent.equipmentId];
            if (e) {

              e.updateState(command.commandContent.assigned, command.commandContent.states);
              /*
               * SMARTOB-6923 - equipment should be dressed before filters are applied
               */
              if(command.commandContent.snowReadiness)
              {
            	  e.snowReadiness = command.commandContent.snowReadiness;
              }
              
              if (command.commandContent.tasks) {
                for (var i = 0; i < command.commandContent.tasks.length; i++) {
                  var tCommand = command.commandContent.tasks[i];
                  var t = tasks[tCommand.taskId];
                  if (tCommand.assignmentTime == null) {
                    t.clearEquipmentAssignment();               
                  } else {
                    t.setEquipmentAssignment(tCommand.assignmentTime, tCommand.completed, e);
                  }

                  t = $filter('formatTask')(t);
                }
              }
            }

            return e;
          };

          return {
            addEquipmentProperties: function(value) {
              return _addEquipmentProperties(value);
            },
            
            /** Section - Perform synchronous operations on server - Start */
            
            acceptAttachEquipmentOnServer : function($resource, pieceOfEquipment, receivedBy, receivedDate,
                receivedTime, remarks, boardData, successFn, errorFn) {

              var datetime = new Date(receivedDate.slice(6, 10), receivedDate.slice(0, 2) - 1,
                  receivedDate.slice(3, 5), receivedTime.slice(0, 2), receivedTime.slice(3, 5));

              var attachReq = {
            		receivedBy : receivedBy,
            		receivedDatetime : datetime,
            		remarks : remarks
              
              }
              
              var resource = $resource(boardData.pathStart + '/AttachEquipment/:district/:date/:equipmentId', {
                    district : BoardValueService.equipmentLocation,
                    date : boardData.boardDate,
                    equipmentId :  pieceOfEquipment.id,

                  }, {
                    save : {
                      method : 'POST',
                      headers : {
                        'Content-Type' : 'application/json'
                      }
                    }
                  });

                  var records = attachReq;
                  var response = resource.save(JSON.stringify(records), function(data) {
                    successFn(data);
                  }, function(error) {
                    errorFn(error);
                  });
            },

            cancelEquipmentDetachmentOnServer : function($resource, pieceOfEquipment, boardData, successFn, errorFn) {
              
              
              var detachReq = {};
                  
                  var resource = $resource(boardData.pathStart + '/CancelEquipmentDetachment/:district/:date/:equipmentId', {
                    district : boardData.boardLocation,
                    date : boardData.boardDate,
                    equipmentId :  pieceOfEquipment.id,

                  }, {
                    save : {
                      method : 'POST',
                      headers : {
                        'Content-Type' : 'application/json'
                      }
                    }
                  });

                  var records = detachReq;
                  var response = resource.save(JSON.stringify(records), function(data) {
                    successFn(data);
                  }, function(error) {
                    errorFn(error);
                  });
              
          
            },

            detachEquipmentOnServer : function($resource, pieceOfEquipment, to, time, driver, boardData, successFn,
                errorFn) {

              var datetime = new Date(boardData.boardDate.slice(0, 4), boardData.boardDate.slice(4, 6) - 1,
                  boardData.boardDate.slice(6, 8), time.slice(0, 2), time.slice(3, 5));
              
              var detachReq = {    
              		datetime : datetime,
              		driver : driver,
              		to : to,
              		from : pieceOfEquipment.currentLocation                
                }
                
                var resource = $resource(boardData.pathStart + '/DetachEquipment/:district/:date/:equipmentId', {
                  district : boardData.boardLocation,
                  date : boardData.boardDate,
                  equipmentId :  pieceOfEquipment.id,

                }, {
                  save : {
                    method : 'POST',
                    headers : {
                      'Content-Type' : 'application/json'
                    }
                  }
                });

                var records = detachReq;
                var response = resource.save(JSON.stringify(records), function(data) {
                  successFn(data);
                }, function(error) {
                  errorFn(error);
                });
            
            },

            downEquipmentOnServer : function($resource, pieceOfEquipment, downData, boardData, successFn, errorFn) {
              var equipmentId = pieceOfEquipment.id;
              var resource = $resource(boardData.pathStart + '/DownEquipment/:district/:date/:equipmentId', {
                district : BoardValueService.equipmentLocation,
                date : boardData.boardDate,
                equipmentId : equipmentId
              }, {
                save : {
                  method : 'POST',
                  headers : {
                    'Content-Type' : 'application/json'
                  }
                }
              });

              var datetime1 = new Date(downData.dt1.slice(6, 10), downData.dt1.slice(0, 2) - 1, downData.dt1
                  .slice(3, 5), downData.time1.slice(0, 2), downData.time1.slice(3, 5));

              try {
                var datetime2 = new Date(downData.dt2.slice(6, 10), downData.dt2.slice(0, 2) - 1, downData.dt2.slice(3,
                    5), downData.time2.slice(0, 2), downData.time2.slice(3, 5));
              } catch (err) {
                datetime2 = null;
              }

              try {
                var datetime3 = new Date(downData.dt3.slice(6, 10), downData.dt3.slice(0, 2) - 1, downData.dt3.slice(3,
                    5), downData.time3.slice(0, 2), downData.time3.slice(3, 5));

              } catch (err) {
                datetime3 = null;
              }

              var records = [ {
                reporter : downData.reporter1,
                dateTime : datetime1,
                comments : downData.remarks1,
                downCode : downData.selectedDownCode1,
                mechanic : downData.mechanic1,
                repairLocation : downData.selectedRepairLocation1
              }, {
            	  reporter : downData.reporter2,
            	  dateTime : datetime2,
                comments : downData.remarks2,
                downCode : downData.selectedDownCode2,
                mechanic : downData.mechanic2,
                repairLocation : downData.selectedRepairLocation2
              }, {
            	  reporter : downData.reporter3,
            	  dateTime : datetime3,
                comments : downData.remarks3,
                downCode : downData.selectedDownCode3,
                mechanic : downData.mechanic3,
                repairLocation : downData.selectedRepairLocation3
              } ];
              
              var req = {
            		  conditions : records
              };

              var response = resource.save(JSON.stringify(req), function(data) {
                successFn(data);
              }, function(error) {
                errorFn(error);
              });
            },

            updateEquipmentLoadStatusOnServer : function($resource, pieceOfEquipment, loadData, boardData, successFn,
                errorFn) {

              var equipmentId = pieceOfEquipment.id;
              var resource = $resource(boardData.pathStart + '/UpdateEquipmentLoad/:district/:date/:equipmentId', {
                district : boardData.boardLocation,
                date : boardData.boardDate,
                equipmentId : equipmentId
              }, {
                save : {
                  method : 'POST',
                  headers : {
                    'Content-Type' : 'application/json'
                  }
                }
              });

              var bin1Status, bin1Material, bin2Status, bin2Material;
              var records = [];

              if (loadData && loadData.length > 0) {
                for (var i = 0; i < loadData.length; i++) {
                  var bin = loadData[i];
                  if (bin.name === "BIN 1") {
                    bin1Status = bin.status;
                    bin1Material = bin.materialType;
                    records.push({
                      name : bin.name,
                      status : bin1Status,
                      materialType : bin1Material,
                      lastModifiedActual : bin.lastModifiedActual,
                      systemUser : pieceOfEquipment.bins[0].systemUser
                    });
                  } else if (bin.name === "BIN 2") {
                    bin2Status = bin.status;
                    bin2Material = bin.materialType;
                    records.push({
                      name : bin.name,
                      status : bin2Status,
                      materialType : bin2Material,
                      lastModifiedActual : bin.lastModifiedActual,
                      systemUser : pieceOfEquipment.bins[1].systemUser
                    });
                  }
                }
              }

              var response = resource.save(JSON.stringify(records), function(data) {
                successFn(data);
              }, function(error) {
                errorFn(error);
              });
            },

            updateSnowEquipmentOnServer : function($resource, pieceOfEquipment, snowData, boardData, successFn, errorFn) {
              var equipmentId = pieceOfEquipment.id;
              var resource = $resource(boardData.pathStart
                  + '/UpdateEquipmentSnowReadiness/:district/:date/:equipmentId', {
                district : BoardValueService.equipmentLocation,
                date : boardData.boardDate,
                equipmentId : equipmentId,

              }, {
                save : {
                  method : 'POST',
                  headers : {
                    'Content-Type' : 'application/json'
                  }
                }
              });

              var records = snowData;
              var response = resource.save(JSON.stringify(records), function(data) {
                successFn(data);
              }, function(error) {
                errorFn(error);
              });
            },

            upEquipmentOnServer : function($resource, pieceOfEquipment, upData, boardData, successFn, errorFn) {
              var equipmentId = pieceOfEquipment.id;
              var datetime1 = new Date(upData.dt1.slice(0, 4), upData.dt1.slice(4, 6) - 1, upData.dt1.slice(6, 8), upData.time1.slice(0, 2), upData.time1.slice(3, 5));

              var upEquipmentReq = {
            		  datetime : datetime1,
            		  reporter : upData.reporter,
            		  mechanic : upData.mechanic
              };
              var resource = $resource(boardData.pathStart + '/UpEquipment/:district/:date/:equipmentId', {
                district : BoardValueService.equipmentLocation,
                date : boardData.boardDate,
                equipmentId : equipmentId
              },{
            	  save: {
            		  method: 'POST',
            		  headers: {
            			  'Content-Type' : 'application/json'
            		  }
            	  }
              });
              
              var records = upEquipmentReq;
              var response = resource.save(JSON.stringify(records), function(data){
            	  successFn(data);
              }, function(error){
            	  errorFn(error);
              });

            },

            /** Section - Perform synchronous operations on server - End */

            /** Section - Handle Server Commands - Start */

            processAddEquipmentCommand : function(equipment, command, EquipmentModel) {
              var equipmentKey = command.commandContent.equipmentId;
              var e = command.commandContent.equipment;
              
              // Override equipment properties from command
              e.assigned = command.commandContent.assigned
              e.states = command.commandContent.states; 
              
              angular.extend(e, EquipmentModel);
              e = _addEquipmentProperties(e);
              equipment[equipmentKey] = e;
            },
           
            processEquipmentDetachmentCommand : function(tasks, equipment, command) {

              var e = _processGeneralEquipmentCommand(tasks, equipment, command);
              if (e != null) {
                e.currentLocation = command.commandContent.currentLocation;
                var record = {
                  'comments' : command.commandContent.comments,
                  'driver' : command.commandContent.driver,
                  'from' : command.commandContent.fromCode,
                  'status' : command.commandContent.status,
                  'to' : command.commandContent.toCode,
                  'actualUser' : command.commandContent.reporter,
                  'lastModifiedActual' : new Date(command.commandContent.date),
                  'lastModifiedSystem' : new Date(command.commandContent.systemDateTime),
                  'systemUser' : command.commandContent.systemUser
                };

                var lastDetachment = e.detachmentHistory[e.detachmentHistory.length - 1];

                if(e.detachmentHistory.length > 0 && lastDetachment && (lastDetachment.status === command.commandContent.status) && (moment(lastDetachment.lastModifiedActual).format() === moment(command.commandContent.date).format())) {
                  //Duplicate record---skip.
                } else {
                  e.detachmentHistory.push(record);
                  e.detachmentCount++;
                }

                e.formattedDetachments = e.getFormattedDetachments();
                e.equipmentDetachmentPaginationModel = e.formattedDetachments.slice(0, 5);
                e.detachmentCurrentPage = 1;

                e = _addEquipmentProperties(e);
              }
            },

            processRemoveEquipmentCommand : function(tasks, equipment, command) {

              var e = _processGeneralEquipmentCommand(tasks, equipment, command);
              
              if(e != null){            	 
            	  e = _addEquipmentProperties(e);            	  
              }
                            
            },
            
            processUpdateSnowEquipmentCommand : function(tasks, equipment, command) {
              var e = _processGeneralEquipmentCommand(tasks, equipment, command);
              if (e != null) {                
                e = _addEquipmentProperties(e);
              }
            },
            
            processUpdateEquipmentLoadCommand : function(tasks, equipment, command) {
              var e = _processGeneralEquipmentCommand(tasks, equipment, command);
              if (e != null) {
                if (e.bins && e.bins[0] && command.commandContent.bin1) {
                  e.bins[0] = command.commandContent.bin1;
                }
                if (e.bins && e.bins[1] && command.commandContent.bin2) {
                  e.bins[1] = command.commandContent.bin2;
                }
                if (e.bins && command.commandContent.bin1) {
                  e.formattedBins = e.getFormattedBins(BoardDataService.getMaterials());
                }
              }
              e = _addEquipmentProperties(e);
            },
            
            processUpDownEquipmentCommand : function(tasks, equipment, command) {
              var e = _processGeneralEquipmentCommand(tasks, equipment, command);
              if (e != null) {
                var record = {
                  "id" : command.commandContent.upDownData.id,
                  "down" : command.commandContent.upDownData.down,
                  "actualUser" : command.commandContent.upDownData.actualUser,
                  "systemUser" : command.commandContent.upDownData.systemUser,
                  "lastModifiedActual" : command.commandContent.upDownData.lastModifiedActual,
                  "lastModifiedSystem" : command.commandContent.upDownData.lastModifiedSystem,
                  "conditions" : command.commandContent.upDownData.conditions
                };

                var lastUpDown = e.upDownHistory[e.upDownHistory.length - 1];

                if(e.upDownHistory.length > 0 && lastUpDown && (lastUpDown.down === command.commandContent.upDownData.down) && (moment(lastUpDown.lastModifiedActual).format() === moment(command.commandContent.upDownData.lastModifiedActual).format())) {
                  //Duplicate record---skip.
                } else {
                  e.upDownHistory.push(record);
                  e.conditionsCount = e.conditionsCount + command.commandContent.upDownData.conditions.length;
                }

                e.FormattedUpDownHistory = e.getFormattedUpDownHistory();
                e.upDownPaginationModel = e.FormattedUpDownHistory.slice(0, 5);
                e.upDownCurrentPage = 1;
                e = _addEquipmentProperties(e);
              }
            },
            
            /****************************************************************************************************
             Live update changes:
             Added locations, zeroQuota, totalCombinedLocations as parameters
             
             locations = passing the location object when the there is QETest type garage
             zeroQuota = if true, then the quota statistics are zeroed out
             totalCombinedLocations = passing that allows all the equipment to be calculated without location being considered - used for combined summary container
             *************************************************************************************************/
            equipmentCountTypes : function(equipment, boardQuota, locations, zeroQuota, totalCombinedLocations) {
              var equipmentCountTypes = [
                {type: 'Rear Loaders', availablecount: $filter('summaryCountResults')(equipment, groups.equipment.rearLoader, locations, totalCombinedLocations), quota: (zeroQuota === true) ? 0 : boardQuota.rearLoaders, actual: $filter('getActualEquipmentTotal')('rearloader', locations)},
                {type: 'Dual Bins', availablecount: $filter('summaryCountResults')(equipment, groups.equipment.dualBins, locations, totalCombinedLocations), quota: (zeroQuota === true) ? 0 : boardQuota.dualBins, actual: $filter('getActualEquipmentTotal')('dualbin', locations)},
                {type: 'Mechanical Broom', availablecount: $filter('summaryCountResults')(equipment, groups.equipment.mechanicalBrooms, locations,totalCombinedLocations), quota: (zeroQuota === true) ? 0 : boardQuota.mechanicalBrooms, actual: $filter('getActualEquipmentTotal')('mechbroom', locations)},
                {type: 'Ro-Ros', availablecount: $filter('summaryCountResults')(equipment, groups.equipment.roRo, locations, totalCombinedLocations), quota: (zeroQuota === true) ? 0 : boardQuota.roros, actual: $filter('getActualEquipmentTotal')('roros', locations)},
                {type: 'EZ-Packs', availablecount: $filter('summaryCountResults')(equipment, groups.equipment.ezPacks, locations, totalCombinedLocations), quota: (zeroQuota === true) ? 0 : boardQuota.ezPacks, actual: $filter('getActualEquipmentTotal')('ezpack', locations)}
              ];

              for (var i = 0, len = equipmentCountTypes.length, typeObject, delta; i < len; i ++) {
                typeObject = equipmentCountTypes[i];
                delta = (zeroQuota === true) ? 0 : typeObject.availablecount - typeObject.quota;
                equipmentCountTypes[i].quotadeltaflag = delta > 0 ? 'positive' : (!delta ? 0 : 'negative');
                equipmentCountTypes[i].quotadelta = (zeroQuota === true) ? 0 : delta || 'E';
                delta = typeObject.availablecount - typeObject.actual;
                equipmentCountTypes[i].actualdeltaflag = delta > 0 ? 'positive' : (!delta ? 0 : 'negative');
                equipmentCountTypes[i].actualdelta = (zeroQuota === true) ? typeObject.availablecount - typeObject.actual : delta || 'E'; // neutral is marked as E with no color
              }

             equipmentCountTypes =	$filter('emptyEntryFilter')(equipmentCountTypes);	
              
              return equipmentCountTypes;
            }
          /** Section - Handle Server Commands - End */
          };
        });
