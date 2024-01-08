package com.fuseanalytics.gradle.zip4j

import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class AbstractGradleTest {
    @TempDir public File projectDir

    void copyResource(String filename, File root = projectDir) {
        File colors = new File( root, filename )
        if( !root.exists() ) root.mkdirs()
        getClass().getResourceAsStream("/"+filename).withStream {
            Files.copy( it, colors.toPath(), StandardCopyOption.REPLACE_EXISTING )
        }
    }
}
