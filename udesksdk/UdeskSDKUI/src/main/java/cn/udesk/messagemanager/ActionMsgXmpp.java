package cn.udesk.messagemanager;

import udesk.org.jivesoftware.smack.packet.PacketExtension;

public class ActionMsgXmpp implements PacketExtension {

	public static  String elementName = "action";
	public static  String namespace = "udesk:action";

	String type = "";

	String actionText = "";


	public ActionMsgXmpp(String elementName, String namespace) {

	}


	@Override
	public String getElementName() {
		return "action";
	}

	@Override
	public String getNamespace() {
		return "udesk:action";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getActionText() {
		return actionText;
	}

	public void setActionText(String actionText) {
		this.actionText = actionText;
	}

	@Override
	public CharSequence toXML() {
		StringBuilder builder = new StringBuilder();
		builder.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace())
				.append("\"")
				.append(" type= \"").append(getType())
				.append("\">")
				.append(getActionText())
				.append("</").append(getElementName()).append(">");
		return builder.toString();
	}

	

}
