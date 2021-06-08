package cn.udesk.messagemanager;


import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;



/**
 * Created by sks on 2016/6/12.
 */
public class ActionMsgReceive extends ExtensionElementProvider<ActionMsgXmpp> {


    @Override
    public ActionMsgXmpp parse(XmlPullParser parser, int initialDepth) throws Exception {
        ActionMsgXmpp actionMsgXmpp = null;
        try {
            boolean stop = false;
            String xmlName;
            int evtType;
            while (!stop){
                evtType = parser.getEventType();
                xmlName = parser.getName();
                switch (evtType){
                    case XmlPullParser.START_TAG:
                        if ("action".equals(xmlName)){
                            String type  = parser.getAttributeValue("", "type");
                            String actionText = parser.nextText();
                            actionMsgXmpp = new ActionMsgXmpp();
                            actionMsgXmpp.setType(type);
                            actionMsgXmpp.setActionText(actionText);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(xmlName.equals("action")){
                            stop = true;
                        }
                        break;
                }

            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionMsgXmpp;
    }
}
