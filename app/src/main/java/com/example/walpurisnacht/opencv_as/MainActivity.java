package com.example.walpurisnacht.opencv_as;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

//    private Spinner spinner = null;
    private TextView textView = null;
    private boolean Debug = false;

    private String predict = null;
    private String m_chosen = null;

    private Mat m_digit;
    private Mat m_target;
    private Mat sample;

    private SVM svm_train = SVM.create();

    static {
        System.loadLibrary("opencv_java3");
    }

    private void MatTest() {
        Mat mat = new Mat(3,3,CvType.CV_32S);

        int[] r1 = {1,2,3};
        int[] r2 = {4,5,6};
        int[] r3 = {7,8,9};

        mat.put(0,0,r1);
        mat.put(1,0,r2);
        mat.put(2,0,r3);

        StringBuilder text = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int[] temp = new int[3];
            mat.get(i, 0, temp);
            for (int j : temp){
                text.append(j);
            }
        }

        textView = (TextView) findViewById(R.id.textView);
        textView.setText(text.toString());
    }

    private void MatParser(String text) {
        String[] lines = text.split("\n");

//        int row = 0;
//        //Count line
//        for (String line: lines) {
//            if (line != "") row++;
//        }

        int row = lines.length;
        int col = lines[0].split(",").length;


        //Init training dataset
//        m_digit.create(row, col-1, CvType.CV_32S);
//        m_target.create(row,1,CvType.CV_32S);
        m_digit = new Mat(row,col-1,CvType.CV_32F);
        m_target = new Mat(row,1,CvType.CV_32S);

        int[] i_target = new int[1];
        float[] i_digit = new float[col-1];
        //Parse
        for (int i = 0; i < row; i++) {
            String[] s_data = lines[i].split(",");


//            i_target[0] = Integer.parseInt(s_data[s_data.length-1]);
            i_target[0] = Integer.parseInt(s_data[s_data.length - 1]);


            for (int j = 0; j < col-1; j++) {
//                i_digit[j] = Integer.parseInt(s_data[j]);
                i_digit[j] = Float.parseFloat(s_data[j]);
            }

            m_digit.put(i,0,i_digit);
            m_target.put(i, 0, i_target);
        }

        //Debug
        if (Debug) {
            StringBuilder d_text = new StringBuilder();
            d_text.append("Digit: \n");

            for (int i = 0; i < row; i++) {
                m_digit.get(i, 0, i_digit);
                for (float j: i_digit) {
                    d_text.append(j);
                    d_text.append(" ");
                }
                d_text.append("\n");
            }

            d_text.append("Target: \n");

            for (int i = 0; i < row; i++) {
                m_target.get(i,0,i_target);
                d_text.append(i_target[0]);
                d_text.append("\n");
            }

            textView = (TextView) findViewById(R.id.textView);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setText(d_text.toString());
        }
    }

    private void TrainMat() {
        //Param
        svm_train.setC(1000);
        svm_train.setGamma(0.1);
        svm_train.setKernel(SVM.RBF);
        svm_train.setType(SVM.C_SVC);

        //Train
        svm_train.train(m_digit, Ml.ROW_SAMPLE, m_target);
        if (svm_train.isTrained()) {
            Toast.makeText(this.getBaseContext(),"DONE",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        Switch aSwitch = (Switch) findViewById(R.id.Debug);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textView = (TextView) findViewById(R.id.textView);
                if (isChecked) {
                    Debug = true;
                    textView.setText("Debug on");
                } else {
                    Debug = false;
                    textView.setText("Debug off");
                }
            }
        });


        //TODO
        //MatTest();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void PredictClick(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        predict = editText.getText().toString();

        textView = (TextView) findViewById(R.id.textView);

        //Debug
        if (Debug) {
            textView.setText(predict);
        }

        String[] data = predict.split(",");

        sample = new Mat(1,data.length,CvType.CV_32F);

        float[] f_sample = new float[data.length];

        for (int i = 0; i < data.length; i++) {
            f_sample[i] = Float.parseFloat(data[i]);
        }

        sample.put(0,0,f_sample);

        textView.setText(String.valueOf(svm_train.predict(sample)));

    }

    public void TrainClick(View view) {
        SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(MainActivity.this, "FileOpen",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        // The code in this function will be executed when the dialog OK button is pushed
                        m_chosen = chosenDir;
                        File file = new File(m_chosen);

                        StringBuilder text = new StringBuilder();

                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;

                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                        }
                        catch (IOException e) {
                            textView = (TextView) findViewById(R.id.textView);
                            textView.setText(e.toString());
                        }

                        //Debug
//                        if (Debug) {
//                            textView = (TextView) findViewById(R.id.textView);
//                            textView.setMovementMethod(new ScrollingMovementMethod());
//                            textView.setText(text);
//                        }

                        MatParser(text.toString());
                        TrainMat();
                    }
                });

        //You can change the default filename using the public variable "Default_File_Name"
        FileOpenDialog.Default_File_Name = "";
        FileOpenDialog.chooseFile_or_Dir();


    }
}
