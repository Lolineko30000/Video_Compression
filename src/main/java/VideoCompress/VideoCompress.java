package VideoCompress;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.Rational;

import CompressionSaul.BTree;
// import ImageCompression.BTreeTriangularCoding;
import CompressionSaul.Compressor;
import CompressionSaul.Decompressor;


public class VideoCompress {

    public static void compressVideo(String inputFilePath, String outputFilePath, int triangleSize)throws IOException, JCodecException {
        

        SeekableByteChannel out = NIOUtils.writableFileChannel(outputFilePath);
        
        System.out.println(inputFilePath);

        File inputFile = new File(inputFilePath);
        FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(inputFile));
        
        double totalFrames = frameGrab.getVideoTrack().getMeta().getTotalFrames();
        double totalDuration = frameGrab.getVideoTrack().getMeta().getTotalDuration();

        Rational frameRate = totalFrames != 0
                ? Rational.R((int) totalFrames,(int)totalDuration)
                : Rational.R(30, 1);


        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, frameRate);


        System.out.println("Processing video...");
        int fps = (int)frameRate.toDouble();
        int i = 0;

        Picture picture;
        while ((picture = frameGrab.getNativeFrame()) != null) {
            
            i++;
            if(i%fps==0){
                System.out.print(i/fps);
                System.out.println(" seconds proccesed");
            }

            BufferedImage frame = AWTUtil.toBufferedImage(picture);

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
