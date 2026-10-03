package org.pionerds.ftc.teamcode;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.webkit.MimeTypeMap;

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
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.FtcWebSocket;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.FtcWebSocketMessage;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.WebSocketManager;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.WebSocketNamespaceHandler;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import fi.iki.elonen.NanoHTTPD;

public class Dashboard {
    private static final String TAG = "Dashboard";
    private static final String ASSET_ROOT = "web";
    private static final String ROUTE_PREFIX = "/dashboard";
    private static final String INDEX_FILE = "index.html";
    private static final String WS_NAMESPACE = "dashboard";

    private static volatile WebSocketManager webSocketManager;
    private static volatile boolean webSocketRegistered = false;

    @OnCreate
    public static void start(Context context) {
        Log.i(TAG, "starting");

        AtomicReference<UUID> newInfo = new AtomicReference<>();
        AtomicReference<UUID> errorInfo = new AtomicReference<>();
        AtomicReference<UUID> warnInfo = new AtomicReference<>();

        Scheduler.addTask("init", (obj) -> {
            newInfo.set(Scheduler.addTask("log:new:info", (str) -> {
                Dashboard.broadcast("log", "{ \"type\": \"info\", \"tag\": \"Core\", \"msg\":\"" + str + "\" }");
            }));

            warnInfo.set(Scheduler.addTask("log:new:warn", (str) -> {
                Dashboard.broadcast("log", "{ \"type\": \"warn\", \"tag\": \"Core\", \"msg\":\"" + str + "\" }");
            }));

            errorInfo.set(Scheduler.addTask("log:new:error", (str) -> {
                Dashboard.broadcast("log", "{ \"type\": \"error\", \"tag\": \"Core\", \"msg\":\"" + str + "\" }");
            }));

            Scheduler.addTask("exit", (obj2) -> {
                Scheduler.removeTask(newInfo.get());
                Scheduler.removeTask(warnInfo.get());
                Scheduler.removeTask(errorInfo.get());
            });
        });
    }

    @OnDestroy
    public static void stop(Context context) {}

    @OnCreateMenu
    public static void populateMenu(Context context, Menu menu) {}

    @WebHandlerRegistrar
    public static void attachWebServer(Context context, WebHandlerManager manager) {
        try {
            AssetManager assets = context.getAssets();

            ArrayList<String> files = walkAssets(assets, ASSET_ROOT);

            for (String assetPath : files) {
                String relative = assetPath.substring(ASSET_ROOT.length() + 1);
                String route = ROUTE_PREFIX + "/" + relative;

                manager.register(route, assetHandler(assets, assetPath, guessMimeType(relative)));
            }

            manager.register(ROUTE_PREFIX, session -> redirect(ROUTE_PREFIX + "/"));
            manager.register(ROUTE_PREFIX + "/",
                    assetHandler(assets, ASSET_ROOT + "/" + INDEX_FILE, guessMimeType(INDEX_FILE)));
        } catch (Exception e) {
            Log.e(TAG, "Failed to register dashboard assets", e);
        }

        manager.register(ROUTE_PREFIX + "/api/get-health", session -> NanoHTTPD.newFixedLengthResponse("ok"));

        WebSocketManager sockets = manager.getWebServer().getWebSocketManager();

        if (sockets != null && !webSocketRegistered) {
            sockets.registerNamespaceHandler(new DashboardSocketHandler());
            webSocketManager = sockets;
            webSocketRegistered = true;
        }
    }

    /**
     * Broadcast a message to every dashboard WebSocket subscribed to {@link #WS_NAMESPACE}.
     *
     * @param type a short message type, e.g. "log"
     * @param payload an optional payload, typically a JSON string
     * @return the number of clients the message was sent to
     */
    public static int broadcast(String type, String payload) {
        WebSocketManager sockets = webSocketManager;
        if (sockets == null) return 0;
        return sockets.broadcastToNamespace(WS_NAMESPACE, new FtcWebSocketMessage(WS_NAMESPACE, type, payload));
    }

    private static final class DashboardSocketHandler extends WebSocketNamespaceHandler {
        DashboardSocketHandler() {
            super(WS_NAMESPACE);
        }

        @Override
        public void onSubscribe(FtcWebSocket webSocket) {
            Log.i(TAG, "ws subscribed: " + webSocket.getRemoteHostname());
            webSocket.send(new FtcWebSocketMessage(WS_NAMESPACE, "welcome", "{\"status\":\"ok\"}"));
        }

        @Override
        public void onUnsubscribe(FtcWebSocket webSocket) {
            Log.i(TAG, "ws unsubscribed: " + webSocket.getRemoteHostname());
        }

        @Override
        public boolean onMessage(FtcWebSocketMessage message, FtcWebSocket webSocket) {
            if (super.onMessage(message, webSocket)) return true;

            webSocket.send(new FtcWebSocketMessage(WS_NAMESPACE, "echo", message.getPayload()));
            return true;
        }
    }

    private static WebHandler assetHandler(AssetManager assets, String assetPath, String mimeType) {
        return session -> {
            try {
                InputStream stream = assets.open(assetPath);
                return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, mimeType, stream);
            } catch (IOException e) {
                return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
            }
        };
    }

    private static NanoHTTPD.Response redirect(String location) {
        NanoHTTPD.Response response =
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REDIRECT, NanoHTTPD.MIME_PLAINTEXT, "");
        response.addHeader("Location", location);
        return response;
    }

    private static ArrayList<String> walkAssets(AssetManager assets, String path) throws IOException {
        ArrayList<String> output = new ArrayList<>();
        walkAssets(assets, path, output);
        return output;
    }

    private static void walkAssets(AssetManager assets, String path, ArrayList<String> output) throws IOException {
        String[] children = assets.list(path);

        if (children == null || children.length == 0) {
            output.add(path);
            return;
        }

        for (String child : children) {
            walkAssets(assets, path + "/" + child, output);
        }
    }

    private static String guessMimeType(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String extension = name.substring(dot + 1).toLowerCase(Locale.US);

            if (extension.equals("js")) return "text/javascript";

            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType != null) return mimeType;
        }
        return "application/octet-stream";
    }

    @OnCreateEventLoop
    public static void attachEventLoop(Context context, FtcEventLoop eventLoop) {}

    @OpModeRegistrar
    public static void registerOpMode(OpModeManager manager) {}
}
