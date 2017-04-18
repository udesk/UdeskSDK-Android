package cn.udesk.voice;

import android.media.MediaRecorder;
import android.text.TextUtils;

import java.io.File;

import cn.udesk.UdeskConst;


public class AudioRecordingAacThread extends Thread implements VoiceRecord {

	private String fileNamePath;
	static final int MIN_RECOED_TIME = 1000;// 目前是1秒
	private boolean isRecording = true;
	private boolean isCancelDelTmpFileWhenStop = false;
	private long recordTime = -1;

	private AudioRecordState mState = null;
	MediaRecorder recorder;

	public AudioRecordingAacThread() {
		try{
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			// 设置MediaRecorder录制的音频格式(不要修改其它格式了，后端对格式做了限定)
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			recorder.setAudioSamplingRate(8000);
		}catch (Exception e){

		}

	}

	@Override
	public synchronized void initResource(String filePath,
			AudioRecordState state) {
		this.fileNamePath = filePath;
		this.mState = state;
		recorder.setOutputFile(filePath);
	}

	private void handleError() {
		// 反馈失败状态
		if (mState != null) {
			mState.onRecordllegal();
		}

		isRecording = false;// 中止录音
		deleteTmpFile();
	}

	@Override
	public void run() {

		// 资源准备
		try {

			recorder.prepare();// 准备录制
			recorder.start();// 开始录制
			recordTime = System.currentTimeMillis();// 记录下当前时间
			getMaxAmplitude();
			boolean isError = false;
			// 继续录音中
			while (isRecording) {
				try {
					Thread.currentThread().sleep(200);
					getMaxAmplitude();
					if((System.currentTimeMillis() - recordTime)>1500
							&& getRecordAudioLength() < 20){
						finishRecord();
						deleteTmpFile();
						isRecording = false;
						if(mState != null){
							mState.onRecordllegal();
						}
						isError = true;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(!isError){
				// 结束录音
				finishRecord();
				// 最后的状态处理
				if (isCancelDelTmpFileWhenStop) {// 用户手势取消了
					if (mState != null) {
						mState.onRecordCancel();
					}
					deleteTmpFile();
				} else {
					if ((System.currentTimeMillis() - recordTime) <= MIN_RECOED_TIME) {// 用户操作太快
						if (mState != null) {
							mState.onRecordTooShort();
						}
						deleteTmpFile();
					} else {// 正常结束
						if (mState != null) {
							
							mState.onRecordSuccess(fileNamePath,(System.currentTimeMillis() - recordTime));
						}
					}
				}
			}
		
			
		} catch (Exception e) {
			e.printStackTrace();

			// 让UI界面展示一下录音状态
			try {
				Thread.currentThread().sleep(300);
			} catch (Exception e2) {
				e.printStackTrace();
			}

			handleError();
		}
	
	}

	private void deleteTmpFile() {
		File rawFile = new File(this.fileNamePath);
		int count = 0;
		while (rawFile.exists() && count < 3) {
			if (rawFile.delete()) {
				break;
			}
			count++;
		}

	}

	private synchronized void stopRecording() {
		isRecording = false;
	}

	@Override
	public synchronized void startRecord() {
		this.start();
	}

	@Override
	public synchronized void stopRecord() {
		stopRecording();
	}

	public synchronized void cancelRecord() {
		isCancelDelTmpFileWhenStop = true;
		stopRecording();
	}

	@Override
	public synchronized void receycleResource() {

	}

	@Override
	public long getMaxAmplitude() {
		if (mState != null) {
			int recordStatus = getMicMaxAmplitude(UdeskConst.recordStateNum);
			mState.updateRecordState(recordStatus);
		}
		return 0;
	}
	/**
	 *  
	 */
	public int getMicMaxAmplitude(int nMax) {
		if (recorder != null) {
			int maxAmplitude = recorder.getMaxAmplitude();
			if(maxAmplitude == 0){
				return 0;
			}
			double peakPower = maxAmplitude/32767.0;
			if ( peakPower <= 0.05) {
		        return 1;
		    } else if (peakPower > 0.05 && peakPower <= 0.15) {
		        return 2 ;
		    } else if (peakPower > 0.15 && peakPower <= 0.3) {
		        return 3;
		    } else if (peakPower > 0.3 && peakPower <= 0.375) {
		        return 4;
		    } else if (peakPower > 0.375 && peakPower <= 0.4) {
		        return 5;
		    } else if (peakPower > 0.4 && peakPower <= 0.6) {
		        return 6;
		    } else if (peakPower > 0.6 && peakPower <= 0.8) {
		       return 7;
		    } else if (peakPower > 0.8&& peakPower <= 1.0) {
		        return 8;
		    }
		}
		return 0;
	}
	
	
	public long getRecordAudioLength() {
		if (TextUtils.isEmpty(fileNamePath)) {
			return 0;
		}
		File file = new File(fileNamePath);
		long length = file.length();
		return length;
	}
	
	public void finishRecord(){
		// 结束录音
		try {
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}