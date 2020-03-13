package utils;

import domain.Invention;
import domain.Nplcit;
import domain.Patent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/13 12:25
 * @description
 */
public class FileUtils {
    //保存普通列表
    public static void writeFile(String path, List<Patent> patents) {
        File file = new File(path);
        boolean append = false;
        if (file.exists()) {
            append = true;
        }
        StringBuilder builder = new StringBuilder();
        for (Patent patent : patents) {
            builder.append(patent.getId() + "\t");
            Invention invention = patent.getInvention();
            builder.append(invention.getId() + "\t" + invention.getTitle() + "\t");
            List<Nplcit> nplcits = patent.getNplcit();
            for (Nplcit nplcit : nplcits) {
                builder.append(nplcit.getNum() + "\t" + nplcit.getOthercit() + "\t" + nplcit.getCategory());
            }
            builder.append("\r\n");
        }
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(path, append);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter, 1024)
        ) {
            bufferedWriter.write(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //序列化保存list列表
    public static void writeSerializableFile(String path, List<Patent> patents) {
        boolean append = false;
        File file = new File(path);
        if (file.exists()) {
            append = true;
        }
        if (append) {
            //取出之前存入的list
            patents.addAll(readSerializableFile(path));
        }
        try (
                FileOutputStream fstream = new FileOutputStream(file);
                ObjectOutputStream ostream = new ObjectOutputStream(fstream);
        ) {
            ostream.writeObject(patents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //反序列化读取list列表
    public static List<Patent> readSerializableFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        List<Patent> patents = new ArrayList<>();
        try (
                FileInputStream fstream = new FileInputStream(file);
                ObjectInputStream ostream = new ObjectInputStream(fstream);
        ) {
            while (fstream.available() > 0) {
                patents.addAll((List<Patent>) ostream.readObject());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return patents;
    }

    /**
     * 将大文件分割成小文件
     *
     * @param sourceFilePath      源文件路径
     * @param targetDirectoryPath 生成文件目录
     * @return
     */
    public static void splitDataToSaveFile(String sourceFilePath, String targetDirectoryPath) {
        long startTime = System.currentTimeMillis();
        System.out.println("开始分割文件");
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetDirectoryPath);
        if (!sourceFile.exists() || sourceFile.isDirectory()) {
            throw new RuntimeException("源文件不存在或源文件是一个文件夹");
        }
        if (targetFile.exists()) {
            if (!targetFile.isDirectory()) {
                throw new RuntimeException("目标路径不是一个目录");
            }
        } else {
            targetFile.mkdirs();
        }
        try (FileInputStream fileInputStream = new FileInputStream(sourceFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String lineStr;
            int fileNum = 1;
            while ((lineStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineStr).append("\r\n");
                if (lineStr.equals("</us-patent-grant>")) {
                    File file = new File(targetDirectoryPath + File.separator + fileNum + sourceFile.getName());
                    writeFile(stringBuilder.toString(), file);
                    //清空文本
                    stringBuilder.delete(0, stringBuilder.length());
                    fileNum++;
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println(String.format("分割文件结束，耗时：%d秒", (endTime - startTime) / 1000));
        } catch (Exception e) {
            System.out.println("分割文件异常" + e);
        }
    }

    /**
     * 写文件
     *
     * @param text
     * @param file
     */
    private static void writeFile(String text, File file) {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter, 1024)
        ) {
            bufferedWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
