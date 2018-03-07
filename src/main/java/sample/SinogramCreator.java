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
    public void run(){
        Thread[] rowCounters = new Thread[scansNumber];
        for (int i = 0; i<scansNumber; i++){
            rowCounters[i] = new Thread(new SinogramRowCalc(inputBitmap,sinogramBitmap, getEmitterAndDetectorsPositions(i),i));
            rowCounters[i].start();
        }
        for (Thread rowCounter : rowCounters) {
            try {
                rowCounter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        notifyObservers();
    }

    private int[][] getEmitterAndDetectorsPositions(int rowScanNumber){
        int radius = inputBitmap.length/2;
        int[] center = {inputBitmap.length/2, inputBitmap[0].length/2};
        int[][] positions = new int[detectorNumber+1][2];
        double rotationAngle = ((double) rowScanNumber/(double)scansNumber)*360.0;
        positions[0][0] = center[0]+(int)(radius*cos(toRadians(rotationAngle)));//emitter x
        positions[0][1] = center[1]+(int)(radius*sin(toRadians(rotationAngle)));//emitter y

        for (int i = 1; i<positions.length; i++){
            double angle;
            if (detectorNumber == 1){
                angle = rotationAngle+HALF_FULL_ANGLE;
            }else {
                angle = rotationAngle+HALF_FULL_ANGLE-(double)angleRange/2+(i-1)*((double)angleRange/((double)detectorNumber-1));
            }
            angle = angle > 360 ? angle-360 : angle;
            double radians = toRadians(angle);
            positions[i][0] = center[0]+(int)(radius*cos(radians));//detector i x
            positions[i][1] = center[1]+(int)(radius*sin(radians));//detector i y
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

    public int[][] getSinogramBitmap(){
        return sinogramBitmap;
    }
}
