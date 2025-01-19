package com.dominikcebula.aws.samples.spring.cloud.shipment.testing;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;

import java.io.IOException;

@Slf4j
public class ContainerSupport {
    public static void execInContainer(Container<?> container, String... command) throws IOException, InterruptedException {
        Container.ExecResult execResult = container.execInContainer(command);
        int exitCode = execResult.getExitCode();

        if (exitCode != 0) {
            String commandStr = String.join(" ", command);

            log.info("Command [{}] did not returned with 0 exit code, exit code was [{}]", commandStr, exitCode);
            log.info("Command stdout: {}", execResult.getStdout());
            log.info("Command stderr: {}", execResult.getStderr());

            throw new IllegalStateException("Command [%s] failed with exit code: [%d]".formatted(commandStr, exitCode));
        }
    }
}
