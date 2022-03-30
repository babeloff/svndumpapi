package com.github.cstroe.svndumps

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.StandardCopyOption


abstract class SvnBashTask : DefaultTask() {
    @get:Input
    abstract val scriptName: Property<String>

    @get:Input
    abstract val scriptDeps: ListProperty<String>

    @get:Input
    abstract val scriptArgs: ListProperty<String>

    @get:OutputDirectory
    abstract val svnWorkDir: DirectoryProperty

    @get:OutputDirectory
    abstract val scriptCache: DirectoryProperty

    @get:OutputFile
    abstract val dumpFile: RegularFileProperty

    @TaskAction
    fun exec() {
        val dumpFileStream = dumpFile.get().asFile.outputStream()

        scriptDeps.get().forEach {
            val depCacheFile = scriptCache.get().file(it).asFile
            depCacheFile.parentFile.mkdirs()

            val depUrl = javaClass.classLoader.getResource(it) ?: return@forEach

            java.nio.file.Files.copy(
                depUrl.openStream(),
                depCacheFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
        val scriptCacheFile = scriptCache.get().file(scriptName).get().asFile
        scriptCacheFile.parentFile.mkdirs()

        val scriptUrlStream = javaClass.classLoader.resources(scriptName.get())
        scriptUrlStream.forEach { scriptUrl ->
            java.nio.file.Files.copy(
                scriptUrl.openStream(),
                scriptCacheFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )

            val args = if (scriptArgs.isPresent) {
                listOf(scriptCacheFile.canonicalPath).plus(scriptArgs.get())
            } else {
                listOf(scriptCacheFile.canonicalPath)
            }

            project.exec {
                executable("/bin/bash")
                args(args)
                workingDir(svnWorkDir)
                standardOutput = dumpFileStream
            }
            return@forEach
        }
    }
}