package org.pionerds.ftc.teamcode;

import static java.nio.file.Files.walk;

import android.content.Context;
import android.util.Log;
import android.view.Menu;

import androidx.tracing.perfetto.handshake.protocol.Response;

import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.util.WebHandlerManager;

import org.firstinspires.ftc.ftccommon.external.OnCreate;
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop;
import org.firstinspires.ftc.ftccommon.external.OnCreateMenu;
import org.firstinspires.ftc.ftccommon.external.OnDestroy;
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar;
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

import fi.iki.elonen.NanoHTTPD;

public class Dashboard {
    @OnCreate
    public static void start(Context context) {
        Log.i("Robot-Observer", "starting");
    }

    @OnDestroy
    public static void stop(Context context) {}

    @OnCreateMenu
    public static void populateMenu(Context context, Menu menu) {}

    @WebHandlerRegistrar
    public static void attachWebServer(Context context, WebHandlerManager manager)  {
        try {
            File file = new File(context.getFilesDir() + "/web");

            ArrayList<String> files = walkFolder(file);

            for (String currentFileURI : files) {
                File currentFile = new File(currentFileURI);
                URLConnection connection = file.toURL().openConnection();
                String mimeType = connection.getContentType();

                Log.i("HTTP_FILE_SERVER", mimeType);

                manager.register("PioNerds-dashboard/" + currentFileURI, session ->
                        NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, mimeType, new FileInputStream(currentFile)));
            }
        } catch(Exception e) {
        }

//        manager.register();
    }

    private static ArrayList<String> walkFolder(File root) {
        File[] list = root.listFiles();
        ArrayList<String> output = new ArrayList<>();

        walkFolder(root, output);

        return output;
    }

    private static ArrayList<String> walkFolder(File root, ArrayList<String> output) {
        File[] list = root.listFiles();

        assert list != null;

        for (File f : list) {
            if (f.isDirectory()) {
                walkFolder(f);
            } else {
                output.add(f.getAbsolutePath());
            }
        }

        return output;
    }

    @OnCreateEventLoop
    public static void attachEventLoop(Context context, FtcEventLoop eventLoop) {}

    @OpModeRegistrar
    public static void registerOpMode(OpModeManager manager) {}
}
