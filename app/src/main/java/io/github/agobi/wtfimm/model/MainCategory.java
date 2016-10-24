package io.github.agobi.wtfimm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gobi on 10/21/16.
 */
public class MainCategory implements Category {
    private String name;
    private Map<String, SubCategory> subcategories;

    public MainCategory() {
    }

    public MainCategory(String name) {
        this.name = name;
        subcategories = new HashMap<>();
    }

    public Map<String, SubCategory> getSubcategories() {
        return subcategories;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainCategory that = (MainCategory) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
