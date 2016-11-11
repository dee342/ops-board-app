(function () {
	
	angular
		.module('model-on-blur', [])
		.directive('modelOnBlur', [changeInputOnBlur]);

	function changeInputOnBlur() {
		return {
			restrict: 'A',
			require: 'ngModel',
			link: function(scope, element, attr, modelCtrl) {
				if (attr.type === 'radio' || attr.type === 'checkbox') return;
				element
					.off('input keydown change')
					.on('blur', function () {
						scope.$apply(function () {
							modelCtrl.$setViewValue(element.val());
						});
					});
			}
		};
	}

}());