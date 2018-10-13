package edu.tamu.lenss.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 9/23/2018.
 */

public class IPToolBox {


//    public static Map<String, String> RDNS = new HashMap<>();

    public static class RDNS{

        public static String get(String ip){
//            System.out.println(ip.substring(1,ip.length()));
            if (ip.equals("/127.0.0.1")) return null;
            String[] temp = ip.substring(1,ip.length()).split("\\.");
            return "phone-"+temp[0]+"-"+temp[1]+"-"+temp[2]+"-"+temp[3];
        }

    }

//    public static volatile String theMainIP = "/192.168.1.30";
//    public static String theSubnet = "200";
//    public static int startIP = 1;
//    public static int endIP = 255;



}
