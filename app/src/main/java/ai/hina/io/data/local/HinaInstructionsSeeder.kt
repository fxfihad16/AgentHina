package ai.hina.io.data.local

import android.content.Context
import android.util.Log
import ai.hina.io.BuildConfig
import java.io.File

/**
 * Personal build only: seeds Fihad's own instruction pack into the Linux environment, so his agents follow his
 * rules without him pasting anything into a new project.
 *
 * - The pack itself is an asset of the `personal` flavor only (`src/personal/assets/`, gitignored — it lives in
 *   the private repo, never in the public one). This file holds no personal text at all: only the asset's name.
 * - Seeding is gated twice: the build must be the personal flavor (`BuildConfig.IS_PERSONAL_BUILD`) AND the asset
 *   must actually be present. A shareable build no-ops cleanly (no asset, no flavor flag).
 * - Seeding NEVER overwrites: if `~/.claude/CLAUDE.md` or `~/.config/opencode/AGENTS.md` already exists, the
 *   user's file wins and the pack is left alone. (Codex reads `AGENTS.md` per project; OpenCode's global file
 *   covers it for the global case, and `agy` gets the short entry through [ai.hina.io.data.context.GlobalContext].)
 *
 * Called when the environment finishes installing, and again whenever the local runtime starts (so a personal
 * build installed over an existing environment still seeds). Both calls are cheap once the files exist.
 */
object HinaInstructionsSeeder {
    private const val TAG = "HinaInstructionsSeeder"

    /** The full pack, shipped as an asset of the personal flavor only. */
    const val PACK_ASSET = "hina-default-instructions.md"

    /** The short standing-rules entry (~30 lines) every agent gets through GlobalContext, personal flavor only. */
    const val CONTEXT_ASSET = "hina-global-context.md"

    /** Seed the pack into the environment's home folder when the files are not already there. */
    fun seedIfNeeded(context: Context) {
        if (!BuildConfig.IS_PERSONAL_BUILD) return
        val pack = readAsset(context, PACK_ASSET) ?: return
        val home = LocalEnvironmentManager.getHomeDir(context)
        // Claude Code's global instructions, and OpenCode's global instructions (also read by Codex-style agents).
        seedFile(File(home, ".claude/CLAUDE.md"), pack)
        seedFile(File(home, ".config/opencode/AGENTS.md"), pack)
    }

    /** The short GlobalContext entry for this build, or null (shareable builds, or the asset is absent). */
    fun loadPersonalContext(context: Context): String? {
        if (!BuildConfig.IS_PERSONAL_BUILD) return null
        return readAsset(context, CONTEXT_ASSET)?.toString(Charsets.UTF_8)?.takeIf { it.isNotBlank() }
    }

    private fun readAsset(context: Context, name: String): ByteArray? =
        runCatching { context.assets.open(name).use { it.readBytes() } }.getOrNull()

    private fun seedFile(target: File, pack: ByteArray) {
        if (target.exists()) return // a user edit always wins
        runCatching {
            target.parentFile?.mkdirs()
            target.writeBytes(pack)
            Log.i(TAG, "Seeded default instructions into ${target.absolutePath}")
        }.onFailure { Log.w(TAG, "Could not seed ${target.absolutePath}: ${it.message}") }
    }
}
