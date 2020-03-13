package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fangjie
 * @version 1.0
 * @date 2020/3/12 17:36
 * @description
 */
public class Patent implements Serializable {
    private String id;
    private Invention invention;
    private List<Nplcit> nplcits;

    public Patent() {
        nplcits = new ArrayList<Nplcit>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Invention getInvention() {
        return invention;
    }

    public void setInvention(Invention invention) {
        this.invention = invention;
    }

    public List<Nplcit> getNplcit() {
        return nplcits;
    }

    public void setNplcit(List<Nplcit> nplcits) {
        this.nplcits.addAll(nplcits);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Patent: \nid: " + id + "\ninvention: " + invention + "\nnplcits:{\n");
        for (int i = 0; i < nplcits.size(); i++) {
            if (i < nplcits.size() - 1) {
                res.append(nplcits.get(i) + ",\n");
            } else {
                res.append(nplcits.get(i) + "\n}");
            }
        }
        return res.toString();
    }
}
