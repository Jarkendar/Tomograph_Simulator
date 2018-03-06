package sample;//package sample;

import sample.SinogramRowCalc;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import static java.lang.Math.*;

public class SinogramCreator extends Observable implements Runnable {

    private final static String END_COMMUNICATE = "Sinogram is end.";
    private final static double HALF_FULL_ANGLE = 180.0;

    private int[][][] inputBitmap;
    private int[][] sinogramBitmap;
    private int detectorNumber;
    private int scansNumber;
    private int angleRange;
    private LinkedList<Observer> observers = new LinkedList<>();

    public SinogramCreator(int[][][] inputBitmap, int detectorNumber, int scansNumber, int angleRange) {
        this.inputBitmap = inputBitmap;
        this.detectorNumber = detectorNumber;
        this.scansNumber = scansNumber;
        this.angleRange = angleRange;
        sinogramBitmap = new int[detectorNumber][scansNumber];
    }

    @Override
    public void run() {
        System.out.println("0");
        getEmitterAndDetectorsPositions(0);
        System.out.println("90");
        getEmitterAndDetectorsPositions(90);
        System.out.println("180");
        getEmitterAndDetectorsPositions(180);
        System.out.println("270");
        getEmitterAndDetectorsPositions(270);
        SinogramRowCalc[] threads = new SinogramRowCalc[scansNumber];

        //todo
    }

    private int[][] getEmitterAndDetectorsPositions(int rowScanNumber){
        int radius = inputBitmap.length/2;
        int[] center = {inputBitmap.length/2, inputBitmap[0].length/2};
        int[][] positions = new int[detectorNumber+1][2];
        double rotationAngle = ((double) rowScanNumber/(double)scansNumber)*360.0;
        positions[0][0] = center[0]+(int)(radius*cos(toRadians(rotationAngle)));//emitter x
        positions[0][1] = center[1]+(int)(radius*sin(toRadians(rotationAngle)));//emitter y

        for (int i = 1; i<positions.length; i++){
            double radians = toRadians(rotationAngle+HALF_FULL_ANGLE-angleRange/2+(i-1)*(angleRange/(detectorNumber-1)));
            positions[i][0] = center[0]+(int)(radius*cos(radians));//detector i x
            positions[i][1] = center[1]+(int)(radius*sin(radians));//detector i y
        }

        for (int[] device : positions){
            System.out.println("position ="+device[0]+";"+device[1]);
        }
        return positions;
    }

    @Override
    public synchronized void addObserver(Observer observer) {
        super.addObserver(observer);
        observers.addLast(observer);
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
        for (Observer observer : observers){
            observer.update(this,END_COMMUNICATE);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }
}
