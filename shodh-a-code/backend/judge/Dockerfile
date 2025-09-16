# Minimal judge execution image
# Supports Java (javac/java) and Python (python3) for simple contest runs.
FROM openjdk:17-jdk-slim

# Install bash and python3 and utilities
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    python3 \
    python3-pip \
    ca-certificates \
    procps \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Create a non-root user to run submissions (optional - but we keep root for simplicity here)
# Expose nothing. JudgeService will mount host workspace into /workspace.

# Minimal entrypoint: keep /bin/bash available for -c commands from JudgeService
CMD ["/bin/bash"]
