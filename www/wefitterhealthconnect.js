module.exports = {
  configure: function (config, successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      "WeFitterHealthConnect",
      "configure",
      [config]
    );
  },
  connect: function (successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      "WeFitterHealthConnect",
      "connect",
      []
    );
  },
  disconnect: function (successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      "WeFitterHealthConnect",
      "disconnect",
      []
    );
  },
  isConnected: function (successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      "WeFitterHealthConnect",
      "isConnected",
      []
    );
  },
  isSupported: function (successCallback, errorCallback) {
    cordova.exec(
      successCallback,
      errorCallback,
      "WeFitterHealthConnect",
      "isSupported",
      []
    );
  },
};
