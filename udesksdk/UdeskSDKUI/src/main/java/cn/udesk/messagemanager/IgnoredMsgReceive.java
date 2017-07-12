package cn.udesk.messagemanager;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import udesk.org.jivesoftware.smack.SmackException;
import udesk.org.jivesoftware.smack.packet.PacketExtension;
import udesk.org.jivesoftware.smack.provider.PacketExtensionProvider;


/**
 * Created by sks on 2016/6/12.
 */
public class IgnoredMsgReceive implements PacketExtensionProvider {


    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws XmlPullParserException, IOException, SmackException {
        IgnoredMsgXmpp ignoredMsg = null;
        boolean stop = false;
        String xmlName = null;
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
