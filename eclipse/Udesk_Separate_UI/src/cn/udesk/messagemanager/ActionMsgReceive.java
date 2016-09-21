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
public class ActionMsgReceive implements PacketExtensionProvider {


    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws XmlPullParserException, IOException, SmackException {
        ActionMsgXmpp actionMsgXmpp = null;
        boolean stop = false;
        String n = null;
        int evtType;
        while (!stop){
            evtType = parser.getEventType();
            n = parser.getName();
            switch (evtType){
                case XmlPullParser.START_TAG:
                    if ("action".equals(n)){
                        String type  = parser.getAttributeValue("", "type");
                        String actionText = parser.nextText();
                        actionMsgXmpp = new ActionMsgXmpp(ActionMsgXmpp.elementName, ActionMsgXmpp.namespace);
                        actionMsgXmpp.setType(type);
                        actionMsgXmpp.setActionText(actionText);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(n.equals("action")){
                        stop = true;
                    }
                    break;
            }

        }

        return actionMsgXmpp;
    }

}
