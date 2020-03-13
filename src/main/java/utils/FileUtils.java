package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/13 12:25
 * @description
 */
public class FileUtils {
    //保存list列表
    private <T> void saveFile(String path, List<T> list) {
        boolean append = false;
        File file = new File(path);
        if (file.exists()) {
            append = true;
        }
        FileOutputStream fstream = null;
        ObjectOutputStream ostream = null;
        try {
            fstream = new FileOutputStream(file, append);
            ostream = new ObjectOutputStream(fstream);
            long pos = 0;
            if (append) {
                // getChannel()返回此通道的文件位置，这是一个非负整数，它计算从文件的开始到当前位置之间的字节数
                pos = fstream.getChannel().position() - 4;// StreamHeader有4个字节所以减去
                // 将此通道的文件截取为给定大小
                fstream.getChannel().truncate(pos);
            }
            ostream.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
                fstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
