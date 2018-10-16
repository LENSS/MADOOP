
package edu.tamu.lenss.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.io.File;
import java.nio.file.*;



public final class MyFiles {


    public static void deleteDir(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                try {
                    deleteDir(f);
                } 
                catch (Exception e) {
                }
            }
        }
        dir.delete();
    }

//    public static boolean isDirEmpty(final File dir) throws IOException {
//        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
//            return !dirStream.iterator().hasNext();
//        }
//    }

    public static void emptyDir(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                try {
                    f.delete();
                }
                catch (Exception e) {
                }
            }
        }
    }



    public static boolean isDirEmpty(final File dir) {

        if (dir.isDirectory() && dir.list().length == 0) {
            return true;
        } else {
            return false;
        }

    }


    private static void lsFiles(File curDir) {
    
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                System.out.println(f.getName());
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
    
    }
    
    
    private static void lsAllFiles(File curDir) {
    
        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                lsAllFiles(f);
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
    
    }


    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    

}

