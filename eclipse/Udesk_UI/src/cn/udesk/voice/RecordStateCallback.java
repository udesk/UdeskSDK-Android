package cn.udesk.voice;

public interface RecordStateCallback {
	
	public void readyToCancelRecord();

	public void doCancelRecord();

	public void readyToContinue();

	public void endRecord();

}
