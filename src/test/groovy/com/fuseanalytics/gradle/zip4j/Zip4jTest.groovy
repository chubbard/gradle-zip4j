package com.fuseanalytics.gradle.zip4j

import net.lingala.zip4j.ZipFile
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class Zip4jTest extends AbstractGradleTest {

    File buildFile

    @BeforeEach
    void setup() throws IOException {
        buildFile = new File(projectDir,"build.gradle")
        copyResource("annual-enterprise-survey-2021-financial-year-provisional-size-bands.csv", new File( projectDir, "src"))

        String build = """   
        import com.fuseanalytics.gradle.zip4j.* 
               
        plugins {
            id "com.fuseanalytics.gradle.zip4j"
        }
        
        task testPasswordZip(type: Zip4j) {
            password = "The Tall Dark Monkey D#maj7"
            archiveFileName = "zip4j-test.zip"
            from("\${projectDir}/src/")
            destinationDirectory = file("\${projectDir}/build")
        }
        
        task testBaseName(type: Zip4j) {
            password = "The Tall Dark Monkey Abmaj7"
            archiveBaseName = "zip4j"
            archiveAppendix = "source"
            metadataCharset = "UTF-8"
            archiveVersion = "v1"
            from(project.files("\${projectDir}/src"))
            destinationDirectory = project.file("\${projectDir}/build")
        }
        
        task unzipProtectedZip(type:Copy) {
            dependsOn("testBaseName")
            from(zip4j.tree(file("\${projectDir}/build/zip4j-source-v1.zip")) {
                password = "The Tall Dark Monkey Abmaj7"
            })
            into file("\${projectDir}/build/unzipProtectedZip")
        }
        """
        buildFile.withPrintWriter { it.write( build ) }
    }

    @Test
    void testZip4jPasswords() {
        BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withPluginClasspath()
                .withArguments( "testPasswordZip")
                .build()

        println( result.getOutput() )

        File output = new File( projectDir, "build/zip4j-test.zip")
        assert result.getTasks().first().outcome == TaskOutcome.SUCCESS
        assert output.exists()

        assertExtractionOfZip("build/zip4j-test.zip", "The Tall Dark Monkey D#maj7")
    }

    @Test
    public void testBaseName() {
        BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withPluginClasspath()
                .withArguments( "testBaseName")
                .build()

        println( result.getOutput() )

        File output = new File( projectDir, "build/zip4j-source-v1.zip")
        assert result.getTasks().first().outcome == TaskOutcome.SUCCESS
        assert output.exists()

        assertExtractionOfZip("build/zip4j-source-v1.zip", "The Tall Dark Monkey Abmaj7")
    }

    @Test
    public void testUnzip() {
        BuildResult result = GradleRunner.create()
                .withProjectDir( projectDir )
                .withPluginClasspath()
                .withArguments( "unzipProtectedZip")
                .build()

        println( result.getOutput() )

        File output = new File( projectDir, "build/unzipProtectedZip" )
        assert result.getTasks().first().outcome == TaskOutcome.SUCCESS
        assert output.exists()
        assert output.listFiles().length == 1
    }

    private void assertExtractionOfZip(String zipfile, String pwd) {
        ZipFile testOutput = new ZipFile(
                new File(projectDir, zipfile),
                pwd.toCharArray())
        File destDir = new File(projectDir.absolutePath, "build/contents")
        destDir.mkdirs()
        testOutput.extractAll(destDir.absolutePath)
        assert destDir.list().length == 1
    }
}
