/*
 * Calaca - Search UI for Elasticsearch
 * https://github.com/romansanchez/Calaca
 * http://romansanchez.me
 * @rooomansanchez
 * 
 * v1.2.0
 * MIT License
 */

/* Module */
window.Calaca = angular.module('calaca', ['elasticsearch', 'ngAnimate', 'ngSanitize', 'ngWindowManager', 'ngclipboard'],
    ['$locationProvider', function($locationProvider){
        $locationProvider.html5Mode(true);
    }]
);


window.Calaca.directive('ngColResizeable', function() {
  return {
    restrict: 'A',
    link: function(scope, elem) {
      setTimeout(function() {
        elem.colResizable({
          liveDrag: true,
          gripInnerHtml: "<div class='grip'></div>",
          draggingClass: "dragging",
          onDrag: function() {
            //trigger a resize event, so width dependent stuff will be updated
            $(window).trigger('resize');
          }
        });
      });
    }
  };
});