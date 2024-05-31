package ConcurrentVideoCompress;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Rational;

import ImageFormat.SortedImage;


public class ConcurrentVideoCompress {

    public static void compressVideo(String inputFilePath, String outputFilePath, int triangleSize)throws IOException, JCodecException {
        

        SeekableByteChannel out = NIOUtils.writableFileChannel(outputFilePath);
        
        File inputFile = new File(inputFilePath);
        FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(inputFile));
        
        double totalFrames = frameGrab.getVideoTrack().getMeta().getTotalFrames();
        double totalDuration = frameGrab.getVideoTrack().getMeta().getTotalDuration();

        Rational frameRate = totalFrames != 0
                ? Rational.R((int) totalFrames,(int)totalDuration)
                : Rational.R(30, 1);


        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, frameRate);


        PictureWithMetadata picture;
        ArrayList<SortedImage> frames = new ArrayList<SortedImage>();
        
        System.out.println("Reading video...");
        while ((picture = frameGrab.getNativeFrameWithMetadata()) != null) {
            
            frames.add(new SortedImage(picture));
            
        }

        Collections.sort(frames);
            
        System.out.println("Processing video...");

        int workerThreads = frames.size();
        Thread[] threads = new Thread[workerThreads];
        ConcurrentBTTC[] results = new ConcurrentBTTC[workerThreads];

        for( int i =0; i<workerThreads; i++){
            results[i] = new ConcurrentBTTC(frames.get(i).data, i);
            threads[i] = new Thread(results[i]);
        }

        for( int i = 0; i < workerThreads; i += 100){
            int stop = i+100;
            if(stop>workerThreads){
                stop = workerThreads;
            }

            for(int j = i; j < stop; j++){
                threads[j].start();
            }

            try{
                for(int j = i; j < stop; j++){
                    threads[j].join();
                }
            }catch ( Exception e ){
                System.out.println("No se pudo padrino");
            }
        }

        // for( Thread t : threads){
        //     t.start();
        // }

        // try{
        //     for( Thread t : threads){
        //         t.join();
        //     }
        // }catch ( Exception e ){
        //     System.out.println("No se pudo padrino");
        // }

        for( ConcurrentBTTC h: results){
            encoder.encodeImage(h.getFrame());
        }
        encoder.finish();
        System.out.println("Video encoded");

    }
}