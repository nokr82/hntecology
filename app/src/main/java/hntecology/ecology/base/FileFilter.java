package hntecology.ecology.base;

import android.util.Log;

import java.io.File;


public class FileFilter {

    public static void  delete_img2(String file) {
        File testDir = new File(file);
        Log.d("이미지경로",file.toString());
        Log.d("이미지경로",testDir.toString());
        testDir.delete();
      /*  if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(".png"));
            for (File curFile : fileNameList) {
                File path = new File(curFile.getPath());
                path.delete();
            }
        }*/

    }

    public static String delete_img(String file, String name) {
        Log.d("파일", file);
        File testDir = new File(file);
        String path = null;
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".png"));
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


    public static String main(String file, String name) {
        Log.d("파일", file);
        File testDir = new File(file);
        String path = null;
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".shp"));
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
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".shp"));
            for (File curFile : fileNameList) {
                File path = new File(curFile.getPath());
                path.delete();
            }
        }
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".prj"));
            for (File curFile : fileNameList) {
                File path = new File(curFile.getPath());
                path.delete();
            }
        }
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".dbf"));
            for (File curFile : fileNameList) {
                File path = new File(curFile.getPath());
                path.delete();
            }
        }
        if (testDir.listFiles()!=null){
            File[] fileNameList = testDir.listFiles((dir, name1) -> name1.endsWith(name+".shx"));
            for (File curFile : fileNameList) {
                File path = new File(curFile.getPath());
                path.delete();
            }
        }
    }

}


