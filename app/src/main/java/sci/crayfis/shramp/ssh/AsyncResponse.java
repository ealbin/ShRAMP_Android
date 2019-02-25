package sci.crayfis.shramp.ssh;

/**
 * Interface for AsyncTasks to send information back to the Activity.
 */
public interface AsyncResponse {
    /**
     * Called in the Activity once the AsyncTask finishes.
     * @param status a string of information to give back to the Activity.
     */
    void processFinish(String status);
}