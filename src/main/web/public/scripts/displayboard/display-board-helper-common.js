'use strict';

angular.module('OpsBoard')
  .service(
  'DisplayBoardHelperCommon',
  function($filter, BoardValueService) {
    return {
      visualIndicator: function(personId, homeLocation) {
        var person = BoardValueService.personnel[personId];
        var refData = BoardValueService.refData;
        var indicator = '';
        var boarddate = new Date(refData.boardDate);
        var boarddate2  = new Date(refData.boardDate);
        var boarddateyesterday = new Date(boarddate2.setDate(boarddate2.getDate() - 1));

        _.each(person.unavailabilityHistory, function(h) {

          if(h.action && h.action === 'D') {
            indicator = ''; //if 'Delete', no class 'C'; this is different from 'Cancel'.
          }
          else if(person.currentLocation === person.homeLocation || homeLocation === true) {

            if(boarddateyesterday >= new Date(h.start) && boarddateyesterday <= new Date(h.end) && h.code === 'VACATION' ) {
              indicator = 'OV';
            }

            if(boarddateyesterday >= new Date(h.start) && boarddateyesterday <= new Date(h.end) && h.code === 'CHART' ) {
              indicator = 'OC';
            }

            if(boarddateyesterday >= new Date(h.start) && boarddateyesterday <= new Date(h.end) && h.code === 'XWP' ) {
              indicator = 'OX';
            }

            if(boarddateyesterday >= new Date(h.start) && boarddateyesterday <= new Date(h.end) && h.code === 'XWOP' ) {
              indicator = 'OP';
            }

            if(boarddate >= new Date(h.start) && boarddate <= new Date(h.end) && h.code === 'VACATION' ) {
              indicator = 'V';
            }

            if(boarddate >= new Date(h.start) && boarddate <= new Date(h.end) && h.code === 'CHART' ) {
              indicator = 'C';
            }
          }
        });

        return indicator;
      },
      getDetachedSWStatus: function (person) {
        return (person.detachedSW === true) ? "detachedsw " : "";
      },
      getDetachedSupStatus: function (person) {
        return (person.detachedSup === true) ? "detachedsup " : "";
      },
      getDetachedOSPayrollStatus: function (person) {
        return (person.detachedOutSidePayroll === true) ? "detachedospayroll " : "";
      },
      getAttachedToPayrollLocStatus: function (person) {
        return (person.attachedToPayrollLocation === true) ? "attachedtopayrollloc " : "";
      },
      getPersonnelHiddenStatus: function (person){
    	return (person.state === "Hidden" && person.assigned === false) ? "personhidden " : "";
      },
      mapgroups: function(orderByLocation, refData) {
          var taskmap = {};
          var startmap = {};
          var taskmapSorted = [];
          var multiroutes = [];
          var mapgroupsObject = {};
          var tasks = [];

          orderByLocation.forEach(function(location) {
            var shifts = location.locationShifts;
            shifts = $filter('orderByShiftId')(shifts);
            shifts.forEach(function(shift2) {
              refData.shiftCategories = $filter('orderByCategoryId')(shift2.shiftCategories, refData.categories);
              refData.shiftCategories.forEach(function(shiftcategory2) {
                refData.subcategorytasks = $filter('orderBySubcategorySequence')(shiftcategory2.subcategoryTasks, refData.allSubcategories);
                refData.subcategorytasks.forEach(function(categorytask2) {
                  if(categorytask2.subcategoryId !== 263 && categorytask2.subcategoryId !== 264 && categorytask2.subcategoryId !== 258) {
                    tasks = $filter('orderByTaskId')(categorytask2.tasks);

                    tasks.forEach(function(task2) {
                      if(parseInt(task2.partialTaskSequence) > 0) {
                        if(task2.linkedTaskParentId === null) {
                          task2.multigroup = task2.groupId;
                          startmap[task2.id] = task2;
                        }
                        taskmap[task2.id] = task2;
                      }
                    });

                    refData.sections = $filter('orderBySection')(categorytask2.sections);

                    refData.sections.forEach(function(section2) {
                      tasks = $filter('orderByTaskId')(section2.tasks);
                      tasks.forEach(function(task2) {
                        task2.section = section2.sectionName;
                        task2.location = location.locationCode;
                        if(parseInt(task2.partialTaskSequence) > 0) {
                          if(task2.linkedTaskParentId === null) {
                            task2.multigroup = task2.groupId;
                            startmap[task2.id] = task2;
                          }

                          taskmap[task2.id] = task2;
                        }
                      });
                    });
                  }
                });
              });
            });
          });

          taskmapSorted = $filter('orderBySequence')(taskmap);
          var multiroutindex = 0;

          taskmapSorted.forEach(function(i) {
            if(i.linkedTaskParentId) {
              try {
                i.multigroup = taskmap[i.linkedTaskParentId].multigroup;
              }
              catch(err) {
                console.log(err);
              }
            }

            multiroutindex = i.multigroup -1;

            if(!multiroutes[multiroutindex]) {
              multiroutes[multiroutindex] = [];
            }

            multiroutes[multiroutindex].push(i);
          });

          mapgroupsObject.orderByLocation = orderByLocation;
          mapgroupsObject.multiroutes = multiroutes;
          return mapgroupsObject;
      },
      orphanPop: function(prevCol, refData) {
        if(prevCol[prevCol.length - 1].type === 'section' || prevCol[prevCol.length - 1].type === 'shift' || prevCol[prevCol.length - 1].type === 'location' || prevCol[prevCol.length - 1].type === 'unavailableheader' || prevCol[prevCol.length - 1].type === 'lastShiftBlock' || prevCol[prevCol.length - 1].type === 'multiheader' || prevCol[prevCol.length - 1].type === 'subcategory') {
          refData.cols['col' + refData.col].pop();
        }
        return refData;
      }
    }
  });