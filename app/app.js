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

var app = angular.module('myApp', ['angular-loading-bar', 'ngAnimate']);
setTimeout(function () {
    if (!localStorage.getItem('user')) {
        editUserName();
    } else {
        refreshUserName();
    }
}, 500);

function closeModal() {
    localStorage.setItem('user', $("#user-name").val());
    refreshUserName();

    $('#exampleModal').modal('hide');
}

app.factory('apiService', function ($http) {
    var getLastVersions = function () {
        return new Promise(function (res, rej) {
            $http.get('http://strumyk-next-build:3030/jira-versions/versions').then(function (data) {
                res(data.data.filter(word => word.projectId == 10000 && word.released).map(e => {
                    e.name = e.name.replace('1.0.', '');
                    return e;
                }).slice(0, 10));
            });
        });
    };
    var getVersions = function () {
        return new Promise(function (res, rej) {
            $http.get('http://localhost:8080/vp/version/list').then(function successCallback(response) {
                res(response.data);
            });
        });
    };
    var addVersion = function (name) {
        return new Promise(function (res, rej) {
            $http.post('http://localhost:8080/vp/version', {name: name}).then(res);
        });
    };
    var getVotes = function () {
        return new Promise(function (res, rej) {
            $http.get('http://localhost:8080/vp/vote/list').then(function successCallback(response) {
                res(response.data);
            });
        });
    };
    var releaseVersion = function (version) {
        return new Promise(function (res, rej) {
            $http.post('http://localhost:8080/vp/version/releaseVersion', {version: version}).then(res);
        });
    };
    var releaseAll = function () {
        return new Promise(function (res, rej) {
            $http.post('http://localhost:8080/vp/version/releaseAll').then(res);
        });
    };
    var addVote = function (version, login) {
        return new Promise(function (res, rej) {
            $http.post('http://localhost:8080/vp/vote', {version: version, login: login}).then(res);
        });
    };
    var deleteVote = function (version, login) {
        return new Promise(function (res, rej) {
            $http.post('http://localhost:8080/vp/vote/delete', {version: version, login: login}).then(res);
        });
    };
    return {getLastVersions, getVotes, getVersions, releaseVersion, releaseAll, addVote, deleteVote, addVersion};
});

app.component('version', {
    templateUrl: '/version.html',
    controller: function VersionController($scope) {
    }, bindings: {
        role: '@',
        data: '=',
        user: '=',
        add: '<',
        remove: '<',
        release: '<'
    }
});

app.filter('humanTime', function () {
    return function (input) {
        return moment.duration(moment(input).diff(moment())).humanize();
    };
});

app.component('history', {
    templateUrl: '/history.html',
    controller: function HistoryController($http, $scope, apiService) {
        this.$onInit = function () {
            apiService.getLastVersions().then(function (res) {
                $scope.history = res;
            });
        };
    }
});
app.component('current', {
    templateUrl: '/current.html',
    controller: function CurrentController($http, $scope, apiService) {
        this.$onInit = function () {
            apiService.getVersions().then(function (res) {
                $scope.current = res.filter((e) => e.state === 'RUNNING');
            });
        };
    }
});
app.controller('versionManager', function ($http, $scope, $timeout, apiService) {
    var self = this;
    $scope.$watch('user', function () {
        if ($scope.user) {
            var all = [].concat(self.avaliable).concat(self.planned);
            for (let o of all) {
                self.scoreChange(o);
            }
        }
    });
    this.$onInit = function () {
        $scope.user = {"name": localStorage.getItem('user')};
        this.reloadVotes();
    };
    this.reloadVotes = function () {
        self.planned = [];
        self.avaliable = [];
        apiService.getVersions().then(function (data) {
            self.avaliable = data.map(el => {
                el.watchers = [];
                return el;
            });
        }).then(function () {
            apiService.getVotes().then(function successCallback(response) {
                for (let d of response) {
                    let find = _.find([].concat(self.avaliable).concat(self.planned), (o) => o.name == d.version);
                    if (find) {
                        self.addWatcher(find, d.login)
                    }
                }
            }, function errorCallback(response) {
            });
        });
    };

    this.avaliable = [];
    this.planned = [];
    this.releaseVersion = function (version) {
        confirm().then(function () {
            apiService.releaseVersion(version).then(self.reloadVotes);
        })
    };
    this.releaseAll = function () {
        confirm().then(function () {
            apiService.releaseAll().then(self.reloadVotes);
        })
    };
    this.addWatcher = function (version, userName, voteType) {
        let user = userName != null ? userName : $scope.user.name;
        if (_.findIndex(version.watchers, (o) => o.user.name == user) == -1) {

            version.watchers.push({user: {name: user}, date: new Date()});
            if (version.watchers.length == 1) {
                _.remove(self.avaliable, {
                    name: version.name
                });
                self.planned.push(version);
            }
        }
        self.scoreChange(version, voteType);
    };
    this.removeWatcher = function (version, userName, voteType) {
        let user = userName != null ? userName : $scope.user.name;
        version.watchers.splice(_.findIndex(version.watchers, (o) => o.name == user), 1);
        if (version.watchers.length == 0) {
            _.remove(self.planned, {
                name: version.name
            });
            self.avaliable.push(version);
        }
        self.scoreChange(version, voteType);
    };
    this.scoreChange = function (version, voteType) {
        let vote = _.find(version.watchers, (o) => o.user.name == $scope.user.name);
        version.voted = vote != undefined;
        if (voteType === 'up') {
            apiService.addVote(version.name, $scope.user.name);
        } else if (voteType === 'down') {
            apiService.deleteVote(version.name, $scope.user.name);
        }

    };

    this.addVersion = function () {
        var self = this;
        Swal.fire({
            title: 'Version name',
            input: 'text',
            inputValue: 175,
            showCancelButton: true
        }).then(function (data) {
            if (data.value) {
                apiService.addVersion(data.value).then(self.reloadVotes)
            }
        })
    }
});


app.directive('avaliable', function () {
    return {restrict: 'E', templateUrl: '/avaliable.html', replace: true};
});
app.directive('planned', function () {
    return {restrict: 'E', templateUrl: '/planned.html', replace: true};
});

function confirm() {
    return new Promise(function (res, rej) {
        Swal.fire({
            title: 'Are you sure?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, release it!'
        }).then((result) => {
            if (result.value) {
                res();
            } else {
                rej();
            }
        })
    })
}
