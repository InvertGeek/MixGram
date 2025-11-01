package com.donut.mixgram.util.objects

import com.donut.mixfile.server.core.utils.extensions.isTrue
import org.jetbrains.annotations.Contract
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.logging.Logger


/**
 * Checks for updates on a GitHub repository
 */
class UpdateChecker(author: String, repoName: String, currentVersion: String) {
    /**
     * Gets the current version of the program.<br></br>
     * Useful in case you want to log a custom message.<br></br>
     * <br></br>
     * Does not actually check for updates
     *
     * @return The current version of the program
     */
    val currentVersion: String = removePrefix(currentVersion)
    private var url: URL? = null
    private val disabled: Boolean = System.getProperty("technicjelle.updatechecker.disabled") != null

    @Transient
    private var latestVersionFuture: CompletableFuture<String>? = null

    /**
     * Start the program with `-Dtechnicjelle.updatechecker.disabled` to disable the update checker
     *
     * @param author         GitHub Username
     * @param repoName       GitHub Repository Name
     * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
     */
    init {
        try {
            this.url = URL("https://github.com/$author/$repoName/releases/latest")
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Checks for updates from a GitHub repository's releases<br></br>
     * *This method blocks the thread it is called from*
     *
     * @see .checkAsync
     */
    fun check() {
        checkAsync()
        latestVersionFuture!!.join()
    }

    /**
     * Checks for updates from a GitHub repository's releases<br></br>
     * *This method does not block the thread it is called from*
     *
     * @see .check
     */
    fun checkAsync() {
        latestVersionFuture = CompletableFuture.supplyAsync { this.fetchLatestVersion() }
    }

    @get:Synchronized
    val latestVersion: String
        /**
         * Checks if necessary and returns the latest available version
         *
         * @return the latest available version
         */
        get() {
            if (latestVersionFuture == null || latestVersionFuture?.isCompletedExceptionally.isTrue()) checkAsync()
            return latestVersionFuture!!.join()
        }

    private fun fetchLatestVersion(): String {
        if (disabled) return currentVersion
        try {
            // Connect to GitHub website
            val con = url!!.openConnection() as HttpURLConnection
            con.instanceFollowRedirects = false

            // Check if the response is a redirect
            val newUrl =
                con.getHeaderField("Location") ?: throw IOException("Did not get a redirect")

            // Get the latest version tag from the redirect url
            val split = newUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return removePrefix(split[split.size - 1])
        } catch (ex: IOException) {
            throw CompletionException("Exception trying to fetch the latest version", ex)
        }
    }

    val isUpdateAvailable: Boolean
        /**
         * Checks if necessary and returns whether an update is available or not
         *
         * @return `true` if there is an update available or `false` otherwise.
         */
        get() = latestVersion != currentVersion

    val updateMessage: Optional<String>
        /**
         * Checks if necessary and returns a message if an update is available.<br></br>
         * The message will contain the latest version and a link to the GitHub releases page.<br></br>
         * Useful if you don't use Java's own [java.util.logging.Logger] and you want to use your own.<br></br>
         * Example:<br></br>
         * `New version available: v2.5 (current: v2.4)<br></br>
         * Download it at [https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest](https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest)`
         *
         * @return An optional containing the update message or an empty optional if there is no update available
         */
        get() {
            if (isUpdateAvailable) {
                return Optional.of(
                    """New version available: v${latestVersion} (current: v$currentVersion)
Download it at $url"""
                )
            }
            return Optional.empty()
        }

    val updateUrl: String
        /**
         * Gets the URL to the GitHub releases page,
         * where the latest version can be downloaded.<br></br>
         * Useful in case you want to log a custom message.<br></br>
         * <br></br>
         * Does not actually check for updates
         *
         * @return The URL to the GitHub releases page
         */
        get() = url.toString()

    /**
     * This method logs a message to the console if an update is available<br></br>
     *
     * @param logger Logger to log a potential update notification to
     */
    fun logUpdateMessage(logger: Logger) {
        updateMessage.ifPresent { msg: String? -> logger.warning(msg) }
    }

    /**
     * This method logs a message to the console if an update is available, asynchronously<br></br>
     *
     * @param logger Logger to log a potential update notification to
     */
    @Synchronized
    fun logUpdateMessageAsync(logger: Logger) {
        if (latestVersionFuture == null) checkAsync()
        latestVersionFuture!!.thenRun { logUpdateMessage(logger) }
    }

    companion object {
        /**
         * Removes a potential `v` prefix from a version
         *
         * @param version Version to remove the prefix from
         * @return The version without the prefix
         */
        @Contract(pure = true)
        private fun removePrefix(version: String): String {
            return version.replaceFirst("^v".toRegex(), "")
        }
    }
}