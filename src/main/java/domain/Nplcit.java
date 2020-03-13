package domain;

import java.io.Serializable;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/12 17:43
 * @description
 */
public class Nplcit implements Serializable {
    private Integer num;
    private String othercit;
    private String category;


    public String getOthercit() {
        return othercit;
    }

    public void setOthercit(String othercit) {
        this.othercit = othercit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "Nplcit{" +
                "num=" + num +
                ", othercit='" + othercit + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
