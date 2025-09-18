package com.shodhacode.service;

import com.shodhacode.dto.JudgeResultDto;
import com.shodhacode.model.TestCaseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class JudgeService {

    @Value("${judge.image:shodh-judge:latest}")
    private String judgeImage;

    @Value("${judge.timeout.seconds:2}")
    private String timeoutSecondsStr;

    private int getTimeoutSeconds() {
        try {
            return Integer.parseInt(timeoutSecondsStr);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public JudgeResultDto runSubmissionInDocker(String language,
                                                String sourceCode,
                                                String submissionUuid,
                                                List<TestCaseEntity> testCases) throws IOException, InterruptedException {

        // Determine class name
        String className = "Main";
        if (sourceCode != null && sourceCode.contains("public class Solution")) {
            className = "Solution";
        }

        // Normalize/prepare source
        if (sourceCode == null) sourceCode = "";
        StringBuilder codeBuilder = new StringBuilder();
        if (!sourceCode.contains("java.util.Scanner")) {
            codeBuilder.append("import java.util.Scanner;\n");
        }
        if (!sourceCode.contains("java.io")) {
            codeBuilder.append("import java.io.*;\n");
        }
        codeBuilder.append(sourceCode);
        String normalizedSource = codeBuilder.toString().replace("\r\n", "\n").replace("\r", "");

        int passed = 0;
        int total = (testCases == null) ? 0 : testCases.size();
        StringBuilder combinedDetails = new StringBuilder();
        if (total == 0) combinedDetails.append("No test cases provided\n");

        List<TestCaseEntity> tcList = (testCases == null) ? Collections.emptyList() : testCases;

        // Create a safe docker volume name (docker volume names must be simple)
        String volumeName = "shodh_judge_" + submissionUuid.replaceAll("[^a-zA-Z0-9_.-]", "_");

        // STEP A: Compile once into a docker volume
        List<String> compileCmd = Arrays.asList(
                "docker", "run", "--rm",
                "-i",
                "-v", volumeName + ":/workspace",
                judgeImage,
                "bash", "-c",
                String.format("cat > /workspace/%1$s.java && javac /workspace/%1$s.java", className)
        );

        ProcessBuilder compilePb = new ProcessBuilder(compileCmd);
        compilePb.redirectErrorStream(true);

        Process compileProcess = null;
        try {
            compileProcess = compilePb.start();

            // send source only (no input)
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(compileProcess.getOutputStream()))) {
                w.write(normalizedSource);
                w.flush();
                // close stream to signal EOF to container's cat
            }

            boolean compiled = compileProcess.waitFor(getTimeoutSeconds() + 3L, TimeUnit.SECONDS);
            String compileOutput;
            try (InputStream is = compileProcess.getInputStream()) {
                compileOutput = new String(is.readAllBytes());
            }

            if (!compiled || compileProcess.exitValue() != 0) {
                // Compilation failed: return Compilation Error immediately
                combinedDetails.append("Compilation failed:\n").append(compileOutput).append("\n");
                return new JudgeResultDto("Compilation Error", combinedDetails.toString(), 0, total);
            }
        } finally {
            if (compileProcess != null && compileProcess.isAlive()) compileProcess.destroyForcibly();
        }

        // STEP B: For each testcase, run the compiled class and feed only the testcase input
        for (TestCaseEntity tc : tcList) {
            List<String> runCmd = Arrays.asList(
                    "docker", "run", "--rm",
                    "-i",
                    "-v", volumeName + ":/workspace",
                    judgeImage,
                    "bash", "-c",
                    String.format("timeout %2$ds java -cp /workspace %1$s", className, getTimeoutSeconds())
            );

            ProcessBuilder runPb = new ProcessBuilder(runCmd);
            runPb.redirectErrorStream(true);

            Process runProc = null;
            try {
                runProc = runPb.start();

                // Send only testcase input (do NOT re-send the source here)
                try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(runProc.getOutputStream()))) {
                    if (tc.getInputData() != null && !tc.getInputData().isEmpty()) {
                        // ensure newline endings are correct
                        String input = tc.getInputData().replace("\r\n", "\n");
                        w.write(input);
                        if (!input.endsWith("\n")) w.newLine();
                    }
                    w.flush();
                    // close stream to signal EOF to java process
                }

                boolean finished = runProc.waitFor(getTimeoutSeconds() + 3L, TimeUnit.SECONDS);
                if (!finished) {
                    runProc.destroyForcibly();
                    combinedDetails.append("Testcase input:\n").append(tc.getInputData())
                            .append("\nResult: TIMEOUT\n\n");
                    continue;
                }

                String output;
                try (InputStream is = runProc.getInputStream()) {
                    output = new String(is.readAllBytes());
                }

                String actual = normalizeOutput(output);
                String expected = normalizeOutput(tc.getExpectedOutput());

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

                System.out.println("=== Comparing Outputs ===");
                System.out.println("Input: " + tc.getInputData());
                System.out.println("Expected (raw): [" + tc.getExpectedOutput() + "]");
                System.out.println("Actual (raw): [" + output + "]");
                System.out.println("Expected (normalized): [" + expected + "]");
                System.out.println("Actual (normalized): [" + actual + "]");

            } finally {
                if (runProc != null && runProc.isAlive()) runProc.destroyForcibly();
            }
        }

        // Cleanup: remove the named docker volume (best-effort)
        try {
            ProcessBuilder rmVolPb = new ProcessBuilder("docker", "volume", "rm", "-f", volumeName);
            Process rmVol = rmVolPb.start();
            rmVol.waitFor(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        String status;
        if (total > 0 && passed == total) status = "Accepted";
        else if (total > 0 && passed == 0) status = "Wrong Answer";
        else if (total > 0) status = "Partially Accepted";
        else status = "No Testcases";

        return new JudgeResultDto(status, combinedDetails.toString(), passed, total);
    }

    private String normalizeOutput(String s) {
        if (s == null) return "";
        return s.replace("\r", "").trim();
    }
}
