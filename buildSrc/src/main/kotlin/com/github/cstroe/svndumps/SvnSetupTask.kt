package com.github.cstroe.svndumps

import org.gradle.api.DefaultTask
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import java.io.File

/**
 * This task is intended to provide direction interaction
 * with an SVN repository without resorting to command line actions.
 */
class SvnSetupTask : DefaultTask() {

    fun makeSvnRepo(tgtPath: String = "C:/repos/root/path") {
        try {
            val tgtUrl = SVNRepositoryFactory.createLocalRepository(File(tgtPath), true, false)
            logger.info("not yet implemented {}", tgtUrl)
        } catch ( ex: SVNException) {
            //handle exception
        }
    }
}