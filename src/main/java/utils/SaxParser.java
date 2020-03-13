package utils;

import domain.Invention;
import domain.Nplcit;
import domain.Patent;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    private String sourceFilePath;
    private String targetFilePath;
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

    public SaxParser(String sourceFilePath, String targetFilePath) {
        this.sourceFilePath = sourceFilePath;
        this.targetFilePath = targetFilePath;
        nplcits = new ArrayList<Nplcit>();
        patents = new ArrayList<Patent>();
        saxReader = new SAXReader();
        saxReader.addHandler("/us-patent-grant", SaxParser.this);
        saxReader.addHandler("/us-patent-grant/us-bibliographic-data-grant/invention-title", SaxParser.this);
        saxReader.addHandler("/us-patent-grant/us-bibliographic-data-grant/us-references-cited/us-citation", SaxParser.this);
        saxReader.setEntityResolver(new IgnoreDTDEntityResolver());
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists() || sourceFile.isDirectory()) {
            throw new RuntimeException("源文件不存在或源文件是一个文件夹");
        }
        System.out.println("开始解析文件...");
        long startTime = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(sourceFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String lineStr;
            while ((lineStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineStr).append("\r\n");
                if(lineStr.equals("</sequence-cwu>")){
                    //清空文本
                    stringBuilder.delete(0, stringBuilder.length());
                }
                if (lineStr.equals("</us-patent-grant>")) {
                    InputStream in = new ByteArrayInputStream(stringBuilder.toString().getBytes());
                    saxReader.read(in);
                    //清空文本
                    stringBuilder.delete(0, stringBuilder.length());
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println(String.format("分割文件结束，耗时：%d秒", (endTime - startTime) / 1000));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            System.out.println(stringBuilder.toString());
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
            if (patents.size() > 1000) {
                FileUtils.writeFile(targetFilePath, patents);
                patents.clear();
            }
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

    private static void showResult(List<Patent> patents) {
        for (int i = 0; i < patents.size(); i++) {
            System.out.println(patents.get(i));
        }
    }
    private class IgnoreDTDEntityResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(
                    new ByteArrayInputStream(
                            "<?xml version='1.0' encoding='UTF-8'?>".getBytes()
                    ));
        }
    }
    public static void main(String[] args) {
        String sourceFilePath = "D:\\研究生\\专利数据解析\\ipg190101.xml";
        String targetFilePath = "parseData.txt";
        System.out.println("----程序开始----");
        new SaxParser(sourceFilePath, targetFilePath);
//        List<Patent> patents = FileUtils.readSerializableFile(targetFilePath);
//        showResult(patents);
    }
}
