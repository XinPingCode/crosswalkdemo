package com.example.cxp.crosswalkdemo;

import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import android.util.Log;

public class MyXWalkUIClient extends XWalkUIClient {
    public MyXWalkUIClient(XWalkView view) {
        super(view);
    }
    @Override
    public boolean onJsAlert(XWalkView view, String url, String message, XWalkJavascriptResult result) {
        //return super.onJsAlert(view, url, message, result);
        Log.v("XWalkUIClient", message);
        result.confirm();
        return true;
    }
}
