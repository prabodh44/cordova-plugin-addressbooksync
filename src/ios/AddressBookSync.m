#import "AddressBookSync.h"

#import <Cordova/CDVAvailability.h>

@implementation AddressBookSync

- (void)pluginInitialize {
}

- (void)sync:(CDVInvokedUrlCommand *)command{
    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"data" ofType:@"json"];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    NSDictionary *jsonData = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
    
    

    NSString *jsonString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"Json String %@", jsonString);
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonData];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    NSLog(@"Everything done");
}

@end
