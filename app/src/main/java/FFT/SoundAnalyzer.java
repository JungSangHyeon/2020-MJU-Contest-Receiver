package FFT;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import Constant.FFTConstant;

public class SoundAnalyzer extends Thread{

    // Component
    private STFT stft;
    private AudioRecord record;

    // Working Variable
    private int numOfReadShort;
    private short[] audioSamples;

    // Constructor
    public SoundAnalyzer(){
        this.stft = new STFT();
        this.audioSamples = new short[FFTConstant.ReadChunkSize];
    }

    public void startRecord(){
        this.createAudioRecord();
        try { this.record.startRecording(); }catch (IllegalStateException e) { this.log(FFTConstant.RECORD_START_FAIL_ERR); return; }
    }

    public void stopRecord(){
        if(record != null){
            this.record.stop();
            this.record.release();
            this.record = null;
        }
    }

    public boolean readData(){
        this.numOfReadShort = this.record.read(this.audioSamples, 0, FFTConstant.ReadChunkSize);
        this.stft.feedData(this.audioSamples, this.numOfReadShort);
        if (this.stft.nElemSpectrumAmp() >= FFTConstant.nFFTAverage) { this.stft.getSpectrumAmp(); return true;}
        return false;
    }

    private void log(String text){ Log.d(FFTConstant.SoundAnalyzerErrHead, text); }
    private void createAudioRecord() {
        // Set Min Byte
        int minBytes = AudioRecord.getMinBufferSize(FFTConstant.sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBytes == AudioRecord.ERROR_BAD_VALUE) { this.log(FFTConstant.BAD_VALUE_ERR); }

        // Set Buffer Sample Size
        int bufferSampleSize = Math.max(minBytes / FFTConstant.BYTE_OF_SAMPLE, FFTConstant.fftLen/2) * 2;
        bufferSampleSize = (int)Math.ceil(1.0 * FFTConstant.sampleRate / bufferSampleSize) * bufferSampleSize;

        // Create Audio Record
        try {
            this.record = new AudioRecord(FFTConstant.AudioSource, FFTConstant.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, FFTConstant.BYTE_OF_SAMPLE * bufferSampleSize);
        } catch (IllegalArgumentException e) { this.log(FFTConstant.RECORDER_INITIALIZE_FAIL_ERR); }
        if (this.record.getState() == AudioRecord.STATE_UNINITIALIZED) { this.log(FFTConstant.RECORDER_UNINITIALIZED_ERR); }
    }

    public void clear(){
        this.stft.clear();
    }
    // Getter & Setter
    public double[] getMaxAmpBetween(int startFreq, int endFreq) {return this.stft.getMaxAmpBetween(startFreq, endFreq);}

    public double[] getMaxBetweenExcept(int sF, int eF, int esF, int eeF) {
        double[] a1 =  this.getMaxAmpBetween(sF, esF);
        double[] a2 =  this.getMaxAmpBetween(eeF, eF);
        return (a1[1]>a2[1]) ? a1:a2;
    }
}