package com.oneleft.app.models;

import androidx.annotation.NonNull;

public class Category {

    private String category_name;

    public Category() {
    }

    public Category(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    @NonNull
    @Override
    public String toString() {
        return category_name;
    }
}