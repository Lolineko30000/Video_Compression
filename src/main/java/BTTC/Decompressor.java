package BTTC;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import BTree.BTree;
import BTree.BTreeNode;

public class Decompressor {
    private BTree compressed;
    private BufferedImage image;

    public Decompressor(BTree compresed, int width, int height){
        this.compressed = compresed;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }

    public BufferedImage decompress(){
        ArrayList<BTreeNode> nodes = compressed.nodes;
        int size = nodes.size();
        // System.out.println(size);
        for(int i=0; i< size; i++){
            fillTriangle(nodes.get(i));
        }
        return image;
    }

    private void fillTriangle(BTreeNode T){

        Color c1 = T.c1;
        Color c2 = T.c2;
        Color c3 = T.c3;

        int x_min = min(T.x1, T.x2, T.x3);
        int x_max = max(T.x1, T.x2, T.x3);
        int y_min = min(T.y1, T.y2, T.y3);
        int y_max = max(T.y1, T.y2, T.y3);
        
        double denominator = (T.x2-T.x1)*(T.y3-T.y1) - (T.y2-T.y1)*(T.x3-T.x1);

        for(int x = x_min; x < x_max; x++){
            for(int y = y_min; y < y_max; y++){
                if(T.isInside(x, y)){
                    Color estimado = G(x, y, c1, c2, c3, T, denominator);
                    image.setRGB(x, y, estimado.getRGB());
                }
            }
        }
    }

    private Color G(int x, int y, Color c1, Color c2, Color c3, BTreeNode T, double denominator){
        double alpha = ((x-T.x1)*(T.y3-T.y1)-(y-T.y1)*(T.x3-T.x1))/denominator;
        double beta = ((T.x2-T.x1)*(y-T.y1)-(T.y2-T.y1)*(x-T.x1))/denominator;

        int R = (int)(c1.getRed() + alpha*(c2.getRed() - c1.getRed()) + beta*(c3.getRed() - c1.getRed()));
        int G = (int)(c1.getGreen() + alpha*(c2.getGreen() - c1.getGreen()) + beta*(c3.getGreen() - c1.getGreen()));
        int B = (int)(c1.getBlue() + alpha*(c2.getBlue() - c1.getBlue()) + beta*(c3.getBlue() - c1.getBlue()));
        
        return new Color(R, G, B);
    }

    private int min(int x1, int x2, int x3){
        int min = x1;
        if(x2 < min){
            min = x2;
        }
        if(x3 < min){
            min = x3;
        }
        return min;
    }

    private int max(int x1, int x2, int x3){
        int max = x1;
        if(x2 > max){
            max = x2;
        }
        if(x3 > max){
            max = x3;
        }
        return max;
    }

}

