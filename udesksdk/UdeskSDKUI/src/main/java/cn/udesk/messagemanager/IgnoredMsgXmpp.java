package cn.udesk.messagemanager;

import org.jivesoftware.smack.packet.ExtensionElement;

public class IgnoredMsgXmpp implements ExtensionElement {

	String sdkversion = "";

	@Override
	public String getElementName() {
		return "ignored";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:ignored";
	}

	public String getSdkversion() {
		return sdkversion;
	}

	public void setSdkversion(String sdkversion) {
		this.sdkversion = sdkversion;
	}

	@Override
	public CharSequence toXML() {
		return null;
	}




}
