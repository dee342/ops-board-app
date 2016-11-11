'use strict';

angular.module('OpsBoardFilters', ['personnelFilters', 'locationFilters'])
	.filter('reverseArray', function() {
		return function(items) {
			return items ? items.slice().reverse() : [];
		}
	})
	.filter('personDetailsName', function() {
		return function(input, length) {
			if (input == null || typeof input == undefined || input == '')
				return '';
			var nmArr = input.split(',');
			if (nmArr.length < 2) return input;
			var lastName = nmArr[0].trim(),
			    fnmArr = nmArr[1].trim().split(' '),
			    firstName = fnmArr[0],
			    middleInitial = fnmArr[1] ? fnmArr[1].substring(0, 1).trim().toUpperCase() : '';

			var formattedName = lastName + ', ' + firstName;
			if (middleInitial) formattedName = formattedName + ' ' + middleInitial + '.';
			return  formattedName;
		};
	})
	.filter('appendChartNumber', function() {
		return function(chartName, chartNumber) {
			if (!chartName && !chartNumber) return '';
			if (!chartNumber||chartNumber==" ") return chartName;
			if (!chartName) return chartNumber;
			return chartName + ' / ' + chartNumber;
		};
	})
	.filter('phone', function() {
		return function(phone) {
			if (!phone) return '';
			var formattedPhone = phone.replace(/[\D]+/g, '')
                                      .replace(/(\d{3})(\d{3})(\d{4})/, '($1) $2-$3');
			return formattedPhone;
		};
	})
	.filter('payrollLocation', function() {
		return function(payrollLocation, id) {
			if (!payrollLocation && !id) return '';
			if (!id) return payrollLocation;
			if (!payrollLocation) return id;
			return payrollLocation + ' / ' + id;
		};
	})
	.filter('qualification', function() {
		return function(qualifications) {
			if (!qualifications)
				return '';

			if (Object.prototype.toString.call(qualifications) === '[object Array]')
				return qualifications;

			return qualifications.split(',');
		};
	})
	.filter('formatDate', function() {
		return function(date, format) {
			if (!date) return '';
			if (!format) return date;
			return moment(date).format(format);
		}
	})
	.filter('toArray', function() {
		return function(obj) {
		    if (!(obj instanceof Object)) return obj;
		    return _.map(obj, function(val, key) {
		        return Object.defineProperty(val, '$key', {__proto__: null, value: key});
		    });
	    }
	})
	.filter('convertTypeToClass', function() {
		return function(type) {
		   if (!type) return ''
		   return type.toLowerCase().replace(/\s/g, '-')
	    }
	}).filter('judgeStatus',function(){
		return function(status){
			if(status)
				return 'Down';
			else
				return 'Up';
		}

	}).filter('judgeAction',function(){
		return function(status){
			if(status)
				return 'Set Down';
			else
				return 'Set Up';
		}

	}).filter('firstInitial',

	function() {
		return function(input) {
			if(!input) {
				return '';
			}
			var initial = input.substring(0, 1) + '.';

			return initial;
		}
	}).filter('multiRouteGroup',

	function() {
		return function(linkedTaskMap, groupId, tasks, sourcetask, durations){

			var linkedTasks = [];
			var linkedTask = {};
			var results = [];
			var locationObject = {};
			var shiftObject = {};
			var shiftCategoryObject = {};
			var subcategoryTaskObject = {};
			var taskObject = {};
			var sectionObject = {};
			var duration = '';

			angular.forEach(linkedTaskMap, function(value, key) {
				if (value.groupId === groupId) {
					linkedTasks.push(value);
				}
			});

			for (var i = 0; i<linkedTasks.length; i++) {
				linkedTask = linkedTasks[i];
				for (var location in tasks.locations) {
					locationObject = tasks.locations[location];
					for (var shift in locationObject.locationShifts) {
						shiftObject = locationObject.locationShifts[shift];
						for (var shiftCategory in shiftObject.shiftCategories) {
							shiftCategoryObject = shiftObject.shiftCategories[shiftCategory];
							for (var subcategoryTask in shiftCategoryObject.subcategoryTasks) {
								subcategoryTaskObject = shiftCategoryObject.subcategoryTasks[subcategoryTask];
								for (var task in subcategoryTaskObject.tasks) {
									taskObject = subcategoryTaskObject.tasks[task];
									if(taskObject.id === linkedTask.taskId ) {

										for (var j = 0; j < durations.length; j++) {
											if (durations[j].duration == taskObject.hours) {
												duration = durations[j].label;
												break;
											}
										}

										if(taskObject.partialTaskSequence === sourcetask.partialTaskSequence) {
											taskObject.highlight = true;
										} else {
											taskObject.highlight = false;
										}

										taskObject.number = i + 1;
										taskObject.sequence = linkedTask.sequence;
										taskObject.category = shiftCategoryObject.category;
										taskObject.subcategory = subcategoryTaskObject.subcategory;
										taskObject.duration = duration;
										taskObject.hours = linkedTask.duration;
										results.push(taskObject);
									}
								}

								for (var section in subcategoryTaskObject.sections) {
									sectionObject = subcategoryTaskObject.sections[section];
									for (var task in sectionObject.tasks) {
										taskObject = sectionObject.tasks[task];
										if(taskObject.id === linkedTask.taskId ) {
											for (var j = 0; j < durations.length; j++) {
												if (durations[j].duration == taskObject.hours) {
													duration = durations[j].label;
													break;
												}
											}

											if(taskObject.partialTaskSequence === sourcetask.partialTaskSequence) {
												taskObject.highlight = true;
											} else {
												taskObject.highlight = false;
											}

											taskObject.number = i + 1;
											taskObject.sequence = linkedTask.sequence;
											taskObject.category = shiftCategoryObject.category;
											taskObject.subcategory = subcategoryTaskObject.subcategory;
											taskObject.section = sectionObject;
											taskObject.duration = duration;
											taskObject.hours = linkedTask.duration;
											results.push(taskObject);
										}
									}
								}
							}
						}
					}
				}
			};

			return results;
		}
	}).filter('orderByShiftId',

	function() {
		return function(items, reverse) {
			var filtered = [];
			angular.forEach(items, function(item) {
				filtered.push(item);
			});

			filtered.sort(function (a, b) {
				try{
					var aId = a.shiftId ? a.shiftId : a.shift.id,
					  bId = b.shiftId ? b.shiftId : b.shift.id;
					return (aId > bId ? 1 : -1);
				} catch(err) {
				}
			});

			if(reverse) filtered.reverse();

			return filtered;
		};
	}).filter('orderByCategoryId',

	function() {
		return function(items, reverse, categoryData) {
			var filtered = [];
			angular.forEach(items, function(item) {
				if(!item.category || item.category === undefined) {
					item.category = categoryData[item.categoryId];
				}
				filtered.push(item);
			});

			filtered.sort(function (a, b) {
				return (a.category.sequence > b.category.sequence ? 1 : -1);
			});

			if(reverse) filtered.reverse();
			return filtered;
		};
	}).filter('orderBySubcategorySequence',

	function() {
		return function(items, reverse, subcategoryData) {
			var filtered = [];
			angular.forEach(items, function(item) {
				if(!item.subcategory) {
					item.subcategory = subcategoryData[item.subcategoryId];
				}
				filtered.push(item);
			});
			filtered.sort(function (a, b) {
				return (a.subcategory.sequence > b.subcategory.sequence ? 1 : -1);
			});

			if(reverse) filtered.reverse();
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
				return parseInt(a.sectionName) > parseInt(b.sectionName) ? 1 : -1;
			});
			
			if(reverse === true) filtered.reverse();

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
	}).filter('orderByLocation',

	function() {
		return function(items, reverse) {

			var filtered = [];

			angular.forEach(items, function(item, key) {
				if (!item.location) {
					item.location = {code: key}
				}
				filtered.push(item);
			});

			filtered.sort(function (a, b) {
				return (a.location.code > b.location.code ? 1 : -1);
			});

			if(reverse) filtered.reverse();


			return filtered;
		};
	}).filter('orderDistrict', function () {

		var sortLocation = function (sortKey) {
			return function (a, b) {
				var broomRegEx = /[0-9]$/;
				if (a[sortKey].match(broomRegEx) && !b[sortKey].match(broomRegEx)) {
					return -1;
				}
				if (!a[sortKey].match(broomRegEx) && b[sortKey].match(broomRegEx)) {
					return 1;
				}
				if (a[sortKey].match(broomRegEx) && b[sortKey].match(broomRegEx)) {
					if (a[sortKey] > b[sortKey]) {
						return 1;
					}
					if (a[sortKey] < b[sortKey]) {
						return -1;
					}
					if (a[sortKey] == b[sortKey]) {
						return 1;
					}
				}
				if (a[sortKey] > b[sortKey]) {
					return 1;
				}
				if (a[sortKey] < b[sortKey]) {
					return -1;
				}
				if (a[sortKey] == b[sortKey]) {
					return 1;
				}
			};
		};

		return function (locations, boardLocation, codeOrId) {
			
			// normalize the different keys we use for settings or tasks panels
			var sortKey = codeOrId ? 'locationCode' : 'locationId';
			var filtered = _.toArray(locations).sort(sortLocation(sortKey));

			// pluck current board location from filtered, it should always be first
			for (var i = 0, len = filtered.length, currentLocation; i < len; i ++) {
				if (filtered[i] && filtered[i][sortKey] === boardLocation) {
					currentLocation = filtered.splice(i, 1);
					filtered.unshift(currentLocation[0]);
				}
			}

			return filtered;
		};
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
	}).filter('formattedName',

	function() {
		return function(person) {

			var lastName = person.lastName,
				firstName = person.firstName ? person.firstName.substring(0, 1).toUpperCase() : '';

			var name = firstName ? firstName + '. ' + lastName : lastName;
			var context = canvas.getContext("2d");
			context.font = "400 12px 'Open Sans', sans-serif";
			var metrics = context.measureText(name)
			if (metrics && metrics.width > 80) return person.lastName.substring(0,12);
			return name;
		}
	}).filter('filterDistrictObjects',

	function() {
		return function(districtdata) {
			var shiftObject = {};
			var shiftCategoryObject = {};
			var shiftCategories = {};
			var subcategoryTaskObject = {};
			var taskObject = {};
			var districtPersonnel = [];
			var districtEquipment = [];
			var districtData = {};

			for (var shift in districtdata.locationShifts) {
				shiftObject = districtdata.locationShifts[shift];
				for (var shiftCategory in shiftObject.shiftCategories) {
					shiftCategoryObject = shiftObject.shiftCategories[shiftCategory];
					for (var subcategoryTask in shiftCategoryObject.subcategoryTasks) {
						subcategoryTaskObject = shiftCategoryObject.subcategoryTasks[subcategoryTask];
						for (var task in subcategoryTaskObject.tasks) {
							taskObject = subcategoryTaskObject.tasks[task];

							if(taskObject.assignedPerson1.person) {
								if (districtPersonnel.indexOf(taskObject.assignedPerson1.person.id) < 0) {
									districtPersonnel.push(taskObject.assignedPerson1.person.id);
								}
							}

							if(taskObject.assignedEquipment.equipment) {
								if(districtEquipment.indexOf(taskObject.assignedEquipment.equipment.id) < 0) {
									districtEquipment.push(taskObject.assignedEquipment.equipment.id);
								}
							}
						}
					}
				}
			}

			districtData.personnel = districtPersonnel;
			districtData.equipment = districtEquipment;

			return districtData;
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
			if(lastName.length>4) {
			  lastName = lastName.substr(0, 4) + '.';
			}
			var name = firstName ? firstName + ' ' + lastName : lastName;
			return name;
		}
	}).filter('mdaFilter',
	function($filter) {
		return function(persons, filterType) {
			var results = {};
			var unavailablePersonnelSorted = $filter('unavailablePersonnelFilter')(persons, filterType);
			var resultsMDA = [];

			if (unavailablePersonnelSorted && unavailablePersonnelSorted.length > 0) {
				angular.forEach(unavailablePersonnelSorted, function (valuePersonnel, keyPersonnel) {
					if (valuePersonnel.unavailabilityHistory.length === 0 && valuePersonnel.detachmentHistory.length === 0 && valuePersonnel.mdaStatusHistory.length > 0) {
						resultsMDA.push(valuePersonnel);
					}
				});
			}

			results.resultsMDA = resultsMDA;
			results.mdatotal = resultsMDA.length;

			return results;
		}
	}).filter('detachLocationsFilter',
	function($filter) {
		return function(locations, location, currentLocation, filterType) {
			var fiiteredLocations = $filter('removeInvalidLocations')(locations, filterType);
			
			var repairLocations = [];
			for (var i = 0; i < fiiteredLocations.length; i++) {
				repairLocations.push(fiiteredLocations[i].code);
			}
			
			
			repairLocations = $filter('orderBy')(repairLocations);
			repairLocations.splice(angular.element.inArray(location, repairLocations),1);
			repairLocations.splice(angular.element.inArray(currentLocation, repairLocations),1);
			repairLocations.splice(angular.element.inArray("UNKNOWN", repairLocations),1);
			repairLocations.splice(angular.element.inArray("OTHER", repairLocations),1);


			return repairLocations;
		}
	});