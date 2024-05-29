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
import ImageCompression.BTreeTriangularCoding;


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

        Picture picture;
        while ((picture = frameGrab.getNativeFrame()) != null) {
            
            BufferedImage frame = AWTUtil.toBufferedImage(picture);
            BTreeTriangularCoding bTreeCoding = new BTreeTriangularCoding(frame, triangleSize);
            bTreeCoding.compress();
            BufferedImage compressedFrame = bTreeCoding.decompress();
            
            
            encoder.encodeImage(compressedFrame);
            
        }

        encoder.finish();

    }
}
