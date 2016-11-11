(function () {

	angular
		.module('OpsBoard')
		.factory('FilterRunner', ['$rootScope', '$log', filterRunner]);

	function filterRunner($rootScope, $log) {

		return function (events) {

			this.filters = {};
			this.events = events; // todo: add support for more than one binding

			this.add = function (filter) {
				if (!filter) return;
				_.extend(this.filters, filter);
			};

			this.run = function (args) {
				// no support for multiple arguments yet
				var results = {},
					keys = Object.keys(this.filters);
				for (var i = 0, len = keys.length, func; i < len; i ++) {
					func = this.filters[keys[i]];
					if (typeof func !== 'function') break;
					// invoke filter
					results[keys[i]] = this.filters[keys[i]](args);
				}
				return results;
			};

			this.getEvents = function () {
				return this.events;
			};

			return this;
		};

	}

}());