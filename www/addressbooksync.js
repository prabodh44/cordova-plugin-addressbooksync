var temp = {

	sync: function(pluginParam, successCallback, errorCallback) {
		cordova.exec(
			successCallback, 		// success callback function
			errorCallback, 		// error callback function
			'AddressBookSync', 		// mapped to native via platform section in plugin.xml
			'sync', 	// with this action name passed to native function per platform
			[{                  // and this array of custom arguments to create our entry
				"message": pluginParam
			}]
		);
	},

  exit: function(pluginParam, successCallback, errorCallback) {
	  navigator.app.exitApp();
    // cordova.exec(
    //   successCallback, 		// success callback function
    //   errorCallback, 		// error callback function
    //   'AddressBookSync', 		// mapped to native via platform section in plugin.xml
    //   'exit', 	// with this action name passed to native function per platform
    //   [{                  // and this array of custom arguments to create our entry
    //     "message": pluginParam
    //   }]
    // );
  }

};

module.exports = temp;
