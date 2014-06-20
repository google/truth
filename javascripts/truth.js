var myApp = angular.module('truthApp', []);
myApp.config(function ($interpolateProvider) {
  $interpolateProvider.startSymbol('<[');
  $interpolateProvider.endSymbol(']>');
});
myApp.factory('$date', function () {
  return Date;
});

function DateController($date) {
  $scope.now = $date;
}