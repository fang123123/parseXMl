package domain;

import java.io.Serializable;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/12 17:42
 * @description
 */
public class Invention implements Serializable {
    private String title;
    private String id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Invention{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
