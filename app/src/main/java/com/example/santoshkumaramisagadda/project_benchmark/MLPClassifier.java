package com.example.santoshkumaramisagadda.project_benchmark;

import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import static com.example.santoshkumaramisagadda.project_benchmark.First_screen.percent_value;

public class MLPClassifier extends AppCompatActivity {

    static int percentage;

    String commandFile="command.txt";
    String command_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +commandFile;

    String modelFile="output.model";
    String model_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +modelFile;

    static Button train_button;
    static Button test_button;
    static TextView learningRate;
    static TextView seed;
    static TextView momentum;
    static TextView noOfEpoch;
    static TextView validationSetSize;


    Handler batteryHandler;
    BatteryManager mBatteryManager;
    long powerConsumption=0;
    boolean running;
    Instances splitTest;

    private static final String[] loss_functions = {"SquaredError", "ApproximateAbsoluteError "};
    private static final String[] activ_funcitons = {"Sigmoid ","ApproximateSigmoid", "Softplus "};
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlpclassifier);

        percentage= getIntent().getExtras().getInt("percentage");

        learningRate = (TextView) findViewById(R.id.editText12);
        momentum=(TextView)findViewById(R.id.editText13);
        seed = (TextView) findViewById(R.id.editText11);
        noOfEpoch=(TextView)findViewById(R.id.editText7);
        validationSetSize=(TextView)findViewById(R.id.editText8);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(MLPClassifier.this,
                android.R.layout.simple_spinner_item, loss_functions);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MLPClassifier.this,
                android.R.layout.simple_spinner_item, activ_funcitons);
        train_button = (Button) findViewById(R.id.button8);
        train_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String cmd = "java -cp wekaSTRIPPED.jar weka.classifiers.functions.MultilayerPerceptron  -L "+ learningRate.getText()+" -M "+momentum.getText()+" -N "+noOfEpoch.getText()+" -V "+validationSetSize.getText()+ " -S "+ seed.getText()+" -t trial1.arff -d output.model";
                System.out.println(cmd);
                Toast.makeText(view.getContext(), cmd, Toast.LENGTH_LONG).show();
                try {
                    //delete both output.model and command.txt
                    File file1=new File(command_location);
                    if(file1.exists())
                        file1.delete();

                    File file2=new File(model_location);
                    if(file2.exists())
                        file2.delete();

                    BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(command_location)));
                    writer1.write(cmd);
                    writer1.flush();
                    writer1.close();

                    Toast.makeText(
                            view.getContext(), "Saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(view.getContext(), "Error in storing the command file : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
//                } else {
//                    Toast.makeText(view.getContext(), "Please enter all the fields", Toast.LENGTH_LONG).show();
//                }

                //upload command.txt

                Toast.makeText(MLPClassifier.this,"Model training, Please wait.",Toast.LENGTH_SHORT).show();
                UploadService upload=new UploadService(MLPClassifier.this, getApplicationContext(),command_location,commandFile,train_button,0);
                upload.startUpload();
            }

        });

        test_button=(Button) findViewById(R.id.button9);
        test_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                DownloadService downloadService=new DownloadService(MLPClassifier.this,getApplicationContext(),test_button);
                downloadService.startDownload();

                try {
                    testData();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void testData() throws IOException {
        //batteryHandler.post(batteryRunnable); //starting the battery handler to measure power consumption
        BufferedReader inputReader=new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +"test.arff"));
        splitTest=new Instances(inputReader);
        try
        {
            running=true;
            splitTest.setClassIndex(splitTest.numAttributes() - 1 );
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +"output.model"));
            Classifier cls = (Classifier) ois.readObject();
            ois.close();
            final Evaluation eval = new Evaluation(splitTest);
            eval.evaluateModel(cls, splitTest);
            running=false;


            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //calculating TPR, TNR etc
                        double TPR = eval.truePositiveRate(0);
                        double TNR = eval.trueNegativeRate(0);
                        double FPR = eval.falsePositiveRate(0);
                        double FNR = eval.falseNegativeRate(0);
                        double HTER = (FPR + FNR) / 2;

                        String output = "Classifier used: Bayseian Network,\nInput file: Breast.arff" + ",\nPercentage for training: " + percentage + ",\nCorrect prediction: " + eval.pctCorrect() + ",\nPower consumption: " +
                                //powerConsumption / (1000 * 3600) +
                                ",\nTrue positive: " +
                                TPR + ",\nTrue negative: " + TNR + ",\nFalse Postive: " + FPR + ",\nFalse negative: " + FNR + ",\nHTER: " + HTER + ",\nRMSE: " + eval.rootMeanSquaredError();

                        Toast.makeText(MLPClassifier.this,output,Toast.LENGTH_LONG).show();
                        //writing to the file

                        /*BufferedWriter out = new BufferedWriter(new FileWriter(file, true), 1024);
                        out.write(data);
                        out.newLine();
                        out.newLine();
                        out.close();*/
                        Thread.sleep(2000);
                        TextView tv=(TextView) findViewById(R.id.textView);
                        tv.setText(output);
                        //Intent intent = new Intent(RFClassifier.this, log.class);
                        //startActivity(intent);


                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    Runnable batteryRunnable=new Runnable() {
        @Override
        public void run() {
            long currentLife= 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentLife = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            }
            powerConsumption=powerConsumption+currentLife*10;
//            Log.d("power consumed",""+powerConsumption);
            if(running)
                batteryHandler.postDelayed(this,10);
        }
    };
}