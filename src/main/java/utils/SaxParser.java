package utils;

import domain.Invention;
import domain.Nplcit;
import domain.Patent;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/12 17:29
 * @description
 */
public class SaxParser implements ElementHandler {
    private SAXReader saxReader;
    private String inFilePath;
    private String outFilePath;
    private String patentId;
    private String inventionId;
    private String inventionTitle;
    private Integer nplcitNum;
    private String nplcitOthercit;
    private String nplcitCategory;
    private Invention invention;
    private Nplcit nplcit;
    private Patent patent;
    private List<Nplcit> nplcits;
    private List<Patent> patents;
    private static Integer times = 10;

    public SaxParser(String inFilePath, String outFilePath) {
        this.inFilePath = inFilePath;
        this.outFilePath = outFilePath;
        try {
            File file = new File(inFilePath);
            if (!file.exists()) {
                throw new RuntimeException("输入文件不存在");
            }
            nplcits = new ArrayList<Nplcit>();
            patents = new ArrayList<Patent>();
            InputStream in = new FileInputStream(file);
            saxReader = new SAXReader();
            saxReader.addHandler("/us-patent-grant", SaxParser.this);
            saxReader.addHandler("/us-patent-grant/us-bibliographic-data-grant/invention-title", SaxParser.this);
            saxReader.addHandler("/us-patent-grant/us-bibliographic-data-grant/us-references-cited/us-citation", SaxParser.this);
            saxReader.read(in);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onStart(ElementPath elementPath) {
    }

    public void onEnd(ElementPath elementPath) {
        Element root = elementPath.getCurrent(); //获得当前节点
        if (root.getName().equals("us-patent-grant")) {
            if (root.attributeValue("id") == null) {
                patentId = "";
            } else {
                patentId = root.attributeValue("id");
            }
            patent = new Patent();
            patent.setId(patentId);
            patent.setInvention(invention);
            //注意这里传递的是引用，不能直接清空
            patent.setNplcit(nplcits);
            nplcits.clear();
            //得先添加进去之后再清空
            patents.add(patent);
            saveFile(outFilePath);
        } else if (root.getName().equals("invention-title")) {
            if (root.getText() == null) {
                inventionTitle = "";
            } else {
                inventionTitle = root.getText();
            }
            if (root.attributeValue("id") == null) {
                inventionId = "";
            } else {
                inventionId = root.attributeValue("id");
            }
            invention = new Invention();
            invention.setId(inventionId);
            invention.setTitle(inventionTitle);
        } else if (root.getName().equals("us-citation")) {
            Element element = (Element) root.elements().get(0);
            //判断子标签是否是nplcit
            if (element.getName().equals("nplcit")) {
                if (element.attributeValue("num") == null) {
                    nplcitNum = -1;
                } else {
                    nplcitNum = Integer.valueOf(element.attributeValue("num"));
                }
                if (element.element("othercit").getText() == null) {
                    nplcitOthercit = "";
                } else {
                    nplcitOthercit = element.element("othercit").getText();
                }
                if (root.element("category").getText() == null) {
                    nplcitCategory = "";
                } else {
                    nplcitCategory = root.element("category").getText();
                }
                nplcit = new Nplcit();
                nplcit.setNum(nplcitNum);
                nplcit.setOthercit(nplcitOthercit);
                nplcit.setCategory(nplcitCategory);
                nplcits.add(nplcit);
            }
        }
        root.detach(); //记得从内存中移去
    }

    //保存中间结果
    private void saveFile(String path) {
        boolean append = false;
        File file = new File(path);
        if (file.exists()) {
            append = true;
        }
        if (times >= 0) {
            times--;
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
                ostream.writeObject(patents);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    patents.clear();
                    ostream.close();
                    fstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Patent> loadFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        FileInputStream fstream = null;
        ObjectInputStream ostream = null;
        List<Patent> patents = new ArrayList<Patent>();
        try {
            fstream = new FileInputStream(file);
            ostream = new ObjectInputStream(fstream);
            patents.addAll((List<Patent>) ostream.readObject());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ostream.close();
                fstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return patents;
    }
    private static void showResult(List<Patent> patents) {
        for (int i = 0; i < patents.size(); i++) {
            System.out.println(patents.get(i));
        }
    }
    public static void main(String[] args) {
        String inFilePath = "D:\\研究生\\专利数据解析\\ipg190101.xml";
        String outFilePath = "parseData.txt";
        System.out.println("----程序开始----");
        long startTime = System.currentTimeMillis(); //获取开始时间
        new SaxParser(inFilePath, outFilePath);
        List<Patent> patents = loadFile(outFilePath);
        showResult(patents);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
    }
}
