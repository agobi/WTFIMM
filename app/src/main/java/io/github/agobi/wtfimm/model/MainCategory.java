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
}
