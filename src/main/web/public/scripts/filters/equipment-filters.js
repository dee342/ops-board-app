'use strict';

angular.module('equipmentFilters', [])
  .filter('locationEquipment',
  function($filter) {
    return function(equipment, equipmentLocation) {
      var results = {};

      for (var key in equipment) {
        if(equipment[key].currentLocation === equipmentLocation || equipment[key].states[equipmentLocation] === 'Pending Attach' || equipment[key].states[equipmentLocation] === 'Detached') {
          results[key] = equipment[key];
        }
      }

      return results;
    }
  }).filter('roRoFilter',
    function($filter, BoardValueService) {
      return function(equipment, groups, boardlocation, states, roRos) {
      var results = $filter('filterByGroup')(equipment, groups, boardlocation);
      var availableResults = $filter('availableFilter')(results, states, list);
      roRos.show = false;
      for (var key in availableResults) {
        var r = availableResults[key];
        if (r.states[BoardValueService.equipmentLocation] != states.equipment.detached) {
          roRos.show = true;
          break;
        }
      }
      roRos.total = Object.keys(availableResults).length;
      if(roRos.total == 0 && isAnyEquipmentOnLocation(results)) {
        roRos.show = true;
      }

      return {availableResults: availableResults, roRosShow: roRos.show};
    }
  }).filter('materialTypeClassFilter',
  function(BoardDataService) {
    return function(index, pieceOfEquipment) {
      var materials = BoardDataService.getMaterials();
      if (!isNaN(index) && pieceOfEquipment.bins.length >= index + 1) {
        var bins = pieceOfEquipment.getFormattedBins(materials);
        var bin = bins[index];
        if (bin.status === 'Empty') return '';
        return 'M' + bin.materialType;
      }
      return '';
    }
  }).filter('snowReadinessIndicatorFilter',
  function($filter) {
    return function(pieceOfEquipment) {
      var e = pieceOfEquipment;
      var indicator = $filter('snowReadinessFilter')(e);

      if(!indicator){
        if(e.snowReadiness.chained) {
          indicator = true;
        }
      }

      return indicator;
    }
  }).filter('snowReadinessFilter',
  function($filter) {
    return function(pieceOfEquipment) {
      var e = pieceOfEquipment;
      var isSnowReady = false;
      if(e.hasOwnProperty('group')){
        if($filter('isEquipmentDressed')(e)) {
          isSnowReady = true;
        }
      }

      return isSnowReady;
    }
  }).filter('isEquipmentDressed',
  function() {
    return function(pieceOfEquipment) {
      var e = pieceOfEquipment;
      var isDressed = false;
      if((e.snowReadiness.plowType != null && e.snowReadiness.plowType != "NO_PLOW" ) || (e.snowReadiness.load != null && e.snowReadiness.load != "NONE")) {
        isDressed = true;
      }
      return isDressed;
    }
  }).filter('secondIndicator',
  function() {
    return function(pieceOfEquipment) {
      if(pieceOfEquipment.snowReadiness.load=="SALT"||pieceOfEquipment.snowReadiness.load=="SAND")
        return true;
      else if(pieceOfEquipment.bins.length>1){
        return true;
      }
      return false;
    }
  }).filter('plowTypeFilter',
  function(plowDirections) {
    return function(thisequipment) {
      var e = thisequipment;
      var plowType = e.snowReadiness.plowType;
      var direction = e.snowReadiness.plowDirection;
      var chained = e.snowReadiness.chained;
      var indicator = '';

      if (plowType === 'REGULAR_PLOW'){
        if(direction === plowDirections[0]) {
          indicator = 'R';
        } else if(direction === plowDirections[1]) {
          indicator = 'L';
        } else if(direction === plowDirections[2]) {
          indicator = 'S';
        } else if(direction === plowDirections[3]) {
          indicator = 'W';
        } else {
          indicator = 'R';
        }

      } else if(plowType === 'MINI_V_PLOW') {
        indicator = 'M';
      } else if(plowType === 'LARGE_V_PLOW') {
        indicator = 'V';
      } else if(chained) {
        indicator = 'C';
      }

      return indicator;
    }
  }).filter('loadTypeFilter',
  function() {
    return function(pieceOfEquipment) {
      if (pieceOfEquipment.snowReadiness.load=='SALT')
        return 'SL'
      else if(pieceOfEquipment.snowReadiness.load=='SAND')
        return 'SN';

      return '';
    }
  }).filter('snowReadinessOneIndicator',
  function($filter) {
    return function(pieceOfEquipment) {
      return ($filter('snowReadinessFilter')(pieceOfEquipment)|| pieceOfEquipment.snowReadiness.chained) && !$filter('secondIndicator')(pieceOfEquipment);
    }
  }).filter('materialTypeFilter',
  function(BoardDataService) {
    return function(index, pieceOfEquipment) {
      var materials = BoardDataService.getMaterials();
      if (!isNaN(index) && pieceOfEquipment.bins.length >= index + 1) {
        var bins = pieceOfEquipment.getFormattedBins(materials);
        var bin = bins[index];
        if (bin.status === 'Empty') {
          return 'E';
        }
        return bin.materialType;
      }
      return '';
    }
  }).filter('isLoadStatusUpdatable',
  function(states) {
    return function(pieceOfEquipment) {
      var isLoadStatusUpdatable = false;

      if (pieceOfEquipment && (states.equipment.pendingLoad === pieceOfEquipment.states[pieceOfEquipment.currentLocation] || ( ((states.equipment.available === pieceOfEquipment.states[pieceOfEquipment.currentLocation]) || (states.equipment.down === pieceOfEquipment.states[pieceOfEquipment.currentLocation])) && pieceOfEquipment.bins && pieceOfEquipment.bins.length > 0))) {
        isLoadStatusUpdatable = true;
      }

      return isLoadStatusUpdatable;
    }
  }).filter('isEquipmentDressable',
  function() {
    return function(pieceOfEquipment) {
      if (!pieceOfEquipment) return false;
      return (pieceOfEquipment.subTypeObj.canBeChained ||
        pieceOfEquipment.subTypeObj.plowTypes.length > 0 ||
        pieceOfEquipment.subTypeObj.loads.length > 0) ||
        pieceOfEquipment.subTypeObj.code === '2200' ||
        pieceOfEquipment.subTypeObj.code === '5000';
    }
  }).filter('availableFilter',
  function($filter, states) {
    return function(equipment, equipmentLocation) {
      var results = {},
        relayResults = $filter('relayFilter')(equipment, equipmentLocation),
        rolloverResults = $filter('rolloverFilter')(equipment, equipmentLocation);

        angular.forEach(equipment, function(value, key) {
        if (!value.assigned && value.states[equipmentLocation] === states.equipment.available && !relayResults[key] && !rolloverResults[key]) {
          value = $filter('appendBinStatus')(value);
          results[key] = value;
        }
      });

      return results;
    }
  }).filter('relayFilter',
  function(states, binStatus, BoardDataService, $filter) {
    return function(equipment, equipmentLocation) {
      var results = {};
      var materials = BoardDataService.getMaterials();

      angular.forEach(equipment, function(value, equipKey) {
        var bins = value.getFormattedBins(materials);
        angular.forEach(bins, function(bin, binKey) {
          if (!bin) {
            value = $filter('appendBinStatus')(value);
          }
          if (bin.status.toUpperCase() === binStatus.equipment.relay && value.states[equipmentLocation] === states.equipment.available && !value.assigned) {
            results[equipKey] = value;
          }
        });
      });

      return results;
    }
  }).filter('rolloverFilter',
  function(states, binStatus, BoardDataService, $filter) {
      return function (equipment, equipmentLocation) {
      var results = {};
      var materials = BoardDataService.getMaterials();

      angular.forEach(equipment, function(value, equipKey) {
        var bins = value.getFormattedBins(materials);
        angular.forEach(bins, function(bin, binKey) {
          if (!bin) {
              value = $filter('appendBinStatus')(value);
          }
                
          if (bin.status.toUpperCase() === binStatus.equipment.rollover && value.states[equipmentLocation] === states.equipment.available && !value.assigned) {
            results[equipKey] = value;
          }
        });
      });
     
      return results;
    }
  }).filter('appendBinStatus',
  function() {
    return function(equipment) {
      if (!equipment.bins)
        equipment.bins = [];
      return equipment;
    }
  }).filter('summaryCountResults',
  function($filter, BoardValueService) {
    return function(equipment, group, currentLocation, combinedTotal) {
      var currentLocationString = "";
      /****** added check for locations with more than one equipment location *******/
      if (BoardValueService.equipmentLocations.length > 1){
    	  for (var key in currentLocation){
    		  currentLocation = key;
    		  break;
    	  }
    	  currentLocationString = currentLocation;
      }else{
    	  currentLocationString = BoardValueService.equipmentLocation;
      }
      
      var results = {};
      var availableSummaryResults = {};
      /****** if combined total - we ignore the location string and process the whole equipment - for combined location table *******/
      if (combinedTotal){
    	  results = $filter('filterByCombinedGroup')(equipment, group); 
    	  availableSummaryResults = $filter('availableSummaryFilter')(results, currentLocationString, true);
      }else{
    	  results = $filter('filterByGroup')(equipment, group, currentLocationString);
    	  availableSummaryResults = $filter('availableSummaryFilter')(results, currentLocationString, false);
      }
       
      return Object.keys(availableSummaryResults).length;
    }
  }).filter('availableSummaryFilter',
  function(states, BoardValueService) {
    return function(equipment, currentLocation, combined) {
      var results = {};
      if (!combined){
        angular.forEach(equipment, function(value, key) {
          if (value.states[currentLocation] === states.equipment.available || 
              value.states[currentLocation] === states.equipment.pendingLoad) {
            results[key] = value;
          }
        });
      }else{
        var locations = BoardValueService.equipmentLocations;
        angular.forEach(locations, function(currentLocation) {
          angular.forEach(equipment, function(value, key) {
              if (value.states[currentLocation] === states.equipment.available || 
                  value.states[currentLocation] === states.equipment.pendingLoad) {
                results[key] = value;
              } 
          });
        });
      }

      return results;
    }
  }).filter('filterByGroup',
  function($filter, OpsBoardRepository) {
    return function(list, group, currentLocation) {
      var results = {};
      angular.forEach(list, function(value, key) {
        if(currentLocation === value.currentLocation) {
          if (value.hasOwnProperty('group') && value.group === group) {
            value = $filter('appendBinStatus')(value);
            results[key] = value;
          }
        }
      });
      return results;
    }
  }).filter('filterByCombinedGroup',
	function($filter) {
	    return function(list, group) {
	      var results = {};
	      angular.forEach(list, function(value, key) {
	          if (value.hasOwnProperty('group') && value.group === group) {
	            value = $filter('appendBinStatus')(value);
	            results[key] = value;
	          }
	      });
	      return results;
	    }
   }).filter('filterByLocation',
	function($filter, OpsBoardRepository) {
	    return function(list, currentLocation) {
	      var results = {};
	      angular.forEach(list, function(value, key) {
	        if(currentLocation === value.currentLocation) {
	            results[key] = value;
	        }
	      });
	      return results;
	    }
 }).filter('filterCombinedLocations',
	function($filter, OpsBoardRepository) {
	    return function(list, locations) {
	      var results = {};
	      angular.forEach(locations, function(locationString){
		      angular.forEach(list, function(value, key) {
		        if(locationString === value.currentLocation) {
		            results[key] = value;
		        }
		      });
	      });
	      return results;
	    }
}).filter('emptyEntryFilter',
  function($filter) {
    return function(equipment) {
      var results = [];
      for(var i=0; i < equipment.length; i++) {
    	  var removeEntry = true;    // flag to check whether to remove object from array
          for(var key in equipment[i]){
        	  key = key.toString();
        	  if(key !== "type" && equipment[i][key] !== "positive" && equipment[i][key] !== "negative" && equipment[i][key] !== 0 && equipment[i][key] !== "E"){
        		  removeEntry = false;
        		  break;				// If value other than 0 or "E" than break.
        	  }
          }
          if(!removeEntry)
        	 results.push(equipment[i]);
       }
       return results;		// return results array after removing objects with 0 counts
    }
}).filter('getActualEquipmentTotal',
  function($filter,states,OpsBoardRepository) {
    return function(equipmentType, locations) {
      var equipmentCode = "";
      switch(equipmentType){
        case "rearloader":
          equipmentCode = "1000/1125";
          break;
        case "dualbin":
          equipmentCode = "1025";
          break;
        case "mechbroom":
          equipmentCode = "2000";
          break;
        case "roros":
          equipmentCode = "1250";
          break;
        case "ezpack":
          equipmentCode = "1200";
          break;
      }

      var actualEquipmentAmt = 0;
      var linkedTasks = OpsBoardRepository.getLinkedTaskMap();
      angular.forEach(locations, function (currentLocation) {
        angular.forEach(currentLocation.locationShifts, function (value) {
          angular.forEach(value.shiftCategories, function (value) {
        	var singleTaskArray = _.toArray(value.subcategoryTasks);
            angular.forEach(value.subcategoryTasks, function (value) {
              var equipmentSubTypes =  value.subcategory.equipmentSubTypes;
              /* Convert the object to an array so it is not sorted by key */
              var singleTaskArray = _.toArray(value.tasks);
              var taskArray = [];
              var numbOfTasksPerCategory = 0;
              
              /***** array used to compare groups to linkedTask Array group *******/
              var linkTaskGroups = _.pluck(linkedTasks, "groupId");
              var uniqLinkTaskGroup = _.uniq(linkTaskGroups);
              angular.forEach(value.sections, function (section) {
            	/* Convert the object to an array so it is not sorted by key */
            	taskArray = _.toArray(section.tasks);
                angular.forEach(taskArray, function (task) {
                	/* Check for the non linked tasks first*/
                    if((task.linkedTaskParentId == null && task.linkedTaskChildId == null)){
                    	  numbOfTasksPerCategory ++;
                    }
                });
              });
              angular.forEach(singleTaskArray, function (task) {
            	  /* Check for the non linked tasks first*/
                  if((task.linkedTaskParentId == null && task.linkedTaskChildId == null)){
                	  numbOfTasksPerCategory ++;
                  }
              });
              
              /******* Linked Tasks Section ********/
              for (var i=0; i<linkedTasks.length; i++){
            	  /* If the linkedTask is in that subCategory, then process it */
            	  if (linkedTasks[i].subcategoryTaskId === value.id){
            		  /* Homogeneous parent, only process the parent */
	            	  if ( (linkedTasks[i].homogeneous === true ) && (linkedTasks[i].linkedTaskParentId === null && linkedTasks[i].linkedTaskChildId !== null)){
	            		  numbOfTasksPerCategory++;
	            	/* If the linkedTask is not in the same subcategory, (not homogeneous) */
	            	  }else if (linkedTasks[i].homogeneous === false){
	            		  for (var j=0; j<uniqLinkTaskGroup.length; j++){
	            			  if (linkedTasks[i].groupId == uniqLinkTaskGroup[j]){
	            				  uniqLinkTaskGroup.splice(j, 1);
	            				  numbOfTasksPerCategory++;
	            			  }
	            		  }
	            	  }
            	  }
              }
              if (equipmentSubTypes !== undefined) {
                if (equipmentCode.indexOf("/") !== 0 ){
                  var equipmentCodeArray = equipmentCode.split("/");
                  if (equipmentSubTypes.length &&
                    (equipmentSubTypes[0].equipmentSubType.code === equipmentCodeArray[0] ||
                    value.subcategory.equipmentSubTypes[0].equipmentSubType.code === equipmentCodeArray[1])) {
                    actualEquipmentAmt = actualEquipmentAmt + numbOfTasksPerCategory;
                  }
                }else{
                  if (equipmentSubTypes[0].equipmentSubType.code === equipmentCode){
                    actualEquipmentAmt = actualEquipmentAmt + numbOfTasksPerCategory;
                  }
                }
              }
            })
          })
        })
      });
      return actualEquipmentAmt;
    }
  });