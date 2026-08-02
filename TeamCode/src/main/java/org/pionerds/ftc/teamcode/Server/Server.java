package org.pionerds.ftc.teamcode.Server;
import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket socket;

    Server() {
        try {
            socket = new ServerSocket(8080); // Port number
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hook() {

    }
}
