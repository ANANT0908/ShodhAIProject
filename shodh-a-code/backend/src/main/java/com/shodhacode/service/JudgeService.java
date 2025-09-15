package com.shodhacode.service;

import com.shodhacode.dto.JudgeResultDto;
import com.shodhacode.model.TestCaseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JudgeService:
 * - Writes user source to a temp directory
 * - Invokes docker run of the judge image and runs compile+run commands inside it
 * - Enforces timeout and memory/cpu limits via docker flags
 *
 * NOTE:
 * - This simple implementation runs 'docker' CLI on the host where backend runs.
 * - Ensure the user running this backend has permission to run docker.
 * - For production, use hardened sandboxes (gVisor, Firecracker, or a dedicated judge service).
 */
@Service
public class JudgeService {

    @Value("${judge.image:shodh-judge:latest}")
    private String judgeImage;

    @Value("${judge.timeout-seconds:3}")
    private int timeoutSeconds;

    @Value("${judge.memory-limit:128m}")
    private String memoryLimit;

    @Value("${judge.cpus:0.5}")
    private String cpus;

    /**
     * Run a submission in Docker.
     *
     * @param submissionUuid unique id (used to create temp dir)
     * @param language e.g. "java", "python"
     * @param sourceCode user code
     * @param testCases list of testcases
     * @return JudgeResultDto
     * @throws IOException
     * @throws InterruptedException
     */
    public JudgeResultDto runSubmissionInDocker(String submissionUuid, String language, String sourceCode, List<TestCaseEntity> testCases) throws IOException, InterruptedException {
        Path tmpBase = Files.createTempDirectory("judge-" + submissionUuid);
        try {
            // Map language to file name and commands
            String filename;
            String compileCmd = null;
            String runCmd; // command executed inside container
            if ("java".equalsIgnoreCase(language)) {
                filename = "Main.java";
                Files.writeString(tmpBase.resolve(filename), sourceCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                compileCmd = "javac Main.java";
                runCmd = "java Main";
            } else if ("python".equalsIgnoreCase(language) || "py".equalsIgnoreCase(language)) {
                filename = "solution.py";
                Files.writeString(tmpBase.resolve(filename), sourceCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                runCmd = "python3 solution.py";
            } else {
                // fallback - store code and try to run with 'bash'
                filename = "script.txt";
                Files.writeString(tmpBase.resolve(filename), sourceCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                runCmd = "cat script.txt";
            }

            int passed = 0;
            int total = testCases.size();
            StringBuilder combinedDetails = new StringBuilder();

            // For each test, run inside docker to keep separation between runs (simple approach)
            for (TestCaseEntity tc : testCases) {
                // Build docker command: mount tmpBase to /workspace, workdir /workspace
                List<String> cmd = new ArrayList<>();
                cmd.add("docker");
                cmd.add("run");
                cmd.add("--rm");
                cmd.add("--network");
                cmd.add("none"); // disable network for safety
                cmd.add("--memory");
                cmd.add(memoryLimit);
                cmd.add("--cpus");
                cmd.add(cpus);
                cmd.add("-v");
                cmd.add(tmpBase.toAbsolutePath().toString() + ":/workspace");
                cmd.add("-w");
                cmd.add("/workspace");
                cmd.add(judgeImage);
                // Compose final inner command: optionally compile, then run with timeout
                String inner;
                if (compileCmd != null) {
                    // compile then run for single test
                    inner = String.format("%s && timeout %ds /bin/bash -c '%s'", compileCmd, timeoutSeconds, runCmd);
                } else {
                    inner = String.format("timeout %ds /bin/bash -c '%s'", timeoutSeconds, runCmd);
                }
                // use /bin/bash -c to run sequence
                cmd.add("/bin/bash");
                cmd.add("-lc");
                cmd.add(inner);

                ProcessBuilder pb = new ProcessBuilder(cmd);
                // Important: redirect error stream to capture all output
                pb.redirectErrorStream(true);
                Process p = pb.start();

                // write stdin
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
                    writer.write(tc.getInputData() == null ? "" : tc.getInputData());
                    writer.flush();
                } catch (IOException e) {
                    // ignore if pipe closed
                }

                boolean finished = p.waitFor(timeoutSeconds + 2L, TimeUnit.SECONDS);
                String output;
                if (!finished) {
                    p.destroyForcibly();
                    output = "TIMEOUT or KILLED\n";
                    combinedDetails.append("Testcase input:\n").append(tc.getInputData()).append("\nResult: TIMEOUT\n\n");
                } else {
                    try (InputStream is = p.getInputStream()) {
                        output = new String(is.readAllBytes());
                    }
                    // normalize outputs (trim endings)
                    String actual = normalize(output);
                    String expected = normalize(tc.getExpectedOutput());
                    if (actual.equals(expected)) {
                        passed++;
                        combinedDetails.append("Testcase passed\n");
                    } else {
                        combinedDetails.append("Testcase failed\n");
                        combinedDetails.append("Input:\n").append(tc.getInputData()).append("\n");
                        combinedDetails.append("Expected:\n").append(tc.getExpectedOutput()).append("\n");
                        combinedDetails.append("Actual:\n").append(output).append("\n");
                    }
                    combinedDetails.append("\n---\n");
                }
            } // end for each test

            String status;
            if (passed == total) {
                status = "Accepted";
            } else if (passed == 0) {
                status = "Wrong Answer";
            } else {
                status = "Partially Accepted";
            }

            return new JudgeResultDto(status, combinedDetails.toString(), passed, total);
        } finally {
            // cleanup: delete temp dir
            try {
                deleteDirectoryRecursively(tmpBase);
            } catch (IOException ex) {
                // log warning but not fail
                ex.printStackTrace();
            }
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        // normalize newlines and trim trailing spaces on lines
        String[] lines = s.replace("\r", "").split("\n");
        StringBuilder b = new StringBuilder();
        for (String line : lines) {
            b.append(line.stripTrailing()).append("\n");
        }
        return b.toString().trim();
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
