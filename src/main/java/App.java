import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class App {

    private static final String HTML_FILE_PATH = "index.html";
    private static final String JS_FILE_PATH = "js/messages.js";
    private static final String CSS_FILE_PATH = "css/styles.css";

    public static void main(final String[] args) {

        Undertow server = Undertow.builder().addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {

                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {

                        String key = exchange.getQueryParameters().get("key") != null
                                ? exchange.getQueryParameters().get("key").getFirst()
                                : null;
                        String requestPath = exchange.getRequestPath();

                        if (requestPath.startsWith("/css/")) {

                            String htmlContent = readHtmlFile(CSS_FILE_PATH);
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                            exchange.getResponseSender().send(htmlContent);
                        } else if (requestPath.startsWith("/js/")) {
                            String htmlContent = readHtmlFile(JS_FILE_PATH);
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                            exchange.getResponseSender().send(htmlContent);
                        } else {

                            if (key != null) {


                                exchange.startBlocking();
                                String responseMessage = "wajaka forever";
                                exchange.getResponseSender().send(responseMessage);





                            } else {
                                String htmlContent = readHtmlFile(HTML_FILE_PATH);
                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                                exchange.getResponseSender().send(htmlContent);
                            }
                        }
                    }

                }).setServerOption(UndertowOptions.MAX_HEADER_SIZE, 900_000).build();

        server.start();
    }

    private static String readHtmlFile(String file) {
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream(file)) {
            if (inputStream != null) {
                try (Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A")) {
                    return scanner.hasNext() ? scanner.next() : "";
                }
            } else {
                return "HTML file not found";
            }
        } catch (IOException e) {
            // Handle the IOException or log the exception
            e.printStackTrace();
            return "Error leyendo archivo HTML principal: debe estar en src\\resources\\html";
        }
    } // Request send

}
