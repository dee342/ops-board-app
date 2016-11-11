'use strict';

angular.module('OpsBoard').controller(
    'PersonnelCtrl',
    function ($scope, $rootScope, OpsBoardRepository, states, groups, titleHierarchy, titles, $timeout, OpsBoardInterval, PersonnelHelperService, $filter, BoardValueService, BoardHelperService) {

    	  $scope.states = states;
        $scope.persons = OpsBoardRepository.getPersonnel();
        $scope.isBoro = OpsBoardRepository.isBoroBoard();

        $scope.boardQuota = OpsBoardRepository.getBoardQuota();
        $scope.personnelSummary = {
                active: false,
                isBoro : $scope.isBoro
        };

        $scope.setAvailableNextDay = function(person){
        	OpsBoardRepository.setPersonNextDayAvailable(person.id);
        };
        
        $scope.removeAvailableNextDay = function(person){
        	OpsBoardRepository.removePersonNextDayAvailable(person.id);
        };

      var formattedNames = {};

        angular.forEach($scope.persons, function(value, key) {
          value = $filter('extendPerson')(value);

          if(!formattedNames[value.formattedName]) {
            formattedNames[value.formattedName] = [];
          }
          formattedNames[value.formattedName].push(value.id);
        });

        angular.forEach(formattedNames, function(name, key) {
          if(name.length > 1) {
            angular.forEach(name, function(id, key) {
              $scope.persons[id].formattedName = $filter('getFormattedNameMI')($scope.persons[id]);
            });
          }
        });

        var _togglePersonnelSummary = function() {
          $scope.personnelSummary.active = !$scope.personnelSummary.active;
          BoardValueService.PersonnelSummary = $scope.personnelSummary.active;
        };

        $scope.togglePersonnelSummary = function(){
          _togglePersonnelSummary();
        };

        $rootScope.$on('SHOW-PERSONNEL-SUMMARY', function(event, data) {
          _togglePersonnelSummary();
        });

      $scope.showPersonDetails = function(event, personId) {
          if (!personId) {
            return;
          }

          if (event.target.className.indexOf("menu-item") > -1) {
            return;
          }
          $scope.$emit('SHOW-PERSON-DETAILS',{personId: personId});
        };

        /* State counts */
        $scope.numAvailable = 0;
        $scope.numAssigned = 0;
        $scope.numDetached = 0;
        $scope.numMDA = 0;
        $scope.numBoroAssignmentOrder = 0;
        $scope.numUnavailable = 0;

        /* "Available" Group counts */

        $scope.commissioners = {
            open: true,
            show: false,
            total: 0
        };


        $scope.chiefs = {
            open: true,
            show: false,
            total: 0
        };

        $scope.civilians = {
            open: true,
            show: false,
            total: 0
        };

        $scope.sanitationWorkers = {
            open: true,
            show: false,
            total: 0
        };

        $scope.superintendents = {
            open: true,
            show: false,
            total: 0
        };

        $scope.supervisors = {
            open: true,
            show: false,
            total: 0
        };

        $scope.available = {
            open: true,
            show: true,
            total: 0
        };

        $scope.detached = {
            open: true,
            show: true,
            total: 0
        };

        $scope.mda = {
            open: true,
            show: true,
            total: 0
        };

        $scope.unavailable = {
            open: true,
            show: true,
            total: 0
        };
        $scope.assigned = {
            open: true,
            show: true,
            total: 0
        };
        $scope.boro = {
            open: true,
            show: true,
            total: 0
        };



        /*-------------Added by Max-------------*/


        $scope.charts = {
            open: true,
            show: false,
            total: 0
        };

        $scope.vacationCharts = {
            open: true,
            show: false,
            total: 0
        };


        $scope.xwp = {
            open: true,
            show: false,
            total: 0
        };

        $scope.xwop = {
            open: true,
            show: false,
            total: 0
        };


        $scope.sickCharts = {
            open: true,
            show: false,
            total: 0
        };


        $scope.vacation = {
            open: true,
            show: false,
            total: 0
        };


        $scope.lodi = {
            open: true,
            show: false,
            total: 0
        };


        $scope.app = {
            open: true,
            show: false,
            total: 0
        };

        $scope.awol = {
            open: true,
            show: false,
            total: 0
        };

        $scope.dif = {
            open: true,
            show: false,
            total: 0
        };

        $scope.suspended = {
            open: true,
            show: false,
            total: 0
        };

        $scope.terminalLeave = {
            open: true,
            show: false,
            total: 0
        };


        $scope.juryDuty = {
            open: true,
            show: false,
            total: 0
        };


        $scope.milDutyWithPay = {
            open: true,
            show: false,
            total: 0
        };


        $scope.milDutyWithoutPay = {
            open: true,
            show: false,
            total: 0
        };


        $scope.milDutyWithoutPay = {
            open: true,
            show: false,
            total: 0
        };

        $scope.honorGuard = {
            open: true,
            show: false,
            total: 0
        };

        $scope.fmla = {
            open: true,
            show: false,
            total: 0
        };


        $scope.maternityLeave = {
            open: true,
            show: false,
            total: 0
        };

        $scope.civilServiceExam = {
          open: true,
          show: false,
          total: 0
        };

        $scope.multiple = {
            open: true,
            show: false,
            total: 0
        };


        //------Detached Objects--------------

        $scope.chiefsDetached = {
            open: true,
            show: false,
            total: 0
        };



        $scope.superintendentsDetached = {
            open: true,
            show: false,
            total: 0
        };


        $scope.supervisorsDetached = {
            open: true,
            show: false,
            total: 0
        };



        $scope.sanitationDetached = {
            open: true,
            show: false,
            total: 0
        };




        $scope.civiliansDetached = {
            open: true,
            show: false,
            total: 0
        };


        /*------------------End Added By Max-----------------*/

        /* State filters */
        // Separated out from "scoped" method of same name so it could be used with "Available" group filters



        $scope.boroAssignmentOrderFilter = function (persons, filterType) {
            var results = [];

            angular.forEach(persons, function (value, key) {
                if (value.hasOwnProperty('state') && value.state === 'boroAssignmentOrder') {
                    results[key] = value;
                }
            });

            var results = _.uniq(results);

            $scope.numBoroAssignmentOrder = results.length;
            if (!results || results.length == 0) {
              return results
            }

            var sortedResults = $filter('sortPersonnelPanel')(results, filterType);

            return sortedResults;
        };

        
        var cachedLocations = $scope.tasks.locations,
            cachedLocationsKeys = Object.keys(cachedLocations),
            buildAssigned = {};

        // group filter for all shifts
        var assignedGroupFilter = function (persons, group) {

          var results = $filter('filterPersonnelByGroup')(persons, group);
          var len = Object.keys(results).length;
          if (len > 1) {
            results = $filter('sortPersonnelPanel')(_.toArray(results), $scope.filterType);
          }

          return _.toArray(results);
        };

        // all unavailable group filters dump to scope
        var unavailableGroupFilter = function (mdaFiltered, actualMdaFiltered) {

          var unavailableFiltered = $filter('unavailablePersonnelFilter')($scope.persons, $scope.filterType);

          $scope.personnelpane.numAvailable = Object.keys($filter('availPersonnelFilter')($scope.persons)).length;
          mdaFiltered.results = $filter('sortCivilServiceTitle')(mdaFiltered.results);
          $scope.personnelpane.mdaSorted = mdaFiltered.results;
          $scope.personnelpane.mdaTotal = mdaFiltered.total;
          $scope.numUnavailable = unavailableFiltered.total - actualMdaFiltered.total;

          var vacationsChartsFiltered = $filter('personnelTwoCodeFilter')($scope.persons, $scope.filterType, 'CHART', 'VACATION', mdaFiltered);
          vacationsChartsFiltered.results = $filter('sortPersonnelPanel')(vacationsChartsFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.vacationsChartsSorted = vacationsChartsFiltered.results;
          $scope.personnelpane.vacationsChartsShow = vacationsChartsFiltered.show;
          $scope.personnelpane.vacationsChartsTotal = vacationsChartsFiltered.total;

          var sickChartFiltered = $filter('sickChartFilter')($scope.persons, $scope.filterType, mdaFiltered, $scope.board.realDate);
          sickChartFiltered.results = $filter('sortCivilServiceTitle')(sickChartFiltered.results);
          $scope.personnelpane.sickChartSorted = sickChartFiltered.results;
          $scope.personnelpane.sickChartShow = sickChartFiltered.show;
          $scope.personnelpane.sickChartTotal = sickChartFiltered.total;
          
          // unavailable filters get mda filter results removed, so we partially apply it to the code filter

          var codeFilterPartial = $filter('personnelCodeFilter').bind(null, $scope.persons, $scope.filterType, mdaFiltered, $scope.board.realDate);
          
          var chartsFiltered = codeFilterPartial('CHART');
          chartsFiltered.results = $filter('sortPersonnelPanel')(chartsFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.chartsSorted = chartsFiltered.results;
          $scope.personnelpane.chartsShow = chartsFiltered.show;
          $scope.personnelpane.chartsTotal = chartsFiltered.total;

          var xwpFiltered = codeFilterPartial('XWP');
          xwpFiltered.results = $filter('sortPersonnelPanel')(xwpFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.xwpSorted = xwpFiltered.results;
          $scope.personnelpane.xwpShow = xwpFiltered.show;
          $scope.personnelpane.xwpTotal = xwpFiltered.total;

          var xwopFiltered = codeFilterPartial('XWOP');
          xwopFiltered.results = $filter('sortPersonnelPanel')(xwopFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.xwopSorted = xwopFiltered.results;
          $scope.personnelpane.xwopShow = xwopFiltered.show;
          $scope.personnelpane.xwopTotal = xwopFiltered.total;

          var vacationFiltered = codeFilterPartial('VACATION');
          vacationFiltered.results = $filter('sortPersonnelPanel')(vacationFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.vacationSorted = vacationFiltered.results;
          $scope.personnelpane.vacationShow = vacationFiltered.show;
          $scope.personnelpane.vacationTotal = vacationFiltered.total;

          var lodiFiltered = codeFilterPartial('LODI');
          lodiFiltered.results = $filter('sortPersonnelPanel')(lodiFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.lodiSorted = lodiFiltered.results;
          $scope.personnelpane.lodiShow = lodiFiltered.show;
          $scope.personnelpane.lodiTotal = lodiFiltered.total;

          var appFiltered = codeFilterPartial('APP');
          appFiltered.results = $filter('sortPersonnelPanel')(appFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.appSorted = appFiltered.results;
          $scope.personnelpane.appShow = appFiltered.show;
          $scope.personnelpane.appTotal = appFiltered.total;

          var awolFiltered = codeFilterPartial('AWOL');
          awolFiltered.results = $filter('sortPersonnelPanel')(awolFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.awolSorted = awolFiltered.results;
          $scope.personnelpane.awolShow = awolFiltered.show;
          $scope.personnelpane.awolTotal = awolFiltered.total;

          var difFiltered = codeFilterPartial('DIF');
          difFiltered.results = $filter('sortPersonnelPanel')(difFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.difSorted = difFiltered.results;
          $scope.personnelpane.difShow = difFiltered.show;
          $scope.personnelpane.difTotal = difFiltered.total;

          var suspendedFiltered = codeFilterPartial('SUSPENDED');
          suspendedFiltered.results = $filter('sortPersonnelPanel')(suspendedFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.suspendedSorted = suspendedFiltered.results;
          $scope.personnelpane.suspendedShow = suspendedFiltered.show;
          $scope.personnelpane.suspendedTotal = suspendedFiltered.total;

          var terminalLeaveFiltered = codeFilterPartial('TERMINAL LEAVE');
          terminalLeaveFiltered.results = $filter('sortPersonnelPanel')(terminalLeaveFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.terminalLeaveSorted = terminalLeaveFiltered.results;
          $scope.personnelpane.terminalLeaveShow = terminalLeaveFiltered.show;
          $scope.personnelpane.terminalLeaveTotal = terminalLeaveFiltered.total;

          var juryDutyFiltered = codeFilterPartial('JURY DUTY');
          juryDutyFiltered.results = $filter('sortPersonnelPanel')(juryDutyFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.juryDutySorted = juryDutyFiltered.results;
          $scope.personnelpane.juryDutyShow = juryDutyFiltered.show;
          $scope.personnelpane.juryDutyTotal = juryDutyFiltered.total;

          var milDtyWithPayFiltered = codeFilterPartial('Mil Dty w/Pay');
          milDtyWithPayFiltered.results = $filter('sortPersonnelPanel')(milDtyWithPayFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.milDtyWithPaySorted = milDtyWithPayFiltered.results;
          $scope.personnelpane.milDtyWithPayShow = milDtyWithPayFiltered.show;
          $scope.personnelpane.milDtyWithPayTotal = milDtyWithPayFiltered.total;

          var milDutyWithoutPayFiltered = codeFilterPartial('Mil Dty w/o Pay');
          milDutyWithoutPayFiltered.results = $filter('sortPersonnelPanel')(milDutyWithoutPayFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.milDutyWithoutPaySorted = milDutyWithoutPayFiltered.results;
          $scope.personnelpane.milDutyWithoutPayShow = milDutyWithoutPayFiltered.show;
          $scope.personnelpane.milDutyWithoutPayTotal = milDutyWithoutPayFiltered.total;

          var honorGuardFiltered = codeFilterPartial('HONOR GUARD');
          honorGuardFiltered.results = $filter('sortPersonnelPanel')(honorGuardFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.honorGuardSorted = honorGuardFiltered.results;
          $scope.personnelpane.honorGuardShow = honorGuardFiltered.show;
          $scope.personnelpane.honorGuardTotal = honorGuardFiltered.total;

          var fmlaFiltered = codeFilterPartial('FMLA');
          fmlaFiltered.results = $filter('sortPersonnelPanel')(fmlaFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.fmlaSorted = fmlaFiltered.results;
          $scope.personnelpane.fmlaShow = fmlaFiltered.show;
          $scope.personnelpane.fmlaTotal = fmlaFiltered.total;

          var maternityLeaveFiltered = codeFilterPartial('MATERNITY LEAVE');
          maternityLeaveFiltered.results = $filter('sortPersonnelPanel')(maternityLeaveFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.maternityLeaveSorted = maternityLeaveFiltered.results;
          $scope.personnelpane.maternityLeaveShow = maternityLeaveFiltered.show;
          $scope.personnelpane.maternityLeaveTotal = maternityLeaveFiltered.total;

          var civilServiceExamFiltered = codeFilterPartial('Civil Service Exam');
          civilServiceExamFiltered.results = $filter('sortPersonnelPanel')(civilServiceExamFiltered.results, $scope.filterType, 'Unavailable');
          $scope.personnelpane.civilServiceExamSorted = civilServiceExamFiltered.results;
          $scope.personnelpane.civilServiceExamShow = civilServiceExamFiltered.show;
          $scope.personnelpane.civilServiceExamTotal = civilServiceExamFiltered.total;

        };

        var markNotDraggable = function (multipleFiltered) {
          for (var i = 0; i < multipleFiltered.length; i++) {
            angular.forEach(multipleFiltered[i].activeUnavailabilityReasons, function(value, key) {
              if (value.code === 'SICK' || value.code === 'LODI') {
                $scope.persons[multipleFiltered[i].id].notDraggable = true;
              }
            })
          }
        }

        var updatePersonnelPane = function() {

          var mdaFiltered = $filter('mdaPersonnelFilter')($scope.persons, $scope.filterType, $scope.board.realDate, true);
          var actualMdaFiltered = $filter('mdaPersonnelFilter')($scope.persons, $scope.filterType, $scope.board.realDate, false);
          
          var multipleFiltered = $filter('multipleFilter')($scope.persons, $scope.filterType, $scope.board.realDate);
          $scope.personnelpane.multipleSorted = multipleFiltered.results;
          $scope.personnelpane.multipleShow = multipleFiltered.show;
          $scope.personnelpane.multipleTotal = multipleFiltered.total;

          markNotDraggable(multipleFiltered.results);

          $scope.personnelpane.detachedPersonnelCount = $filter('detachedPersonnelFilter')($scope.persons, $scope.filterType).total;

          var chiefsDetachedFiltered = $filter('chiefsDetachedFilter')($scope.persons, $scope.filterType);

          $scope.personnelpane.chiefsDetachedSorted = chiefsDetachedFiltered.results;
          $scope.personnelpane.chiefsDetachedShow = chiefsDetachedFiltered.show;
          $scope.personnelpane.chiefsDetachedTotal = chiefsDetachedFiltered.total;

          var superintendentsDetachedFiltered = $filter('codeDetachedFilter')($scope.persons, $scope.filterType, 'GS I');
          $scope.personnelpane.superintendentsDetachedSorted = superintendentsDetachedFiltered.results;
          $scope.personnelpane.superintendentsDetachedShow = superintendentsDetachedFiltered.show;
          $scope.personnelpane.superintendentsDetachedTotal = superintendentsDetachedFiltered.total;

          var supervisorsDetachedFiltered = $filter('codeDetachedFilter')($scope.persons, $scope.filterType, 'SUP');
          $scope.personnelpane.supervisorsDetachedSorted = supervisorsDetachedFiltered.results;
          $scope.personnelpane.supervisorsDetachedShow = supervisorsDetachedFiltered.show;
          $scope.personnelpane.supervisorsDetachedTotal = supervisorsDetachedFiltered.total;

          var sanitationDetachedFiltered = $filter('codeDetachedFilter')($scope.persons, $scope.filterType, 'SW');
          $scope.personnelpane.sanitationDetachedSorted = sanitationDetachedFiltered.results;
          $scope.personnelpane.sanitationDetachedShow = sanitationDetachedFiltered.show;
          $scope.personnelpane.sanitationDetachedTotal = sanitationDetachedFiltered.total;

          var civiliansDetachedFiltered = $filter('codeDetachedFilter')($scope.persons, $scope.filterType, 'CIVILIAN');
          civiliansDetachedFiltered.results = $filter('sortPersonnelPanel')(civiliansDetachedFiltered.results, 'Last Name', 'Detached');
          $scope.personnelpane.civiliansDetachedSorted = civiliansDetachedFiltered.results;
          $scope.personnelpane.civiliansDetachedShow = civiliansDetachedFiltered.show;
          $scope.personnelpane.civiliansDetachedTotal = civiliansDetachedFiltered.total;

          var sanitationWorkersFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.sanitationWorkers);
          $scope.personnelpane.sanitationWorkersSorted = sanitationWorkersFiltered.results;
          $scope.personnelpane.sanitationWorkersShow = sanitationWorkersFiltered.show;
          $scope.personnelpane.sanitationWorkersTotal = sanitationWorkersFiltered.total;

          var superintendentsFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.superintendents);
          $scope.personnelpane.superintendentsSorted = superintendentsFiltered.results;
          $scope.personnelpane.superintendentsShow = superintendentsFiltered.show;
          $scope.personnelpane.superintendentsTotal = superintendentsFiltered.total;

          var supervisorsFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.supervisors);
          $scope.personnelpane.supervisorsSorted = supervisorsFiltered.results;
          $scope.personnelpane.supervisorsShow = supervisorsFiltered.show;
          $scope.personnelpane.supervisorsTotal = supervisorsFiltered.total;

          var commissionersFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.commissioners);
          $scope.personnelpane.commissionersSorted = commissionersFiltered.results;
          $scope.personnelpane.commissionersShow = commissionersFiltered.show;
          $scope.personnelpane.commissionersTotal = commissionersFiltered.total;

          var chiefsFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.chiefs);
          $scope.personnelpane.chiefsSorted = chiefsFiltered.results;
          $scope.personnelpane.chiefsShow = chiefsFiltered.show;
          $scope.personnelpane.chiefsTotal = chiefsFiltered.total;

          var civiliansFiltered = $filter('workersFilter')($scope.persons, $scope.filterType, groups.personnel.civilians);
          civiliansFiltered.results = $filter('sortPersonnelPanel')(civiliansFiltered.results, 'Last Name', 'Available');
          $scope.personnelpane.civiliansSorted = civiliansFiltered.results;
          $scope.personnelpane.civiliansShow = civiliansFiltered.show;
          $scope.personnelpane.civiliansTotal = civiliansFiltered.total;

          $scope.personnelCountTypes = PersonnelHelperService.personnelCountTypes($scope.persons, $scope.boardQuota, $scope.numAssigned, $scope.personnelpane, $scope.isBoro, groups );

          // unavailable group
          unavailableGroupFilter(mdaFiltered, actualMdaFiltered);

          // assigned group
          buildAssigned = PersonnelHelperService.buildAssigned(cachedLocations, cachedLocationsKeys);
          
          var dayBeforeShifts = {};
        
          for(var personId in $scope.persons){
        	  if($scope.persons.hasOwnProperty(personId) && $scope.persons[personId].state !== states.personnel.hidden){
        		  var person = $scope.persons[personId];        		  
        		  if(person.dayBeforeShifts && person.dayBeforeShifts.length && !person.availableNextDay){
        			  person.dayBeforeShifts.forEach(function(s, i){	 
        				  
            			  if(!dayBeforeShifts[s.id]){
            				  dayBeforeShifts[s.id] = {
            						  name: s.name,
            						  id: s.id
            				  }            				              				  
            			  }
            			  
            			  if(!dayBeforeShifts[s.id]._personnel || !dayBeforeShifts[s.id]._personnel.length)
            				  dayBeforeShifts[s.id]._personnel = [];
            			  
            			  var found = _.findWhere(dayBeforeShifts[s.id]._personnel, {id: person.id});
            			  if(!found)
            			  {
            				  dayBeforeShifts[s.id]._personnel.push(person);             				  
            			  }
            			  
        			  });
        		  }
        		  
        	  }
          }
          
          var numDayBeforeAssigned = 0;
          for(var shiftId in dayBeforeShifts){
        	  if(dayBeforeShifts.hasOwnProperty(shiftId)){
        		  numDayBeforeAssigned+=dayBeforeShifts[shiftId]._personnel.length;
        	  }
          }
                    
          $scope.assigned.shifts = buildAssigned.shifts;
          
          $scope.assigned.dayBeforeShifts = dayBeforeShifts;
          
          $scope.numAssigned = buildAssigned.numAssigned + buildAssigned.nextDayNumAssigned + numDayBeforeAssigned;          
          
          $scope.assigned.dayBeforeGroups = (function () {
              var base = {},
                personnelKeys = Object.keys(groups.personnel),
                shiftKeys = Object.keys( $scope.assigned.dayBeforeShifts);

              // populate personnel titles
              for (var i = 0, len = personnelKeys.length; i < len; i ++) {
                base[personnelKeys[i]] = {};
                // nested-object shifts using shift id as key
                for (var j = 0, persons; j < shiftKeys.length; j ++) {
                  // persons assigned to shift only
                  if( $scope.assigned.dayBeforeShifts[shiftKeys[j]] &&  $scope.assigned.dayBeforeShifts[shiftKeys[j]]._personnel)
                	  persons =  $scope.assigned.dayBeforeShifts[shiftKeys[j]]._personnel;
                  else
                	  persons = [];
                  
                  base[personnelKeys[i]][shiftKeys[j]] = {
                    sorted: assignedGroupFilter(persons, groups.personnel[personnelKeys[i]])
                                 
                  };
                }
              }
              return base;
            }());
         
          $scope.assigned.groups = (function () {
            var base = {},
              personnelKeys = Object.keys(groups.personnel),
              shiftKeys = Object.keys(cachedShiftData);

            // populate personnel titles
            for (var i = 0, len = personnelKeys.length; i < len; i ++) {
              base[personnelKeys[i]] = {};
              // nested-object shifts using shift id as key
              for (var j = 0, persons, nextDayPersons; j < shiftKeys.length; j ++) {
                // persons assigned to shift only
                persons = cachedShiftData[shiftKeys[j]]._personnel;
                nextDayPersons = cachedShiftData[shiftKeys[j]]._nextDayPersonnel;
                base[personnelKeys[i]][shiftKeys[j]] = {
                  sorted: assignedGroupFilter(persons, groups.personnel[personnelKeys[i]]),
                  nextDay: {
                	  sorted: assignedGroupFilter(nextDayPersons, groups.personnel[personnelKeys[i]])
                  }                
                };
              }
            }
            return base;
          }());

        };

        // remove after 7/2015
        window._test = function () { debugger };

        $scope.titleMap = function (title) {
            var results = OpsBoardRepository.getMappedTitle(title);
            return results;
        };

        if ($scope.isBoro)
            $scope.sortFilters = ['Seniority', 'Reverse Seniority', 'Location Seniority', 'Location Reverse Seniority'];
        else
            $scope.sortFilters = ['Seniority', 'Reverse Seniority', 'Location Seniority', 'Last Name'];

        $scope.filterType = 'Location Seniority';
        $scope.filterTypeClass = 'fa-sort-numeric-desc';
        $scope.changeFilterType = function (choice) {
            $scope.filterType = choice;
            updatePersonnelPane('sort');
        };

        /*
         * Sub panel control
         *
         * Current logic is to open all top-level categories on expand,
         * close all on collapse, or, if no categories, to open/close the body.
         *
         * TODO: This is somewhat fragile - adding/removing top level categories will require modifying these functions
         *
         * */

         $scope.toggleBoroSubpanel = function () {
            $scope.boro.open = !$scope.boro.open;
            // TODO: ?
        };

        $scope.toggleAvailableSubpanel = function () {
          $scope.available.open = !$scope.available.open;

          $scope.chiefs.open = $scope.available.open;
          $scope.superintendents.open = $scope.available.open;
          $scope.supervisors.open = $scope.available.open;
          $scope.sanitationWorkers.open = $scope.available.open;
          $scope.civilians.open = $scope.available.open;
        };

        $scope.toggleDetachedSubpanel = function () {
          $scope.detached.open = !$scope.detached.open;
          $scope.chiefsDetached.open = $scope.detached.open;
          $scope.superintendentsDetached.open = $scope.detached.open;
          $scope.supervisorsDetached.open = $scope.detached.open;
          $scope.sanitationDetached.open = $scope.detached.open;
          $scope.civiliansDetached.open = $scope.detached.open;
        };

        $scope.toggleMDASubpanel = function () {
          $scope.mda.open = !$scope.mda.open;
        };

        $scope.toggleUnavailableSubpanel = function () {
          $scope.unavailable.open = !$scope.unavailable.open;
          $scope.charts.open = $scope.unavailable.open;
          $scope.vacationCharts.open = $scope.unavailable.open;
          $scope.xwp.open = $scope.unavailable.open;
          $scope.xwop.open = $scope.unavailable.open;
          $scope.sickCharts.open = $scope.unavailable.open;
          $scope.vacation.open = $scope.unavailable.open;
          $scope.lodi.open = $scope.unavailable.open;
          $scope.app.open = $scope.unavailable.open;
          $scope.awol.open = $scope.unavailable.open;
          $scope.dif.open = $scope.unavailable.open;
          $scope.suspended.open = $scope.unavailable.open;
          $scope.terminalLeave.open = $scope.unavailable.open;
          $scope.juryDuty.open = $scope.unavailable.open;
          $scope.milDutyWithPay.open = $scope.unavailable.open;
          $scope.milDutyWithoutPay.open = $scope.unavailable.open;
          $scope.honorGuard.open = $scope.unavailable.open;
          $scope.fmla.open = $scope.unavailable.open;
          $scope.maternityLeave.open = $scope.unavailable.open;
          $scope.multiple.open = $scope.unavailable.open;
        };

        $scope.toggleAssignedSubpanel = function () {
          $scope.assigned.open = !$scope.assigned.open;
        };

        // control of assigned panel toggling

        var cachedShiftData = $scope.shiftData.toJSON();
        $scope.assigned.shiftToggleData = {};

        angular.forEach(cachedShiftData, function (val) {
          $scope.assigned.shiftToggleData[val.id] = { open: true };
        });

        // toggle entire panel by setting all shift scopes open
        $scope.assigned.toggleAssignedSubpanel = function () {
          $scope.assigned.open = !$scope.assigned.open;
          angular.forEach(cachedShiftData, function (val) {
              $scope.assigned.shiftToggleData[val.id].open = $scope.assigned.open
          });
        };

        // toggle individual shift scope
        $scope.assigned.toggleAssignedShift = function (id) {
          var wasOpen = $scope.assigned.shiftToggleData[id].open;
          $timeout(function() {
              $scope.$apply(function () {
                  $scope.assigned.shiftToggleData[id].open = !wasOpen;
              });
          }, 0, false);
        };

        $scope.assigned.groups = {};
        $scope.assigned.dayBeforeGroups = {};

        updatePersonnelPane();

        // main method for filtering assignments to a shift
        $scope.$on('UPDATE-PERSONNEL-PANE', updatePersonnelPane);
        $scope.$on('ASSIGNED-PERSON', updatePersonnelPane);
        $scope.$on('UNASSIGNED-PERSON', updatePersonnelPane);
        $scope.$on('DETACH-PERSON', updatePersonnelPane);

    });