package com.nextech.moadream.server.v1.global.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class MockApiServerRunner implements ApplicationRunner {

    private Process mockServerProcess;
    private static final int MOCK_SERVER_PORT = 9000;
    private static final int MAX_STARTUP_WAIT = 30;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isMockServerRunning()) {
            log.info("Mock API server is already running on port {}", MOCK_SERVER_PORT);
            return;
        }

        if (!isPythonInstalled()) {
            log.warn("Python is not installed. Mock server will not be started.");
            log.warn("Please install Python 3.x and try again.");
            return;
        }

        if (!areDependenciesInstalled()) {
            log.warn("Required Python dependencies are not installed. Mock server will not be started.");
            log.warn("Please run: cd mock-api && pip install -r requirements.txt");
            return;
        }

        startMockServer();
        registerShutdownHook();
    }

    private boolean isPythonInstalled() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String pythonCmd = os.contains("win") ? "python" : "python3";

            ProcessBuilder pb = new ProcessBuilder(pythonCmd, "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String version = reader.readLine();
                    log.info("Detected Python: {}", version);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("Python check failed", e);
            return false;
        }
    }

    private boolean areDependenciesInstalled() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String pythonCmd = os.contains("win") ? "python" : "python3";

            ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-c", "import flask; import werkzeug");
            pb.directory(new File("mock-api"));
            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (Exception e) {
            log.debug("Dependencies check failed", e);
            return false;
        }
    }

    private boolean isMockServerRunning() {
        try {
            URL url = new URL("http://localhost:" + MOCK_SERVER_PORT + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void startMockServer() {
        try {
            File mockApiDir = new File("mock-api");
            if (!mockApiDir.exists()) {
                log.warn("mock-api directory not found. Mock server will not be started.");
                return;
            }

            File appPy = new File(mockApiDir, "app.py");
            if (!appPy.exists()) {
                log.warn("app.py not found in mock-api directory. Mock server will not be started.");
                return;
            }

            log.info("Starting Mock API server...");

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(mockApiDir);

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                processBuilder.command("python", "app.py");
            } else {
                processBuilder.command("python3", "app.py");
            }

            processBuilder.redirectErrorStream(true);
            mockServerProcess = processBuilder.start();

            final Thread outputThread = getOutputThread();
            outputThread.start();

            waitForServerStartup();

        } catch (Exception e) {
            log.error("Failed to start Mock API server", e);
        }
    }

    private Thread getOutputThread() {
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(mockServerProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("Mock API: {}", line);
                }
            } catch (Exception e) {
                log.error("Error reading mock server output", e);
            }
        });
        outputThread.setDaemon(true);
        return outputThread;
    }

    private void waitForServerStartup() throws InterruptedException {
        for (int i = 0; i < MAX_STARTUP_WAIT; i++) {
            if (isMockServerRunning()) {
                log.info("Mock API server started successfully on port {}", MOCK_SERVER_PORT);
                return;
            }
            Thread.sleep(1000);
        }
        log.warn("Mock API server did not start within {} seconds", MAX_STARTUP_WAIT);
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mockServerProcess != null && mockServerProcess.isAlive()) {
                log.info("Stopping Mock API server...");
                mockServerProcess.destroy();
                try {
                    mockServerProcess.waitFor();
                    log.info("Mock API server stopped");
                } catch (InterruptedException e) {
                    log.error("Error stopping mock server", e);
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }
}
