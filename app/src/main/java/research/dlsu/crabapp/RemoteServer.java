package research.dlsu.crabapp;

import android.util.Log;

/**
 * Created by courtneyngo on 5/2/16.
 */
public class RemoteServer {

    // A-pe phone serial number 356899054383422

    public static String INIT_IPADDRESS = "192.168.1.133:8081";
    public static String SCHEMA = "http://";
    public static String WEBAPP = "ServerCrab";
    public static String INSERTCRAB = "insertcrab";
    public static String INSERTCRABUPDATE = "insertcrabupdate";
    public static String GETRESULTS = "getresults";


    public static String buildInsertCrabUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCRAB);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCRAB;
    }

    public static String buildInsertCrabUpdateUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCRABUPDATE);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCRABUPDATE;
    }

    public static String buildGetResultsUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + GETRESULTS);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + GETRESULTS;
    }
}

