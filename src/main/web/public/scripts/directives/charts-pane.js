'use strict';

angular
  .module('OpsBoard')
  .directive(
  'chartsPane', ['BoardDataService', '$filter', '$rootScope', '$modal', 'OpsBoardRepository',
    function(BoardDataService, $filter, $rootScope, $modal, OpsBoardRepository) {
      return {
        restrict: 'E',
        scope: {
          layout: '=',
          panes: '=',
          personnelpane: '=',
          ModalCtrl: '='
        },

        templateUrl: function(elem,attrs) {
          return BoardDataService.getPathStart() + '/views/fragments/charts-pane';
        },

        link: function (scope, element, attributes) {
          element.bind("$destroy", function() {
            scope.$destroy();
          });
          
          scope.constMassChart = 20;
          scope.persons = OpsBoardRepository.getPersonnel();
          scope.chartDate = BoardDataService.getFormattedBoardDate();
          scope.processing = false;

          var updateChartsPane = function () {
            var chartsFiltered = $filter('chartFilter')(scope.persons, scope.chartDate),
              chartCancelledFiltered = $filter('chartCancelledFilter')(scope.persons, scope.chartDate),
              chartCancelledTagged = $filter('chartTagged')(chartsFiltered.results, 'chartCancelledTagged'),
              chartReverseCancelledTagged = $filter('chartTagged')(chartCancelledFiltered.results, 'chartReverseCancelledTagged'),
              charts = _.difference((_.union(chartsFiltered.results, chartReverseCancelledTagged.results)), chartCancelledTagged.results),
              chartCancelled = _.difference((_.union(chartCancelledFiltered.results, chartCancelledTagged.results)), chartReverseCancelledTagged.results),     
              chartConst = scope.constMassChart > charts.length ? scope.constMassChart - charts.length : 0,
              chartCancelledConst = scope.constMassChart > chartCancelled.length ? scope.constMassChart - chartCancelled.length : 0;

            scope.charts = charts;
            scope.chartsShow = !!charts.length;
            scope.chartsTotal = charts.length;
            scope.chartsConstTotal = new Array(chartConst);

            scope.chartCancelled = chartCancelled;
            scope.chartCancelledShow = !!chartCancelled.length;
            scope.chartCancelledTotal = chartCancelled.length;
            scope.chartCancelledConstTotal = new Array(chartCancelledConst);

            scope.chartsFiltered = chartsFiltered;
            scope.chartCancelledFiltered = chartCancelledFiltered;
            scope.chartCancelledTagged = chartCancelledTagged;
            scope.chartReverseCancelledTagged = chartReverseCancelledTagged;
          }

          scope.datepickers = {
            dateOpened : false
          }

          scope.untagAll = function () {
            angular.forEach(scope.persons, function(value, pId) {
              delete value.chartCancelledTagged;
              delete value.chartReverseCancelledTagged;
            })
          }

          scope.toggleChartsPane = function () {
            scope.panes.charts.active = !scope.panes.charts.active;
            scope.layout.screen2.pane1.visible = scope.panes.charts.active ? false : scope.panes.tasks.active ? true : false;
            scope.layout.screen2.visible = scope.panes.charts.active || scope.panes.tasks.active;
            $rootScope.$broadcast('adjust-layout');//dont have time to actually expose adjust-layout
            scope.untagAll();
            updateChartsPane();
          }

          $rootScope.$on('reset-charts-pane', function(event, flag) {
            var bData = BoardDataService.getBoardData();
            if (!moment().subtract(1, 'minutes').isBetween(moment(bData.startDate), moment(bData.endDate), 'minutes')) {
              openError()
            } else {
              scope.toggleChartsPane();
            }
          })
          
          scope.opData = {
            titleAction : 'Mass Chart Updates',
            submitButtonText: 'Ok',
            errors: [{message: 'This Board is not for the current date. To perform mass chart updates, please load the Board for the current date.'}]
          };

          scope.passPreValidation = function () {
            return false;
          }
          
          scope.operation = angular.noop;

          var openError = function () {
            $modal.open({
              templateUrl : appPathStart + '/views/modals/modal-mass-chart-updates',
              controller : 'ModalCtrl',
              backdrop : 'static',
              resolve : {
                data : function() {
                  return [scope.opData];
                }
              },
              scope : scope
            });
          }

          scope.open = function($event, opened) {
            $event.preventDefault();
            $event.stopPropagation();
            scope.datepickers[opened] = !scope.datepickers[opened];
          };

          scope.massChartUpdate = function () {
            var reverseCancelled = _.difference(scope.chartReverseCancelledTagged.results, scope.chartsFiltered.results),
              cancelled = _.difference(scope.chartCancelledTagged.results, scope.chartCancelledFiltered.results),
              cancelledIds = [],
              reverseCancelledIds = [];

            cancelled.forEach(function(value, key) {
              cancelledIds.push(value.id)
            })

            reverseCancelled.forEach(function(value, key) {
              reverseCancelledIds.push(value.id)
            })
            scope.processing = true;
            OpsBoardRepository.massChartUpdate(cancelledIds, reverseCancelledIds, moment(scope.chartDate).format('YYYYMMDD'), function (data){
              scope.processing = false;
              scope.untagAll();
            }, function (err) {
              //error handling
              scope.processing = false;
              scope.untagAll();
            })
          }

          scope.tagChartCancelled = function (person) {
            person.chartCancelledTagged = true;
            person.chartReverseCancelledTagged = false;
            updateChartsPane();
          }

          scope.loadChart = function () {
           updateChartsPane()
          }

          scope.tagAllChartCancelled = function () {
            var i = 0;
            for (; i < scope.chartsTotal; i++) {
              scope.charts[i].chartCancelledTagged = true;
              scope.charts[i].chartReverseCancelledTagged = false;
            }
            updateChartsPane();
          }

          scope.tagAllChartReverseCancelled = function () {
            var i = 0;
            for (; i < scope.chartCancelledTotal; i++) {
              scope.chartCancelled[i].chartCancelledTagged = false;
              scope.chartCancelled[i].chartReverseCancelledTagged = true;
            }
            updateChartsPane();
          }

          scope.tagChartReverseCancelled = function (person) {
            person.chartCancelledTagged = false;
            person.chartReverseCancelledTagged = true;
            updateChartsPane();
          }

          updateChartsPane();

          // main method for filtering assignments to a shift
          scope.$on('UPDATE-PERSONNEL-PANE', updateChartsPane);
          scope.$on('ASSIGNED-PERSON', updateChartsPane);
          scope.$on('UNASSIGNED-PERSON', updateChartsPane);
          scope.$on('DETACH-PERSON', updateChartsPane);

        }
      };
    }
  ]);