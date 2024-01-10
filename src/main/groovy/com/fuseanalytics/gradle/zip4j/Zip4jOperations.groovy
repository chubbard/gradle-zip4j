package com.fuseanalytics.gradle.zip4j

import net.lingala.zip4j.ZipFile
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import java.nio.charset.Charset

class Zip4jOperations {

    Project project

    Zip4jOperations(Project project) {
        this.project = project
    }

    /**
     * Take the provided zip file represented by the 1st parameter (ie file) and return a gradle
     * FileTree of that zip.  This will extract the zip file, but do so lazily and only when its needed.
     * By default it will extract it to a temporary folder, but you may specify another location in the configure
     * closure. Any type of object accepted by Project.file is valid here.
     * @param file The object representing the zip file.  Any type accepted by
     *       {@see <a href="https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:file(java.lang.Object)">Project.file</a>}
     *       is valid here.
     * @param configure The closure used to configure the object.  The delegate of the closure is of type {@see Zip4jConfig }
     * @return Returns a FileTree instance of the zip file contents
     */
    FileTree tree(Object file, @DelegatesTo(Zip4jConfig) Closure configure = null) {
        Zip4jConfig config = new Zip4jConfig()
        configure.delegate = config
        return project.fileTree({
            configure.call()
            File theFile = project.file( file )
            File destination = config.destination ?: File.createTempDir( theFile.name )

            ZipFile zip = new ZipFile( theFile, config.password?.toCharArray() )
            zip.charset = Charset.forName(config.metadataCharset)
            zip.useUtf8CharsetForPasswords = true
            if( zip.isSplitArchive() ) {
                File mergedFile = File.createTempFile("merged_${zip.file.name.replace(".zip", "")}", ".zip")
                zip.mergeSplitFiles( mergedFile )
                zip = new ZipFile( mergedFile, config.password?.toCharArray() )
                zip.charset = Charset.forName( config.metadataCharset )
                zip.useUtf8CharsetForPasswords = true
                zip.extractAll( destination.absolutePath )
                mergedFile.delete()
            } else {
                zip.extractAll( destination.absolutePath )
            }
            return destination
        })
    }
}
