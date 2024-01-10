package com.fuseanalytics.gradle.zip4j

import org.gradle.api.Plugin
import org.gradle.api.Project

class Zip4jPlugin implements Plugin<Project> {

    void apply(Project target) {
        target.getExtensions().add("zip4j", new Zip4jOperations(target))
    }

}
