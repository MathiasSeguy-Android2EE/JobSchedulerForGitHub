/**
 * <ul>
 * <li>MyJobServiceUsingExecutor</li>
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
import android.util.Log;

import com.android2ee.jobschedulersimple.MyApplication;

/**
 * Created by Mathias Seguy - Android2EE on 20/10/2015.
 *  This JobService use an ExecutorService defined in your Application object
 *  So it use this ExecutorService to submit its runnable
 *  When the Job is done you have to kill the executor service (because it's created only for you)
 *  If you have multiple jobs, you should have to manage the destruction of the executorService in a smarter way
 *  For example you can inspired of the shutdown of a service and its unbind and bind
 *  (you count the number of jobs to be execute) => ok i do the code
 *
 *  * /!\ Use it if you want your multiple jobs to be ran in a working different threads (in fact you manage that through the ExecutorService) /!\
 * ------------------------------------------------------------------------------------------------------------------------------------------------
 */
public class MyJobServiceUsingExecutor extends JobService {
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
        Log.e("MyJobServiceExecutor", "onStartJob called <--------------------------------");
        //in this code we use the ExecutorService defined in the Application object
        MyApplication.getInstance().getMyExecutorServiceForJobs().submit(new MyRunnable(params));

        //the job is done in a separate thread
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
        //seems to never been called
        Log.e("MyJobServiceExecutor", "onStopJob called");
        //please kill the executorservice it's not needed anymore
        MyApplication.getInstance().killMyExecutorServiceForJob();
        //no need to resceduled it
        return false;
    }
    @Override
    public void onDestroy() {
        //be sure to overwrite this method to kill the looper else it will generate a memory leak
        Log.e("MyJobServiceExecutor", "onDestroy called, executor service is dead  <******************************************");
        //please kill the executorservice it's not needed anymore
        MyApplication.getInstance().killMyExecutorServiceForJob();
        super.onDestroy();
    }


    public class MyRunnable implements Runnable {
        /**
         * We need them to tell the system the job is done
         */
        JobParameters params;

        /**
         * use this constructor to give the JobParam to the runnable
         * @param params
         */
        public MyRunnable(JobParameters params) {
            this.params = params;
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //we want to check that we run in a seperate thread
            Log.e("MyRunnable", "The work is done in a separate thread called " + Thread.currentThread().getName());
            //The job is over and I don't need to resceduled it
            jobFinished(params,false);
        }
    }
}
