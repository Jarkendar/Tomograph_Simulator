package sample;//package sample;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import static java.lang.Math.*;

public class SinogramCreator extends Observable implements Runnable {

    public final static String SINOGRAM_IS_END = "Sinogram is end.";
    public final static String REVERSE_IS_END = "Reverse is end.";
    private final static double HALF_FULL_ANGLE = 180.0;
    private final static String STATUS_START = "start";
    private final static String STATUS_SINOGRAM = "sinogram";
    private final static String STATUS_REVERSE = "reverse";

    private int[][][] inputBitmap;
    private int[][] outputImage;
    private int[][] sinogramBitmap;
    private int detectorNumber;
    private int scansNumber;
    private int angleRange;
    private LinkedList<Observer> observers = new LinkedList<>();
    private String status = STATUS_START;
    private String name;

    public SinogramCreator(int[][][] inputBitmap, int detectorNumber, int scansNumber, int angleRange, String name) {
        this.inputBitmap = inputBitmap;
        this.detectorNumber = detectorNumber;
        this.scansNumber = scansNumber;
        this.angleRange = angleRange;
        this.name = name;
        sinogramBitmap = new int[detectorNumber][scansNumber];
        outputImage = new int[inputBitmap.length][inputBitmap[0].length];
    }

    @Override
    public void run() {
        Thread[] rowCounters = new Thread[scansNumber];
        for (int i = 0; i < scansNumber; i++) {
            int[][] positions = getEmitterAndDetectorsPositions(i);
            rowCounters[i] = new Thread(new SinogramRowCalc(inputBitmap, sinogramBitmap, positions, i, createLinearFunctions(positions)));
            rowCounters[i].start();
        }
        for (Thread rowCounter : rowCounters) {
            try {
                rowCounter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        status = STATUS_SINOGRAM;
        notifyObservers();
        createImageFromSinogram(new FileManager(), name);
        status = STATUS_REVERSE;
        notifyObservers();
        status = STATUS_START;
    }


    private double[][] createLinearFunctions(int[][] positions) {
        double[][] linearFunctionParameters = new double[positions.length][2];//first position in matrix 0 0
        for (int i = 1; i < positions.length; i++) {
            linearFunctionParameters[i][0] = ((double) (positions[0][1] - positions[i][1])) / ((double) (positions[0][0] - positions[i][0]));
            linearFunctionParameters[i][1] = positions[0][1] - linearFunctionParameters[i][0] * positions[0][0];
        }
        return linearFunctionParameters;
    }

    private int[][] getEmitterAndDetectorsPositions(int rowScanNumber) {
        int radius = inputBitmap.length / 2;
        int[] center = {inputBitmap.length / 2, inputBitmap[0].length / 2};
        int[][] positions = new int[detectorNumber + 1][2];
        double rotationAngle = ((double) rowScanNumber / (double) scansNumber) * 360.0;
        positions[0][0] = center[0] + (int) (radius * cos(toRadians(rotationAngle)));//emitter x
        positions[0][1] = center[1] + (int) (radius * sin(toRadians(rotationAngle)));//emitter y

        for (int i = 1; i < positions.length; i++) {
            double angle;
            if (detectorNumber == 1) {
                angle = rotationAngle + HALF_FULL_ANGLE;
            } else {
                angle = rotationAngle + HALF_FULL_ANGLE - (double) angleRange / 2 + (i - 1) * ((double) angleRange / ((double) detectorNumber - 1));
            }
            angle = angle > 360 ? angle - 360 : angle;
            double radians = toRadians(angle);
            positions[i][0] = center[0] + (int) (radius * cos(radians));//detector i x
            positions[i][1] = center[1] + (int) (radius * sin(radians));//detector i y
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
    public synchronized void notifyObservers() {
        super.notifyObservers();
        for (Observer observer : observers) {
            switch (status) {
                case STATUS_SINOGRAM: {
                    observer.update(this, SINOGRAM_IS_END);
                    break;
                }
                case STATUS_REVERSE: {
                    observer.update(this, REVERSE_IS_END);
                    break;
                }
            }
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }

    public int[][] getSinogramBitmap() {
        return sinogramBitmap;
    }

    public int[][] getOutputImage() {
        return outputImage;
    }

    private void createImageFromSinogram(FileManager fileManager, String name) {
        for (int i = 0; i < scansNumber; i++) {
            int[][] positions = getEmitterAndDetectorsPositions(i);
            double[][] linearFunctionParameters = createLinearFunctions(positions);
            for (int j = 0; j < detectorNumber; j++) {
                int color = sinogramBitmap[j][i];
                fillBitmapColor(outputImage, color, positions, linearFunctionParameters, j + 1);
            }
            fileManager.saveTmpIndirectImage(makeCopyBitmap(outputImage), name, i);
        }
        normalize(outputImage);
    }

    private int[][] makeCopyBitmap(int[][] bitmap) {
        int[][] copyBitmap = new int[bitmap.length][bitmap[0].length];
        for (int i = 0; i < copyBitmap.length; i++) {
            for (int j = 0; j < copyBitmap[i].length; j++) {
                copyBitmap[i][j] = bitmap[i][j];
            }
        }
        return copyBitmap;
    }

    private void fillBitmapColor(int[][] bitmap, int color, int[][] positions, double[][] linearFunctionParameters, int currentDetector) {
        int numberOfStep = Math.abs(positions[0][0] - positions[currentDetector][0]);
        for (int i = 1; i < numberOfStep; i++) {
            int currentX = positions[0][0] < positions[currentDetector][0] ? (positions[0][0] + i) : (positions[0][0] - i);
            int currentY = (int) (linearFunctionParameters[currentDetector][0] * currentX + linearFunctionParameters[currentDetector][1]);
            bitmap[currentX][currentY] += color;
        }
    }

    private void normalize(int[][] bitmap) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] aBitmap : bitmap) {
            for (int anABitmap : aBitmap) {
                if (min > anABitmap) {
                    min = anABitmap;
                }
                if (max < anABitmap) {
                    max = anABitmap;
                }
            }
        }
        max = max - min;
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                bitmap[i][j] = (int) (((double) (bitmap[i][j] - min) / (double) max) * (255.0));
            }
        }
    }
}
