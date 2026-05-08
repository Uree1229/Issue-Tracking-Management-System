package com.example.its.ui.javafx.model;

public record ProjectTagOption(Long tagId, String name, String description) {

    @Override
    public String toString() {
        return name;
    }
}
