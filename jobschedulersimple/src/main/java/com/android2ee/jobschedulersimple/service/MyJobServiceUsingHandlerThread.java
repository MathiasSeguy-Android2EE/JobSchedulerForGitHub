/**
 * <ul>
 * <li>MyJobScheduler</li>
 * <li>com.android2ee.jobschedulersimple.service</li>
 * <li>20/10/2015</li>
 * <p/>
 * <li>======================================================</li>
 * <p/>
 * <li>Projet : Mathias Seguy Project</li>
 * <li>Produit par MSE.</li>
 * <p/>
 * /**
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br>
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 * Belongs to <strong>Mathias Seguy</strong></br>
 * ***************************************************************************************************************</br>
 * This code is free for any usage but can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * <p/>
 * *****************************************************************************************************************</br>
 * Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 * Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br>
 * Sa propriété intellectuelle appartient à <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */

package com.android2ee.jobschedulersimple.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by Mathias Seguy - Android2EE on 20/10/2015.
 * This JobService is based on the principle of the IntentService
 * So it launches a working Thread that does the job.
 * To do that it uses a HandlerThread and gives its looper to the Handler that does the job
 * When the job is done, the Thread is destroyed by calling destroy on the looper.
 *
 * /!\ Use it if you want your multiple jobs to be ran and enqueued in a working thread /!\
 * ----------------------------------------------------------------------------------------
 */
public class MyJobServiceUsingHandlerThread extends JobService {
    /**
     * The what of the message send to the handler to ensure which job to launch
     */
    public static final int WHAT = 11021974;
    /**
     * The handler that runs the Job
     */
    MyHandler myHandler;
    /**
     * The Looper that runs in a separate thread (a working thread)
     * cretaes by the handlerThread
     */
    Looper myLooper;

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //you want your service not to be launched again automaticly
//        return START_NOT_STICKY;
//    }

    /**
     * Override this method with the callback logic for your job. Any such logic needs to be
     * performed on a separate thread, as this function is executed on your application's main
     * thread.
     *
     * @param params Parameters specifying info about this job, including the extras bundle you
     *               optionally provided at job-creation time.
     * @return True if your service needs to process the work (on a separate thread). False if
     * there's no more work to be done for this job.
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("MyJobServiceHandler", "onStartJob called <--------------------------------");
        //I want to make a work in a separate thread using hanlder
        //It's the same code than the one for the ServiceHandler:
        //create an HanlderThread (a thread for handler)
        HandlerThread myThread =new HandlerThread("MyJobServiceUsingHandlerThread");
        //start it
        myThread.start();
        //obtain its looper (in a way the queue where the works are dropped
        myLooper=myThread.getLooper();
        //use this looper to instanciate the handler that makes the works
        myHandler=new MyHandler(myLooper);
        //obtain a message from it
        Message msg=myHandler.obtainMessage();
        //I need to send those parameters to the handler,
        //so when job's done it will inform the system the work is done
        msg.obj=params;
        msg.what=WHAT;
        //send the message and so begin the work
        myHandler.sendMessage(msg);
        //I make the job in a separate thread (yes I did)
        return true;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     * <p/>
     * <p>This will happen if the requirements specified at schedule time are no longer met. For
     * example you may have requested WiFi with
     * {@link JobInfo.Builder#setRequiredNetworkType(int)}, yet while your
     * job was executing the user toggled WiFi. Another example is if you had specified
     * {@link JobInfo.Builder#setRequiresDeviceIdle(boolean)}, and the phone left its
     * idle maintenance window. You are solely responsible for the behaviour of your application
     * upon receipt of this message; your app will likely start to misbehave if you ignore it. One
     * immediate repercussion is that the system will cease holding a wakelock for you.</p>
     *
     * @param params Parameters specifying info about this job.
     * @return True to indicate to the JobManager whether you'd like to reschedule this job based
     * on the retry criteria provided at job creation-time. False to drop the job. Regardless of
     * the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("MyJobServiceHandler", "onStopJob called");
        //kill the looper to kill the service
        myLooper.quit();
        //no need to reschedule
        return false;
    }

    @Override
    public void onDestroy() {
        //be sure to overwrite this method to kill the looper else it will generate a memory leak
        Log.e("MyJobServiceHandler", "onDestroy called, Looper is dead  <******************************************");
        //kill the looper to kill the service
        myLooper.quit();
        super.onDestroy();
    }

    /**
     * Specific Hanlder instantiate with a Looper that runs in a separate thread
     * Be sure to follow this pattern
     */
    public class MyHandler extends Handler {
        /**
         * The constructor to call
         * @param looper The looper that enqueues works in a separate thread
         */
        public MyHandler(Looper looper) {
            super(looper);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            //now do your work
            if(msg.what==WHAT){
                //my work in a separate thread
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //we want to check that we run in a seperate thread
                Log.e("MyHandler","The work is done in a separate thread called "+Thread.currentThread().getName());
                //The job is over and I don't need to resceduled it
                jobFinished((JobParameters) msg.obj,false);
            }
            //You don't need to stop the service by yourself the system will do it for you
            //stopSelf();
            //do your work here
            super.handleMessage(msg);
        }


    }
}
