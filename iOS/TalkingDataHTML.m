//
//  TalkingDataHTML.m
//  TalkingData-HTML
//
//  Created by liweiqiang on 14-1-9.
//  Copyright (c) 2014年 tendcloud. All rights reserved.
//

#import "TalkingDataHTML.h"
#import "TalkingData.h"


@interface TalkingDataHTML ()

@property (nonatomic, strong) NSString *pageName;

@end


@implementation TalkingDataHTML

static TalkingDataHTML *talkingDataHTML = nil;

+ (BOOL)execute:(NSString *)parameters webView:(UIWebView *)webView {
    if ([parameters hasPrefix:@"talkingdata"]) {
        if (nil == talkingDataHTML) {
            talkingDataHTML = [[TalkingDataHTML alloc] init];
        }
        NSString *str = [parameters substringFromIndex:12];
        NSDictionary *dic = [self jsonToDictionary:str];
        NSString *functionName = [dic objectForKey:@"functionName"];
        NSArray *args = [dic objectForKey:@"arguments"];
        if ([functionName isEqualToString:@"getDeviceId"]) {
            [talkingDataHTML getDeviceId:args webView:webView];
        } else {
            SEL selector = NSSelectorFromString([NSString stringWithFormat:@"%@:", functionName]);
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            if ([talkingDataHTML respondsToSelector:selector]) {
                [talkingDataHTML performSelector:selector withObject:args];
            }
#pragma clang diagnostic pop
        }
        return YES;
    }
    
    return NO;
}

+ (NSDictionary *)jsonToDictionary:(NSString *)jsonStr {
    if (jsonStr) {
        NSError* error = nil;
        id object = [NSJSONSerialization JSONObjectWithData:[jsonStr dataUsingEncoding:NSUTF8StringEncoding] options:kNilOptions error:&error];
        if (error == nil && [object isKindOfClass:[NSDictionary class]]) {
            return object;
        }
    }
    
    return nil;
}

- (void)getDeviceId:(NSArray *)arguments webView:(UIWebView *)webView {
    NSString *arg0 = [arguments objectAtIndex:0];
    if (arg0 == nil || [arg0 isKindOfClass:[NSNull class]] || arg0.length == 0) {
        return;
    }
    NSString *deviceId = [TalkingData getDeviceID];
    NSString *callBack = [NSString stringWithFormat:@"%@('%@')", arg0, deviceId];
    [webView stringByEvaluatingJavaScriptFromString:callBack];
}

- (void)setLocation:(NSArray *)arguments {
    NSString *arg0 = [arguments objectAtIndex:0];
    if (arg0 == nil || [arg0 isKindOfClass:[NSNull class]]) {
        return;
    }
    NSString *arg1 = [arguments objectAtIndex:1];
    if (arg1 == nil || [arg1 isKindOfClass:[NSNull class]]) {
        return;
    }
    double latitude = [arg0 doubleValue];
    double longitude = [arg1 doubleValue];
    [TalkingData setLatitude:latitude longitude:longitude];
}

- (void)trackEvent:(NSArray *)arguments {
    NSString *eventId = [arguments objectAtIndex:0];
    if (eventId == nil || [eventId isKindOfClass:[NSNull class]]) {
        return;
    }
    [TalkingData trackEvent:eventId];
}

- (void)trackEventWithLabel:(NSArray *)arguments {
    NSString *eventId = [arguments objectAtIndex:0];
    if (eventId == nil || [eventId isKindOfClass:[NSNull class]]) {
        return;
    }
    NSString *eventLabel = [arguments objectAtIndex:1];
    if ([eventLabel isKindOfClass:[NSNull class]]) {
        eventLabel = nil;
    }
    [TalkingData trackEvent:eventId label:eventLabel];
}

- (void)trackEventWithParameters:(NSArray *)arguments {
    NSString *eventId = [arguments objectAtIndex:0];
    if (eventId == nil || [eventId isKindOfClass:[NSNull class]]) {
        return;
    }
    NSString *eventLabel = [arguments objectAtIndex:1];
    if (eventLabel == nil || [eventLabel isKindOfClass:[NSNull class]]) {
        eventLabel = nil;
    }
    NSDictionary *parameters = [arguments objectAtIndex:2];
    if (parameters == nil && [parameters isKindOfClass:[NSNull class]]) {
        parameters = nil;
    }
    [TalkingData trackEvent:eventId label:eventLabel parameters:parameters];
}

- (void)trackPageBegin:(NSArray *)arguments {
    NSString *pageName = [arguments objectAtIndex:0];
    if (pageName == nil || [pageName isKindOfClass:[NSNull class]]) {
        return;
    }
    [TalkingData trackPageBegin:pageName];
    
    self.pageName = pageName;
}

- (void)trackPageEnd:(NSArray *)arguments {
    NSString *pageName = [arguments objectAtIndex:0];
    if (pageName == nil || [pageName isKindOfClass:[NSNull class]]) {
        return;
    }
    [TalkingData trackPageEnd:pageName];
}

- (void)trackPage:(NSArray *)arguments {
    if (_pageName) {
        [TalkingData trackPageEnd:_pageName];
    }
    
    NSString *pageName = [arguments objectAtIndex:0];
    if (pageName == nil || [pageName isKindOfClass:[NSNull class]]) {
        return;
    }
    [TalkingData trackPageBegin:pageName];
    
    self.pageName = pageName;
}

@end
