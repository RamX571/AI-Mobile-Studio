package com.example.workspace

import android.content.Context
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class WorkspaceFileItem(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val extension: String
)

class WorkspaceManager(private val context: Context) {

    val rootWorkspaceDir: File by lazy {
        File(context.filesDir, "app-workspace/projects").apply { mkdirs() }
    }

    fun getProjectDir(projectId: String): File {
        return File(rootWorkspaceDir, projectId)
    }

    fun getSourceDir(projectId: String): File {
        return File(getProjectDir(projectId), "source").apply { mkdirs() }
    }

    fun getAiDir(projectId: String): File {
        return File(getProjectDir(projectId), ".ai").apply { mkdirs() }
    }

    fun getSessionsDir(projectId: String): File {
        return File(getProjectDir(projectId), ".sessions").apply { mkdirs() }
    }

    fun getBuildDir(projectId: String): File {
        return File(getProjectDir(projectId), ".build").apply { mkdirs() }
    }

    fun getLogsDir(projectId: String): File {
        return File(getProjectDir(projectId), ".logs").apply { mkdirs() }
    }

    fun initProjectWorkspace(projectId: String, projectType: String, projectName: String): File {
        val projectDir = getProjectDir(projectId).apply { mkdirs() }
        val sourceDir = getSourceDir(projectId)
        getAiDir(projectId)
        getSessionsDir(projectId)
        getBuildDir(projectId)
        getLogsDir(projectId)

        // Populate Starter Template
        when (projectType.lowercase()) {
            "react", "vite" -> populateReactTemplate(sourceDir, projectName)
            "next.js" -> populateNextJsTemplate(sourceDir, projectName)
            "node.js" -> populateNodeTemplate(sourceDir, projectName)
            "static html/css/js" -> populateStaticWebTemplate(sourceDir, projectName)
            "python" -> populatePythonTemplate(sourceDir, projectName)
            "android/kotlin", "android/java" -> populateAndroidTemplate(sourceDir, projectName)
            "c/c++" -> populateCppTemplate(sourceDir, projectName)
            "php" -> populatePhpTemplate(sourceDir, projectName)
            else -> populateGenericTemplate(sourceDir, projectName)
        }

        return projectDir
    }

