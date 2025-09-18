# Use OpenJDK as base image
FROM openjdk:17-jdk-slim

# Install additional tools needed for judging
RUN apt-get update && apt-get install -y \
    timeout \
    bash \
    coreutils \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /workspace

# Verify Java installation
RUN java -version && javac -version

# Default command
CMD ["/bin/bash"]