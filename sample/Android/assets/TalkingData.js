var isWebviewFlag;

function setWebViewFlag() {
    isWebviewFlag = true;
};

function loadURL(url) {
    var iFrame;
    iFrame = document.createElement("iframe");
    iFrame.setAttribute("src", url);
    iFrame.setAttribute("style", "display:none;");
    iFrame.setAttribute("height", "0px");
    iFrame.setAttribute("width", "0px");
    iFrame.setAttribute("frameborder", "0");
    document.body.appendChild(iFrame);
    iFrame.parentNode.removeChild(iFrame);
    iFrame = null;
};

function exec(funName, args) {
    var commend = {
        functionName:funName,
        arguments:args
    };
    var jsonStr = JSON.stringify(commend);
    var url = "talkingdata:" + jsonStr;
    loadURL(url);
};

var TalkingData = {
    getDeviceId:function(callBack) {
        if (isWebviewFlag) {
            exec("getDeviceId", [callBack.name]);
        }
    },
    onEvent:function(eventId) {
        if (isWebviewFlag) {
            exec("trackEvent", [eventId]);
        }
    },
    onEventWithLabel:function(eventId, eventLabel) {
        if (isWebviewFlag) {
            exec("trackEventWithLabel", [eventId, eventLabel]);
        }
    },
    onEventWithParameters:function(eventId, eventLabel, eventData) {
        if (isWebviewFlag) {
            exec("trackEventWithParameters", [eventId, eventLabel, eventData]);
        }
    },
    onPageBegin:function(pageName) {
        if (isWebviewFlag) {
            exec("trackPageBegin", [pageName]);
        }
    },
    onPageEnd:function(pageName) {
        if (isWebviewFlag) {
            exec("trackPageEnd", [pageName]);
        }
    },
    setLocation:function(latitude, longitude) {
        if (isWebviewFlag) {
            exec("setLocation", [latitude, longitude]);
        }
    }
};
