package com.fuseanalytics.gradle.zip4j

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.AesVersion
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.CopyActionProcessingStreamAction
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import org.gradle.api.tasks.bundling.AbstractArchiveTask

import java.nio.charset.Charset

class Zip4j extends AbstractArchiveTask {

    @Input
    @Optional
    String password = null

    @Input
    @Optional
    EncryptionMethod encryptionMethod

    @Input
    @Optional
    AesKeyStrength aesStrength = AesKeyStrength.KEY_STRENGTH_256

    @Input
    @Optional
    AesVersion aesVersion = AesVersion.TWO

    @Input
    @Optional
    Integer splitLength = null

    @Input
    @Optional
    Boolean unixMode = Boolean.TRUE

    @Input
    @Optional
    String metadataCharset = "UTF-8"

    Zip4j() {
        super()
        this.getArchiveExtension().convention("zip")
        this.getArchiveBaseName().convention("zip4j")
    }

    @Override
    protected CopyAction createCopyAction() {
        try( ZipFile output = new ZipFile(getArchiveFile().get().asFile, password?.toCharArray()) ) {
            output.setCharset(Charset.forName(metadataCharset))
            output.setUseUtf8CharsetForPasswords(true)

            return new CopyAction() {
                @Override
                WorkResult execute(CopyActionProcessingStream stream) {
                    stream.process(new CopyActionProcessingStreamAction() {
                        @Override
                        void processFile(FileCopyDetailsInternal copyDetailsInternal) {
                            try {
                                if (copyDetailsInternal.isDirectory()) {
                                    copyDir(output, copyDetailsInternal)
                                } else {
                                    copyFile(output, copyDetailsInternal)
                                }
                            } catch(UnsupportedOperationException ex) {
                                project.logger.error("Could not executed zip task because of an unsupported operation.  This typically means something isn't configured correctly.", ex)
                            }
                        }

                        void copyDir(ZipFile zipFile, FileCopyDetails copyDetails) {
                            ZipParameters zipParams = createZipParameters()
                            zipFile.addFolder(copyDetails.file, zipParams)
                        }

                        void copyFile(ZipFile zipFile, FileCopyDetails copyDetails) {
                            ZipParameters zipParams = createZipParameters()
                            if (splitLength) {
                                zipFile.createSplitZipFile([copyDetails.file], zipParams, true, splitLength)
                            } else {
                                zipFile.addFile(copyDetails.file, zipParams)
                            }
                        }

                        private ZipParameters createZipParameters() {
                            ZipParameters zipParams = new ZipParameters()
                            if (password) {
                                zipParams.setEncryptFiles(true)
                                zipParams.setEncryptionMethod(encryptionMethod ?: EncryptionMethod.AES)
                                zipParams.setAesKeyStrength(aesStrength)
                                zipParams.setAesVersion(aesVersion)
                            }
                            if( !unixMode ) zipParams.setUnixMode(false)
                            zipParams
                        }
                    })
                    return WorkResults.didWork(true)
                }
            }
        }
    }
}
