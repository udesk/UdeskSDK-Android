package cn.udesk.messagemanager;

import udesk.org.jivesoftware.smack.packet.PacketExtension;

public class ProductXmpp implements PacketExtension {


	private String body = "";

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String getElementName() {
		return "product";
	}

	@Override
	public String getNamespace() {
		return "udesk:product";
	}

	@Override
	public CharSequence toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace()).append("\">").append(getBody())
				.append("</").append(getElementName()).append(">");
		return sb.toString();
	}

	

}
