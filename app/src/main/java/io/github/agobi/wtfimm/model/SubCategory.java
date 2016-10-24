package io.github.agobi.wtfimm.model;

/**
 * Created by gobi on 10/24/16.
 */
public class SubCategory implements Category {
    private String name;

    public SubCategory() {}

    public SubCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
