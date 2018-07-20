package com.hoikhong.labthread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.hoikhong.labthread.service.CounterIntentService;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

    int counter;
    TextView tvNumber;
    Thread thread;
    Handler handler;

    HandlerThread backgroundHandlerThread;
    Handler backgroundHandler;
    Handler mainHandler;

    SampleAsyncTask sampleAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counter = 0;
        tvNumber = findViewById(R.id.tv_number);

        //Methods Thread 1: Normal thread

        /*
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Run in background
                for (int i = 0; i < 100; i++) {
                    counter++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }

                    // สั่งให้ทำงานบน main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvNumber.setText(counter + "");
                        }
                    });
                }
            }
        });
        thread.start();
        */

        //Methods Thread 2 : handler thread

        /*handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                //Run in Main Thread
                tvNumber.setText(msg.arg1 + "");
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    counter++;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }

                    Message message = new Message();
                    message.arg1 = counter;
                    handler.sendMessage(message);
                }
            }
        });

        thread.start();
        */

        //Thread Methods 3 Handler only
       /* handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                counter++;
                tvNumber.setText(counter + "");
                if (counter < 100) {
                    handler.sendEmptyMessageDelayed(0, 1000);
                }

            }
        };
        handler.sendEmptyMessageDelayed(0, 1000);
    */

        //Thread Methods 4 Handler Thread
        /*
        backgroundHandlerThread = new HandlerThread("BackgroundHandlerThread");
        backgroundHandlerThread.start();

        backgroundHandler = new Handler(backgroundHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //Run in background Thread
                Message msgMain = new Message();
                msgMain.arg1 = msg.arg1 + 1;
                mainHandler.sendMessage(msgMain);
            }
        };

        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //Run in Main Thread
                tvNumber.setText(msg.arg1 + "");
                if (msg.arg1 < 100) {
                    Message msgBack = new Message();
                    msgBack.arg1 = msg.arg1;
                    backgroundHandler.sendMessageDelayed(msgBack, 1000);
                }
            }
        };

        Message msgBack = new Message();
        msgBack.arg1 = 0;
        backgroundHandler.sendMessageDelayed(msgBack, 1000);

        */

        //Thread Methods 5 AsyncTask
//        sampleAsyncTask = new SampleAsyncTask();
//        sampleAsyncTask.execute(0, 100); // ทำทีละงาน
//        sampleAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0, 100); // ทำหลายๆงานพร้อมกัน

        //Thread Methods: 6 AsyncTaskLoader
//        getSupportLoaderManager().initLoader(1, null, this);

        //Thread Methods: 7 IntentService
        LocalBroadcastManager.getInstance(MainActivity.this)
                .registerReceiver(counterBroadcastReceiver, new IntentFilter("a"));

        Intent intent = new Intent(MainActivity.this,
                CounterIntentService.class);
        startService(intent);

    }

    protected BroadcastReceiver counterBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Work on main Thread
            int counter = intent.getIntExtra("b", 0);
            tvNumber.setText(counter + "");
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        thread.interrupt();
//        backgroundHandlerThread.quit();
//        sampleAsyncTask.cancel(true);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(counterBroadcastReceiver);
    }



    @NonNull
    @Override
    public Loader<Object> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 1) {
            return new AdderAsyncTaskLoader(MainActivity.this, 5, 11);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        Log.d("LLLL", "onLoadFinshed");
        if (loader.getId() == 1) {
            Integer result = (Integer) data;
            tvNumber.setText(result + "");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {

    }

    static class AdderAsyncTaskLoader extends AsyncTaskLoader<Object> {

        int a;
        int b;
        Integer result;

        Handler handler;

        public AdderAsyncTaskLoader(@NonNull Context context, int a, int b) {
            super(context);
            this.a = a;
            this.b = b;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (result != null) {
                deliverResult(result);
            }
            //Initialize handler
            if (handler == null) {
                handler = new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        a = (int) (Math.random() * 100);
                        b = (int) (Math.random() * 100);
                        onContentChanged();
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                };

                handler.sendEmptyMessageDelayed(0, 1000);
            }
            if (takeContentChanged() || result == null){
                forceLoad();
            }
        }

        @Nullable
        @Override
        public Integer loadInBackground() {
            Log.d("LLLL", "loadInBackground");
            //Run in Background Thread
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//
//            }
            result = a + b;
            return result;
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            Log.d("LLLL", "onStopLoader");
        }

        @Override
        protected void onReset() {
            super.onReset();
            if (handler != null){
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }
        }
    }

    class SampleAsyncTask extends AsyncTask<Integer, Float, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... integers) {

            //Run in background Thread
            int start = integers[0];
            int end = integers[1];

            for (int i = start; i < end; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
                publishProgress(i + 0.0f);
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            //Run in main Thread
            super.onProgressUpdate(values);
            float progress = values[0];
            tvNumber.setText(progress + "%");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }


}
