package com.example.its.ui.javafx.support;

import com.example.its.persistence.entity.Tag;
import com.example.its.ui.javafx.model.ProjectTagOption;

public final class TagMapper {

    private TagMapper() {
    }

    public static ProjectTagOption toOption(Tag tag) {
        return new ProjectTagOption(tag.getTagId(), tag.getName(), tag.getDescription());
    }
}
