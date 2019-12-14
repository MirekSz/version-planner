'use strict';

function refreshUserName() {
    var appElement = document.querySelector('[id=app]');
    var $scope = angular.element(appElement).scope();
    $scope.$apply(function () {
        $scope.user.name = localStorage.getItem('user');
    });
}

function editUserName() {
    $("#user-name").val(localStorage.getItem('user'));
    $('#exampleModal').modal('toggle');
}

var app = angular.module('myApp', ['ngAnimate']);
setTimeout(function () {
    if (!localStorage.getItem('user')) {
        editUserName();
    } else {
        refreshUserName();
    }
}, 500)

function closeModal() {
    localStorage.setItem('user', $("#user-name").val());
    refreshUserName();

    $('#exampleModal').modal('hide');
}

app.component('version', {
    templateUrl: '/version.html',
    controller: function VersionController($scope) {
        this.addWatcher = function () {
            var user = this.user;
            if (_.findIndex(this.data.watchers, (o) => o.user.name == user.name) == -1) {
                this.data.watchers.push({ user: this.user, date: new Date() });
                this.callback(this.data);
            }
        };
        this.removeWatcher = function (index) {
            this.data.watchers.splice(index, 1);
            this.callback(this.data);
        };
        this.removeCurrentWatcher = function (index) {
            this.data.watchers.splice(_.findIndex(this.data.watchers, (o) => o.name == $scope.$ctrl.user.name), 1);
            this.callback(this.data);
        };
    }, bindings: {
        role: '@',
        data: '=',
        user: '=',
        callback: '<'
    }
});

var INIT = [{ name: 175, watchers: [] }, { name: 177, watchers: [] }, { name: 178, watchers: [] }, { name: 179, watchers: [] }];
app.controller('versionManager', function ($http, $scope, $timeout) {
    var self = this;
    $scope.$watch('user', function () {
        if ($scope.user) {
            var all = [].concat(self.avaliable).concat(self.planned);
            debugger
            for (let o of all) {
                o.voted = _.findIndex(o.watchers, (o) => o.user.name == self.user.name) == -1
            }
        }
    });
    this.$onInit = function () {
        $scope.user = { "name": localStorage.getItem('user') };
        $http.get('http://localhost:8080/vp/vote/list').then(function successCallback(response) {
            for (let d of response.data) {
                let find = _.find(self.avaliable, (o) => o.name == d.version);
                if (find)
                    find.watchers.push({ name: d.login, date: d.date });
            }
            self.avaliable.forEach(self.scoreChange);
        }, function errorCallback(response) {
        });
    };

    this.avaliable = [].concat(INIT);
    this.planned = [];
    this.release = function () {
        this.planned = [];
        this.avaliable = [].concat(INIT);
    };
    this.scoreChange = function (version) {
        if (version.watchers.length > 0) {
            _.remove(self.avaliable, {
                name: version.name
            });
            if (_.findIndex(self.planned, function (o) {
                return o.name == version.name;
            }) == -1)
                self.planned.push(version);
        } else {
            _.remove(self.planned, {
                name: version.name
            });
            if (_.findIndex(self.avaliable, (o) => o.name == version.name) == -1)
                self.avaliable.push(version);
        }
        version.voted = _.findIndex(version.watchers, (o) => o.user.name == $scope.user.name) == -1

    };
});
app.directive('avaliable', function () {
    return { restrict: 'E', templateUrl: '/avaliable.html', replace: true };
});
app.directive('planned', function () {
    return { restrict: 'E', templateUrl: '/planned.html', replace: true };
});
