package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.checkpoint.CheckpointManager
import com.example.data.local.StudioDatabase
import com.example.data.local.entity.ProjectEntity
import com.example.data.security.SecureCredentialManager
import com.example.git.DiffType
import com.example.git.GitRepositoryManager
import com.example.runtime.LinuxRuntimeManager
import com.example.runtime.NativeProcessBridge
import com.example.workspace.WorkspaceManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: StudioDatabase
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var runtimeManager: LinuxRuntimeManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = StudioDatabase.getInstance(context)
        workspaceManager = WorkspaceManager(context)
        runtimeManager = LinuxRuntimeManager(context)
    }

    @Test
    fun readStringFromContext_matchesAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Mobile Studio", appName)
    }

    @Test
    fun workspaceManager_initializesReactProjectTemplate() {
        val projectId = "test-react-proj"
        val projectDir = workspaceManager.initProjectWorkspace(projectId, "React", "Test React App")
        assertTrue(projectDir.exists())

        val sourceDir = workspaceManager.getSourceDir(projectId)
        val packageJson = File(sourceDir, "package.json")
        assertTrue(packageJson.exists())
        assertTrue(packageJson.readText().contains("vite"))

        val files = workspaceManager.listFiles(projectId)
        assertTrue(files.isNotEmpty())
    }

    @Test
    fun workspaceManager_initializesPythonProjectTemplate() {
        val projectId = "test-python-proj"
        workspaceManager.initProjectWorkspace(projectId, "Python", "Test Python App")
        val mainPy = File(workspaceManager.getSourceDir(projectId), "main.py")
        assertTrue(mainPy.exists())
        assertTrue(mainPy.readText().contains("AI Mobile Development Studio"))
    }

    @Test
    fun secureCredentialManager_encryptsAndMasksApiKey() {
        val credentialManager = SecureCredentialManager(context)
        val testKey = "sk-ant-api03-abcdef1234567890xyz"
        credentialManager.saveApiKey("anthropic", testKey)

        assertTrue(credentialManager.hasApiKey("anthropic"))
        val retrieved = credentialManager.getApiKey("anthropic")
        assertEquals(testKey, retrieved)

        val masked = credentialManager.getMaskedApiKey("anthropic")
        assertTrue(masked.contains("••••••••"))
        assertFalse(masked.contains("1234567890")) // Key plaintext not leaked

        credentialManager.deleteApiKey("anthropic")
        assertFalse(credentialManager.hasApiKey("anthropic"))
    }

    @Test
    fun gitDiffEngine_computesLineChangesCorrectly() {
        val gitManager = GitRepositoryManager(runtimeManager)
        val oldText = "line 1\nline 2\nline 3"
        val newText = "line 1\nline 2 modified\nline 3\nline 4"

        val diff = gitManager.computeDiff(oldText, newText)
        assertTrue(diff.any { it.type == DiffType.ADDED && it.text.contains("line 4") })
        assertTrue(diff.any { it.type == DiffType.REMOVED || it.type == DiffType.ADDED })
    }

    @Test
    fun database_insertsAndRetrievesProject() = runBlocking {
        val project = ProjectEntity(
            id = "proj-unit-1",
            name = "Unit Test Project",
            type = "Node.js",
            workspacePath = "/data/workspace/proj-unit-1"
        )
        db.projectDao().insertProject(project)

        val retrieved = db.projectDao().getProjectById("proj-unit-1")
        assertNotNull(retrieved)
        assertEquals("Unit Test Project", retrieved?.name)
        assertEquals("Node.js", retrieved?.type)
    }

    @Test
    fun runtimeManager_detectsArchitectureGracefully() {
        val arch = NativeProcessBridge.getSystemArchitecture()
        assertNotNull(arch)
        val health = runtimeManager.getRuntimeHealth()
        assertTrue(health.availableStorageMb >= 0)
        assertTrue(health.totalRamMb > 0)
    }

    @Test
    fun runtimeManager_verifiesPackageChecksumCorrectly() {
        val testFile = File(context.cacheDir, "test_pkg.txt").apply { writeText("bundle-content-123") }
        // SHA-256 of "bundle-content-123" is 0a5cfd1369cfcae5ec8c6d36eec89e9447d966d5dc74ec6f5569e5999b9cfc1c
        val valid = runtimeManager.verifyPackageSha256(testFile, "0a5cfd1369cfcae5ec8c6d36eec89e9447d966d5dc74ec6f5569e5999b9cfc1c")
        assertTrue(valid)

        val invalid = runtimeManager.verifyPackageSha256(testFile, "corrupted_hash")
        assertFalse(invalid)
    }
}
