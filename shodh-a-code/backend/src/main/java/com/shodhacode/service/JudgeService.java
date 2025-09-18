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
     * param order: language, sourceCode, submissionUuid, testCases
     */
    public JudgeResultDto runSubmissionInDocker(String language,
                                                String sourceCode,
                                                String submissionUuid,
                                                List<TestCaseEntity> testCases) throws IOException, InterruptedException {
        Path tmpBase = Files.createTempDirectory("judge-" + submissionUuid);
        try {
            // Only Java is supported by your request
            String className = "Main";
            if (sourceCode != null && sourceCode.contains("public class Solution")) {
                className = "Solution";
            }
            String filename = className + ".java";

            // write file (normalized source)
            String normalizedSource = (sourceCode == null) ? "" : sourceCode.replace("\\r\\n", "\n").replace("\\n", "\n").replaceAll("\r", "");
            Files.writeString(tmpBase.resolve(filename), normalizedSource, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String compileCmd = "javac " + filename;
            String runCmd = "java " + className;

            int passed = 0;
            int total = (testCases == null) ? 0 : testCases.size();
            StringBuilder combinedDetails = new StringBuilder();

            if (total == 0) {
                // no testcases — treat as error (caller should have checked, but defensive)
                combinedDetails.append("No test cases provided\n");
                return new JudgeResultDto("Error", combinedDetails.toString(), 0, 0);
            }

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

                // Compose the inner command: compile (stop on compile error) then run with timeout
                String inner;
                if (compileCmd != null) {
                    inner = String.format("%s && timeout %ds /bin/bash -c '%s'", compileCmd, timeoutSeconds, runCmd);
                } else {
                    inner = String.format("timeout %ds /bin/bash -c '%s'", timeoutSeconds, runCmd);
                }

                cmd.add("/bin/bash");
                cmd.add("-lc");
                cmd.add(inner);

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();

                // Provide stdin (test input)
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
                    if (tc.getInputData() != null) {
                        writer.write(tc.getInputData());
                    }
                    writer.flush();
                } catch (IOException ignored) {}

                boolean finished = p.waitFor(timeoutSeconds + 2L, TimeUnit.SECONDS);
                String output;
                if (!finished) {
                    p.destroyForcibly();
                    output = "TIMEOUT\n";
                    combinedDetails.append("Testcase input:\n").append(tc.getInputData()).append("\nResult: TIMEOUT\n\n");
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
            // cleanup
            try {
                deleteDirectoryRecursively(tmpBase);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
