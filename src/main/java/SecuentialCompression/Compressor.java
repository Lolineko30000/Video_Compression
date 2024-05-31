package SecuentialCompression;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Stack;

import BTree.BTree;
import BTree.BTreeNode;


public class Compressor {
    private double error;
    private BTree compressed;
    private BufferedImage image;


    public Compressor(BufferedImage image, double error){
        this.image = image;
        this.error = error;
        this.compressed = new BTree();
    }

    public Compressor(BufferedImage image){
        this.image = image;
        this.error = 0.5;
        this.compressed = new BTree();
    }

    public BTree compress(){

        Stack<BTreeNode> stack = new Stack<BTreeNode>();

        int width = this.image.getWidth();
        int height = this.image.getHeight();

        // 1. Initialize the first two PRATS
        BTreeNode T1 = new BTreeNode(0, 0, 0, height-1, width-1, 0);
        BTreeNode T2 = new BTreeNode(width-1, height-1, 0, height-1, width-1, 0);

        // 2. Push T1 and T2 into the stack
        stack.push(T1);
        stack.push(T2);

        while(!stack.empty()){

            // 3. Pop the PRAT T from the stack
            BTreeNode T = stack.pop();
            
            // 4. Test if the approximation for T is good enough
            double error_calculado = calculateError(T);;
            if( error_calculado <= this.error ){
                // 6. Insert T into L
                Color c1 = new Color(image.getRGB(T.x1, T.y1));
                Color c2 = new Color(image.getRGB(T.x2, T.y2));
                Color c3 = new Color(image.getRGB(T.x3, T.y3));
                T.setColors(c1, c2, c3);
                compressed.addNode(T);
            }else{
                compressed.addNode();

                // 5. Divide T into two parts
                int x1 = (T.x2 + T.x3) / 2;
                int y1 = (T.y2 + T.y3) / 2;
                T1 = new BTreeNode(x1, y1, T.x1, T.y1, T.x2, T.y2);
                T2 = new BTreeNode(x1, y1, T.x3, T.y3, T.x1, T.y1);

                // Go to Step 2
                stack.push(T1);
                stack.push(T2);
            }
            
        }
        return compressed;
    }

    private double calculateError(BTreeNode T){


        Color c1 = new Color(image.getRGB(T.x1, T.y1));
        Color c2 = new Color(image.getRGB(T.x2, T.y2));
        Color c3 = new Color(image.getRGB(T.x3, T.y3));

        int x_min = min(T.x1, T.x2, T.x3);
        int x_max = max(T.x1, T.x2, T.x3);
        int y_min = min(T.y1, T.y2, T.y3);
        int y_max = max(T.y1, T.y2, T.y3);
        
        double calculated_error = 0;
        double denominator = (T.x2-T.x1)*(T.y3-T.y1) - (T.y2-T.y1)*(T.x3-T.x1);

        for(int x = x_min; x < x_max; x++){
            for(int y = y_min; y < y_max; y++){
                if(T.isInside(x, y)){
                    Color estimado = G(x, y, c1, c2, c3, T, denominator);
                    double d = distancia(new Color(image.getRGB(x, y)), estimado);
                    // System.out.println(d);
                    calculated_error += d;
                    if(calculated_error > error){
                        return calculated_error;
                    }
                }
            }
        }
        return calculated_error;
    }

    private Color G(int x, int y, Color c1, Color c2, Color c3, BTreeNode T, double denominator){
        double alpha = ((x-T.x1)*(T.y3-T.y1)-(y-T.y1)*(T.x3-T.x1))/denominator;
        double beta = ((T.x2-T.x1)*(y-T.y1)-(T.y2-T.y1)*(x-T.x1))/denominator;

        int R = (int)(c1.getRed() + alpha*(c2.getRed() - c1.getRed()) + beta*(c3.getRed() - c1.getRed()));
        int G = (int)(c1.getGreen() + alpha*(c2.getGreen() - c1.getGreen()) + beta*(c3.getGreen() - c1.getGreen()));
        int B = (int)(c1.getBlue() + alpha*(c2.getBlue() - c1.getBlue()) + beta*(c3.getBlue() - c1.getBlue()));

        return new Color(R, G, B);
    }

    private double distancia(Color c1, Color c2){
        double d = 0;
        d += Math.abs(c1.getRed() - c2.getRed())/255.0;
        d += Math.abs(c1.getGreen() - c2.getGreen())/255.0;
        d += Math.abs(c1.getBlue() - c2.getBlue())/255.0;
        return d/3.0;
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
