(function () {

	angular
		.module('OpsBoard')
		.service('TaskIndicatorService', ['$rootScope', taskIndicatorService]);

	function taskIndicatorService($rootScope) {

		var _applyTasksIndicators = function (linkedTaskMap, data) {
			var processedGroupIds = [];
      for (var j = 0; j < linkedTaskMap.length; j++) {
        var processed = false;
        for (var  k = 0; k < processedGroupIds.length; k++) {
          if (processedGroupIds[k] === linkedTaskMap[j].groupId) {
            processed = true;
          }
        }
        if (!processed) {
          processedGroupIds.push(linkedTaskMap[j].groupId)
          var homogeneous = true, 
            subcategoryTaskId = null,
            sectionId = null,
            catId = null,
            sameSection = true,
            sameCat = true,
            catIds = [],
            subCatHeads = {};
          for (var i = 0; i < linkedTaskMap.length; i++) {
          	catIds.push(linkedTaskMap[i].shiftCategoryId);
            if (linkedTaskMap[i].groupId === linkedTaskMap[j].groupId) {
              if (!subcategoryTaskId) {
                subcategoryTaskId = linkedTaskMap[j].subcategoryTaskId;
              }
              if (!catId) {
                catId = linkedTaskMap[j].shiftCategoryId;
              }
              if (!sectionId) {
                sectionId = linkedTaskMap[j].sectionId;
              }
              if (linkedTaskMap[i].subcategoryTaskId !== subcategoryTaskId) {
                homogeneous = false;
              }
              if (linkedTaskMap[i].sectionId !== sectionId) {
                sameSection = false;
              }
              if (linkedTaskMap[i].shiftCategoryId !== catId) {
                sameCat = false;
              }
              if (!subCatHeads[linkedTaskMap[i].subcategoryTaskId]) {
                subCatHeads[linkedTaskMap[i].subcategoryTaskId] = linkedTaskMap[i].sequence;
              } else {
                if (subCatHeads[linkedTaskMap[i].subcategoryTaskId] > linkedTaskMap[i].sequence) {
                  subCatHeads[linkedTaskMap[i].subcategoryTaskId] = linkedTaskMap[i].sequence;
                }
              }
            }
          }
          catIds = _.uniq(catIds);
          for (var y = 0; y < catIds.lenght; y++) {
          	var cat = data.locations[linkedTaskMap[i].locationId].locationShifts[linkedTaskMap[i].locationShiftId].shiftCategories[catIds[y]];
						for (var x = 0; x < cat.allsubcategoryTasks.length; x++) {
              cat.allsubcategoryTasks[x].disabled = false;
            }
          }
            
          for (var i = 0; i < linkedTaskMap.length; i++) {
            if (linkedTaskMap[i].groupId === linkedTaskMap[j].groupId) {
              linkedTaskMap[i].homogeneous = homogeneous;
              var cat = data.locations[linkedTaskMap[i].locationId].locationShifts[linkedTaskMap[i].locationShiftId].shiftCategories[linkedTaskMap[i].shiftCategoryId];
              var sub = cat.subcategoryTasks[linkedTaskMap[i].subcategoryTaskId];
              if (!sameCat) {
                cat.canNotBeDeleted = true;
              }
              for (var x = 0; x < cat.allsubcategoryTasks.length; x++) {
                if (sub.subcategoryId === cat.allsubcategoryTasks[x].id) {
                  cat.allsubcategoryTasks[x].disabled = true;
                }
              }
              if (!homogeneous) {
                sub.canNotBeDeleted = true;
              }
              if (sub.subcategory.containsSections) {
                if (subCatHeads[linkedTaskMap[i].subcategoryTaskId] && subCatHeads[linkedTaskMap[i].subcategoryTaskId] === linkedTaskMap[i].sequence) {
                  sub.sections[linkedTaskMap[i].sectionId].tasks[linkedTaskMap[i].taskId].catHead = true;
                }
                if (!sameSection) {
                  sub.sections[linkedTaskMap[i].sectionId].canNotBeDeleted = true;
                }
                sub.sections[linkedTaskMap[i].sectionId].tasks[linkedTaskMap[i].taskId].homogeneous = linkedTaskMap[i].homogeneous;
              } else {
                if (subCatHeads[linkedTaskMap[i].subcategoryTaskId] && subCatHeads[linkedTaskMap[i].subcategoryTaskId] === linkedTaskMap[i].sequence) {
                  sub.tasks[linkedTaskMap[i].taskId].catHead = true;
                }
                sub.tasks[linkedTaskMap[i].taskId].homogeneous = linkedTaskMap[i].homogeneous;
              }
            }
          }
        }
      }
		}
		return {
			applyTasksIndicators: _applyTasksIndicators
		}
	}
}());
