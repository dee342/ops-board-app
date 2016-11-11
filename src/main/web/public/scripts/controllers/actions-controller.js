(function () {

    'use strict';

    angular.module('OpsBoard')
        .controller('ActionPanelCtrl', ['$scope', '$timeout', '$window', 'OpsBoardRepository', 'serverConfig', 'BoardValueService',
            function ($scope, $timeout, $window, OpsBoardRepository, serverConfig, BoardValueService) {

                var recentActivityExport = (function () {

                    var winUrl = $window.URL || $window.webkitURL || {};
                    var styleSheet = buildStyleSheet();
                    var exportData = {
                        mimeType: 'text/plain',
                        fileName: [BoardValueService.boardData.boardLocation, BoardValueService.boardData.boardDate].join('-') + '.html'
                    };

                    function buildStyleSheet() {
                        var rawCss = '';
                        var style = ['<style type="text/css">'];
                        // get stylesheet by <link> class
                        var cssRules = _.toArray(document.styleSheets).filter(function (val) {
                            return val.ownerNode.classList.contains('recent-activity');
                        });
                        // concat all of the rules as raw text
                        _.toArray(cssRules[0].rules).forEach(function (val) {
                            rawCss += val.cssText;
                        });
                        style.push(rawCss, '</style>');
                        return style.join('');
                    }

                    function buildTemplateHTML() {
                        var html = ['<body class="recent-activity-export">', styleSheet];
                        html.push('<div class="html-output">');
                        html.push(document.querySelector('div.recent-activity').innerHTML);
                        html.push('</div>');
                        html.push('<div class="client-info">');
                        html.push($window.navigator.userAgent + '<br>');
                        html.push(serverConfig.appVersion + '<br>');
                        html.push(serverConfig.environment);
                        html.push('</body></div>');
                        return html.join('');
                    }

                    function preloadExport() {
                        var blob = new Blob([buildTemplateHTML()], { type: exportData.mimeType });
                        exportData.href = winUrl.createObjectURL(blob);
                        $scope.export.href = exportData.href;
                    }

                    return {
                        href: '',
                        fileName: exportData.fileName,
                        preloadExport: preloadExport
                    };

                }());

                $scope.export = recentActivityExport;

                $scope.toggleRecentActivityPane = function() {
                    $scope.panes.recentActivity.visible = !$scope.panes.recentActivity.visible;
                    $timeout(
                        function() {
                            $scope.panes.recentActivity.active = !$scope.panes.recentActivity.active;
                        }, 100);
                };

            }
        ]);

}());