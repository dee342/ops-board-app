'use strict';

angular
  .module('OpsBoard')
  .controller(
    'DetachPerson',
    function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, groups, states, BoardDataService) {

      function updateDetachment(record, person) {

        var updateDetachmentScope = $scope.$new(false);
        updateDetachmentScope.showContent = true;
        updateDetachmentScope.minDate =  getMinDate(record);
        updateDetachmentScope.maxDate = record.end || null;
        updateDetachmentScope.opData = {
          titleAction: 'Edit Detachment',
          titleEntity: person.fullName,
          submitButtonText: 'Save',
          cancelButtonText: 'Cancel',
          progress: 100,
          errors: [],
          id: record.id,
          startDate: record.start,
          originalEndDate: record.end,
          endDate: moment(new Date($scope.board.displayDate)).format('MM/DD/YYYY dddd')
        };

        openUpdateDetachmentModal(updateDetachmentScope);

        updateDetachmentScope.passPreValidation = function () { // todo: share this with other controllers
          return BoardDataService.validateBoardDate($scope.board.displayDate, updateDetachmentScope.opData.errors, 'update person detachment');
        };

        updateDetachmentScope.operation = function (success, error) {

          var originalEndDate = moment(updateDetachmentScope.opData.originalEndDate);
          var startDate = moment(updateDetachmentScope.opData.startDate);

          updateDetachmentScope.opData.clientErrors = [];
          updateDetachmentScope.opData.serverErrors = [];

          // falsy date or date has not changed
          if (!updateDetachmentScope.opData.endDate && !originalEndDate.diff(updateDetachmentScope.opData.endDate)) {
            updateDetachmentScope.opData.clientErrors.push({
              message: 'Please select a new date.'
            });
          }

          if (updateDetachmentScope.opData.clientErrors.length) {
           return error(new ClientSideError('ValidationError'));
          }

          OpsBoardRepository.updatePersonDetachment(person, {
            detachmentId: updateDetachmentScope.opData.id,
            endDate: updateDetachmentScope.opData.endDate
          }, success, error);

        };

      }

      function openUpdateDetachmentModal(scope) {
        $modal.open({
          templateUrl : appPathStart + '/views/modals/modal-personnel-edit-detachment',
          controller: 'ModalCtrl',
          scope: scope,
          backdrop: 'static',
          resolve: {
            data: function () {
              return [];
            }
          }
        });
      }

      $scope.updateDetachment = updateDetachment;

      $scope.isPersonDetachable = function(person) {
        if (person && person.state === states.personnel.available) {
          return true;
        }
        return false;
      }           

      $scope.detachPerson = function(person) {
        var locations = BoardDataService.getRepairLocations();
        var detachLocations = $filter('detachLocationsFilter')(locations, $scope.board.location, null, 'personnel');
        var name = person.fullName;

        if(name.length>30)
          name=name.substring(0,30);

        var date = new Date();
        $scope.minDate = date.setDate(date.getDate() - 1);
        $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' ];
        $scope.format = $scope.formats[4];


        var defaultShift = '';
        if (person.civilServiceTitle === groups.personnel.sanitationWorkers) {
          defaultShift="0600-1400";
          Object.keys($scope.shiftData).forEach(function(key) {
            if ($scope.shiftData[key].id == 8) {
              defaultShift = $scope.shiftData[key].start + '-' + $scope.shiftData[key].end;
            }
          })
        }

        $scope.remarksMaxLength = 50;

        $scope.opData = {
          titleAction : 'Initiate Detach',
          titleEntity : name,
          cancelButtonText : 'Cancel',
          submitButtonText : 'Detach',
          clientErrors : [],
          serverErrors:[],
          shifts : $scope.shiftData,
          errors : [],
          name : name,
          locations : detachLocations,
          startDate : moment(new Date($scope.board.displayDate)).format('MM/DD/YYYY'),
          endDate : moment(new Date($scope.board.displayDate)).format('MM/DD/YYYY'),
          from : $scope.board.location,
          to : '',
          shift : defaultShift,
          remarks : '',
          progress : 100, // Set to 100 b/c unable to determine
          required : []
        }

        $scope.passPreValidation = function () {
          return BoardDataService.validateBoardDate($scope.board.displayDate, $scope.opData.errors, 'detach person');
        };

        $scope.isRequired = function(element) {
          $scope.submitted = false;
          if ($scope.opData.required.indexOf(element) > -1)
            return true;
          return false;
        };

        $scope.submitted = false;
        $scope.operation = function(success, error) {
          $scope.submitted = true;
          $scope.opData.clientErrors = [];
          $scope.opData.serverErrors = [];
          $scope.opData.required = [];
          $scope.detachData = getDetachData($scope.opData);
          var validStartDate = false, validEndDate = false;

          // Validate received by
          if (!$scope.opData.startDate) {
            $scope.opData.clientErrors.push({
              type : 'danger',
              message : 'Start Date is a required field.'
            });
            $scope.opData.required.push('startDate');
          } else {
            if (!moment($scope.opData.startDate).isValid()) {
              $scope.opData.clientErrors.push({
                type : 'danger',
                message : 'Start Date is invalid.'
              });
              $scope.opData.required.push('startDate');
            } else {
              if (moment(new Date()).diff(moment($scope.opData.startDate), 'days') > 1) {
                $scope.opData.clientErrors.push({
                  type : 'danger',
                  message : 'Start Date cannot be prior to Yesterday\'s Date.'
                });
                $scope.opData.required.push('startDate');
              }
              validStartDate = true;
            }
          }

          if ($scope.opData.endDate != null) {
            if (!moment($scope.opData.endDate).isValid()) {
              $scope.opData.clientErrors.push({
                type : 'danger',
                message : 'End Date is invalid.'
              });
              $scope.opData.required.push('endDate');
            } else {
              validEndDate = true;
            }
          }                   

          if ($scope.opData.startDate) {
            if (validStartDate && validEndDate
              && moment($scope.opData.endDate).diff(moment($scope.opData.startDate), 'days') < 0) {
              $scope.opData.clientErrors.push({
                type : 'danger',
                message : 'Start Date cannot be greater than End Date.'
              });
              $scope.opData.required.push('startDate');
            }
          }

          if (!$scope.opData.to) {
            $scope.opData.clientErrors.push({
              type : 'danger',
              message : 'To is a required field.'
            });
            $scope.opData.required.push('to');
          }

          if (!$scope.opData.shift) {
            $scope.opData.clientErrors.push({
              type : 'danger',
              message : 'Shift is a required field.'
            });
            $scope.opData.required.push('shift');
          }
          
          if(person.availableNextDay || (person.assignedNextDayShifts && person.assignedNextDayShifts.length)){
            if ((validStartDate && validEndDate
                      && moment($scope.opData.endDate).diff(moment($scope.opData.startDate), 'days') >= 1) || !$scope.opData.endDate) {
              
              $scope.opData.clientErrors.push({
                      type : 'danger',
                      message : 'This person is working a day ahead, they can only be detached for the current date. Please update your end date.'
                    });
                    $scope.opData.required.push('endDate');
            }

          }
          
          var skipUnavailValidation = false;
          if ($scope.opData.startDate) {
              if ((validStartDate && validEndDate
                && moment($scope.opData.endDate).diff(moment($scope.opData.startDate), 'days') >= 1) || !$scope.opData.endDate) {
                skipUnavailValidation = true;
              }
            }
          var unavailableHist = person.unavailabilityHistory;
          if (unavailableHist.length && !skipUnavailValidation) {
            for (var i = 0; i < unavailableHist.length; i++) {
            if (unavailableHist[i].status == 'A' && unavailableHist[i].action != 'C' &&
                ((!unavailableHist[i].end && !$scope.detachData.endDate)
              || (!unavailableHist[i].end && moment($scope.detachData.endDate).diff(moment(unavailableHist[i].start)) >= 0)
              || (!$scope.detachData.endDate && moment(unavailableHist[i].end).diff(moment($scope.detachData.startDate)) >= 0)
              || (moment(unavailableHist[i].start).diff(moment($scope.detachData.startDate)) >= 0 && moment(unavailableHist[i].start).diff(moment($scope.detachData.endDate)) <= 0)
              || (moment($scope.detachData.startDate).diff(moment(unavailableHist[i].start)) >= 0 && moment($scope.detachData.startDate).diff(moment(unavailableHist[i].end)) <= 0)
              || (moment(unavailableHist[i].end).diff(moment($scope.detachData.startDate)) >= 0 && moment(unavailableHist[i].end).diff(moment($scope.detachData.endDate)) <= 0))) {
              $scope.opData.clientErrors.push({
              type : 'danger',
              message : 'Cannot detach person as person is unavailable for the selected time frame.'
              });
              $scope.opData.required.push('startDate');
              break;
            }
            }
          }
          
          

          if ($scope.opData.clientErrors.length > 0) {
           return error(new ClientSideError('ValidationError'));
          }

          // Perform operation
          OpsBoardRepository.detachPerson(person, $scope.detachData, success, error);
        };

        // Configure modal controller and create instance
        var modalInstance = $modal.open({
          templateUrl : appPathStart + '/views/modals/modal-personnel-detachment',
          controller : 'ModalCtrl',
          backdrop : 'static',
          resolve : {
            data : function() {
              return [ person ]
            }
          },
          scope : $scope
        });

        modalInstance.result.then(function(selectedItem) {
          $scope.selected = selectedItem;
        });

        $scope.detachPersonSelect2Options = {
          allowClear : true
        }

        $scope.detachPersonDateOpts = {
          formatYear : 'yyyy',
          startingDay : 0
        }


        function getDetachData(opData) {
          return {
            from : $scope.opData.from,
            to : $scope.opData.to,
            startDate : $scope.opData.startDate,
            endDate : $scope.opData.endDate,
            shift : $scope.opData.shift,
            remarks : $scope.opData.remarks
          };
        }
      };
      
      function getMinDate(record) {
      	 var minDate;
      	 var prevDayDate = moment().add(-1, 'day');
      	if(BoardDataService.getBoardData().boardLocation === record.from){
      		if(moment(record.start).isBefore(moment())){
      			minDate = moment();
      		}else{
      			minDate =record.start;
      		}
      	}else{
      		if(moment(record.start).isBefore(prevDayDate)){
      			minDate = prevDayDate;
      		}else{
      			minDate =record.start;
      		}
      	}
         return minDate;
        }
    });