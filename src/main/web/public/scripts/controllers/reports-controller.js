'use strict';

angular.module('OpsBoard')
  .controller('ReportsCtrl', function ($scope, $filter, $modal, $log, $controller, $rootScope, OpsBoardRepository, InfoService, UUIDGenerator, ClientSideError,  UtilityService) {


      $scope.addVolunteerCounts = function () {
          $scope.opData = {
              titleAction: 'Add Volunteer Counts',
              cancelButtonText: 'Cancel',
              submitButtonText: 'Confirm',
              clientErrors: [],
              errors: [],
              progress: 100
          };
          

          
          
          var volunteerCounts = OpsBoardRepository.getvolunteerCounts();
          var persons= OpsBoardRepository.getPersonnel();
          if(volunteerCounts){
        	  $scope.opData.chartVolunteers = volunteerCounts.chartVolunteers;
        	  $scope.opData.mandatoryChart = volunteerCounts.mandatoryChart;
        	  $scope.opData.vacationVolunteers = volunteerCounts.vacationVolunteers;
          }  else{
        	  $scope.opData.chartVolunteers = 0;
        	  $scope.opData.mandatoryChart = 0;
        	  $scope.opData.vacationVolunteers = 0;
          }

      
         var numberVacationPersonnel = function (persons) {
                 var resultsVacation = [];
                 angular.forEach(persons, function (person) {
                     if (person.hasOwnProperty('state') && person.state === "Unavailable" && person.activeUnavailabilityReasons.length === 1) {
                         if (person.activeUnavailabilityReasons[0].hasOwnProperty('code')) {
                             if (person.activeUnavailabilityReasons[0].code === "VACATION") {
                                 resultsVacation.push(person);
                             }
                         }
                     }
                 });

                 return resultsVacation.length;
            
         };
       
        
          $scope.passPreValidation = function () {
              return true;
          };


          
          
          $scope.operation = function(success, error) {
              $scope.submitted = true;
              $scope.opData.clientErrors = [];

             if (!isZeroOrPositiveInteger($scope.opData.chartVolunteers)) {
            	  $scope.opData.clientErrors.push({
                      type : 'danger',
                      message : 'Chart Volunteers should be either 0 or whole positive number.'
                  });
              }

              
              if (!isZeroOrPositiveInteger($scope.opData.mandatoryChart)) {
            	  $scope.opData.clientErrors.push({
                      type : 'danger',
                      message : 'Mandatory Chart should be either 0 or whole positive number.'
                  });
              }
              
              
              var numberOfPersonnelOnVacation = numberVacationPersonnel(OpsBoardRepository.getPersonnel());
              if (!isZeroOrPositiveInteger($scope.opData.vacationVolunteers)){
            	  $scope.opData.clientErrors.push({
                      type : 'danger',
                      message : 'Vacation Volunteers should be either 0 or whole positive number.'
              })}
              else if ($scope.opData.vacationVolunteers > numberOfPersonnelOnVacation){
            	  $scope.opData.clientErrors.push({
            		  type : 'danger',
                      message : 'Vacation Volunteers can not be greater than the total number of personnel who are scheduled to be on vacation. (' + numberOfPersonnelOnVacation + ')' 
            	  })
            	  
              }
              
              
              
              if ($scope.opData.clientErrors.length > 0) {
                  return error(new ClientSideError('ValidationError'));
              }
              
              
              OpsBoardRepository.saveVolunteerCounts(1, $scope.opData.chartVolunteers, $scope.opData.mandatoryChart,  $scope.opData.vacationVolunteers,  success, error);
              
              
              
          };
          
          
  
          
          
          
          function isZeroOrPositiveInteger(val){
        	  var bisPositiveInteger = true;
        	  /*if (val === "" || isNaN(val) || val < 0 || val.indexOf(".")!==-1  ){
        		  bisPositiveInteger = false;
        	  }
        	  return bisPositiveInteger;
        	  */
        	  //return /^\(0|[1-9])$/.test(val);
        	  
        	  //var index = val.indexOf(".")
        	
        	  
        	  //var b = val.indexOf(".")!==-1;
        	  
        	  
        	  var strVal = val.toString();
        	  var b = strVal.indexOf(".")!==-1;
        	  if(val === 0){
        		  bisPositiveInteger = true;
        	  }
        	  
        	  else if (isNaN(val) || val < 0 || strVal.indexOf(".")!==-1){
        		  bisPositiveInteger = false;
        	  }
        	
        	  
        	  //return val == "0" || /^([1-9][1-9])$/.test(val);
        	  
        	  return bisPositiveInteger;
          }

          var locals = {
              $scope: $scope,
              $filter: $filter,
              $modal: $modal,
              $log: $log,
              OpsBoardRepository: OpsBoardRepository,
              ClientSideError: ClientSideError
          }



          var modalInstance = $modal.open({
              templateUrl: appPathStart + '/views/modals/modal-add-volunteer-counts',
              controller: 'ModalCtrl',
              backdrop: 'static',
              resolve: {
                  data: function () {
                      return [$scope.opData]
                  }
                 
              },
              scope: $scope

          });
         

      }
  });