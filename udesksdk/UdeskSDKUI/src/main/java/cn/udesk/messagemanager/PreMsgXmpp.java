package cn.udesk.messagemanager;

import org.jivesoftware.smack.packet.ExtensionElement;

public class PreMsgXmpp implements ExtensionElement {


	String premsg = "true";

	@Override
	public String getElementName() {
		return "premsg";
	}

	@Override
	public String getNamespace() {
		return "udesk:premsg";
	}

	public String getPremsg() {
		return premsg;
	}

	public void setPremsg(String premsg) {
		this.premsg = premsg;
	}

	@Override
	public CharSequence toXML() {
		StringBuilder builder = new StringBuilder();
		builder.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace())
				.append("\"")
				.append(" premsg= \"").append(getPremsg())
				.append("\">")
				.append("</").append(getElementName()).append(">");
		return builder.toString();
	}

	

}
