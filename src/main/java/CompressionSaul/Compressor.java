package CompressionSaul;

import java.awt.Color;
import java.awt.image.BufferedImage;
// import java.util.ArrayList;
import java.util.Stack;
// import CompressionSaul.BTree;
// import CompressionSaul.BTreeNode;

public class Compressor {
    private double error;
    private int min_depht;
    private BTree compressed;
    private BufferedImage image;

    public Compressor(BufferedImage image, double error, int min_depht){
        this.image = image;
        this.error = error;
        this.min_depht = min_depht;
    }

    public Compressor(BufferedImage image, double error){
        this.image = image;
        this.error = error;
        this.min_depht = 5;
    }

    public Compressor(BufferedImage image){
        this.image = image;
        this.error = 0.0001;
        this.min_depht = 5;
    }

    public BTree compress(){
        int depht = 0;
        // boolean previous_was_leaf = false;

        Stack<BTreeNode> stack = new Stack<BTreeNode>();

        int width = this.image.getWidth();
        int height = this.image.getHeight();

        // 1. Initialize the first two PRATS
        BTreeNode T1 = new BTreeNode(0, 0, 0, width, height, 0);
        BTreeNode T2 = new BTreeNode(width, height, 0, width, height, 0);

        // 2. Push T1 and T2 into the stack
        stack.push(T1);
        stack.push(T2);

        while(!stack.empty()){
            // 3. Pop the PRAT T from the stack
            BTreeNode T = stack.pop();

            while (true) {
                // 4. Test if the approximation for T is good enough
                if(calculateError(T) <= this.error){
                    // 6. Insert T into L
                    compressed.addNode(T);
                }else{
                    compressed.addNode();

                    // 5. Divide T into two parts
                    int x1 = (T.x2 + T.x3) / 2;
                    int y1 = (T.y2 + T.y3) / 2;
                    T1 = new BTreeNode(x1, y1, T.x1, T.y1, T.x2, T.x2);
                    T2 = new BTreeNode(x1, y1, T.x3, T.y3, T.x1, T.x1);

                    // Go to Step 2
                    stack.push(T1);
                    stack.push(T2);
                }
            }
        }
        return compressed;
    }

    private float calculateError(BTreeNode T){

        Color c1 = new Color(image.getRGB(T.x1, T.y1));
        Color c2 = new Color(image.getRGB(T.x2, T.y2));
        Color c3 = new Color(image.getRGB(T.x3, T.y3));

        int x_min = min(T.x1, T.x2, T.x3);
        int x_max = max(T.x1, T.x2, T.x3);
        int y_min = min(T.y1, T.y2, T.y3);
        int y_max = max(T.y1, T.y2, T.y3);
        
        float error = 0;
        float denominator = (T.x2-T.x1)*(T.y3-T.y1) - (T.y2-T.y1)*(T.x3-T.x1);

        for(int x = x_min; x < x_max; x++){
            for(int y = y_min; y < y_max; y++){
                if(T.isInside(x, y)){
                    Color estimado = G(x, y, c1, c2, c3, T, denominator);
                    error += distancia(new Color(image.getRGB(x, y)), estimado);
                }
            }
        }
        return error;
    }

    private Color G(int x, int y, Color c1, Color c2, Color c3, BTreeNode T, float denominator){
        float alpha = ((x-T.x1)*(T.y3-T.y1)-(y-T.y1)*(T.x3-T.x1))/denominator;
        float beta = ((T.x2-T.x1)*(y-T.y1)-(T.y2-T.y1)*(x-T.x1))/denominator;

        float R = c1.getRed() + alpha*(c2.getRed() - c1.getRed()) + beta*(c3.getRed() - c1.getRed());
        float G = c1.getGreen() + alpha*(c2.getGreen() - c1.getGreen()) + beta*(c3.getGreen() - c1.getGreen());
        float B = c1.getBlue() + alpha*(c2.getBlue() - c1.getBlue()) + beta*(c3.getBlue() - c1.getBlue());
        return new Color(R, G, B);
    }

    private float distancia(Color c1, Color c2){
        float d = 0;
        d += Math.abs(c1.getRed() - c2.getRed())/255;
        d += Math.abs(c1.getGreen() - c2.getGreen())/255;
        d += Math.abs(c1.getBlue() - c2.getBlue())/255;
        return d/3;
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
