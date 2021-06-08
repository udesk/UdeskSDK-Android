package cn.udesk.messagemanager;


import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;


/**
 * Created by sks on 2016/6/12.
 */
public class IgnoredMsgReceive extends ExtensionElementProvider<IgnoredMsgXmpp> {

    @Override
    public IgnoredMsgXmpp parse(XmlPullParser parser, int initialDepth) throws Exception {
        IgnoredMsgXmpp ignoredMsg = null;
        boolean stop = false;
        String xmlName;
        int evtType;
        while (!stop){
            evtType = parser.getEventType();
            xmlName = parser.getName();
            switch (evtType){
                case XmlPullParser.START_TAG:
                    if ("sdk_version".equals(xmlName)){
                        String actionText = parser.nextText();
                        ignoredMsg = new IgnoredMsgXmpp();
                        ignoredMsg.setSdkversion(actionText);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(xmlName.equals("sdk_version")){
                        stop = true;
                    }
                    break;
            }

        }

        return ignoredMsg;
    }
}
