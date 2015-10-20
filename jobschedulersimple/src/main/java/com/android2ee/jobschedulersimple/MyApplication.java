/**
 * <ul>
 * <li>MyApplication</li>
 * <li>com.android2ee.jobschedulersimple</li>
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

package com.android2ee.jobschedulersimple;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.util.Log;

import com.android2ee.jobschedulersimple.service.MyJobServiceUsingAsyncTask;
import com.android2ee.jobschedulersimple.service.MyJobServiceUsingExecutor;
import com.android2ee.jobschedulersimple.service.MyJobServiceUsingHandlerThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mathias Seguy - Android2EE on 20/10/2015.
 */
public class MyApplication extends Application {
    private static final int JOB_ID_HanlderThread = 100;
    private static final int JOB_ID_ExecutorService = 200;
    private static final int JOB_ID_AsyncTask = 300;
    JobScheduler mJobScheduler;
    ExecutorService myExecutorServiceForJobs=null;
    private static MyApplication INSTANCE;
    public static MyApplication getInstance(){
        return INSTANCE;
    }


    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        Log.e("MyApplication", "*********************** onCreate *****************************");
        super.onCreate();
        //use only for the ExceutorService case
        INSTANCE=this;
        //instanciate your JobScheduler
        mJobScheduler= (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        Log.e("MyApplication", "onCreate: JobScheduler instanciate");

        //this first example use the HandlerThread (no need of executor service)
        //---------------------------------------------------------------------
        //define your JobServices here
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID_HanlderThread,
                new ComponentName( getPackageName(),
                        MyJobServiceUsingHandlerThread.class.getName() ) );
        //begin in one second
        builder.setOverrideDeadline(1000);
        int returnedValue;
        //the return value is failure(0) or success(1) not the JobId if success (Javadoc wrong)
        returnedValue=mJobScheduler.schedule( builder.build() );
        //launch it
        if( returnedValue <= 0 ) {
            //If something goes wrong (manage exception/error is better than logging them)
            Log.e("MyApplication", "onCreate: JobScheduler launch the task failure");
        }else{
            //nothing goes wrong
            Log.e("MyApplication", "onCreate: JobScheduler launch the task suceess JOB_ID_HanlderThread "+returnedValue);
        }

        //this second example use ExecutorService
        //---------------------------------------
        //then again define your Job and launch it
        JobInfo.Builder builder1 = new JobInfo.Builder(JOB_ID_ExecutorService,
                new ComponentName( getPackageName(),
                        MyJobServiceUsingExecutor.class.getName() ) );
        //begin in one second
        builder1.setOverrideDeadline(1000);
        //launch it
        returnedValue=mJobScheduler.schedule( builder1.build() );
        if( returnedValue <= 0 ) {
            //If something goes wrong (manage exception/error is better than logging them)
            Log.e("MyApplication", "onCreate: JobScheduler launch the task failure");
        }else{
            //nothing goes wrong
            Log.e("MyApplication", "onCreate: JobScheduler launch the task suceess JOB_ID_ExecutorService "+returnedValue);
        }

        //this third example use AsyncTask
        //--------------------------------
        //then again define your Job and launch it
        JobInfo.Builder builder2 = new JobInfo.Builder(JOB_ID_AsyncTask,
                new ComponentName( getPackageName(),
                        MyJobServiceUsingAsyncTask.class.getName() ) );
        //begin in one second
        builder2.setOverrideDeadline(1000);
        //launch it
        returnedValue=mJobScheduler.schedule( builder2.build() );
        if( returnedValue <= 0 ) {
            //If something goes wrong (manage exception/error is better than logging them)
            Log.e("MyApplication", "onCreate: JobScheduler launch the task failure");
        }else{
            //nothing goes wrong
            Log.e("MyApplication", "onCreate: JobScheduler launch the task suceess JOB_ID_AsyncTask "+returnedValue);
        }
    }

    /**
     * Use to manage multiple jobs run
     * The executorService will be killed when the last job has done its work
     */
    int numberOfJobsInTheExecutor=0;
    /**
     * retrieve the ExcutorService for your Jobs (JobScheduler)
     * @return
     */
    public ExecutorService getMyExecutorServiceForJobs() {
        if(myExecutorServiceForJobs==null){
            //Define your ExecutorService
            myExecutorServiceForJobs = Executors.newFixedThreadPool(1, new BackgroundThreadFactory());
        }
        //one more job, so increment
        numberOfJobsInTheExecutor=numberOfJobsInTheExecutor+1;
        return myExecutorServiceForJobs;
    }


    /**
     * Call this method in the onStop of your job to ensure memory garbage
     * It's the only case, because in current use of your application,
     * nobody will post anything in your executor, so it's empty, so it's garbage collect
     */
    public void killMyExecutorServiceForJob(){
        Log.e("MyApplication", "killMyExecutorServiceForJob called");
        //one less job, so decrement
        numberOfJobsInTheExecutor=numberOfJobsInTheExecutor-1;
        //when no more job working, just shut down the Executor Service
        if(numberOfJobsInTheExecutor==0) {
            myExecutorServiceForJobs.shutdown(); // Disable new tasks from being submitted
            try {
                //while not dead kill it
                while (!myExecutorServiceForJobs.isShutdown()) {
                    Log.e("MyApplication", " while(!myExecutorServiceForJobs.isShutdown()) called");
                    // Wait a while for existing tasks to terminate
                    if (!myExecutorServiceForJobs.awaitTermination(6, TimeUnit.SECONDS)) {
                        myExecutorServiceForJobs.shutdownNow(); // Cancel currently executing tasks

                        // Wait a while for tasks to respond to being cancelled
                        if (!myExecutorServiceForJobs.awaitTermination(6, TimeUnit.SECONDS)) {
                            System.err.println("Pool did not terminate");
                        }
                    }
                }
                Log.e("MyApplication", "myExecutorServiceForJobs isShutDown");
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                myExecutorServiceForJobs.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
            myExecutorServiceForJobs = null;
        }
    }

    /** * And its associated factory */
    public class BackgroundThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {Thread t = new Thread(r,"MyJobServiceUsingExecutorService");return t;}
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * This method is for use in emulated process environments.  It will
     * never be called on a production Android device, where processes are
     * removed by simply killing them; no user code (including this callback)
     * is executed when doing so.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
