package cn.udesk.messagemanager;

import org.jivesoftware.smack.packet.ExtensionElement;

public class ReceivedXmpp implements ExtensionElement {
	private String msgId = "";

	@Override
	public String getElementName() {
		return "received";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:receipts";
	}
	@Override
	public CharSequence toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace()).append("\"")
				.append(" id= \"").append(getMsgId())
				.append("\"").append("/>");
		return sb.toString();
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}


}
