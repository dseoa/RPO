package com.example.lab01;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.internal.SafeIterableMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;


import com.example.lab01.databinding.ActivityMainBinding;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


interface TransactionEvents {
    String enterPin(int ptc, String amount);

    void transactionResult(boolean result);
}


public class MainActivity extends AppCompatActivity implements TransactionEvents {
    public static native byte[] encrypt(byte[] key, byte[] data);

    public static native byte[] decrypt(byte[] key, byte[] data);

    private ActivityMainBinding binding;
    ActivityResultLauncher activityResultLauncher;

    public native String stringFromJNI();

    public static native int initRng();

    public static native byte[] randomBytes(int no);

    public native boolean transaction(byte[] trd);


    // Used to load the 'lab01' library on application startup.
    static {
        System.loadLibrary("lab01");
        System.loadLibrary("mbedcrypto");
    }

    private String pin;

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                //todo: log error
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int res = initRng();
        byte[] v = randomBytes(10);


        TextView ta = findViewById(R.id.txtAmount);
        String amt = String.valueOf(getIntent().getStringExtra("amount"));
        Long f = Long.valueOf(amt);
        DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
        String s = df.format(f);
        ta.setText("Сумма: " + s);

        TextView tp = findViewById(R.id.txtPtc);
        int pts = getIntent().getIntExtra("ptc", 0);
        if (pts == 2)
            tp.setText("Осталось две попытки");
        else if (pts == 1)
            tp.setText("Осталась одна попытка");


        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback() {
                    @Override
                    public void onActivityResult(Object result) {
                    }

                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            // обработка результата
                            //String pin = data.getStringExtra("pin");
                            //Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                            pin = data.getStringExtra("pin");
                            synchronized (MainActivity.this) {
                                MainActivity.this.notifyAll();
                            }
                        }
                    }
                });


       /* binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());*/

        // Example of a call to a native method
//        TextView tv = binding.sampleText;
//        tv.setText(stringFromJNI());
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }


    public void onButtonClick(View v) {

        new Thread(() -> {
            try {
                byte[] trd = stringToHex("9F0206000000000100");
                boolean ok = transaction(trd);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, ok ? "ok" : "failed", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception ex) {
                // todo: log error
            }

            byte[] trd = stringToHex("9F0206000000000100");
            transaction(trd);
        }).start();

        Intent it = new Intent(this, PinpadActivity.class);
        //startActivity(it);
        activityResultLauncher.launch(it);
    }

    /**
     * A native method that is implemented by the 'lab01' native library,
     * which is packaged with this application.
     */


    public static byte[] stringToHex(String s) {
        byte[] hex;
        try {
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
            hex = null;
        }
        return hex;
    }


    protected void testHttpClient() {
        new Thread(() -> {
            try {
                HttpURLConnection uc = (HttpURLConnection)
                        (new URL("https://www.wikipedia.org").openConnection());
                InputStream inputStream = uc.getInputStream();
                String html = IOUtils.toString(inputStream);
                String title = getPageTitle(html);
                runOnUiThread(() ->
                {
                    Toast.makeText(this, title, Toast.LENGTH_LONG).show();
                });

            } catch (Exception ex) {
                Log.e("fapptag", "Http client fails", ex);
            }
        }).start();
    }


    protected String getPageTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        String p;
        if (matcher.find())
            p = matcher.group(1);
        else
            p = "Not found";
        return p;
    }

}

