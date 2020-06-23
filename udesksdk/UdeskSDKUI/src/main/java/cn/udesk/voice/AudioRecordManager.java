package cn.udesk.voice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.UUID;

import cn.udesk.rich.LoaderTask;
import udesk.core.UdeskConst;


public class AudioRecordManager {

    private static AudioRecordManager instance;
    //    private MediaRecorder mMediaRecorder;
    private String mAudioDir;
    private String mCurrentFilePath;
    private OnAudioStateListener mStateListener;
    private boolean hasPrepare = false;

    // 不压缩将使用这个进行录音
    private AudioRecord audioRecorder = null;
    // 当前的振幅 (只有在未压缩的模式下)
    private int cAmplitude = 0;
    // 文件 (只有在未压缩的模式下)
    private RandomAccessFile randomAccessWriter;
    private int bufferSize;
    // 录音 通知周期(只有在未压缩的模式下)
    private int framePeriod;
    // 输出的字节(只有在未压缩的模式下)
    private byte[] buffer;

    private short samples;
    private short channels;
    private int payloadSize;
    //录音的开始时间
    private long startTime;

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            try {
                audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
                randomAccessWriter.write(buffer); // Write buffer to file
                payloadSize += buffer.length;
                if (samples == 16) {
                    for (int i = 0; i < buffer.length / 2; i++) { // 16bit sample size
                        short curSample = getShort(buffer[i * 2], buffer[i * 2 + 1]);
                        if (curSample > cAmplitude) { // Check amplitude
                            cAmplitude = curSample;
                        }
                    }
                } else { // 8bit sample size
                    for (int i = 0; i < buffer.length; i++) {
                        if (buffer[i] > cAmplitude) { // Check amplitude
                            cAmplitude = buffer[i];
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        @Override
        public void onMarkerReached(AudioRecord recorder) {
            // NOT USED
        }
    };

    /**
     * Converts a byte[2] to a short, in LITTLE_ENDIAN format
     */
    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }

    public static AudioRecordManager getInstance(String audioDir) {
        if (instance == null) {
            synchronized (AudioRecordManager.class) {
                if (instance == null) {
                    instance = new AudioRecordManager(audioDir);
                }
            }
        }
        return instance;
    }

    public void setAudioStateListener(OnAudioStateListener listener) {
        mStateListener = listener;
    }

    public void prepareAudio() {
        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    hasPrepare = false;
                    File dir = new File(mAudioDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
//                    mMediaRecorder = new MediaRecorder();
                    String fileName = UUID.randomUUID().toString() + UdeskConst.AUDIO_SUF_WAV;
                    File file = new File(dir, fileName);
                    mCurrentFilePath = file.getAbsolutePath();
//                    // 设置输出路径
//                    mMediaRecorder.setOutputFile(mCurrentFilePath);
//                    // 设置音频源,麦克风
//                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                    // 设置输出格式
//                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//                    // 设置音频编码
//                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//                    mMediaRecorder.setAudioSamplingRate(8000);
//                    // 准备
//                    mMediaRecorder.prepare();
//                    mMediaRecorder.start();
//
                    int sampleRate = 16000;
                    int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
                    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                    int audioSource = MediaRecorder.AudioSource.MIC;
                    samples = 16;
                    channels = 1;
                    framePeriod = sampleRate * 120 / 1000;
                    bufferSize = framePeriod * 2 * samples * channels / 8;
                    if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
                        // Check to make sure
                        // buffer size is not
                        // smaller than the
                        // smallest allowed one
                        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                        // Set frame period and timer interval accordingly
                        framePeriod = bufferSize / (2 * samples * channels / 8);

                    }
                    audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
                    if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                        if (mStateListener != null) {
                            releaseAudio();
                            mStateListener.prepareError("AudioRecord initialization failed");
                        }
                        return;
                    }
                    audioRecorder.setRecordPositionUpdateListener(updateListener);
                    audioRecorder.setPositionNotificationPeriod(framePeriod);
                    cAmplitude = 0;

                    if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (mCurrentFilePath != null)) {
                        // 写文件头
                        randomAccessWriter = new RandomAccessFile(mCurrentFilePath, "rw");
                        //设置文件长度为0，为了防止这个file以存在
                        randomAccessWriter.setLength(0);
                        randomAccessWriter.writeBytes("RIFF");
                        //不知道文件最后的大小，所以设置0
                        randomAccessWriter.writeInt(0);
                        randomAccessWriter.writeBytes("WAVE");
                        randomAccessWriter.writeBytes("fmt ");
                        // Sub-chunk
                        // size,
                        // 16
                        // for
                        // PCM
                        randomAccessWriter.writeInt(Integer.reverseBytes(16));
                        // AudioFormat, 1 为 PCM
                        randomAccessWriter.writeShort(Short.reverseBytes((short) 1));
                        // 数字为声道, 1 为 mono, 2 为 stereo
                        randomAccessWriter.writeShort(Short.reverseBytes(channels));
                        // 采样率
                        randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate));
                        // 采样率, SampleRate*NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate * samples * channels / 8));
                        randomAccessWriter.writeShort(Short.reverseBytes((short) (channels * samples / 8)));
                        // Block
                        // align,
                        // NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeShort(Short.reverseBytes(samples)); // Bits per sample
                        randomAccessWriter.writeBytes("data");
                        randomAccessWriter.writeInt(0); // Data chunk size not
                        // known yet, write 0

