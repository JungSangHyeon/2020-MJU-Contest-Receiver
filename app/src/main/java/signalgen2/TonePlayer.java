package signalgen2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multion_offkeyingprototype.R;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import signalgen2.tasks.GeneratorTask;
import signalgen2.tasks.PlayingTask;

public class TonePlayer  {

    private GeneratorTask generatorTask;

    public void play(int freq) {
        this.stop();

        BlockingQueue<Short> blockingQueue = new LinkedBlockingDeque<>(10);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Wave wave = new Wave();
        wave.setWaveForm(Wave.TYPE_SINE);
        wave.setFrequency(freq);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                Wave.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2*512,
                AudioTrack.MODE_STREAM);

        this.generatorTask = new GeneratorTask(blockingQueue, wave);
        PlayingTask playingTask = new PlayingTask(audioTrack, blockingQueue);

        executorService.execute(this.generatorTask);
        executorService.execute(playingTask);
    }

    public void stop(){
        if (this.generatorTask!=null && this.generatorTask.isGenerating()) {
            this.generatorTask.stop();
        }
    }
}
