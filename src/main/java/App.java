import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.Handlers;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import org.jcodec.api.JCodecException;


import java.io.InputStream;
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import VideoCompress.VideoCompress;

public class App {

    private static final String HTML_FILE_PATH = "index.html";

    public static void main(final String[] args) {

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        if (exchange.getRequestMethod().equalToString("POST") &&
                                exchange.getRequestPath().equals("/upload")) {
                            handleUpload(exchange);
                        } else {
                            sendHtmlPage(exchange);
                        }
                    }
                })
                .setServerOption(UndertowOptions.MAX_HEADER_SIZE, 900_000)
                .build();

        server.start();
    }

    private static void handleUpload(HttpServerExchange exchange) throws IOException {
        FormDataParser parser = exchange.getAttachment(FormDataParser.ATTACHMENT_KEY);
        if (parser == null) {
            
            exchange.getResponseSender().send("Bad Request: No form data found");
            return;
        }

        parser.parse(formData -> {
            FormData.FormValue file = formData.getFirst("videoFile");
            if (file != null && file.isFile()) {
                try {
                    File uploadedFile = saveUploadedFile(file);
                    String inputFilePath = uploadedFile.getAbsolutePath();
                    String outputFilePath = "processed_video.mp4"; // Output path for processed video
                    int triangleSize = 1; // Adjust triangle size as needed
                    long startTime = System.currentTimeMillis();
                    VideoCompress.compressVideo(inputFilePath, outputFilePath, triangleSize);
                    long endTime = System.currentTimeMillis();
                    long processingTime = endTime - startTime;
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Video processing completed in " + processingTime + " milliseconds");
                } catch (IOException | JCodecException e) {
                    e.printStackTrace();
                    
                    exchange.getResponseSender().send("Internal Server Error");
                }
            } else {
                
                exchange.getResponseSender().send("No file uploaded");
            }
        });
    }

    private static void sendHtmlPage(HttpServerExchange exchange) throws IOException {
        String htmlContent = readFile(HTML_FILE_PATH);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(htmlContent);
    }

    private static File saveUploadedFile(FormData.FormValue file) throws IOException {
        File outputFile = File.createTempFile("uploaded_video", ".mp4");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            file.getFileItem().write(outputStream);
        }
        return outputFile;
    }


    private static String readFile(String file) {
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
    }
    
    
}






