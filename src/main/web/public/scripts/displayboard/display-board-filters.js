'use strict';

var titleHierarchy =  ['GS V', 'GS IV', 'GS III','GS II', 'GS I', 'SUP', 'SW', 'Civilian'];
var titles =   {
    'gsI' : 'GS I',
    'gsII' : 'GS II',
    'gsIII' : 'GS III',
    'sup' : 'SUP',
    'sw' : 'SW',
    'civilian' : 'Civilian'
};

var sortPersonnelPanel = function(list, filterType, location) {
    if (!list) return list;
    if (!filterType) filterType = 'Location Seniority';

    return list.sort(function(me, that) {


        if(filterType === 'Location Seniority' || filterType === 'Location Reverse Seniority') {

            if (me.homeLocation !== location && that.homeLocation === location)
             return 1;

             if (me.homeLocation === location && that.homeLocation !== location)
             return -1;

            if (me.homeLocation > that.homeLocation)
                return 1;

            if (me.homeLocation < that.homeLocation)
                return -1;

            if (me.payrollLocationId > that.payrollLocationId)
                return 1;

            if (me.payrollLocationId < that.payrollLocationId)
                return -1;
        }

        if (filterType === 'Seniority' || filterType === 'Location Seniority') {

            if(me.seniorityDate > that.seniorityDate)
                return 1;

            if(me.seniorityDate < that.seniorityDate)
                return -1;

            if(me.listNumber && !that.listNumber)
                return 1;

            if(!me.listNumber && that.listNumber)
                return -1;

            if(!me.listNumber && !that.listNumber)
                return 0;

            if(Number(me.listNumber.replace(/[^0-9]+/g, '')) > Number(that.listNumber.replace(/[^0-9]+/g, '')))
                return 1;

            if(Number(me.listNumber.replace(/[^0-9]+/g, '')) < Number(that.listNumber.replace(/[^0-9]+/g, '')))
                return -1;

            return 0;
        }

        if (filterType === 'Reverse Seniority' || filterType === 'Location Reverse Seniority') {


            if(me.seniorityDate < that.seniorityDate)
                return 1;

            if(me.seniorityDate > that.seniorityDate)
                return -1;

            if(me.listNumber && !that.listNumber)
                return -1;

            if(!me.listNumber && that.listNumber)
                return 1;

            if(!me.listNumber && !that.listNumber)
                return 0;

            if(Number(me.listNumber.replace(/[^0-9]+/g, '')) < Number(that.listNumber.replace(/[^0-9]+/g, '')))
                return 1;

            if(Number(me.listNumber.replace(/[^0-9]+/g, '')) > Number(that.listNumber.replace(/[^0-9]+/g, '')))
                return -1;

            return 0;
        }
    });
};

