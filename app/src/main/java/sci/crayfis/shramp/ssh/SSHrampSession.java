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

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
//                         (TODO)      UNDER CONSTRUCTION      (TODO)
////////////////////////////////////////////////////////////////////////////////////////////////////
// This class works well for transmitting data via SSH, but I've currently disabled that
// functionality.  I want to revisit this after I've done some work on StorageMedia

public class SSHrampSession extends AsyncTask<String, Void, String> {

    // This is a link back to the main activity
    public AsyncResponse mainactivity = null;

    /**
     * SSHrampSession operations to be done in the background asynchronously from the main thread.
     * @param filenames dummy name
     * @return returns the status of the SSHrampSession operation which gets passed back to the main activity
     */
    protected String doInBackground(String... filenames) {
        String filename = filenames[0];

        // status string for reporting back to the main activity
        String status = "";

        String user = "shramp";
        String host = "craydata.ps.uci.edu";
        //String knownhostsfile = Environment.getExternalStorageDirectory() + "/.ssh/known_hosts";
        String pubkeyfile = Environment.getExternalStorageDirectory() + "/.ssh/id_rsa";
        int port=22;

        try {
            JSch jsch = new JSch();
            //jsch.setKnownHosts(knownhostsfile);
            jsch.addIdentity(pubkeyfile);

            Session session = jsch.getSession(user, host, port);
            //session.setConfig("PreferredAuthentications", "publickey");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(10000);
            session.connect();

            //ChannelExec channel = (ChannelExec)session.openChannel("exec");
            //channel.setCommand("touch ShRAMP_was_here");

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outfile = "/data/shramp/" + timestamp + ".jpeg";

            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand("scp -t " + outfile);

            try {
                OutputStream out = channel.getOutputStream();
                InputStream   in = channel.getInputStream();

                channel.connect();

                File file2upload = new File(filename);
                long filesize = file2upload.length();
                String command = "C0644 " + filesize + " ";
                if (filename.lastIndexOf('/') > 0) {
                    command += filename.substring(filename.lastIndexOf('/') + 1);
                } else {
                    command += filename;
                }
                command += "\n";

                out.write(command.getBytes());
                out.flush();

                FileInputStream fis = new FileInputStream(filename);
                byte[] buf = new byte[1024];
                while (true) {
                    int len = fis.read(buf, 0, buf.length);
                    if (len <= 0)
                        break;
                    out.write(buf, 0, len);
                    out.flush();
                }
                fis.close();
                fis = null;

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

            }
            catch (Exception e) {
                status = status.concat("fuck\n");
            }

            channel.disconnect();
            session.disconnect();
            status = status.concat("\tImage Uploaded!\n\n");
            status = status.concat("App finished, ready to close..");
        }
        catch(JSchException e) {
            status = status.concat("ERROR:\n");
            status = status.concat("\t");
            status = status.concat(e.getLocalizedMessage());
        }
        return status;
    }

    /**
     * Executed automatically when doInBackground finishes.
     * Passes status string back to the main activity.
     * @param status string to pass back
     */
    @Override
    protected void onPostExecute(String status) {
        mainactivity.processFinish(status);
    }

}