    private fun populateReactTemplate(sourceDir: File, projectName: String) {
        val srcDir = File(sourceDir, "src").apply { mkdirs() }
        File(sourceDir, "package.json").writeText(
            """
            {
              "name": "${projectName.lowercase().replace(" ", "-")}",
              "private": true,
              "version": "0.1.0",
              "type": "module",
              "scripts": {
                "dev": "vite --port 3000 --host 0.0.0.0",
                "build": "vite build",
                "preview": "vite preview --port 3000"
              },
              "dependencies": {
                "react": "^18.3.1",
                "react-dom": "^18.3.1",
                "lucide-react": "^0.344.0"
              },
              "devDependencies": {
                "@vitejs/plugin-react": "^4.2.1",
                "vite": "^5.2.0"
              }
            }
            """.trimIndent()
        )

        File(sourceDir, "index.html").writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>$projectName</title>
                <style>
                  body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0b0f19; color: #f8fafc; }
                </style>
              </head>
              <body>
                <div id="root"></div>
                <script type="module" src="/src/main.jsx"></script>
              </body>
            </html>
            """.trimIndent()
        )

        File(srcDir, "main.jsx").writeText(
            """
            import React from 'react';
            import ReactDOM from 'react-dom/client';
            import App from './App';

            ReactDOM.createRoot(document.getElementById('root')).render(
              <React.StrictMode>
                <App />
              </React.StrictMode>
            );
            """.trimIndent()
        )

        File(srcDir, "App.jsx").writeText(
            """
            import React, { useState } from 'react';

            export default function App() {
              const [count, setCount] = useState(0);

              return (
                <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '24px', textAlign: 'center' }}>
                  <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '16px', padding: '32px', maxWidth: '420px', width: '100%', boxShadow: '0 8px 30px rgba(0,0,0,0.5)' }}>
                    <div style={{ fontSize: '42px', marginBottom: '12px' }}>⚡</div>
                    <h1 style={{ fontSize: '24px', fontWeight: 'bold', margin: '0 0 8px 0', color: '#38bdf8' }}>$projectName</h1>
                    <p style={{ color: '#94a3b8', fontSize: '14px', lineHeight: 1.5, marginBottom: '24px' }}>
                      Running live on-device in AI Mobile Development Studio. Powered by rootless Linux environment.
                    </p>
                    <button 
                      onClick={() => setCount(c => c + 1)}
                      style={{ background: '#38bdf8', color: '#090d16', border: 'none', padding: '12px 24px', borderRadius: '8px', fontSize: '15px', fontWeight: '600', cursor: 'pointer' }}
                    >
                      Count is {count}
                    </button>
                  </div>
                </div>
              );
            }
            """.trimIndent()
        )

        File(sourceDir, "vite.config.js").writeText(
            """
            import { defineConfig } from 'vite';
            import react from '@vitejs/plugin-react';

            export default defineConfig({
              plugins: [react()],
              server: {
                host: '0.0.0.0',
                port: 3000
              }
            });
            """.trimIndent()
        )
    }

    private fun populateNodeTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "package.json").writeText(
            """
            {
              "name": "${projectName.lowercase().replace(" ", "-")}",
              "version": "1.0.0",
              "main": "server.js",
              "scripts": {
                "start": "node server.js",
                "dev": "node --watch server.js"
              },
              "dependencies": {
                "express": "^4.19.2",
                "cors": "^2.8.5"
              }
            }
            """.trimIndent()
        )

        File(sourceDir, "server.js").writeText(
            """
            const express = require('express');
            const app = express();
            const PORT = process.env.PORT || 3000;

            app.use(express.json());

            app.get('/', (req, res) => {
              res.json({
                message: "Hello from $projectName running in AI Mobile Development Studio!",
                runtime: "Node.js on PRoot Linux",
                uptime: process.uptime(),
                arch: process.arch
              });
            });

            app.get('/api/health', (req, res) => {
              res.json({ status: "healthy", timestamp: new Date().toISOString() });
            });

            app.listen(PORT, '0.0.0.0', () => {
              console.log(`Server listening on port ${'$'}{PORT}`);
            });
            """.trimIndent()
        )
    }

    private fun populateStaticWebTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "index.html").writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>$projectName</title>
              <link rel="stylesheet" href="style.css">
            </head>
            <body>
              <main class="container">
                <header>
                  <span class="badge">AI Mobile Studio</span>
                  <h1>$projectName</h1>
                  <p>Client-side HTML5 & Modern CSS web application</p>
                </header>
                <section class="card">
                  <p id="status-text">Interactive mobile workspace ready.</p>
                  <button id="action-btn">Trigger Action</button>
                </section>
              </main>
              <script src="app.js"></script>
            </body>
            </html>
            """.trimIndent()
        )

        File(sourceDir, "style.css").writeText(
            """
            :root {
              --bg: #090d16;
              --card: #131b2e;
              --accent: #38bdf8;
              --text: #f8fafc;
            }
            body {
              margin: 0;
              background: var(--bg);
              color: var(--text);
              font-family: system-ui, sans-serif;
              display: flex;
              justify-content: center;
              padding: 2rem;
            }
            .container {
              max-width: 480px;
              width: 100%;
            }
            .badge {
              background: #1e293b;
              color: var(--accent);
              padding: 4px 10px;
              border-radius: 9999px;
              font-size: 12px;
              font-weight: bold;
            }
            .card {
              background: var(--card);
              border: 1px solid #1e293b;
              border-radius: 12px;
              padding: 1.5rem;
              margin-top: 1.5rem;
            }
            button {
              background: var(--accent);
              color: #090d16;
              border: none;
              padding: 10px 18px;
              border-radius: 6px;
              font-weight: 600;
              cursor: pointer;
            }
            """.trimIndent()
        )

        File(sourceDir, "app.js").writeText(
            """
            console.log("$projectName loaded!");
            let clicks = 0;
            document.getElementById('action-btn')?.addEventListener('click', () => {
              clicks++;
              document.getElementById('status-text').textContent = `Activated ${'$'}{clicks} times inside mobile preview!`;
            });
            """.trimIndent()
        )
    }

    private fun populatePythonTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "main.py").writeText(
            """
            import sys
            import platform

            def main():
                print("=========================================")
                print(" $projectName ")
                print(" AI Mobile Development Studio - Python ")
                print("=========================================")
                print(f"Python Version: {platform.python_version()}")
                print(f"OS Architecture: {platform.machine()}")
                print(f"Process PID: {platform.node()}")
                print("Development workspace active.")

            if __name__ == '__main__':
                main()
            """.trimIndent()
        )

        File(sourceDir, "requirements.txt").writeText(
            """
            requests>=2.31.0
            pytest>=8.0.0
            """.trimIndent()
        )
    }

    private fun populateAndroidTemplate(sourceDir: File, projectName: String) {
        val javaDir = File(sourceDir, "app/src/main/java/com/mobileapp").apply { mkdirs() }
        File(sourceDir, "build.gradle.kts").writeText(
            """
            // Android Studio Mobile On-Device Build
            plugins {
                id("com.android.application") version "8.5.0" apply false
                id("org.jetbrains.kotlin.android") version "2.0.0" apply false
            }
            """.trimIndent()
        )

        File(sourceDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "$projectName"
            include(":app")
            """.trimIndent()
        )

        File(javaDir, "MainActivity.kt").writeText(
            """
            package com.mobileapp

            import android.os.Bundle
            import android.app.Activity
            import android.widget.TextView

            class MainActivity : Activity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    val tv = TextView(this)
                    tv.text = "Built on phone via AI Mobile Dev Studio!"
                    setContentView(tv)
                }
            }
            """.trimIndent()
        )
    }

    private fun populateCppTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "main.c").writeText(
            """
            #include <stdio.h>

            int main() {
                printf("AI Mobile Studio C/C++ Engine: $projectName\n");
                printf("Architecture: ARM64 / aarch64 native execution\n");
                return 0;
            }
            """.trimIndent()
        )

        File(sourceDir, "Makefile").writeText(
            """
            CC = clang
            CFLAGS = -Wall -O2

            all: app

            app: main.c
            	${'$'}(CC) ${'$'}(CFLAGS) -o app main.c

            clean:
            	rm -f app
            """.trimIndent()
        )
    }

    private fun populatePhpTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "index.php").writeText(
            """
            <?php
            echo "<h1>$projectName</h1>";
            echo "<p>Running PHP " . phpversion() . " in PRoot Linux</p>";
            ?>
            """.trimIndent()
        )
    }

    private fun populateNextJsTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "package.json").writeText(
            """
            {
              "name": "${projectName.lowercase().replace(" ", "-")}",
              "version": "0.1.0",
              "private": true,
              "scripts": {
                "dev": "next dev -p 3000",
                "build": "next build",
                "start": "next start"
              },
              "dependencies": {
                "next": "^14.2.0",
                "react": "^18.3.1",
                "react-dom": "^18.3.1"
              }
            }
            """.trimIndent()
        )
    }

    private fun populateGenericTemplate(sourceDir: File, projectName: String) {
        File(sourceDir, "README.md").writeText(
            """
            # $projectName
            Generated in AI Mobile Development Studio.
            Ready for AI agent instructions and rootless Linux commands.
            """.trimIndent()
        )
    }

    fun listFiles(projectId: String, relativeSubDir: String = ""): List<WorkspaceFileItem> {
        val base = getSourceDir(projectId)
        val target = if (relativeSubDir.isBlank()) base else File(base, relativeSubDir)
        if (!target.exists() || !target.isDirectory) return emptyList()

        return target.listFiles()?.map { file ->
            val rel = file.relativeTo(base).path
            WorkspaceFileItem(
                name = file.name,
                relativePath = rel,
                isDirectory = file.isDirectory,
                sizeBytes = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                extension = file.extension.lowercase()
            )
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
    }

    fun readFile(projectId: String, relativePath: String): String {
        val file = File(getSourceDir(projectId), relativePath)
        return if (file.exists() && file.isFile) file.readText() else ""
    }

    fun writeFile(projectId: String, relativePath: String, content: String) {
        val file = File(getSourceDir(projectId), relativePath)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun deleteFile(projectId: String, relativePath: String): Boolean {
        val file = File(getSourceDir(projectId), relativePath)
        return if (file.isDirectory) file.deleteRecursively() else file.delete()
    }

    fun renameFile(projectId: String, oldRelativePath: String, newName: String): Boolean {
        val oldFile = File(getSourceDir(projectId), oldRelativePath)
        val newFile = File(oldFile.parentFile ?: getSourceDir(projectId), newName)
        return oldFile.renameTo(newFile)
    }

    fun searchFiles(projectId: String, query: String): List<WorkspaceFileItem> {
        val base = getSourceDir(projectId)
        val matches = mutableListOf<WorkspaceFileItem>()
        base.walkTopDown().forEach { file ->
            if (file.name.contains(query, ignoreCase = true) || 
               (file.isFile && file.length() < 100_000 && file.readText().contains(query, ignoreCase = true))) {
                matches.add(
                    WorkspaceFileItem(
                        name = file.name,
                        relativePath = file.relativeTo(base).path,
                        isDirectory = file.isDirectory,
                        sizeBytes = file.length(),
                        lastModified = file.lastModified(),
                        extension = file.extension.lowercase()
                    )
                )
            }
        }
        return matches
    }

    fun exportProjectZip(projectId: String, outputStream: OutputStream) {
        val projectDir = getProjectDir(projectId)
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            projectDir.walkTopDown().forEach { file ->
                val entryPath = file.relativeTo(projectDir).path
                if (file.isDirectory) {
                    zipOut.putNextEntry(ZipEntry("$entryPath/"))
                    zipOut.closeEntry()
                } else {
                    zipOut.putNextEntry(ZipEntry(entryPath))
                    file.inputStream().use { input -> input.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
    }

    fun importProjectZip(projectId: String, inputStream: InputStream) {
        val projectDir = getProjectDir(projectId).apply { mkdirs() }
        ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val outFile = File(projectDir, entry.name)
                // Prevent Zip Slip
                if (!outFile.canonicalPath.startsWith(projectDir.canonicalPath)) {
                    throw SecurityException("Arbitrary file write attempt detected in zip archive")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fileOut ->
                        zipIn.copyTo(fileOut)
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }
}
