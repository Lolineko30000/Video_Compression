import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import org.jcodec.api.JCodecException;

import ConcurrentVideoCompress.ConcurrentVideoCompress;
import SecuentialVideoCompress.VideoCompress;
import io.undertow.server.handlers.form.MultiPartParserDefinition;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.InputStream;
import java.util.Scanner;

public class App {

    private static final String HTML_FILE_PATH = "index.html";

    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")

                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {

                        if (exchange.isInIoThread()) {
                            exchange.dispatch(this);
                            return;
                        } else if (exchange.getRequestPath().equals("/upload")) {

                            exchange.startBlocking();
                            handleUpload(exchange);
                        } else {
                            sendHtmlPage(exchange);
                        }

                    }
                }).build();
        server.start();
    }

    private static void handleUpload(HttpServerExchange exchange) {
        exchange.startBlocking();

        FormParserFactory parserFactory = FormParserFactory.builder()
                .addParser(new MultiPartParserDefinition())
                .build();

        FormDataParser parser = parserFactory.createParser(exchange);
        if (parser == null) {
            exchange.getResponseSender().send("Bad Request: No form data found");
            return;
        }

        try {
            FormData formData = parser.parseBlocking();
            FormData.FormValue fileValue = formData.getFirst("videoFile");

            if (fileValue != null && fileValue.isFileItem()) {

                File uploadedFile = saveUploadedFile(fileValue);
                String inputFilePath = uploadedFile.getAbsolutePath();
                String outputFilePath = "processed_video.mp4";
                int triangleSize = 1;

                System.out.println("Secuential");
                long startTime = System.currentTimeMillis();
                VideoCompress.compressVideo(inputFilePath, outputFilePath, triangleSize);
                long endTime = System.currentTimeMillis();
                long processingTime = endTime - startTime;
                System.err.println(processingTime);
                File processedFile = new File("processed_video.mp4");

                System.out.println("Concurrent");
                startTime = System.currentTimeMillis();
                ConcurrentVideoCompress.compressVideo(inputFilePath, outputFilePath, triangleSize);
                endTime = System.currentTimeMillis();
                long ConcurrentProcessingTime = endTime - startTime;
                System.out.println(ConcurrentProcessingTime);


                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                //exchange.getResponseSender().send("Video processing completed in " + processingTime + " milliseconds");

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "video/mp4");
                exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION,
                        "attachment; filename=\"processed_video.mp4\"");
                exchange.getResponseHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, processedFile.length());

                try (FileInputStream fileInputStream = new FileInputStream(processedFile)) {
                    exchange.getResponseSender().transferFrom(fileInputStream.getChannel(), new IoCallback() {
                        @Override
                        public void onComplete(HttpServerExchange exchange, io.undertow.io.Sender sender) {
                    
                            System.out.println("Video processing completed in " + processingTime + " milliseconds");
                        }
                        @Override
                        public void onException(HttpServerExchange exchange, io.undertow.io.Sender sender,
                                IOException exception) {
                            exception.printStackTrace();
                            exchange.setStatusCode(500);
                            exchange.getResponseSender().send("Internal Server Error: " + exception.getMessage());
                        }
                    });
                }

                // Optional: log or send the processing time
                System.out.println("Video processing completed in " + processingTime + " milliseconds");
            } else {
                exchange.getResponseSender().send("No file uploaded");
            }
        } catch (IOException | JCodecException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("Internal Server Error");
        }
    }

    private static File saveUploadedFile(FormData.FormValue fileValue) throws IOException {
        Path tempFile = Files.createTempFile("uploaded-", ".mp4");

        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            IOUtils.copy(fileValue.getFileItem().getInputStream(), fos);
        }
        return tempFile.toFile();
    }

    private static void sendHtmlPage(HttpServerExchange exchange) throws IOException {
        String htmlContent = readFile(HTML_FILE_PATH);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(htmlContent);
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

            e.printStackTrace();
            return "Error leyendo archivo HTML principal: debe estar en src\\resources\\html";
        }
    }

}
