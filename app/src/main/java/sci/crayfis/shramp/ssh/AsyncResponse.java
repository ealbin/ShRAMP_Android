/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 20 April 2019
 */

package sci.crayfis.shramp.ssh;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// This interface works well for transmitting data via SSH, but I've currently disabled that
// functionality.  I want to revisit this after I've done some work on StorageMedia

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