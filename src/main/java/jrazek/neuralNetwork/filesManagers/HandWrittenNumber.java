package jrazek.neuralNetwork.filesManagers;

import jrazek.neuralNetwork.utils.Utils;

import java.awt.image.BufferedImage;

import static jrazek.neuralNetwork.utils.Utils.randomDouble;

public class HandWrittenNumber {
    double [] pixels;
    int number;
    HandWrittenNumber(BufferedImage img, int number){
        this.number = number;
        int x = img.getWidth();
        int y = img.getHeight();
        //System.out.println("W: " + x + " Y " + y);
        pixels = new double[x*y];
       // System.out.println(Utils.toGrayscale(img.getRGB(0, 0)));
        for(int i = 0; i < y; i ++){
            for(int j = 0; j < x; j++){
                pixels[j+i*x] = (Utils.toGrayscale(img.getRGB(j, i))/255f);
            }
        }
    }
    public int getTarget(){
        return number;
    }
    public double[] getPixels() {
        return pixels;
    }
}
