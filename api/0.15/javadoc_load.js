    targetPage = "" + window.location.search;
    if (targetPage != "" && targetPage != "undefined")
        targetPage = targetPage.substring(1);
    if (targetPage.indexOf(":") != -1 || (targetPage != "" && !validURL(targetPage)))
        targetPage = "undefined";
    function validURL(url) {
        var pos = url.indexOf(".html");
        if (pos == -1 || pos != url.length - 5)
            return false;
        var allowNumber = false;
        var allowSep = false;
        var seenDot = false;
        for (var i = 0; i < url.length - 5; i++) {
            var ch = url.charAt(i);
            if ('a' <= ch && ch <= 'z' ||
                    'A' <= ch && ch <= 'Z' ||
                    ch == '$' ||
                    ch == '_') {
                allowNumber = true;
                allowSep = true;
            } else if ('0' <= ch && ch <= '9'
                    || ch == '-') {
                if (!allowNumber)
                     return false;
            } else if (ch == '/' || ch == '.') {
                if (!allowSep)
                    return false;
                allowNumber = false;
                allowSep = false;
                if (ch == '.')
                     seenDot = true;
                if (ch == '/' && seenDot)
                     return false;
            } else {
                return false;
            }
        }
        return true;
    }
    function loadFrames() {
        if (targetPage != "" && targetPage != "undefined")
             top.classFrame.location = top.targetPage;
    }
