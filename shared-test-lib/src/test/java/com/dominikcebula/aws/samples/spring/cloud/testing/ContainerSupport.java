package com.dominikcebula.aws.samples.spring.cloud.testing;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.Container.ExecResult;

import java.io.IOException;

@Slf4j
public class ContainerSupport {
    public static void execInContainer(Container<?> container, String... command) throws IOException, InterruptedException {
        ExecResult execResult = container.execInContainer(command);
        int exitCode = execResult.getExitCode();

        if (exitCode != 0) {
            String commandStr = String.join(" ", command);

            log.error("Command [{}] did not returned with 0 exit code, exit code was [{}]", commandStr, exitCode);
            log.error("Command stdout: {}", execResult.getStdout());
            log.error("Command stderr: {}", execResult.getStderr());

            throw new IllegalStateException("Command [%s] failed with exit code: [%d]".formatted(commandStr, exitCode));
        }
    }
}
