'use strict';

angular.module('OpsBoard').controller(
  'PersonDetailsCtrl',
  function ($scope, $filter, groups, $resource, OpsBoardRepository, states, BoardDataService, BoardHelperService) {

	$scope.states = states;
    $scope.dateFormat = 'MM/dd/yyyy';
    $scope.person.showingActiveUnavailableRecords = false;
    $scope.person.showingActiveSpecialPositions = false;
    $scope.size = 5;

    $scope.grounded = {
      show: true
    };

    $scope.employeeInfo = {
      open: false,
      show: false
    };

    $scope.contactInfo = {
      open: false,
      show: false
    };

    $scope.unavailableHistory = {
      open: true,
      show: true
    };
    
    $scope.mdaHistory = {
       open: true,
       show: true
    };
    
    $scope.specialPositionHistory = {
       open: true,
       show: true
    };    

    $scope.detachmentHistory = {
      open: true,
      show: true
    };

    $scope.close = function() {
        $scope.closePersonDetailsPane();
    };
    
    $scope.pageChanged = function() {
      console.log($scope.unavailabilityPaginationModel);
    };

     $scope.setAvailableNextDay = function(person){
     	OpsBoardRepository.setPersonNextDayAvailable(person.id);
     };
     
     $scope.removeAvailableNextDay = function(person){
     	OpsBoardRepository.removePersonNextDayAvailable(person.id);
     };
 
      
     $scope.hasActiveRecords = function(collection){
       if(collection)
       for (var i = 0; i < collection.length; i++) {
          if(collection[i].dispStatus == 'Active')
            return true;
        }
       return false;
     };
     
     $scope.hasNonActiveRecords = function(collection){
       if(collection)
       for (var i = 0; i < collection.length; i++) {
          if(collection[i].dispStatus != 'Active')
            return true;
        }
       return false;
     };
      
      $scope.showAllSpecialPositions = function(specialPositionsPaginationModel){
        $scope.person.showingActiveSpecialPositions = false;
        $scope.person.specialPositionCount = $scope.person.formattedSpecialPositions.length;
        $scope.person.specialPositionsPaginationModel = $scope.person.formattedSpecialPositions.slice(0, 5);
        $scope.person.specialPositionsCurrentPage = 1;
      };
      
      $scope.showActiveSpecialPositions = function(specialPositionsPaginationModel, specialPositionsCurrentPage){
        $scope.person.showingActiveSpecialPositions = true;
        var activeSpecialPositions = [];
        for (var i = 0; i < $scope.person.formattedSpecialPositions.length; i++) {
          if($scope.person.formattedSpecialPositions[i].dispStatus == 'Active')
            activeSpecialPositions.push($scope.person.formattedSpecialPositions[i]);
        }
        $scope.person.formattedActiveSpecialPositions = activeSpecialPositions;
        $scope.person.specialPositionsPaginationModel = $scope.person.formattedActiveSpecialPositions.slice(0, 5);
        $scope.person.specialPositionCount = $scope.person.formattedActiveSpecialPositions.length;
      $scope.person.specialPositionsCurrentPage = 1;
       };
    
    //controll for pagination for detachment history and MDA
    
    
    $scope.personDetachmentChanged = function(person) {
      var pathStart = OpsBoardRepository.getPathStart(),
        resource = $resource(pathStart + '/view/personneldetachments/' + person.id + "?upToDate=" + new Date(BoardDataService.getBoardData().endDate), {
          page: person.personDetachCurrentPage - 1,
          size: $scope.size        
        }),
        response = resource.get();

      response.$promise.then(function(result) {
        person.detachmentHistory = result.items;
        person.personDetachPaginationModel = person.getFormattedDetachmentHistory(0, $scope.size);
        person.detachmentCount = result.count;
        $filter('extendPerson')(person);
      }, function(error) {
        console.log(error);
      });

    };

    $scope.$on('DETACH-PERSON', function (event, person) {
      $scope.personDetachmentChanged(person);
    });

    $scope.unAvailPageChanged = function(person) {
      var pathStart = OpsBoardRepository.getPathStart(),
        resource = $resource(pathStart + '/view/unavailablereasons/' + person.id + "?upToDate=" + new Date(BoardDataService.getBoardData().endDate), {
          page: person.unavailabilityCurrentPage - 1,
          size: $scope.size,       
          sortDir: 'desc', sortColumn: 'start'
        }),
        response = resource.get();

      response.$promise.then(function (result) {
        //person.unavailabilityHistory = result.items;
        person.unavailabilityPaginationModel = person.getFormattedUnavailableReasons(result.items);
        person.unavailabilityReasonCount = result.count;
        $filter('extendPerson')(person);
      }, function (error) {
        console.log(error);
      });

    };

    $scope.$on('UNAVAILABILITY-PERSON', function (event, person) {
      $scope.unAvailPageChanged(person);
    });

    $scope.specialPositionsPageChanged = function (person) {
      var pathStart = OpsBoardRepository.getPathStart(),
      resource = $resource(pathStart + '/view/specialpostions/' + person.id + "?upToDate=" + new Date(BoardDataService.getBoardData().endDate), {
        page: person.specialPositionsCurrentPage - 1,
        size: $scope.size, sortDir: 'desc', sortColumn: 'startDate'
      }),
      response = resource.get();

      response.$promise.then(function (result) {
        person.specialPositionsHistory = result.items;
        person.specialPositionsPaginationModel = person.getFormattedSpecialPositions();
        person.specialPositionCount = result.count;
        $filter('extendPerson')(person);
      }, function (error) {
        console.log(error);
      });
      
    };

    $scope.$on('SPECIAL-POSITION-PERSON', function (event, person) {
      $scope.specialPositionsPageChanged(person);
    });
         
    $scope.personMDAPageChanged = function (person) {
      var pathStart = OpsBoardRepository.getPathStart(),
      resource = $resource(pathStart + '/view/mdastatus/' + person.id + "?upToDate=" + new Date(BoardDataService.getBoardData().endDate), {
        page: person.personMDACurrentPage - 1,
        size: $scope.size,
        sortDir: 'desc',
        sortColumn: 'startDate'
      }),
      response = resource.get();

      response.$promise.then(function (result) {
        person.mdaStatusHistory = result.items;
        person.personMDAPaginationModel = person.getFormattedMdaStatus();
        person.mdaStatusCount = result.count;
        $filter('extendPerson')(person);
      }, function (error) {
        console.log(error);
      });

    };

    $scope.$on('MDA-PERSON', function (event, person) {
      $scope.personMDAPageChanged(person);
    });

    $scope.groundedPageChanged = function (person) {
      var pathStart = OpsBoardRepository.getPathStart(),
      resource = $resource(pathStart + '/view/groundingstatus/' + person.id + "?upToDate=" + new Date(BoardDataService.getBoardData().endDate), {
        page: person.groundingHistoryCurrentPage - 1,
        size: $scope.size
      }),
      response = resource.get();

      response.$promise.then(function (result) {
        person.groundingHistory = result.items;
        person.groundingHistoryPaginationModel = person.getFormattedGroundedHistory();
        person.groundingStatusCount = result.count;
        $filter('extendPerson')(person);
      }, function (error) {
        console.log(error);
      });
    };

    $scope.$on('GROUND-PERSON', function (event, person) {
      $scope.groundedPageChanged(person);
    });
            
    $scope.actionTaken = true;
    
    
    $scope.isActive = function(startDate, endDate, removedFlag, person) {
      var now = moment();
      var start = moment(startDate);
      var end = moment(endDate);
      var formattedDateObj = person.getFormattedDateObj();
      if(!removedFlag){
      if (formattedDateObj.boardDateDt.valueOf() >= startDate) {
    	  if(endDate == null)
    		  return true;
        if (formattedDateObj.boardDateDt.valueOf() <= endDate) {
          return true
        }
      }
      }
      
      return false;
    }
    
    $scope.isComplete = function(endDate, person, removedFlag) {
      var now = moment();
      var end = moment(endDate);
      var formattedDateObj = person.getFormattedDateObj();
      if(endDate == null)
    	  return false;
      
      if (formattedDateObj.boardDateDt.valueOf() > end && !removedFlag) {
        return true
      }
      
      return false;
    }
    
    $scope.isFuture = function(startDate, person, removedFlag) {
      var now = moment();
      var start = moment(startDate);
      var formattedDateObj = person.getFormattedDateObj();
      if (formattedDateObj.boardDateDt.valueOf() < start && !removedFlag) {
        return true
      }
      
      return false;
    }
    
    $scope.isRemoved = function(removeFlag, person) {
    	if (removeFlag) {
            return true
          }    
          return false;
      }
      
        

  });