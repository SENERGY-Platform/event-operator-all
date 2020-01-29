import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class TriggerServerMock {

    public static HttpServer create(Consumer<InputStream> callback) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/endpoint", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = "{\"success\": true}".getBytes();
                callback.accept(exchange.getRequestBody());
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        });
        httpServer.start();
        return httpServer;
    }
}
