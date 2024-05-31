package VideoCompress;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;

import BTree.BTree;
import SecuentialCompression.Compressor;
import SecuentialCompression.Decompressor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Rational;


public class VideoCompress {

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
        int fps = (int)frameRate.toDouble();

        for( int i = 0; i< frames.size(); i++){
            if(i%fps==0){
                System.out.print(i/fps);
                System.out.println(" seconds proccesed");
            }

            BufferedImage frame = frames.get(i).data;

            Compressor compressor = new Compressor(frame);
            BTree compressed = compressor.compress();

            Decompressor decompressor = new Decompressor(compressed, frame.getWidth(), frame.getHeight());
            BufferedImage compressedFrame = decompressor.decompress();

            encoder.encodeImage(compressedFrame);
        }
        
        encoder.finish();
        System.out.println("Video encoded");

    }
}