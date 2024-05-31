package ConcurrentVideoCompress;

import java.awt.image.BufferedImage;

import BTTC.Compressor;
import BTTC.Decompressor;
import BTree.BTree;

public class ConcurrentBTTC extends Thread{

    public BufferedImage frame;
    public BufferedImage compressedFrame;

    int name;

    public ConcurrentBTTC(BufferedImage frame, int name){
        this.frame = frame;
        this.name = name;
    }

    public void run(){
        
        Compressor compressor = new Compressor(frame);
        BTree compressed = compressor.compress();

        Decompressor decompressor = new Decompressor(compressed, frame.getWidth(), frame.getHeight());
        compressedFrame = decompressor.decompress();
        
    }

    public BufferedImage getFrame(){

        return compressedFrame;
    }
}
