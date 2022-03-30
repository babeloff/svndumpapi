import com.github.cstroe.svndumps.SvnBashTask

plugins {
    `base`
}

val scriptCacheExt = layout.buildDirectory.dir("scripts")
val svnWorkDirExt = layout.buildDirectory.dir("svn")


tasks {
    listOf(
        "empty", "first_commit", "svn_replace", "svn_rename",
        "svn_multi_file_delete", "simple_branch_and_merge",
        "simple_copy",
        "utf8_log_message", "add_and_multiple_change", "many_branches",
        "svn_copy_file", "svn_copy_file_many_times",
        "add_and_copy_change", "add_and_change_copy_delete",
        "set_root_property"
    ).forEach {
        register<SvnBashTask>("${it}_SvnDump") {
            group = "svn-dumps"
            scriptName.set("bash/${it}.sh")
            scriptDeps.set( listOf("bash/setup.sh", "bash/export.sh"))
            scriptCache.set(scriptCacheExt)
            svnWorkDir.set(svnWorkDirExt)
            dumpFile.set(layout.projectDirectory.file("src/test/resources/dumps/${it}.dump"))
        }
    }

    listOf("simple_copy2").forEach {
        register<SvnBashTask>("${it}_SvnDump") {
            group = "svn-dumps"
            dependsOn("simple_copy_SvnDump")
            logger.warn("Further manual changes are required to update UUID and timestamps in simple_copy2.dump")
            scriptName.set("bash/${it}.sh")
            scriptDeps.set(listOf("bash/setup.sh", "bash/export.sh"))
            scriptCache.set(scriptCacheExt)
            svnWorkDir.set(svnWorkDirExt)
            dumpFile.set(layout.projectDirectory.file("src/test/resources/dumps/${it}.dump"))
        }
    }

    register("many_branches_renamed_SvnDump") {
        group = "svn-dumps"
        dependsOn("many_branches_SvnDump")
        doLast {
            val inFile = layout.projectDirectory
                .file("src/test/resources/dumps/many_branches.dump")
            val stdoutFile = layout.projectDirectory
                .file("src/test/resources/dumps/many_branches_renamed.dump")
            project.exec {
                workingDir(svnWorkDirExt)
                executable("/bin/sed")
                args(listOf("s/\\/branch2/\\/newbranchname/", inFile.asFile.absolutePath))
                standardOutput = stdoutFile.asFile.outputStream()
            }
            project.exec {
                workingDir(svnWorkDirExt)
                executable("/bin/sed")
                args(listOf("-i", "600s/45/51/", stdoutFile.asFile.absolutePath))
            }
        }
    }

    listOf("svn_copy_file", "svn_copy_file_many_times").forEach {
        val it2 = "${it}_new_content"
        register<SvnBashTask>("${it2}_SvnDump") {
            group = "svn-dumps"
            dependsOn("${it}_SvnDump")
            logger.warn("Further manual changes are required to update UUID and timestamps in ${it2}.dump")
            scriptName.set("bash/${it2}.sh")
            scriptDeps.set(listOf("bash/setup.sh", "bash/export.sh"))
            scriptCache.set(scriptCacheExt)
            svnWorkDir.set(svnWorkDirExt)
            dumpFile.set(layout.projectDirectory.file("src/test/resources/dumps/${it2}.dump"))
        }
    }

    mapOf("before" to "this is some file content",
        "after" to "i replaced the content").forEach { (key,value) ->
        val name = "svn_copy_and_delete"
        register<SvnBashTask>("${name}_${key}_SvnDump") {
            group = "svn-dumps"
            logger.warn("hand hacked time stamps to match the file above")
            scriptName.set("bash/${name}.sh")
            scriptArgs.set(listOf(value))
            scriptDeps.set(listOf("bash/setup.sh", "bash/export.sh"))
            scriptCache.set(scriptCacheExt)
            svnWorkDir.set(svnWorkDirExt)
            dumpFile.set(layout.projectDirectory
                .file("src/test/resources/dumps/${name}.${key}.dump"))
        }
    }

    register("all_SvnDumps") {
        dependsOn(tasks.withType<SvnBashTask>())
    }
}