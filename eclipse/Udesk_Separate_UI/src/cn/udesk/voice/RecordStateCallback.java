package cn.udesk.voice;

public interface RecordStateCallback {
	
	 void readyToCancelRecord();

	 void doCancelRecord();

	 void readyToContinue();

	 void endRecord();

}
