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
        
        System.out.println("Entra Compress video");

        SeekableByteChannel out = NIOUtils.writableFileChannel(outputFilePath);
        
        System.out.println(inputFilePath);

        File inputFile = new File(inputFilePath);
        FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(inputFile));
        
        System.out.println("2222222222222");
        double totalFrames = frameGrab.getVideoTrack().getMeta().getTotalFrames();
        double totalDuration = frameGrab.getVideoTrack().getMeta().getTotalDuration();
        System.out.println(".3333333333333333");

        Rational frameRate = totalFrames != 0
                ? Rational.R((int) totalFrames,(int)totalDuration)
                : Rational.R(30, 1);


        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, frameRate);


        Picture picture;
        while ((picture = frameGrab.getNativeFrame()) != null) {
            
            BufferedImage frame = AWTUtil.toBufferedImage(picture);

            // CAMBIAR
            // BTreeTriangularCoding bTreeCoding = new BTreeTriangularCoding(frame, triangleSize);
            // bTreeCoding.compress();
            // BufferedImage compressedFrame = bTreeCoding.decompress();
            System.out.println("Comprimiendo...");
            Compressor compressor = new Compressor(frame);
            BTree compressed = compressor.compress();
            System.out.println("Archivo comprimido correctamente");
            System.out.println(compressed.nodes);

            System.out.println("Descomprimiendo");
            Decompressor decompressor = new Decompressor(compressed, frame.getWidth(), frame.getHeight());
            BufferedImage compressedFrame = decompressor.decompress();
            System.out.println("Archivo descomprimido");

            
            encoder.encodeImage(compressedFrame);
            System.out.println("Image encoded");

            
        }

        encoder.finish();

    }
}
