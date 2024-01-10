# gradle-zip4j
A Gradle plugin that provides drop-in replacements for Zip and ZipTree that 
uses the Zip4j library to provide features like password-protected zip files, 
split zip files, transparent zip64 which  not offered in typical Gradle Zip task.

## Creating Zip Protected by a Password
```groovy
plugins {
    id "com.fuseanalytics.gradle.zip4j"
}

task zipWithPassword(type: Zip4j) {
    password = "${somePassword}"
    archiveFileName = "zip4j-test.zip"
    from("\${projectDir}/src/")
    destinationDirectory = file("\${projectDir}/build")
}
```

## Extracting a Zip Protected by a Password

```groovy
plugins {
    id "com.fuseanalytics.gradle.zip4j"
}

task unzipPasswordZip(type: Copy) {
    from zip4j.tree("${buildDir}/zip4j-test.zip") {
        password = "Did you turn off the lights?"
    }
    into file("${buildDir}/contents")
}
```