                        buffer = new byte[framePeriod * samples / 8 * channels];
                    } else {
                        if (mStateListener != null) {
                            mStateListener.prepareError("prepare() method called on uninitialized recorder");
                        }
                        return;
                    }
                    payloadSize = 0;
                    audioRecorder.startRecording();
                    if (audioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                        if (mStateListener != null) {
                            releaseAudio();
                            mStateListener.prepareError("AudioRecord initialization failed");
                        }
                        return;
                    }
                    audioRecorder.read(buffer, 0, buffer.length);
                    startTime = (new Date()).getTime();
                    hasPrepare = true;
                    if (mStateListener != null) {
                        mStateListener.prepareFinish(mCurrentFilePath);
                    }
                } catch (Exception e) {
                    if (mStateListener != null) {
                        mStateListener.prepareError(e.getMessage());
                    }
                    releaseAudio();
                    e.printStackTrace();
                }
            }
        });

    }

    public int getVoiceLevel(int maxLevel) {
        try {
            if (hasPrepare) {
                // getMaxAmplitude = 0 - 32767
//               return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1
                int result = cAmplitude;
                cAmplitude = 0;
                return (maxLevel * result / 32768) + 1;
            }
        } catch (Exception e) {
            return 1;
        }
        return 1;
    }

    public void releaseAudio() {
//        try {
//            mMediaRecorder.setOnErrorListener(null);
//            mMediaRecorder.setOnInfoListener(null);
//            mMediaRecorder.reset();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mMediaRecorder.release();
//        mMediaRecorder = null;
        try {
            stop();
            if (audioRecorder != null) {
                audioRecorder.setRecordPositionUpdateListener(null);
                audioRecorder.release();
            }
            hasPrepare = false;
        } catch (Exception e) {
            if (mStateListener != null) {
                mStateListener.prepareError("AudioRecord initialization failed");
            }
            e.printStackTrace();
        }
    }

    public void cancelAudio() {
        try {
            releaseAudio();
            LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentFilePath != null) {
                        File file = new File(mCurrentFilePath);
                        file.delete();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mStateListener != null) {
                mStateListener.prepareError("AudioRecord initialization failed");
            }
        }

    }

    /**
     * @return 录音的时间
     */
    public int stop() {


        try {
            if (audioRecorder == null || audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                return 0;
            }
            audioRecorder.stop();
            randomAccessWriter.seek(4); // Write size to RIFF header
            randomAccessWriter.writeInt(Integer.reverseBytes(36 + payloadSize));

            randomAccessWriter.seek(40); // Write size to Subchunk2Size
            // field
            randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

            randomAccessWriter.close();
            File file = new File(mCurrentFilePath);
            if (file.exists() && file.isFile()) {
                if (file.length() == 0L) {
                    file.delete();
                    return 0;
                } else {
                    int time = (int) ((new Date()).getTime() - this.startTime) / 1000;
                    return time;
                }
            } else {
                return 0;
            }
        } catch (IOException e) {
            if (mStateListener != null) {
                mStateListener.prepareError("AudioRecord initialization failed");
            }
            return 0;
        }
    }

    private AudioRecordManager(String audioDir) {
        mAudioDir = audioDir;
    }

    public interface OnAudioStateListener {
        void prepareError(String message);

        void prepareFinish(String audioFilePath);
    }
}
