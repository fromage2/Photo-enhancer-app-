package com.example.android.zoom;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import android.graphics.Color;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


public class ActivityInference {

    private static ActivityInference activityInferenceInstance;
    private TensorFlowInferenceInterface inferenceInterface;
    private static AssetManager assetManager;


    private static final String MODEL_FILE=  "file:///android_asset/frozen_LPGAN_736_FLOAT.pb";
    private static final String INPUT_NODE = "Placeholder";
    private static final String[] OUTPUT_NODES = {"netG-736/netG-736_var_scope/netG-736_var_scopeA/netG-736_3/Add"};
    private static final String OUTPUT_NODE = "netG-736/netG-736_var_scope/netG-736_var_scopeA/netG-736_3/Add";
    private static final long[] INPUT_SIZE = {1,512,512,3};
    private static final int OUTPUT_SIZE = 512;
    private static final int CHANNELS = 3;



    public static ActivityInference getInstance(final Context context)
    {
        if (activityInferenceInstance == null)
        {
            activityInferenceInstance = new ActivityInference(context);
        }
        return activityInferenceInstance;
    }

    public ActivityInference(final Context context) {
        this.assetManager = context.getAssets();
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public int[] getActivityProb(int[] input_signal)
    {
        //Si va mal probar con float
        float[] input = new float[500*500];
        for (int i = 0; i < input_signal.length; i++){
            input[i] = (float)input_signal[i];
        }

        float[] result= new float[OUTPUT_SIZE * OUTPUT_SIZE ];
        int[] result_int= new int[OUTPUT_SIZE * OUTPUT_SIZE];

        inferenceInterface.feed(INPUT_NODE,input,INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE,result);


        for (int i = 0; i < result.length; i++){
            result_int[i] = (int)result[i];
        }
        return result_int;

    }

    public int[] enhanceImage(float[] input_signal) {

        float[] outputValues_float = new float[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];
        int[] outputValues_int = new int[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];

        inferenceInterface.feed(INPUT_NODE, input_signal,INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE,outputValues_float);


        float cast = 0;
        for (int i = 0; i < outputValues_float.length; i++){

            cast = outputValues_float[i] * 255 + 0.5f;
            if(cast < 0) {
                cast = 0;
            }else{
                if(cast > 255){
                    cast = 255;
                }
            }
            outputValues_int[i] = (int) cast;
        }

        int i2 = 0;

        return outputValues_int;

    }


    public void saveImage(Bitmap finalBitmap, String name) {

        String root = "/home/uc3m1/Descargas/Mobile-DCSCN-master/app/results";
        File myDir = new File(root);
        //myDir.mkdirs();
        //Random generator = new Random();

        //int n = 10000;
        //n = generator.nextInt(n);
        String fname = "image_"+ name +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
