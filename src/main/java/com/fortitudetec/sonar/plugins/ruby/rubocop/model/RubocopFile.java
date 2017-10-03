package com.fortitudetec.sonar.plugins.ruby.rubocop.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RubocopFile {
    private String path;
    private List<RubocopOffense> offenses;
}
