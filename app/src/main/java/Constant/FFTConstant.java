package Constant;

import android.media.MediaRecorder;

public class FFTConstant {
    public static final int AudioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    public static final int fftLen = 8192;
    public static final int nFFTAverage = 1;
    public static final int sampleRate = 48000;
    public static final int BYTE_OF_SAMPLE = 2;
    public static final int ReadChunkSize = 1024;

    // Err Message
    public static final String SoundAnalyzerErrHead = "Sound Analyzer";
    public static final String BAD_VALUE_ERR = "BAD_VALUE";
    public static final String RECORDER_INITIALIZE_FAIL_ERR = "Fail to initialize recorder.";
    public static final String RECORDER_UNINITIALIZED_ERR = "RECORDER_STATE_UNINITIALIZED";
    public static final String RECORD_START_FAIL_ERR = "Fail to start recording.";
}
