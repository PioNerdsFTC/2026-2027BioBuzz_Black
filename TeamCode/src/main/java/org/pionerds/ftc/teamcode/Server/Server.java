package org.pionerds.ftc.teamcode.Server;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class Server {
    private static Http http;

    static {
        if (!Parameters.competing) {
            UUID init = Scheduler.addTask("init", (obj) -> {
                try {
                    Server.http = new Http();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Scheduler.addTask("exit", (obj) -> {
                Scheduler.removeTask(init);
            });
        }
    }
}
