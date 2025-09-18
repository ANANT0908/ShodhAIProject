// File: backend/src/main/java/com/shodhacode/service/JudgeService.java
package com.shodhacode.service;

import com.shodhacode.dto.JudgeResultDto;
import com.shodhacode.model.TestCaseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
     * @param language       e.g. "java", "python"
     * @param sourceCode     user code
     * @param submissionUuid unique id (used to create temp dir)
     * @param testCases      list of testcases
     */
    public JudgeResultDto runSubmissionInDocker(String language,
                                                String sourceCode,
                                                String submissionUuid,
                                                List<TestCaseEntity> testCases) throws IOException, InterruptedException {
        Path tmpBase = Files.createTempDirectory("judge-" + submissionUuid);
        try {
            String filename;
            String compileCmd = null;
            String runCmd;

                String className = "Main";
                if (sourceCode.contains("public class Solution")) {
                    className = "Solution";
                }
                filename = className + ".java";

                Files.writeString(tmpBase.resolve(filename), sourceCode,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                compileCmd = "javac " + filename;
                runCmd = "java " + className;
            

            int passed = 0;
            int total = testCases.size();
            StringBuilder combinedDetails = new StringBuilder();

            for (TestCaseEntity tc : testCases) {
                List<String> cmd = new ArrayList<>();
                cmd.add("docker");
                cmd.add("run");
                cmd.add("--rm");
                cmd.add("--network");
                cmd.add("none");
                cmd.add("--memory");
                cmd.add(memoryLimit);
                cmd.add("--cpus");
                cmd.add(cpus);
                cmd.add("-v");
                cmd.add(tmpBase.toAbsolutePath().toString() + ":/workspace");
                cmd.add("-w");
                cmd.add("/workspace");
                cmd.add(judgeImage);

                // Build execution inside container
                StringBuilder inner = new StringBuilder();
                if (compileCmd != null) {
                    inner.append(compileCmd).append(" || exit 1; "); // stop if compile fails
                }
                inner.append("timeout ").append(timeoutSeconds)
                        .append("s /bin/bash -c '").append(runCmd).append("'");

                cmd.add("/bin/bash");
                cmd.add("-lc");
                cmd.add(inner.toString());

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();

                // Feed input
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
                    if (tc.getInputData() != null) {
                        writer.write(tc.getInputData());
                    }
                }

                boolean finished = p.waitFor(timeoutSeconds + 2L, TimeUnit.SECONDS);
                String output;
                if (!finished) {
                    p.destroyForcibly();
                    output = "TIMEOUT\n";
                    combinedDetails.append("Input:\n").append(tc.getInputData())
                            .append("\nResult: TIMEOUT\n\n");
                } else {
                    try (InputStream is = p.getInputStream()) {
                        output = new String(is.readAllBytes());
                    }
                    String actual = normalize(output);
                    String expected = normalize(tc.getExpectedOutput());
                    if (actual.equals(expected)) {
                        passed++;
                        combinedDetails.append("Testcase passed\n");
                    } else {
                        combinedDetails.append("Testcase failed\n")
                                .append("Input:\n").append(tc.getInputData()).append("\n")
                                .append("Expected:\n").append(expected).append("\n")
                                .append("Actual:\n").append(actual).append("\n");
                    }
                    combinedDetails.append("\n---\n");
                }
            }

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
            deleteDirectoryRecursively(tmpBase);
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
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
