'use strict';

angular
		.module('personnelFilters', [ 'personnelFiltersUtils' ])
		.filter(
				'getAvailableSanitationWorkers',
				function($filter, OpsBoardRepository) {
					return function(numAssigned, sanitationWorkersTotal) {
						var assignedeSanitationWorkers = 0;
						var persons = OpsBoardRepository.getPersonnel();
						var sortedResults = $filter('assignedFilter')(persons,
								'Location Seniority', numAssigned);
						angular.forEach(sortedResults, function(personnel) {
							switch (personnel.civilServiceTitle.toUpperCase()) {
							case 'SW':
								assignedeSanitationWorkers++;
								break;
							}
						});
						return assignedeSanitationWorkers
								+ sanitationWorkersTotal;
					}
				})
		.filter(
				'getAvailableSupervisors',
				function($filter, OpsBoardRepository) {
					return function(numAssigned, supervisorsTotal) {
						var assignedeSupervisors = 0;
						var persons = OpsBoardRepository.getPersonnel();
						var sortedResults = $filter('assignedFilter')(persons,
								'Location Seniority', numAssigned);

						angular.forEach(sortedResults, function(personnel) {
							switch (personnel.civilServiceTitle.toUpperCase()) {
							case 'SUP':
								assignedeSupervisors++;
								break;
							}
						});
						return assignedeSupervisors + supervisorsTotal;
					}
				})
		.filter(
				'getAvailableSuperintendents',
				function($filter, OpsBoardRepository) {
					return function(numAssigned, superintendentsTotal) {
						var assignedeSuperintendents = 0;
						var persons = OpsBoardRepository.getPersonnel();
						var sortedResults = $filter('assignedFilter')(persons,
								'Location Seniority', numAssigned);

						angular.forEach(sortedResults, function(personnel) {
							switch (personnel.civilServiceTitle.toUpperCase()) {
							case 'GS I':
								assignedeSuperintendents++;
								break;
							}
						});
						return assignedeSuperintendents + superintendentsTotal;
					}
				})
		.filter(
				'assignedFilter',
				function($filter, states) {
					return function(persons, filterType, numAssigned) {
						var results = [];

						angular
								.forEach(
										persons,
										function(value, key) {

											if (value.hasOwnProperty('state')
													&& value.state === states.personnel.available) {
												if (value.assigned
														|| (value.dayBeforeShifts
																&& value.dayBeforeShifts.length && !value.availableNextDay))
													results[key] = value;
											}
										});

						var results = _.uniq(results);

						if (!results || results.length == 0)
							return results;

						var sortedResults = $filter('sortPersonnelPanel')(
								results, filterType, 'Available');

						return sortedResults;
					}
				})
		.filter(
				'sortPersonnelPanel',
				function($filter, OpsBoardRepository, titleHierarchy, titles,
						departmentTypeHierarchy, BoardValueService, BoardDataService, boardtypes) {

					return function(list, filterType, pState) {

						var filterTypeClass = 'fa-sort-numeric-desc';
						var location = OpsBoardRepository.getBoardLocation();

						if (!list
								|| Object.prototype.toString.call(list) !== '[object Array]') {
							return list;
						}

						var state = pState === undefined ? "Available" : pState;

						if (!filterType) {
							filterType = 'Location Seniority';
						}

						var sortedList = list;
						_.each(
										sortedList,
										function(p) {
											
											p.departmentTypeSort = departmentTypeHierarchy
													.indexOf(p.departmentType
															.toUpperCase());
											
											p.titleHierachy = titleHierarchy.indexOf(p.civilServiceTitle);

											if (titleHierarchy
													.indexOf(p.civilServiceTitle) === 8) {

												p.locationSeniorityGroup = p.departmentTypeSort;
											} else {
												p.locationSeniorityGroup = 0;
											}

											if (p.homeLocation === location) {
												p.homeLocationSort = 0;
											} else {
												p.homeLocationSort = 1;
											}
										});

						// THIS IS AN OVERRIDE OF THE TRADITIONAL FILTER
						// APPROACH (ABOVE) TO USE THE ANGULAR orderBy THAT
						// SUPPORTS A SIMPLE API FOR MULTIPLE PROPERTY SORTING.
						
						if (filterType === 'Location Seniority') {
							filterTypeClass = 'fa-sort-numeric-desc';
							sortedList = $filter('orderBy')(sortedList,	['homeLocationSort','titleHierachy','payrollLocationId','seniorityDate','listNoFormatted'], false);

						  var sortedGworkers = [];
						  var GworkersList = _.filter(sortedList, function(whiteCard) {
							return whiteCard.departmentType === 'G';
						  });

						  if(GworkersList.length>1) {
							sortedGworkers = $filter('orderBy')(GworkersList, ['homeLocationSort','titleHierachy','seniorityDate','listNoFormatted'], false);

							for(var j= 0,ix=0,len=sortedList.length; j<len; j++) {
							  if(sortedList[j].departmentType === 'G') {
								sortedList[j] = sortedGworkers[ix++];
							  }
							}
						  }
						}
						
						if (filterType === 'Seniority') {
							filterTypeClass = 'fa-sort-numeric-desc';
							
							sortedList = $filter('orderBy')(sortedList, ['homeLocationSort','titleHierachy','seniorityDate','listNoFormatted'], false);
						}
						
						if (filterType === 'Reverse Seniority') {

							sortedList = $filter('orderBy')(sortedList,	['titleHierachy'], false);
							sortedList = $filter('orderBy')(sortedList,	['seniorityDate','listNoFormatted'], true);
							sortedList = $filter('orderBy')(sortedList,	['homeLocationSort'], false);
						}
						
						if (filterType === 'Location Reverse Seniority') {

							sortedList = $filter('orderBy')(sortedList,	['homeLocationSort','titleHierachy','payrollLocationId','seniorityDate','listNoFormatted'], true);
						}
						
						if (filterType === 'Last Name') {
							filterTypeClass = 'fa-sort-alpha-asc';
							sortedList = $filter('orderBy')(sortedList,	['lastName'], false);
						}

						return sortedList;
					}
				})
		.filter(
				'getActualSanitationWorkers',
				function($filter, OpsBoardRepository) {
					return function() {
						var actualSanitationWorkers = 0;
						var tasks = OpsBoardRepository.getTasks();
						angular
								.forEach(
										tasks.locations,
										function(location, key) {
											angular
													.forEach(
															location.locationShifts,
															function(
																	locationShift,
																	key) {
																angular
																		.forEach(
																				locationShift.shiftCategories,
																				function(
																						shiftCategory,
																						key) {
																					angular
																							.forEach(
																									shiftCategory.subcategoryTasks,
																									function(
																											subcategoryTask,
																											key) {
																										if (subcategoryTask.subcategory.personnelTitle
																												&& subcategoryTask.subcategory.personnelTitle.name === "SW") {
																											if (subcategoryTask.subcategory.containsSections) {
																												angular
																														.forEach(
																																subcategoryTask.sections,
																																function(
																																		section,
																																		key) {
																																	angular
																																			.forEach(
																																					section.tasks,
																																					function(
																																							task,
																																							key) {
																																						if (task.partialTaskSequence
																																								&& task.partialTaskSequence === 1) {
																																							actualSanitationWorkers = actualSanitationWorkers
																																									+ subcategoryTask.subcategory.peoplePerTask;
																																						} else {
																																							if (!task.partialTaskSequence) {
																																								actualSanitationWorkers = actualSanitationWorkers
																																										+ subcategoryTask.subcategory.peoplePerTask;
																																							}
																																						}
																																					})
																																})
																											} else {
																												angular
																														.forEach(
																																subcategoryTask.tasks,
																																function(
																																		task,
																																		key) {
																																	if (task.partialTaskSequence
																																			&& task.partialTaskSequence === 1) {
																																		actualSanitationWorkers = actualSanitationWorkers
																																				+ subcategoryTask.subcategory.peoplePerTask;
																																	} else {
																																		if (!task.partialTaskSequence) {
																																			actualSanitationWorkers = actualSanitationWorkers
																																					+ subcategoryTask.subcategory.peoplePerTask;
																																		}
																																	}
																																})
																											}
																										}
																									})
																				})
															})
										});

						return actualSanitationWorkers;
					}
				})
		.filter(
				'getActualSup',
				function($filter, OpsBoardRepository) {
					return function() {
						var actualSupervisors = 0;
						var isBoro = OpsBoardRepository.isBoroBoard();
						var locationcode = OpsBoardRepository
								.getBoardLocation();
						var tasks = OpsBoardRepository.getTasks();

						angular
								.forEach(
										tasks.locations,
										function(location, key) {
											if (!isBoro
													|| (isBoro && location.locationCode === locationcode)) {
												angular
														.forEach(
																location.locationShifts,
																function(
																		locationShift,
																		key) {
																	angular
																			.forEach(
																					locationShift.shiftCategories,
																					function(
																							shiftCategory,
																							key) {
																						angular
																								.forEach(
																										shiftCategory.subcategoryTasks,
																										function(
																												subcategoryTask,
																												key) {
																											// use
																											// this
																											// as a
																											// model
																											// to
																											// fix
																											// all
																											// of
																											// these,
																											// they
																											// have
																											// no
																											// checking
																											// for
																											// attributes
																											var personTitle = subcategoryTask.subcategory.personnelTitle
																													&& subcategoryTask.subcategory.personnelTitle.name;
																											if (personTitle === 'SUP') {
																												if (subcategoryTask.subcategory.containsSections) {
																													angular
																															.forEach(
																																	subcategoryTask.sections,
																																	function(
																																			section,
																																			key) {
																																		angular
																																				.forEach(
																																						section.tasks,
																																						function(
																																								task,
																																								key) {
																																							if (task.partialTaskSequence
																																									&& task.partialTaskSequence === 1) {
																																								actualSupervisors = actualSupervisors
																																										+ subcategoryTask.subcategory.peoplePerTask;
																																							} else {
																																								if (!task.partialTaskSequence) {
																																									actualSupervisors = actualSupervisors
																																											+ subcategoryTask.subcategory.peoplePerTask;
																																								}
																																							}
																																						})
																																	})
																												} else {
																													angular
																															.forEach(
																																	subcategoryTask.tasks,
																																	function(
																																			task,
																																			key) {
																																		if (task.partialTaskSequence
																																				&& task.partialTaskSequence === 1) {
																																			actualSupervisors = actualSupervisors
																																					+ subcategoryTask.subcategory.peoplePerTask;
																																		} else {
																																			if (!task.partialTaskSequence) {
																																				actualSupervisors = actualSupervisors
																																						+ subcategoryTask.subcategory.peoplePerTask;
																																			}
																																		}
																																	});
																												}
																											}
																										});
																					});
																});
											}
										});

						return actualSupervisors;
					}
				})
		.filter(
				'getActualSuperintendents',
				function($filter, OpsBoardRepository) {
					return function() {
						var actualSuperintendents = 0;
						var isBoro = OpsBoardRepository.isBoroBoard();
						var locationcode = OpsBoardRepository
								.getBoardLocation();
						var tasks = OpsBoardRepository.getTasks();

						angular
								.forEach(
										tasks.locations,
										function(location, key) {

											if (!isBoro
													|| (isBoro && location.locationCode === locationcode)) {
												angular
														.forEach(
																location.locationShifts,
																function(
																		locationShift,
																		key) {
																	angular
																			.forEach(
																					locationShift.shiftCategories,
																					function(
																							shiftCategory,
																							key) {
																						angular
																								.forEach(
																										shiftCategory.subcategoryTasks,
																										function(
																												subcategoryTask,
																												key) {
																											var tempNumberOfTasks = subcategoryTask.numOfTasks;
																											if (subcategoryTask.subcategory.personnelTitle
																													&& subcategoryTask.subcategory.personnelTitle.name === "GS I") {
																												if (subcategoryTask.subcategory.containsSections) {
																													angular
																															.forEach(
																																	subcategoryTask.sections,
																																	function(
																																			section,
																																			key) {
																																		angular
																																				.forEach(
																																						section.tasks,
																																						function(
																																								task,
																																								key) {
																																							if (task.partialTaskSequence
																																									&& task.partialTaskSequence === 1) {
																																								actualSuperintendents = actualSuperintendents
																																										+ subcategoryTask.subcategory.peoplePerTask;
																																							} else {
																																								if (!task.partialTaskSequence) {
																																									actualSuperintendents = actualSuperintendents
																																											+ subcategoryTask.subcategory.peoplePerTask;
																																								}
																																							}
																																						})
																																	})
																												} else {
																													angular
																															.forEach(
																																	subcategoryTask.tasks,
																																	function(
																																			task,
																																			key) {
																																		if (task.partialTaskSequence
																																				&& task.partialTaskSequence === 1) {
																																			actualSuperintendents = actualSuperintendents
																																					+ subcategoryTask.subcategory.peoplePerTask;
																																		} else {
																																			if (!task.partialTaskSequence) {
																																				actualSuperintendents = actualSuperintendents
																																						+ subcategoryTask.subcategory.peoplePerTask;
																																			}
																																		}
																																	})
																												}
																											}
																										})
																					})
																});
											}
										});
						return actualSuperintendents;
					}
				})
		.filter(
				'workersFilter',
				function($filter, groups) {
					return function(persons, filterType, group) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var results = $filter('filterPersonnelByGroup')(
								persons, group);
						var availableResults = $filter('availPersonnelFilter')(
								results);

						if (Object.keys(availableResults).length > 0) {
							results.show = true;
						}

						results.results = $filter('sortPersonnelPanel')(
								availableResults, filterType, 'Available');
						results.total = Object.keys(availableResults).length;
						return results;
					}
				})
		.filter(
				'filterPersonnelByGroup',
				function(OpsBoardRepository) {
					return function(list, group) {

						var results = {};
						var isBoro = OpsBoardRepository.isBoroBoard();
						var location = OpsBoardRepository.getBoardLocation();

						angular
								.forEach(
										list,
										function(value, key) {
											if (Array.isArray(group)) {
												for (var i = 0; i < group.length; i++) {
													if (value
															.hasOwnProperty('civilServiceTitle')
															&& value.civilServiceTitle
																	.toUpperCase() === group[i]
																	.toUpperCase()
															&& (!isBoro || (isBoro && value.currentLocation === location))) {
														results[key] = value;
														break;
													}
												}
											} else if (value
													.hasOwnProperty('civilServiceTitle')
													&& value.civilServiceTitle
															.toUpperCase() === group
															.toUpperCase()
													&& (!isBoro || (isBoro && value.currentLocation === location))) {
												results[key] = value;
											}
										});

						return results;
					}
				})
		 .filter(
						'availPersonnelFilter',
						function(OpsBoardRepository, states) {
							return function(persons) {
								var results = [];

								angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& value.state === states.personnel.available) {
												if (!value.assigned
														&& (!value.dayBeforeShifts
																|| !value.dayBeforeShifts.length || value.availableNextDay))
													results[key] = value;
											}
										});

								return _.uniq(results);
							}
						})
		.filter('chartTagged', function ($filter) {
							return function (persons, tagType) {
								var results = {results: [], show: false, total: 0},
								resultsArray = [];

								angular.forEach(persons, function(value, pId) {
									if (value[tagType]) {
										resultsArray.push(persons[pId])
									}
								})

								resultsArray = _.uniq(resultsArray);
								results.results = resultsArray;
								results.show = !!resultsArray.length;
								results.total = resultsArray.length;
								return results;
							}
						})
		.filter('chartFilter', function ($filter, OpsBoardRepository) {
							return function (persons, chartDate) {
								var results = {results: [], show: false, total: 0},
								isBoro = OpsBoardRepository.isBoroBoard(),
								location = OpsBoardRepository.getBoardLocation(),
								resultsArray = [];

								angular.forEach(persons, function (value, pId) {
									if ((!isBoro || (isBoro && value.currentLocation === location))
											&& (value.homeLocation === location)
											|| (!(value.homeLocation === location) && value.activeDetachment && value.activeDetachment.endDate && moment(chartDate).diff(moment(value.activeDetachment.endDate).startOf('day'), 'days') <= 0)
											|| (value.activeDetachment && value.activeDetachment.endDate == null)){
										angular.forEach(value.unavailabilityHistory, function (reason, key) {
											if (reason.code === 'CHART' && (reason.action === 'A' || reason.action === 'R') && reason.status === 'A') {
												if (moment(chartDate).diff(moment(reason.start).startOf('day'), 'days') == 0) {
													resultsArray.push(persons[pId]);
												}
											}
										})
									}
								})

								resultsArray = _.uniq(resultsArray);
								results.results = resultsArray;
								results.show = !!resultsArray.length;
								results.total = resultsArray.length;
								return results;
							}
						})
		.filter('chartCancelledFilter', function ($filter, OpsBoardRepository) {
							return function (persons, chartDate) {
								var results = {results: [], show: false, total: 0},
								isBoro = OpsBoardRepository.isBoroBoard(),
								location = OpsBoardRepository.getBoardLocation(),
								resultsArray = [];

								angular.forEach(persons, function (value, pId) {
									if ((!isBoro || (isBoro && value.currentLocation === location)) 	
										&& (value.homeLocation === location)
										|| (!(value.homeLocation === location) && value.activeDetachment && value.activeDetachment.endDate && moment(chartDate).diff(moment(value.activeDetachment.endDate).startOf('day'), 'days') <= 0)
										|| (value.activeDetachment && value.activeDetachment.endDate == null)){
										angular.forEach(value.unavailabilityHistory, function (reason, key) {
											if (reason.code === 'CHART' && reason.action === 'C' && reason.status === 'A') {
												if (moment(chartDate).diff(moment(reason.start).startOf('day'), 'days') == 0) {
													resultsArray.push(persons[pId]);
												}
											}
										})
									}
								})

								resultsArray = _.uniq(resultsArray);
								results.results = resultsArray;
								results.show = !!resultsArray.length;
								results.total = resultsArray.length;
								return results;
							}
						})		
		.filter(
				'personnelCodeFilter',
				function($filter) {
					return function(persons, filterType, mdaFiltered,
							realBoardDate, code) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var unavailablePersonnelSorted = $filter(
								'unavailablePersonnelFilter')(persons,
								filterType);
						unavailablePersonnelSorted = unavailablePersonnelSorted.results;
						if (unavailablePersonnelSorted
								&& unavailablePersonnelSorted.length > 0) {
							var resultsArray = [];
							angular
									.forEach(
											unavailablePersonnelSorted,
											function(value, key) {
												var isMultipleActive = false;
												var activeCount = 0;
												var now = moment();
												angular
														.forEach(
																unavailablePersonnelSorted[key].activeUnavailabilityReasons,
																function(value,
																		key) {
																	var diff = moment(
																			realBoardDate)
																			.diff(
																					moment(
																							now)
																							.startOf(
																									'day'),
																					'days');
																	switch (true) {
																	// future
																	case (diff > 0):
																		if (value.end == null
																				|| (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0)) {
																			activeCount++;
																			value.isActive = true;
																		} else {
																			value.isActive = false;
																		}
																		break;
																	// past
																	case (diff < 0):
																		if (moment(
																				value.start)
																				.startOf(
																						'day')
																				.diff(
																						moment(
																								realBoardDate)
																								.startOf(
																										'day'),
																						'days') <= 0
																				&& (value.end == null || (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0))) {
																			activeCount++;
																			value.isActive = true;
																		} else {
																			value.isActive = false;
																		}
																		break;
																	// today
																	case (diff == 0):
																		if (moment(value.start) <= now
																				&& (value.end == null || now < moment(value.end))) {
																			activeCount++;
																			value.isActive = true;
																		} else {
																			value.isActive = false;
																		}
																		break;
																	}
																});
												if (activeCount >= 2)
													isMultipleActive = true;
												if (value.activeUnavailabilityReasons.length === 1) {
													if (value.activeUnavailabilityReasons[0]
															.hasOwnProperty('code')) {
														if (value.activeUnavailabilityReasons[0].code === code) {
															// mda takes
															// precedence over
															// unavailability
															if (mdaFiltered.results
																	.indexOf(value) === -1) {
																resultsArray
																		.push(value);
															}
														}
													}
												} else if (value.activeUnavailabilityReasons.length > 1
														&& !isMultipleActive) {
													for (var i = 0; i < value.activeUnavailabilityReasons.length; i++) {
														if (value.activeUnavailabilityReasons[i]
																.hasOwnProperty('code')
																&& value.activeUnavailabilityReasons[i].isActive) {
															if (value.activeUnavailabilityReasons[i].code === code) {
																// mda takes
																// precedence
																// over
																// unavailability
																if (mdaFiltered.results
																		.indexOf(value) === -1) {
																	resultsArray
																			.push(value);
																}
															}
														}
													}
												}
											});
							results.results = resultsArray;
							results.show = !!resultsArray.length;
							results.total = resultsArray.length;
						}
						return results;
					}
				})
		.filter(
				'personnelTwoCodeFilter',
				function($filter) {
					return function(persons, filterType, code1, code2,
							mdaFiltered) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var resultsArray = [];
						var unavailablePersonnelSorted = $filter(
								'unavailablePersonnelFilter')(persons,
								filterType);
						unavailablePersonnelSorted = unavailablePersonnelSorted.results;
						if (unavailablePersonnelSorted
								&& unavailablePersonnelSorted.length > 0) {
							var resultsVacationChart = [];
							angular
									.forEach(
											unavailablePersonnelSorted,
											function(valuePersonnel,
													keyPersonnel) {
												var isCodeChart = false;
												var isCodeVacation = false;
												if (valuePersonnel.activeUnavailabilityReasons.length === 2) {
													angular
															.forEach(
																	valuePersonnel.activeUnavailabilityReasons,
																	function(
																			value,
																			key) {
																		if (value
																				.hasOwnProperty('code')) {
																			if (value.code === code1) {
																				isCodeChart = true;
																			} else if (value.code === code2) {
																				isCodeVacation = true;
																			}
																		}
																	})
													if (isCodeChart
															&& isCodeVacation) {
														if (mdaFiltered.results
																.indexOf(valuePersonnel) === -1) {
															resultsArray
																	.push(valuePersonnel);
														}
													}
												}
											});

							results.results = resultsArray;
							results.show = !!resultsArray.length;
							results.total = resultsArray.length;
						}
						return results;
					}
				})
		.filter(
				'unavailablePersonnelFilter',
				function($filter, states, OpsBoardRepository) {
					return function(persons, filterType) {
						var results = {
							results : [],
							total : 0
						};
						var sortedResults = [];
						var isBoro = OpsBoardRepository.isBoroBoard();
						var location = OpsBoardRepository.getBoardLocation();
						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.unavailable || value.state === states.personnel.partiallyAvailable)
													&& (!isBoro || (isBoro && value.currentLocation === location))) {
												sortedResults.push(value);
											}
										});

						sortedResults = _.uniq(sortedResults);
						sortedResults = $filter('sortPersonnelPanel')(
								sortedResults, filterType, 'Unavailable');

						results.results = sortedResults;
						results.total = sortedResults.length;

						return results;
					}
				})
		.filter(
				'assignedNextDayFilter',
				function($filter, states) {
					return function(persons, filterType) {
						var results = {
							results : [],
							total : 0
						};
						var sortedResults = [];
						angular.forEach(persons, function(value, key) {
							if (value.hasOwnProperty('assignedNextDayShifts')
									&& value.assignedNextDayShifts
									&& value.assignedNextDayShifts.length
									&& !value.dayBeforeShifts.length > 0) {
								sortedResults.push(value);
							}
						});

						results.results = sortedResults;
						results.total = sortedResults.length;

						return results;
					}
				})
		.filter(
				'dayBeforeShiftsFilter',
				function($filter, states) {
					return function(persons, filterType) {
						var results = {
							results : [],
							total : 0
						};
						var sortedResults = [];
						angular.forEach(persons, function(value, key) {
							if (value.hasOwnProperty('dayBeforeShifts')
									&& value.dayBeforeShifts.length > 0
									&& value.availableNextDay === false) {
								sortedResults.push(value);
							}
						});

						results.results = sortedResults;
						results.total = sortedResults.length;

						return results;
					}
				})
		.filter(
				'hiddenPersonnelFilter',
				function($filter, states, OpsBoardRepository) {
					var isBoro = OpsBoardRepository.isBoroBoard();
					var location = OpsBoardRepository.getBoardLocation();
					return function(persons, filterType) {
						var results = {
							results : [],
							total : 0
						};
						var sortedResults = [];
						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.hidden)
													&& (!isBoro || (isBoro && value.currentLocation === location))) {
												sortedResults.push(value);
											}
										});

						results.results = sortedResults;
						results.total = sortedResults.length;

						return results;
					}
				})
		.filter(
				'unavailablePersonnelDistrictFilter',
				function($filter, states, OpsBoardRepository) {
					return function(persons, filterType, code) {
						var results = {
							results : [],
							total : 0
						};
						var sortedResults = [];
						var isBoro = OpsBoardRepository.isBoroBoard();
						var location = OpsBoardRepository.getBoardLocation();

						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.unavailable || value.state === states.personnel.partiallyAvailable)
													&& (!isBoro || (isBoro && value.currentLocation !== location))) {
												sortedResults[key] = value;
											}
										});

						sortedResults = _.uniq(sortedResults);
						sortedResults = $filter('sortPersonnelPanel')(
								sortedResults, filterType, 'Unavailable');
						results.results = sortedResults;
						results.total = sortedResults.length;

						return results;
					}
				})
		.filter(
				'availableFilter',
				function(states, $filter) {
					return function(persons, filterType) {
						var results = [];

						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.unavailable || value.state === states.personnel.partiallyAvailable)) {
												results[key] = value;
											}
										});

						var results = _.uniq(results);

						if (!results || results.length == 0) {
							return results;
						}

						var sortedResults = $filter('sortPersonnelPanel')(
								results, filterType, 'Available');

						return sortedResults;
					}
				})

		.filter(
				'availableFilter',
				function(states, $filter) {
					return function(persons, filterType) {
						var results = [];

						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.available)) {
												if (!value.assigned
														&& (!value.dayBeforeShifts
																|| !value.dayBeforeShifts.length || value.availableNextDay))
													results[key] = value;
											}
										});

						return _.uniq(results);
					}
				})

		.filter(
				'unavailableFilter',
				function(states, $filter) {
					return function(persons, filterType) {
						var results = [];

						angular
								.forEach(
										persons,
										function(value, key) {
											if (value.hasOwnProperty('state')
													&& (value.state === states.personnel.unavailable || value.state === states.personnel.partiallyAvailable)) {
												results[key] = value;
											}
										});

						var results = _.uniq(results);

						if (!results || results.length == 0) {
							return results;
						}

						var sortedResults = $filter('sortPersonnelPanel')(
								results, filterType, 'Unavailable');

						return sortedResults;
					}
				})
		.filter(
				'unavailableCodeDistrictFilter',
				function(states, $filter, groups) {
					return function(persons, code) {
						var resultsChart = [];
						var isCodeChart = false;
						var isCodeSick = false;
						var isMultiple = false;
						var hasLodi = false;
						var hasChart = false;
						var codestring = '';
						var pushperson = false;
						var pushcode = '';
						var unavailableCodes = [ 'CHART', 'XWP', 'XWOP',
								'SICK', 'VACATION', 'LODI', 'APP', 'AWOL',
								'DIF', 'SUSPENDED', 'TERMINAL LEAVE',
								'JURY DUTY', 'Mil Dty w/Pay',
								'Mil Dty w/o Pay', 'HONOR GUARD', 'FMLA',
								'MATERNITY LEAVE' ];
						var results = {
							results : [],
							total : 0
						};

						angular
								.forEach(
										persons,
										function(value, key) {
											isCodeChart = false;
											isCodeSick = false;
											isMultiple = false;
											hasLodi = false;
											hasChart = false;
											codestring = '';

											// only supervisors appear in
											// unavailable groups

											if(value.state !=='Hidden') {
												if (value.civilServiceTitle === groups.personnel.supervisors
														|| value.civilServiceTitle == groups.personnel.superintendents
														|| groups.personnel.chiefs
																.indexOf(value.civilServiceTitle) != -1) {
													if (value.activeUnavailabilityReasons.length === 1) {
														if (value.activeUnavailabilityReasons[0]
																		.hasOwnProperty('code')) {
															if (value.activeUnavailabilityReasons[0].code === code) {
																value = $filter(
																		'extendPerson')
																(value);
																resultsChart
																		.push(value);
															}
														}
													} else if (value.activeUnavailabilityReasons.length > 1) {
														pushperson = false;
														pushcode = '';

														angular
																.forEach(
																value.activeUnavailabilityReasons,
																function (value2,
																					key) {
																	_
																			.each(
																			unavailableCodes,
																			function (ucode) {
																				if (ucode === value2.code) {
																					codestring = codestring
																							+ ucode;
																				}
																			});

																	if (value2
																					.hasOwnProperty('code')) {
																		if (value2.code === code) {
																			pushperson = true;
																		}
																	}
																});

														if ((pushperson === true && codestring === code)
																|| (code === 'MULTIPLE'
																&& codestring !== 'SICKCHART' && codestring !== 'VACATIONCHART')
																|| (code === 'SICK' && codestring === 'SICKCHART')
																|| (code === 'VACATION/CHART' && codestring === 'VACATIONCHART')) {

															value = $filter(
																	'extendPerson')
															(value);
															resultsChart
																	.push(value);
														}
													}
												}
											}
										});

						results.results = resultsChart;
						results.total = resultsChart.length;
						return results;
					}
				})
		.filter(
				'sickChartFilter',
				function($filter) {
					return function(persons, filterType, mdaFiltered,
							realBoardDate) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var resultsArray = [];
						var unavailablePersonnelSorted = $filter(
								'unavailablePersonnelFilter')(persons,
								filterType).results;
						if (unavailablePersonnelSorted
								&& unavailablePersonnelSorted.length > 0) {
							var resultsSickChart = [];
							angular
									.forEach(
											unavailablePersonnelSorted,
											function(valuePersonnel) {
												var isCodeChart = false;
												var isCodeSick = false;
												var now = moment();
												var isMultipleActive = false;
												var currentlyActive = [];
												angular
														.forEach(
																valuePersonnel.activeUnavailabilityReasons,
																function(value,
																		key) {
																	var diff = moment(
																			realBoardDate)
																			.diff(
																					moment(
																							now)
																							.startOf(
																									'day'),
																					'days');
																	switch (true) {
																	// future
																	case (diff > 0):
																		if (value.end == null
																				|| (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0)) {
																			currentlyActive
																					.push(value);
																		}
																		break;
																	// past
																	case (diff < 0):
																		if (moment(
																				value.start)
																				.startOf(
																						'day')
																				.diff(
																						moment(
																								realBoardDate)
																								.startOf(
																										'day'),
																						'days') <= 0
																				&& (value.end == null || (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0))) {
																			currentlyActive
																					.push(value);
																		}
																		break;
																	// today
																	case (diff == 0):
																		if (moment(value.start) <= now
																				&& (value.end == null || now < moment(value.end))) {
																			currentlyActive
																					.push(value);
																		}
																		break;
																	}
																});

												if (currentlyActive.length === 1) {
													if (currentlyActive[0]
															.hasOwnProperty('code')) {
														if (currentlyActive[0].code === "SICK") {
															if (mdaFiltered.results
																	.indexOf(valuePersonnel) === -1) {
																resultsArray
																		.push(valuePersonnel);
															}
														}
													}
												} else if (currentlyActive.length === 2) {
													angular
															.forEach(
																	currentlyActive,
																	function(
																			value,
																			key) {
																		if (value
																				.hasOwnProperty('code')) {
																			if (value.code === "CHART") {
																				isCodeChart = true;
																			} else if (value.code === "SICK") {
																				isCodeSick = true;
																			}
																		}
																	});
													if (isCodeChart
															&& isCodeSick) {
														if (mdaFiltered.results
																.indexOf(valuePersonnel) === -1) {
															resultsArray
																	.push(valuePersonnel);
														}
													}
												}
											});

							if (resultsArray.length > 0) {
								results.show = true;
							}
						}
						results.results = resultsArray;
						results.total = resultsArray.length;
						return results;

					}
				})
		.filter(
				'multipleFilter',
				function($filter) {
					return function(persons, filterType, realBoardDate) {

						var results = {
							results : [],
							show : false,
							total : 0
						};
						var resultsArray = [];

						var unavailablePersonnelSorted = $filter(
								'unavailablePersonnelFilter')(persons,
								filterType);

						if (unavailablePersonnelSorted
								&& unavailablePersonnelSorted.total > 0) {
							angular
									.forEach(
											unavailablePersonnelSorted.results,
											function(valuePersonnel) {
												var isCodeChart = false;
												var isCodeSickOrVacation = false;
												var now = moment();
												var isMultipleActive = false;
												var activeCount = 0;
												angular
														.forEach(
																valuePersonnel.activeUnavailabilityReasons,
																function(value,
																		key) {
																	var diff = moment(
																			realBoardDate)
																			.diff(
																					moment(
																							now)
																							.startOf(
																									'day'),
																					'days');
																	switch (true) {
																	// future
																	case (diff > 0):
																		if (value.end == null
																				|| (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0)) {
																			activeCount++;
																		}
																		break;
																	// past
																	case (diff < 0):
																		if (moment(
																				value.start)
																				.startOf(
																						'day')
																				.diff(
																						moment(
																								realBoardDate)
																								.startOf(
																										'day'),
																						'days') <= 0
																				&& (value.end == null || (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0))) {
																			activeCount++;
																		}
																		break;
																	// today
																	case (diff == 0):
																		if (moment(value.start) <= now
																				&& (value.end == null || now < moment(value.end))) {
																			activeCount++;
																		}
																		break;
																	}
																});
												if (activeCount >= 2)
													isMultipleActive = true;
												if (isMultipleActive
														&& valuePersonnel.activeUnavailabilityReasons.length === 2) {
													angular
															.forEach(
																	valuePersonnel.activeUnavailabilityReasons,
																	function(
																			value,
																			key) {
																		if (value
																				.hasOwnProperty('code')) {
																			if (value.code === "CHART") {
																				isCodeChart = true;
																			} else if (value.code === "VACATION"
																					|| value.code === "SICK") {
																				isCodeSickOrVacation = true;
																			}
																		}
																	});
													if ((isCodeChart && !isCodeSickOrVacation)
															|| !isCodeChart) {
														resultsArray
																.push(valuePersonnel);
													}
												} else if (isMultipleActive
														&& valuePersonnel.activeUnavailabilityReasons.length > 2) {
													resultsArray
															.push(valuePersonnel);
												}
											});

							if (resultsArray.length > 0) {
								results.show = true;
							}
						}

						results.results = resultsArray;
						results.total = resultsArray.length;
						return results;
					}
				})
		.filter(
				'mdaPersonnelFilter',
				function($filter) {
					return function(persons, filterType, realBoardDate,
							countAssgined) {
						var results = {};
						var unavailablePersonnelSorted = $filter(
								'unavailableFilter')(persons, filterType);
						var resultsArray = [];
						var reject = false;
						if (unavailablePersonnelSorted
								&& unavailablePersonnelSorted.length > 0) {
							angular
									.forEach(
											unavailablePersonnelSorted,
											function(valuePersonnel,
													keyPersonnel) {
												if (valuePersonnel.mdaStatusHistory.length) {
													var activeUr = false;
													var now = moment();
													valuePersonnel.activeUnavailabilityReasons
															.forEach(function(
																	value) {
																if (value.status == "A") {
																	var diff = moment(
																			realBoardDate)
																			.diff(
																					moment(
																							now)
																							.startOf(
																									'day'),
																					'days');
																	switch (true) {
																	// future
																	case (diff > 0):
																		if (value.end == null
																				|| (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0)) {
																			activeUr = true
																		}
																		break;
																	// past
																	case (diff < 0):
																		if (moment(
																				value.start)
																				.startOf(
																						'day')
																				.diff(
																						moment(
																								realBoardDate)
																								.startOf(
																										'day'),
																						'days') <= 0
																				&& (value.end == null || (moment(
																						realBoardDate)
																						.startOf(
																								'day')
																						.diff(
																								moment(
																										value.end)
																										.startOf(
																												'day'),
																								'days') <= 0))) {
																			activeUr = true
																		}
																		break;
																	// today
																	case (diff == 0):
																		if (moment(value.start) <= now
																				&& (value.end == null || now < moment(value.end))) {
																			activeUr = true
																		}
																		break;
																	}
																}
															});

													// added addition
													// requirement if item is
													// detached or assigned, it
													// should not show up in the
													// MDA section
													if (!activeUr
															&& (valuePersonnel
																	.hasOwnProperty('state') && valuePersonnel.state !== 'Detached')) {
														if (countAssgined) {
															if (valuePersonnel.assigned !== true
																	&& (!valuePersonnel.dayBeforeShifts
																			|| !valuePersonnel.dayBeforeShifts.length || valuePersonnel.availableNextDay))
																resultsArray
																		.push(valuePersonnel);
														} else
															resultsArray
																	.push(valuePersonnel);
													}
												}
											});
						}

						results.results = resultsArray;
						results.total = resultsArray.length;

						return results;
					}
				})
		.filter(
				'detachedPersonnelFilter',
				function($filter, states) {
					return function(persons, filterType) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var resultsArray = [];
						angular
								.forEach(
										persons,
										function(value, key) {
											var personnelHomeLocation = value.homeLocation.code;
											if (value.hasOwnProperty('state')
													&& value.state === states.personnel.detached) {
												if (value.detachmentHistory
														&& value.detachmentHistory.length) {
													angular
															.forEach(
																	value.detachmentHistory,
																	function(
																			detachmentHistory,
																			keyPersonnel) {
																		if (detachmentHistory.from.code === personnelHomeLocation) {
																			resultsArray
																					.push(value);
																		}

																	});
												}
											}
										});

						var resultsArray = _.uniq(resultsArray);
						var resultsArray = $filter('sortPersonnelPanel')(
								resultsArray, filterType, 'Available');
						results.results = resultsArray;
						results.total = resultsArray.length;
						return results;
					}
				})
		.filter(
				'chiefsDetachedFilter',
				function($filter) {
					return function(persons, filterType) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var detachedPersonnelSorted = $filter(
								'detachedPersonnelFilter')(persons, filterType);
						detachedPersonnelSorted.results.forEach(function(
								valuePersonnel, keyPersonnel) {
							if (/GS.(I{2,}|.V|V)/gi
									.test(valuePersonnel.civilServiceTitle)) {
								results.results.push(valuePersonnel);
							}
						});
						results.show = !!results.results.length;
						results.total = results.results.length;
						return results;
					}
				})
		.filter(
				'codeDetachedFilter',
				function($filter) {
					return function(persons, filterType, code) {
						var results = {
							results : [],
							show : false,
							total : 0
						};
						var resultsArray = [];
						var detachedPersonnelSorted = $filter(
								'detachedPersonnelFilter')(persons, filterType).results;

						if (detachedPersonnelSorted
								&& detachedPersonnelSorted.length > 0) {
							angular.forEach(detachedPersonnelSorted, function(
									valuePersonnel, keyPersonnel) {
								if (valuePersonnel.civilServiceTitle
										.toUpperCase() === code) {
									resultsArray.push(valuePersonnel);
								}
							});

							if (resultsArray.length > 0) {
								results.show = true;

							}
						}

						results.results = resultsArray;
						results.total = resultsArray.length;

						return results;
					}
				})
		.filter(
				'getPersonnelFromTask',
				function(OpsBoardRepository) {
					return function(task, shiftId, assigned) {
						var tmp = [];
						var persons = OpsBoardRepository.getPersonnel();

						// hard-coded operations for person 1 and 2
						for (var i = 0, len = 2; i < len; i++) {

							var currentPerson = task['assignedPerson' + (i + 1)], person = persons[currentPerson.personId], assignmentType = currentPerson.type, personCopy;

							if (assignmentType && assignmentType === 'NEXT_DAY')
								continue;

							if (person) {
								// check if person is detached
								if (persons[person.id].state === 'Detached')
									continue;

								// check type of assignment
								if (person.id && assignmentType
										&& assigned.length > 1) {
									// diversion or overtime gets removed from
									// the previous entry
									if (assignmentType === 'OTHER'
											|| assignmentType === 'OVERTIME') {
										assigned = assigned
												.filter(function(key) {
													return key.id !== person.id;
												});
									}
								}

								// copy the person model since duplicates are
								// needed for next day
								// todo: only copy and draw when needed (improve
								// ui render)
								personCopy = angular.copy(person);

								angular.extend(personCopy, {
									_assignmentType : assignmentType,
									_shiftId : shiftId,
									getFormattedName : person.getFormattedName
								});

								tmp.push(personCopy);

							}

						}

						return tmp;
					}
				})
		.filter(
				'getNextDayPersonnelFromTask',
				function(OpsBoardRepository) {
					return function(task, shiftId, assigned) {
						var tmp = [];
						var persons = OpsBoardRepository.getPersonnel();

						// hard-coded operations for person 1 and 2
						for (var i = 0, len = 2; i < len; i++) {

							var currentPerson = task['assignedPerson' + (i + 1)], person = persons[currentPerson.personId], assignmentType = currentPerson.type, personCopy;

							if (!assignmentType
									|| assignmentType !== 'NEXT_DAY')
								continue;

							if (person) {
								// check if person is detached
								if (persons[person.id].state === 'Detached')
									continue;

								// copy the person model since duplicates are
								// needed for next day
								// todo: only copy and draw when needed (improve
								// ui render)
								personCopy = angular.copy(person);

								angular.extend(personCopy, {
									_assignmentType : assignmentType,
									_shiftId : shiftId,
									getFormattedName : person.getFormattedName
								});

								tmp.push(personCopy);

							}

						}

						return tmp;
					}
				})
		.filter('formatListNo', function(OpsBoardRepository) {
			return function(person) {
				var listNoFormatted = parseInt(person.listNumber);
				return listNoFormatted;
			}
		})				
		.filter('isPersonDetachable', function(states) {
			return function(person) {
				if (person && person.state === states.personnel.available) {
					return true;
				}
				return false;
			}
		})
		.filter(
				'isDetachableSW',
				function(states, groups) {
					return function(person) {
						if (person
								&& person.civilServiceTitle === groups.personnel.sanitationWorkers
								&& person.homeLocation !== person.currentLocation) {
							return true;
						}

						return false;
					}
				})
		.filter(
				'isDetachableSup',
				function($filter, states, groups, boardtypes) {
					return function(person) {
						var payrollLocations = $filter('getPayrollLocations')(
								person);
						if (person
								&& person.civilServiceTitle === groups.personnel.supervisors
								&& person.homeLocation !== person.currentLocation
								&& payrollLocations.personLocationData.home.boardType.code !== boardtypes.boro) {
							return true;
						} else if (person
								&& person.civilServiceTitle === groups.personnel.supervisors
								&& payrollLocations.personLocationData.home.boardType.code === boardtypes.boro) {
							if (payrollLocations.home !== payrollLocations.currentAssignment) {
								return true;
							}
						}

						return false;
					}
				})
		.filter(
				'getPayrollLocations',
				function(states, groups, BoardValueService, boardtypes) {
					return function(person) {
						var personLocationData = {
							home : BoardValueService.locationRefsData[person.homeLocation],
							current : BoardValueService.locationRefsData[person.currentLocation]
						}
						var payrollLocations = {
							home : personLocationData.home.code,
							currentAssignment : personLocationData.current.code,
							personLocationData : personLocationData
						}

						if (personLocationData.home.boardType === null) {
							personLocationData.home.boardType = {
								code : boardtypes.district
							}
						}

						if (personLocationData.current.boardType === null) {
							personLocationData.current.boardType = {
								code : boardtypes.district
							}
						}

						if (personLocationData.home.boardType.code !== boardtypes.boro) {
							// just to make boro board to open in local
							payrollLocations.home = personLocationData.home.borough ? personLocationData.home.borough.code
									: personLocationData.home.code;
						}

						if (personLocationData.current.boardType.code !== boardtypes.boro) {
							// just to make boro board to open in local
							payrollLocations.currentAssignment = personLocationData.current.borough ? personLocationData.current.borough.code
									: personLocationData.current.code;
						}
						return payrollLocations;
					}
				})
		.filter(
				'isDetachableOutsidePayroll',
				function($filter, states, groups) {
					return function(person) {
						var payrollLocations = $filter('getPayrollLocations')(
								person);
						if (person
								&& (person.civilServiceTitle === groups.personnel.superintendents || person.civilServiceTitle === groups.personnel.civilians)
								&& payrollLocations.home !== payrollLocations.currentAssignment) {
							return true;
						}
						return false;
					}
				})
		.filter(
				'isAttachedToPayRollLocation',
				function($filter, states, groups, boardtypes) {
					return function(person) {
						var payrollLocations = $filter('getPayrollLocations')(
								person);
						var boardType = "";
						if (payrollLocations.personLocationData.home.boardType !== null) {
							boardType = payrollLocations.personLocationData.home.boardType.code;
						}
						if (person
								&& person.civilServiceTitle === groups.personnel.sanitationWorkers
								&& (boardType === boardtypes.boro
										|| boardType === boardtypes.broomDepot
										|| boardType === boardtypes.lotCleaning || boardType === boardtypes.splinter)
								&& payrollLocations.personLocationData.home.code === payrollLocations.personLocationData.current.code) {
							return true;
						}
						return false;
					}
				})
		.filter(
				'processPersonState',
				function($filter, PersonModel, PersonnelHelperService,
						BoardValueService, states) {
					return function(value) {
						var now = new Date();
						PersonnelHelperService
								.processPartialAvailabilityAndMda(value, now,
										states);
					}
				})
		.filter(
				'extendPerson',
				function($filter, PersonModel, PersonnelHelperService,
						BoardValueService, states) {
					return function(value) {
						var now = new Date();
						angular.extend(value, PersonModel);
						value.titleMap = $filter('getMappedTitle')(
								value.civilServiceTitle);
						value.titleMap2 = $filter('getMappedTitle')(
								value.civilServiceTitle);
						PersonnelHelperService
								.processPartialAvailabilityAndMda(value, now,
										states);
						value.formattedName = value.getFormattedName();
						value.indicatorBox = value.getIndicatorBox();
						value.indicatorText = value.getIndicatorText();
						value.activeMDA = value.isActiveMDA();

						value.partiallyAvailable = value.isPartiallyAvailable();
						value.partialMdaAvailability = value
								.isPartialMdaAvailability();
						value.probationEndDate = value.getProbationEndDate();

						value.address1 = value.getHomeAddress('address1');
						value.city = value.getHomeAddress('city');
						value.homePhone = value.getPhone('home');
						value.mobilePhone = value.getPhone('cell');
						value.homeAddressState = value.getHomeAddress('state');
						value.homeAddressPostalCode = value
								.getHomeAddress('postalCode');
						value.homeAddressCounty = value
								.getHomeAddress('county');

						value.personDetachable = $filter('isPersonDetachable')(
								value);
						value.listNoFormatted = $filter('formatListNo')(value);
						value.probationEndDate = value.getProbationEndDate();
						value.detachedSW = $filter('isDetachableSW')(value);
						value.detachedSup = $filter('isDetachableSup')(value);
						value.personPayrollLocations = $filter(
								'getPayrollLocations')(value);
						value.detachedOutSidePayroll = $filter(
								'isDetachableOutsidePayroll')(value);
						value.attachedToPayrollLocation = $filter(
								'isAttachedToPayRollLocation')(value);

						value.showPaginationLinksForDetachments = value.detachmentCount > 0;
						value.showPaginationLinksForGrounding = value.groundingStatusCount > 0;
						value.showPaginationLinksForMda = value.mdaStatusCount > 0;
						value.showPaginationLinksForSpecialPositions = value.specialPositionCount > 0;
						value.showPaginationLinksForUnavailable = value.unavailabilityReasonCount > 0;
						return value;
					}
				})
		.filter('getMappedTitle', function(titles) {
			return function(title) {
				var tMap = ''
				if (!title)
					return '';
				Object.keys(titles).forEach(function(key) {
					var val = titles[key];
					if (title.toUpperCase() === val.toUpperCase())
						tMap = key;
				});
				return tMap;
			}
		})
		.filter(
				'sortCivilServiceTitle',
				function($filter, OpsBoardRepository, titleHierarchy, titles) {
					return function(list) {
						var sortedList = list
								.sort(function(me, that) {
									if (titleHierarchy
											.indexOf(me.civilServiceTitle
													.toUpperCase()) > titleHierarchy
											.indexOf(that.civilServiceTitle
													.toUpperCase()))
										return 1;
									if (titleHierarchy
											.indexOf(me.civilServiceTitle
													.toUpperCase()) < titleHierarchy
											.indexOf(that.civilServiceTitle
													.toUpperCase()))
										return -1;
									if (me.civilServiceTitle.toUpperCase() === titles.civilian
											&& that.civilServiceTitle
													.toUpperCase() === titles.civilian) {
										if (me.seniorityDate > that.seniorityDate)
											return 1;
										if (me.seniorityDate < that.seniorityDate)
											return -1;
									}
								});
						return sortedList;
					}
				});
