/* Copyright 2014 Eddy Xiao <bewantbe@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package FFT;

import android.util.Log;

import java.util.Arrays;

import Constant.FFTConstant;

import static java.lang.Math.log10;

public class STFT {

    // Attribute
    private int sampleRate, fftLen, hopLen, spectrumAmpPt, nAnalysed = 0;

    // Component
    private double[] spectrumAmpOutCum, spectrumAmpOutTmp, spectrumAmpOut, spectrumAmpOutDB, spectrumAmpIn, spectrumAmpInTmp;
    private RealDoubleFFT spectrumAmpFFT;

    // Constructor
    public STFT() {
        this.init(FFTConstant.fftLen, FFTConstant.ReadChunkSize, FFTConstant.sampleRate, FFTConstant.nFFTAverage);
    }

    private void init(int fftLen, int _hopLen, int sampleRate, int minFeedSize) {
        if (minFeedSize <= 0) { throw new IllegalArgumentException("STFT::init(): should minFeedSize >= 1."); }
        if (((-fftLen)&fftLen) != fftLen) { throw new IllegalArgumentException("STFT::init(): Currently, only power of 2 are supported in fftLen"); }
        this.sampleRate = sampleRate;
        this.fftLen = fftLen;
        this.hopLen = _hopLen;
        this.spectrumAmpOutCum= new double[fftLen/2+1];
        this.spectrumAmpOutTmp= new double[fftLen/2+1];
        this.spectrumAmpOut   = new double[fftLen/2+1];
        this.spectrumAmpOutDB = new double[fftLen/2+1];
        this.spectrumAmpIn    = new double[fftLen];
        this.spectrumAmpInTmp = new double[fftLen];
        this.spectrumAmpFFT   = new RealDoubleFFT(fftLen);
        this.clear();
    }

    public void feedData(short[] ds, int dsLen) {
        if (dsLen > ds.length) {
            Log.e("STFT", "dsLen > ds.length !");
            dsLen = ds.length;
        }
        int inLen = this.spectrumAmpIn.length;
        int outLen = this.spectrumAmpOut.length;
        int dsPt = 0; // input data point to be read
        while (dsPt < dsLen) {
            while (this.spectrumAmpPt < 0 && dsPt < dsLen) {  // skip data when ReadChunkSize > fftLen
                double s = ds[dsPt++] / 32768.0;
                this.spectrumAmpPt++;
            }
            while (this.spectrumAmpPt < inLen && dsPt < dsLen) {
                double s = ds[dsPt++] / 32768.0;
                this.spectrumAmpIn[this.spectrumAmpPt++] = s;
            }
            if (this.spectrumAmpPt == inLen) {    // enough data for one FFT
                for (int i = 0; i < inLen; i++) {
                    this.spectrumAmpInTmp[i] = this.spectrumAmpIn[i] ;
                }
                this.spectrumAmpFFT.ft(this.spectrumAmpInTmp);
                this.fftToAmp(this.spectrumAmpOutTmp, this.spectrumAmpInTmp);
                for (int i = 0; i < outLen; i++) {
                    this.spectrumAmpOutCum[i] += this.spectrumAmpOutTmp[i];
                }
                this.nAnalysed++;
                if (this.hopLen < this.fftLen) {
                    System.arraycopy(this.spectrumAmpIn, this.hopLen, this.spectrumAmpIn, 0, this.fftLen - this.hopLen);
                }
                this.spectrumAmpPt = this.fftLen - this.hopLen;  // can be positive and negative
            }
        }
    }

    private void fftToAmp(double[] dataOut, double[] data) {
        double scaler = 2.0*2.0 / (data.length * data.length);
        dataOut[0] = data[0]*data[0] * scaler / 4.0;
        int j = 1;
        for (int i = 1; i < data.length - 1; i += 2, j++) { dataOut[j] = (data[i]*data[i] + data[i+1]*data[i+1]) * scaler; }
        dataOut[j] = data[data.length-1]*data[data.length-1] * scaler / 4.0;
    }

    public double[] getSpectrumAmp() {
        if (this.nAnalysed != 0) { // no new result
            int outLen = this.spectrumAmpOut.length;
            double[] sAOC = this.spectrumAmpOutCum;
            for (int j = 0; j < outLen; j++) { sAOC[j] /= this.nAnalysed; }
            System.arraycopy(sAOC, 0, this.spectrumAmpOut, 0, outLen);
            Arrays.fill(sAOC, 0.0);
            this.nAnalysed = 0;
            for (int i = 0; i < outLen; i++) { this.spectrumAmpOutDB[i] = 10.0 * log10(this.spectrumAmpOut[i]); }
        }
        return this.spectrumAmpOut;
    }

    public int nElemSpectrumAmp() {
      return nAnalysed;
    }

    public void clear() {
        this.spectrumAmpPt = 0;
        Arrays.fill(this.spectrumAmpOut, 0.0);
        Arrays.fill(this.spectrumAmpOutDB, log10(0));
        Arrays.fill(this.spectrumAmpOutCum, 0.0);
    }

    // My Methods
    public double getAmp(double realFreq){
        double fakeFreq = realFreq / this.sampleRate * this.fftLen;
        double amp  = this.spectrumAmpOutDB[(int)fakeFreq];
        return amp;
    }

    public double[] getMaxAmpBetween(double startFreq, double endFreq){
        double maxFreq = startFreq, maxAmp = this.getAmp(startFreq);
        for(int i = (int)startFreq; i<endFreq; i++){
            double nowAmp = this.getAmp(i);
            if(maxAmp < nowAmp){ maxFreq = i; maxAmp = nowAmp; }
        }
        return new double[] {maxFreq, maxAmp};
    }
}