angular.module('displayBoardFilters', ['locationFilters'])

    .filter('unavailablePersonnel',function(){
        return function(persons, code, boardDate){
          var results = [];
          var dateObj = new Date(boardDate.year, boardDate.month, boardDate.day, boardDate.hours, boardDate.minutes, boardDate.seconds, boardDate.milliseconds)
          var boardDate = dateObj.valueOf();


          angular.forEach(persons, function(value, key) {
              if (
                  value.hasOwnProperty('state')
                  && ((value.state === 'PartiallyAvailable' || value.state === 'Unavailable')
                  && value.activeUnavailabilityReasons[0]) && value.activeUnavailabilityReasons[0].code === code
                  && (boardDate > value.activeUnavailabilityReasons[0].start && (boardDate < value.activeUnavailabilityReasons[0].end) || value.activeUnavailabilityReasons[0].end === null)
                ) {
                results[key] = value;
              }
          });

          var results = _.uniq(results);
          if (!results || results.length == 0) {
            return results;
          }

          var sortedResults = sortPersonnelPanel(results, 'Location Seniority');

          return sortedResults;
        }

    }).filter('detachedPersonnel',function(){

        return function(persons){
            var results = [];

            angular.forEach(persons, function(value, key) {
                if (value.hasOwnProperty('state') && value.state === 'Detached') {
                    results[key] = value;
                }
            });

            var results = _.uniq(results);

            if (!results || results.length == 0) {
                return results;
            }

            return results;
        }

    }).filter('detachedWithinZonePersonnel',function(){
        return function(persons){
            var results = [];

            angular.forEach(persons, function(value, key) {
                if (value.hasOwnProperty('state') && value.state === 'Detached') {
                    if (value.activeDetachment.from.substr(0,2) === value.activeDetachment.to.substr(0,2)) {
                        results[key] = value;
                    }
                }
            });

            var results = _.uniq(results);

            if (!results || results.length == 0) {
                return results;
            }

            return results;
        }

    }).filter('detachedZoneTasks', function() {
      return function (taskContainers, id, personnel, refData) {
          var results = [], person = {}, shift = '', code = '', departmentType = '';
          _.forEach(taskContainers, function (location) {
            refData = refData;
              code = location.locationCode;
              _.forEach(location.locationShifts, function (locationShift) {
                    refData = refData;
                    shift = refData.shifts[locationShift.shiftId];
                  _.forEach(locationShift.shiftCategories, function (shiftCategory) {
                      _.forEach(shiftCategory.subcategoryTasks, function (subcategoryTask) {
                          if (parseInt(subcategoryTask.subcategoryId) === parseInt(id)) {
                              _.forEach(subcategoryTask.tasks, function (task) {
                                var person = {};
                                if (task.assignedPerson1.personId) {
                                  person = personnel[task.assignedPerson1.personId];
                                }
                                person.activeDetachment = {
                                  shift: shift.name,
                                  to: code,
                                  taskName: task.taskName
                                };
                                  results.push(person);
                              });
                          }
                      });
                  });
              });
          });

          return results;
      }
  }).filter('detachedOutsideZonePersonnel',

    function() {
        return function(persons){
            var results = [];

            angular.forEach(persons, function(value, key) {
                if (value.hasOwnProperty('state') && value.state === 'Detached') {
                  if (value.activeDetachment && value.activeDetachment.from && value.activeDetachment.to) {
                    if (value.activeDetachment.from.substr(0,2) !== value.activeDetachment.to.substr(0,2)) {
                        results[key] = value;
                    }
                  }
                }
            });

            var results = _.uniq(results);

            if (!results || results.length == 0)
                return results


            var sortedResults = sortPersonnelPanel(results, 'Location Seniority');
            return sortedResults;
        }
    }).filter('firstInitial', function () {
        return function (input) {
            if (!input) return '';
            return input.charAt(0) + '. ';
        }
    }).filter('orderByShiftId', function () {
        return function (items) {
            var filtered = [];
            angular.forEach(items, function(item) {
                filtered.push(item);
            });
            filtered.sort(function (a, b) {
                return (a.shiftId > b.shiftId ? 1 : -1);
            });
            return filtered;
        };
    }).filter('orderByCategoryId', function () {
        return function (items, categories) {
            var filtered = _.toArray(items).sort(function (a, b) {
                var subcategoryA = this.filter(function (val) {                    
                    return val && val.id === a.categoryId;
                })[0].sequence;
                var subcategoryB = this.filter(function (val) {
                    return val && val.id === b.categoryId;
                })[0].sequence;
                return subcategoryA > subcategoryB ? 1 : -1;
            }.bind(categories));
            return filtered;
        };
    }).filter('orderBySubcategorySequence', function () {
        return function (items, subcategories) {
            var filtered = _.toArray(items).sort(function (a, b) {
                var subcategoryA = this.filter(function (val) {                    
                    return val && val.id === a.subcategoryId;
                })[0].sequence;
                var subcategoryB = this.filter(function (val) {
                    return val && val.id === b.subcategoryId;
                })[0].sequence;
                return subcategoryA > subcategoryB ? 1 : -1;
            }.bind(subcategories));
            return filtered;
        };
    }).filter('orderBySection',

    function() {
        return function(items, reverse) {

            var filtered = [];

            angular.forEach(items, function(item) {
                filtered.push(item);
            });

            filtered.sort(function (a, b) {

                return (a.sectionName > b.sectionName ? 1 : -1);
            });

            if(reverse) filtered.reverse();


            return filtered;
        };
    }).filter('orderByTaskId',

    function() {
        return function(items, reverse) {

            var filtered = [];

            angular.forEach(items, function(item) {
                filtered.push(item);
            });

            filtered.sort(function (a, b) {
                return (a.sequence > b.sequence ? 1 : -1);
            });

            if(reverse) filtered.reverse();


            return filtered;
        };
    }).filter('orderByLocation', function (BoardValueService) {
        return function (items, locations) {

            var filtered = _.toArray(items);

            filtered.forEach(function(location) {
              if(location.locationCode === BoardValueService.refData.boardLocation) {
                location.sortSequence = -1;
              } else {
                location.sortSequence = BoardValueService.displaylocationRefsData[location.locationCode].sortSequence;
              }

            });

            filtered = filtered.sort(function (a, b) {
                return a.sortSequence > b.sortSequence ? 1 : -1;
            });

          return filtered;
        };
    }).filter('getCompletedTasks', function () {
      return function (tasks) {
        tasks = tasks.filter(function (val) {
          return val.completed;
        });
        return tasks.length ? tasks : null;
      }
    }).filter('extendDisplayPerson', function($filter) {
      return function(person) {
        var tasksAssignedTo = $filter('getAssignedTasks')(person.id);
        person.assignedToCompletedTask = tasksAssignedTo && $filter('getCompletedTasks')(tasksAssignedTo);
        person.detachedSup = $filter('isDetachableDisplaySup')(person);
        person.detachedSW = $filter('isDetachableDisplaySW')(person);
        person.personPayrollLocations = $filter('getDisplayPayrollLocations')(person);
        person.detachedOutSidePayroll = $filter('isDisplayDetachableOutsidePayroll')(person);
        person.attachedToPayrollLocation = $filter('isDisplayAttachedToPayRollLocation')(person);
        return person;
      }
    }).filter('isDetachableDisplaySW', function(groups) { 
        return function(person) {
          if (person && person.civilServiceTitle === groups.personnel.sanitationWorkers &&
            (!person.assignedToCompletedTask && person.homeLocation !== person.currentLocation)) {
            return true;
          } else if (person.activeDetachment && person.assignedToCompletedTask && (person.homeLocation === person.activeDetachment.to)) {
            return true;
          }
          else if (person.activeDetachment && (person.homeLocation === person.currentLocation)) {
              return true;
          }
          return false;
        }
    }).filter('isDetachableDisplaySup', function($filter, states, groups, boardtypes) { 
     return function(person) {
        var payrollLocations = $filter('getDisplayPayrollLocations')(person);
          if (person && person.civilServiceTitle === groups.personnel.supervisors && person.homeLocation !== person.currentLocation 
              && payrollLocations.personLocationData.home.boardType.code !== boardtypes.boro) {
            return true;
          }else if ( person && person.civilServiceTitle === groups.personnel.supervisors && payrollLocations.personLocationData.home.boardType.code === boardtypes.boro ){
            if (payrollLocations.home !== payrollLocations.currentAssignment){
              return true;
            }
          }
          
          return false;
      }
   }).filter('getAssignedTasks', function (BoardValueService) {
    return function (personId) {
      var foundTasks = [];
      _.forEach(BoardValueService.taskContainers, function (location) {
        _.forEach(location.locationShifts, function (locationShift) {
          _.forEach(locationShift.shiftCategories, function (shiftCategory) {
            _.forEach(shiftCategory.subcategoryTasks, function (subcategoryTask) {
              _.forEach(subcategoryTask.tasks, function (task) {
                for (var i = 1; i < 2; i ++) {
                  if (task['assignedPerson' + i].personId === personId) {
                    foundTasks.push(task['assignedPerson' + i]); // maybe just grab id
                  }
                }
              })
            });
          });
        });
      });
      return foundTasks;
    }
   }).filter('getDisplayPayrollLocations', function(states, groups, BoardValueService, boardtypes) {
       return function(person) {
         var personLocationData = {
           home: BoardValueService.displaylocationRefsData[person.homeLocation],
           current: BoardValueService.displaylocationRefsData[person.currentLocation]
         }
         var payrollLocations = {
           home: personLocationData.home.code,
           currentAssignment: personLocationData.current.code,
           personLocationData: personLocationData
         }
         
         if (personLocationData.home.boardType === null){
           personLocationData.home.boardType = {
             code: boardtypes.district
           }
         }
         
         if (personLocationData.current.boardType === null){
           personLocationData.current.boardType = {
             code: boardtypes.district
           }
         }
         
         if (personLocationData.home.boardType.code !== boardtypes.boro) { 
           //just to make boro board to open in local
           payrollLocations.home = personLocationData.home.borough ? personLocationData.home.borough.code : personLocationData.home.code;
         }
         
         if (personLocationData.current.boardType.code !== boardtypes.boro) { 
           //just to make boro board to open in local
           payrollLocations.currentAssignment = personLocationData.current.borough ? personLocationData.current.borough.code : personLocationData.current.code;
         }      
         return payrollLocations;
       }
  }).filter('isDisplayDetachableOutsidePayroll', function($filter,states, groups) {
        return function(person) {
          var payrollLocations = $filter('getDisplayPayrollLocations')(person);
          if (person && (person.civilServiceTitle === groups.personnel.superintendents || person.civilServiceTitle === groups.personnel.civilians ) 
              && payrollLocations.home !== payrollLocations.currentAssignment) {
            return true;
          }
          return false;
        }
  }).filter('isDisplayAttachedToPayRollLocation',
        function($filter,states, groups, boardtypes) {
        return function(person) {
          var payrollLocations = $filter('getDisplayPayrollLocations')(person);
          var boardType = "";
          if (payrollLocations.personLocationData.home.boardType !== null){
            boardType = payrollLocations.personLocationData.home.boardType.code;
          }
          if (person && person.civilServiceTitle === groups.personnel.sanitationWorkers && (boardType === boardtypes.boro || boardType === boardtypes.broomDepot || boardType === boardtypes.lotCleaning || boardType === boardtypes.splinter) 
              && payrollLocations.personLocationData.home.code === payrollLocations.personLocationData.current.code) {
            return true;
          }
          return false;
        }
  }).filter('orderBySequence',
  
    function() {
        return function(items) {
  
            var filtered = [];
  
            angular.forEach(items, function(item) {
                filtered.push(item);
            });
  
            filtered.sort(function (a, b) {
                return (a.partialTaskSequence > b.partialTaskSequence ? 1 : -1);
            });
  
  
            return filtered;
        };
  }).filter('unavailableCodeFilter',
  function(states, $filter) {
    return function(persons, code,boardLocation) {
      var resultsChart = [];
      var isCodeChart = false;
      var isCodeSick = false;
      var isMultiple = false;
      var hasLodi = false;
      var hasChart = false;
      var codestring = '';
      var pushperson = false;
      var pushcode = '';
      var unavailableCodes = ['CHART','XWP','XWOP', 'SICK', 'VACATION', 'LODI', 'APP', 'AWOL', 'DIF', 'SUSPENDED', 'TERMINAL LEAVE', 'JURY DUTY', 'Mil Dty w/Pay', 'Mil Dty w/o Pay', 'HONOR GUARD', 'FMLA', 'MATERNITY LEAVE', 'Civil Service Exam'];
      var results = {results: [], total: 0};

      angular.forEach(persons, function (value, key) {
        isCodeChart = false;
        isCodeSick = false;
        isMultiple = false;
        hasLodi = false;
        hasChart = false;
        codestring = '';

        if(value.currentLocation === boardLocation && value.state !== 'Hidden'){
          if (value.activeUnavailabilityReasons.length === 1 ) {
            if (value.activeUnavailabilityReasons[0].hasOwnProperty('code')) {
              if (value.activeUnavailabilityReasons[0].code === code) {
                resultsChart.push(value);
                }
              }
            } else if (value.activeUnavailabilityReasons.length > 1) {
              pushperson = false;
              pushcode = '';
              
              angular.forEach(value.activeUnavailabilityReasons, function (value2, key) {
                _.each(unavailableCodes, function(ucode) {
                  if(ucode === value2.code ) {
                    codestring = codestring + ucode;
                    }
                  });
                
                if (value2.hasOwnProperty('code')) {
                  if (value2.code === code) {
                    pushperson = true;
                    }
                  }
                });
              
              if((pushperson === true && codestring === code) || (code === 'MULTIPLE' && (codestring !== 'SICKCHART' && codestring !== 'CHARTSICK') && (codestring !== 'VACATIONCHART' && codestring !== 'CHARTVACATION')) || (code === 'SICK' && (codestring === 'SICKCHART' || codestring === 'CHARTSICK')) || (code === 'VACATION/CHART' && (codestring === 'VACATIONCHART' || codestring === 'CHARTVACATION'))) {
                resultsChart.push(value);
                }
              }
          }
      });

      results.results = resultsChart;
      results.total = resultsChart.length;
      return results;
    }
  }).filter('getFormattedName',
  function() {
    return function(person) {
      var lastName = person.lastName,
        firstName = person.firstName ? person.firstName.substring(0, 1).toUpperCase() : '';

      var name = firstName ? firstName + '. ' + lastName : lastName;
      return name;
    }
  }).filter('getFormattedNameMI',
  function() {
    return function(person) {
      var firstName = person.firstName,
        lastName = person.lastName;

      var name = firstName ? firstName + ' ' + lastName : lastName;
      return name;
    }
  });




