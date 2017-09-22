#import <Cordova/CDVPlugin.h>

@interface AddressBookSync : CDVPlugin {
}

// The hooks for our plugin commands
- (void)sync:(CDVInvokedUrlCommand *)command;


@end
