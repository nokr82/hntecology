package hntecology.ecology.base;

import android.util.Log;

import java.io.File;


public class FileFilter {
    public static String main(String file, String name) {
        Log.d("파일", file);
        File testDir = new File(file);
        String path = null;
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(".shp"));
            for (File curFile : fileNameList) {
                path = curFile.getPath();
            }
        }
        if (path != null) {
            return path;
        } else {
            return "";
        }
    }

    public static String img(String file,String num) {
        Log.d("파일", file);
        File testDir = new File(file);
        String path = null;
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(num+".png"));
            for (File curFile : fileNameList) {
                path = curFile.getPath();
                if (path != null) {
                    return path;
                } else {
                    return "";
                }
            }
        }else {
            return "";
        }
        return path;
    }


    public static void delete(String file, String name) {
        Log.d("파일", file);
        File testDir = new File(file);

        if (testDir.listFiles() != null) {
            File[] fileNameList = testDir.listFiles();
            if (fileNameList.length > 0) {
                for (File curFile : fileNameList) {
                    File path = new File(curFile.getPath());
                    path.delete();
                }
            }
        }

    }

}


