package com.example.fftdemo;

import androidx.appcompat.app.AppCompatActivity;

import smile.ica.Exp;
import smile.ica.ICA;
import smile.ica.Kurtosis;
import smile.ica.LogCosh;
import smile.math.MathEx;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.psambit9791.jdsp.transform.ContinuousWavelet;
import com.github.psambit9791.jdsp.transform.FastFourier;
import com.github.psambit9791.jdsp.transform.ShortTimeFourier;
import com.github.psambit9791.jdsp.transform._Fourier;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.complex.Complex;
import org.tc33.jheatchart.HeatChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {


    GraphView graphView;
    LineGraphSeries<DataPoint> series;
    GraphView graphView2;
//    PointsGraphSeries<DataPoint> stft_series;
    LineGraphSeries<DataPoint> series2;

    double y;
    double[] simulatedEEG;
    double[] out;
//    double[] freq = new double[125];
    int dataLength;
    int timeInSecond;
    int fftLength;
    int fs;


//    double[][] components = new double[32][250*5];
//    double[][] results = new double[32][250*5];


    /// create a Timer object to compute the signal processing time on Android
    Timer timer = new Timer();


    // create several buttons for switching functions
    Button fftButton;
    Button stftButton;
    Button cwtButton;
    Button fastICAButton;

    TextView fftResults;
    TextView stftResults;
    TextView cwtResults;
    TextView fastICAResults;

    Thread thread;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        graphView = (GraphView) findViewById(R.id.graphView);
        graphView2 = (GraphView) findViewById(R.id.graphView2);

        fftButton = (Button) findViewById(R.id.fftButton);
        stftButton = (Button) findViewById(R.id.stftButton);
        cwtButton  =(Button) findViewById(R.id.cwtButton);
        fastICAButton = (Button) findViewById(R.id.fastICAButton);


        fftResults = (TextView)findViewById(R.id.fftResults);
        stftResults = (TextView) findViewById(R.id.stftResults);
        cwtResults = (TextView) findViewById(R.id.cwtResults);
        fastICAResults = (TextView) findViewById(R.id.fastICAResults);

//        fastICAButton.setOnClickListener(onClickListener);



        // define some important parameters for generating the signals
        fs=250;
        timeInSecond=5;
        dataLength = fs*timeInSecond;
        fftLength = dataLength/2;
        simulatedEEG = new double[dataLength];


        double[] time = new double[dataLength];
        for (int i =0; i<dataLength; i++){
            time[i] = i;
        }


        // simulated EEG signal --- single channel
        Random random = new Random();
        for (int i = 0; i<dataLength; i++){
            simulatedEEG[i] = 20 * (random.nextDouble()-0.5);
        }


        // simulated EEG signals --- multiple channels for ICA
//        Random random1  =new Random();
//        for (int i=0; i<32; i++){
//            for (int j=0; j<dataLength; j++){
//                multichannelEEG[i][j] = 100*(random1.nextDouble()-0.5);
//            }
//        }
//
//        System.out.println("Multiple-Channel simulated EEG:  Number of rows = " + multichannelEEG.length + "  ,Number of columns = " + multichannelEEG[0].length);
//        for(int i=0; i<10; i++){
//            System.out.println("Row: " + i);
//            for (int j=0; j<10; j++){
//                System.out.println("EEG values = " + multichannelEEG[i][j]);
//            }
//        }




        // time-series EEG plot
//        series = new LineGraphSeries<DataPoint>();
//        for (int i=0; i< time.length; i++){
//            double x = time[i]/fs;
//            y = simulatedEEG[i];
////            System.out.println("x = " + x + ", y = " + y);
//            series.appendData(new DataPoint(x,y),true,time.length);
//        }
//
//        series.setColor(Color.BLACK);
//        graphView.addSeries(series);
//        graphView.setTitle("Simulated EEG signal");
//        GridLabelRenderer gridLabelRenderer1 = graphView.getGridLabelRenderer();
//        gridLabelRenderer1.setHorizontalAxisTitle("Time(s)");
//        gridLabelRenderer1.setVerticalAxisTitle("Amplitude(uV)");


    }  // The OnCreate Method Bracket


    public void setFftButton(View view){

        timer.StartTimer();
        _Fourier ft = new FastFourier(simulatedEEG);
        ft.transform();
        boolean onlyPositive = true;
        out = ft.getMagnitude(onlyPositive); // only positive magnitude values
        //out = ft.getComplex2D(onlyPositive);  // get complex values
//        System.out.println("elapsed time: " +timer.ElapsedTime());

        double processingTime =timer.ElapsedTime();
        fftResults.setText("FFT processing time: "+Double.toString(processingTime) + " s");

        double[] freq = new double[fftLength];
        for (int i =0; i<fftLength; i++){
            freq[i] = i*fs/dataLength;
        }

        // print for debugging
        System.out.println("FFT length = "+ out.length+"freqency length = " + freq.length);


        // plot fft plot in frequency domain
        series2 = new LineGraphSeries<DataPoint>();
        for (int i=0; i< fftLength; i++){
            double x = freq[i];
            y = out[i];
            //System.out.println("freq = " + x + ", fft = " + y);
            series2.appendData(new DataPoint(x,y),true,freq.length);

        }
        series2.setColor(Color.RED);
        graphView2.addSeries(series2);
        graphView2.setTitle("FFT-Frequency domain");
        GridLabelRenderer gridLabelRenderer2 = graphView2.getGridLabelRenderer();
        gridLabelRenderer2.setHorizontalAxisTitle("Frequency(Hz)");
        gridLabelRenderer2.setVerticalAxisTitle("Amplitude(uV)");

    }



    public void setStftButton(View view){


//        Compute the Short-Time Fourier Transform for a time signal, with windowing.
//        Params:
//        signal – Signal for which to compute the STFT
//        frameLength – Number of samples that each FFT-frame should have
//        overlap – Number of samples that overlap between frames
//        fourierLength – Number of samples used in the Fourier analysis of each frame If the value is greater than frameLength, frame gets zero padded
//        window – Windowing function to perform on each STFT frame
//        Fs – Sampling frequency of the signal (in Hz)

        timer.StartTimer();

        int frameLength =100;
        int overlap = 50;
        ShortTimeFourier stft = new ShortTimeFourier(simulatedEEG, frameLength, overlap);
        stft.transform();
        double result[][] = stft.spectrogram(true);
        double frequencyAxis[] = stft.getFrequencyAxis(true);
        double timeAxis[] = stft.getTimeAxis();

        double processingTime =timer.ElapsedTime();
        stftResults.setText("STFT processing time: " + Double.toString(processingTime)+" s,\n The resulting scalogram size is: "+ Integer.toString(result.length) +"x" +Integer.toString(result[0].length));


//        System.out.println("stft results: " + result[5][3]);
        System.out.println("STFT: Number of rows = " + result.length + ",  Number of columns = " + result[0].length);


    }



    public void setCwtButton(View view) throws IOException {


//        This constructor initialises the prerequisites required to use Wavelet.
//        Params:
//        signal – The signal to be transformed
//        widths – The width of the wavelets to be used.

        ////// width should be the scales in matlab

        timer.StartTimer();
        int[] widths = IntStream.range(1,100).toArray();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ContinuousWavelet cwt = new ContinuousWavelet(simulatedEEG, widths);


                double morlet_omega0 = 5.0;
                Complex[][] out_cplx = cwt.transform(ContinuousWavelet.waveletType.MORLET, morlet_omega0);

                double processingTime =timer.ElapsedTime();
                cwtResults.setText("CWT processing time: " + Double.toString(processingTime)+" s,\n The resulting CWT size is: "+ Integer.toString(out_cplx.length) +"x" +Integer.toString(out_cplx[0].length));


                System.out.println("CWT: Number of rows = " + out_cplx.length + ",Number of columns = " + out_cplx[0].length);
            }
        });
        thread.start();


    }






    public void setFastICAButton(View view) {
        double[][] multichannelEEG = new double[32][200*4];
        // simulate a signal: 32 rows, 800 columns
        Random random1 = new Random();
        for (int i=0; i<32; i++){
            for (int j=0; j<EEGDuration; j++){
                multichannelEEG[i][j] = 100*(random1.nextDouble()-0.5);
            }
        }
        System.out.println("Simulated mulitple channel EEG shape = " + multichannelEEG.length +" x " + multichannelEEG[0].length);
        timer.StartTimer();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Kurtosis kurtosis = new Kurtosis();
                    ICA ica = ICA.fit(multichannelEEG, 32, kurtosis,0.001,1000);   // retrun the model
                    double[][] results = ica.components;                                         // return the components
                    System.out.println("ICA result shape: " + results.length + " x " + results[0].length);
                    fastICAResults.setText("fastICA processing time: " + timer.ElapsedTime() + " s,\n The Independent Components size is: " +results.length + " x "+results[0].length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timer.ElapsedTime();
                System.out.println("ICA processing time: " + timer.ElapsedTime());
                saveTxt(String.valueOf(timer.ElapsedTime()));
            }
        });
        thread.start();
    }



    double EEGDuration = 800;
    public void setFastICAButton2(View view){
        java.util.Timer timer1 = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                double[][] multichannelEEG = new double[32][(int)Math.round(EEGDuration)];
                // simulate a signal: 32 rows, 800 columns
                Random random1 = new Random();
                for (int i=0; i<32; i++){
                    for (int j=0; j<EEGDuration; j++){
                        multichannelEEG[i][j] = 100*(random1.nextDouble()-0.5);
                    }
                }
                System.out.println("Simulated mulitple channel EEG shape = " + multichannelEEG.length +" x " + multichannelEEG[0].length);
                timer.StartTimer();
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Kurtosis kurtosis = new Kurtosis();
                            ICA ica = ICA.fit(multichannelEEG, 32, kurtosis,0.001,1000);   // retrun the model
                            double[][] results = ica.components;                                         // return the components
                            System.out.println("ICA result shape: " + results.length + " x " + results[0].length);
                            fastICAResults.setText("fastICA processing time: " + timer.ElapsedTime() + " s,\n The Independent Components size is: " +results.length + " x "+results[0].length);
                            System.out.println("ICA processing time: " + timer.ElapsedTime());
                            saveTxt(String.valueOf(timer.ElapsedTime()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
//                System.out.println("ICA processing time: " + timer.ElapsedTime());
//                saveTxt(String.valueOf(timer.ElapsedTime()));
            }
        };
        timer1.schedule(timerTask, 1000, 1000*5);
    }




    String filename = "ICA800.txt";
    public void saveTxt(String usageLog) {

        String filepath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        // create a new filename
        File file = new File(filepath + File.separator + filename);

        try{
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(System.getProperty("line.separator").getBytes());
            fos.write(usageLog.getBytes(StandardCharsets.UTF_8));
//            Toast.makeText(this,"File saved!", Toast.LENGTH_SHORT).show();

        } catch(IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"Error! File not saved", Toast.LENGTH_SHORT).show();
        }
    }









}