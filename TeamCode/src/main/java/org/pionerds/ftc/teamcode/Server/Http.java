package org.pionerds.ftc.teamcode.Server;

import android.content.Context;
import android.util.Log;

import org.pionerds.ftc.teamcode.Hardware.Hardware;

import java.io.IOException;
import java.util.Arrays;

import fi.iki.elonen.NanoHTTPD;

public class Http extends NanoHTTPD {
    Context context = Hardware.mapping.getAppContext();

    public Http() throws IOException {
        super("0.0.0.0", 8000);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Log.i("HTTP", "Running server on port 8000");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.startsWith("/api")) {
            switch (uri) {
                case "/api/health":
                    return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "healthy");
            }
            return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 not found");
        }


        // Static file
        uri = uri.replaceFirst("^/", "");
        if (uri.isEmpty()) {
            uri = "index.html";
        }

        try {
            String mime;
            switch (uri.substring(uri.lastIndexOf('.') + 1)) {
                case "ico":
                    mime = "image/x-icon";
                    break;
                case "css":
                    mime = "text/css";
                    break;
                case "html":
                    mime = "text/html";
                    break;
                case "json":
                    mime = "application/json";
                    break;
                case "js":
                    mime = "text/javascript";
                    break;
                default:
                    mime = "text/plain";
                    break;
            }

            return NanoHTTPD.newChunkedResponse(
                    Response.Status.OK,
                    mime,
                    context.getAssets().open("web/" + uri)
            );
        } catch (Exception e) {
            String message = "Failed to load asset " + uri + " because " + e;
            Log.e("Server", message);
            Log.e("Server", Arrays.toString(e.getStackTrace()));
            return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, message);
        }
    }
}
