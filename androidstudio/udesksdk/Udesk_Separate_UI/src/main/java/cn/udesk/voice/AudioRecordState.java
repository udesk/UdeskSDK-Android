
package cn.udesk.voice;

public interface AudioRecordState {
    public void onRecordSuccess(final String resultFilePath , long time);

    public void onRecordingError();

    public void onRecordSaveError();

    public void onRecordTooShort();
    
    public void onRecordCancel();
    
    public void updateRecordState(int micAmplitude);
    
    public void onRecordllegal();
}
