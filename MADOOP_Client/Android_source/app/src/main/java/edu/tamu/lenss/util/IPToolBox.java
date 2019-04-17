package edu.tamu.lenss.util;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.tamu.cse.lenss.gnsService.client.GnsServiceClient;


/**
 * Created by root on 9/23/2018.
 */

public class IPToolBox {



//    public static Map<String, String> RDNS = new HashMap<>();

    public static class RDNS{

        public static String get(String ip){
            GnsServiceClient gnsServiceClient = new GnsServiceClient();
            String actualIP = ip.substring(1);
            Log.d("DEBUG","Suman actual ip:"+ actualIP);

//            System.out.println(ip.substring(1,ip.length()));
            if (ip.equals("/127.0.0.1")) return null;


            List<String> hostnames = gnsServiceClient.getAccountNamebyIP(actualIP);
            Log.d("DEBUG","Suman Hostname obtained from GNS:"+ hostnames.toString());


            //String[] temp = ip.substring(1,ip.length()).split("\\.");
            //return "phone-"+temp[0]+"-"+temp[1]+"-"+temp[2]+"-"+temp[3];
            if (hostnames.size()==0){
                Log.d("FATAL", "Could not query reverse DNS to the GNS service");
                return null;
            }
            else
                return hostnames.get(0);
        }

    }

//    public static volatile String theMainIP = "/192.168.1.30";
//    public static String theSubnet = "200";
//    public static int startIP = 1;
//    public static int endIP = 255;



}
