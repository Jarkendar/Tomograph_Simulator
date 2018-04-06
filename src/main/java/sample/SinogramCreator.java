package sample;

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
    private int[][] outputBitmap;
    private int[][] sinogramBitmap;
    private int detectorNumber;
    private int scansNumber;
    private int angleRange;
    private LinkedList<Observer> observers = new LinkedList<>();
    private String status = STATUS_START;
    private String name;
    private boolean isFiltering;
    private double[] rmseArray;

    public SinogramCreator(int[][][] inputBitmap, int detectorNumber, int scansNumber, int angleRange, String name, boolean isFiltering) {
        this.inputBitmap = inputBitmap;
        this.detectorNumber = detectorNumber;
        this.scansNumber = scansNumber;
        this.angleRange = angleRange;
        this.name = name;
        this.isFiltering = isFiltering;
        sinogramBitmap = new int[detectorNumber][scansNumber];
        outputBitmap = new int[inputBitmap.length][inputBitmap[0].length];
        rmseArray = new double[1 + scansNumber];
        countMaxRMSE();
    }


    private void countMaxRMSE() {
        long sumRMSE = 0;
        int radius = inputBitmap.length / 2;
        for (int i = 0; i < inputBitmap.length; i++) {
            for (int j = 0; j < inputBitmap[0].length; j++) {
                if (Math.pow((i - radius),2) + Math.pow((j - radius),2) <= radius * radius) {
                    sumRMSE += countSquareSubstraction(inputBitmap[i][j][0], inputBitmap[i][j][0] > 127 ? 0 : 255);
                }
            }
        }
        rmseArray[0] = Math.sqrt(sumRMSE/(inputBitmap.length*inputBitmap[0].length));
    }

    public double[] getRmseArray() {
        return rmseArray;
    }

    @Override
    public void run() {
        Thread[] rowCounters = new Thread[scansNumber];
        for (int i = 0; i < scansNumber; i++) {
            int[][] positions = getEmitterAndDetectorsPositions(i);
            rowCounters[i] = new Thread(new SinogramRowCalc(inputBitmap, sinogramBitmap, positions, i));
            rowCounters[i].start();
        }
        for (Thread rowCounter : rowCounters) {
            try {
                rowCounter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isFiltering) {
            filterRamLakSinogram();
        }
        status = STATUS_SINOGRAM;
        notifyObservers();
        createImageFromSinogram(new FileManager());
        status = STATUS_REVERSE;
        notifyObservers();
        status = STATUS_START;
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

    public int[][] getOutputBitmap() {
        return outputBitmap;
    }

    private void createImageFromSinogram(FileManager fileManager) {
        for (int i = 0; i < scansNumber; i++) {
            int[][] tmp = new int[outputBitmap.length][outputBitmap[0].length];
            int[][] positions = getEmitterAndDetectorsPositions(i);
            for (int j = 1; j <= detectorNumber; j++) {
                int color = sinogramBitmap[j - 1][i];
                bresenhamFill(tmp, color, positions, j);
            }
            new Thread(new Normalizer(makeCopyBitmap(tmp), fileManager, i)).start();
        }
        normalize(outputBitmap);
    }

    private int[][] makeCopyBitmap(int[][] bitmap) {
        int[][] copyBitmap = new int[bitmap.length][bitmap[0].length];
        for (int i = 0; i < copyBitmap.length; i++) {
            for (int j = 0; j < copyBitmap[i].length; j++) {
                outputBitmap[i][j] += bitmap[i][j];
                copyBitmap[i][j] = outputBitmap[i][j];
            }
        }
        return copyBitmap;
    }

    private void bresenhamFill(int[][] bitmap, int color, int[][] positions, int currentDetector) {
        int coefficientD = 0;

        int dx = Math.abs(positions[currentDetector][0] - positions[0][0]);//lenght on axis X
        int dy = Math.abs(positions[currentDetector][1] - positions[0][1]);//lenght on axis Y

        int stepX = positions[0][0] < positions[currentDetector][0] ? 1 : -1;//direction RIGHT/LEFT
        int stepY = positions[0][1] < positions[currentDetector][1] ? 1 : -1;//direction DOWN/UP

        //start position
        int actualX = positions[0][0];
        int actualY = positions[0][1];

        if (dx >= dy) {//a <= 45 degrees
            while (true) {
                if (isInImage(actualX, actualY, bitmap)) {
                    bitmap[actualX][actualY] = color;
                }
                if (actualX == positions[currentDetector][0]) {
                    break;
                }
                actualX += stepX;
                coefficientD += dy;
                if (coefficientD > dx) {
                    actualY += stepY;
                    coefficientD -= dx;
                }
            }
        } else {//a > 45 degrees
            while (true) {
                if (isInImage(actualX, actualY, bitmap)) {
                    bitmap[actualX][actualY] = color;
                }
                if (actualY == positions[currentDetector][1]) {
                    break;
                }
                actualY += stepY;
                coefficientD += dx;
                if (coefficientD > dy) {
                    actualX += stepX;
                    coefficientD -= dy;
                }
            }
        }

    }

    private boolean isInImage(int x, int y, int[][] referenceToBitmap) {
        return x < referenceToBitmap.length && y < referenceToBitmap[0].length;
    }

    private void normalize(int[][] bitmap) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] row : bitmap) {
            for (int cell : row) {
                if (min > cell) {
                    min = cell;
                }
                if (max < cell) {
                    max = cell;
                }
            }
        }
        max = max - min;
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                int value = (int) (((double) (bitmap[i][j] - min) / (double) max) * (255.0));
                bitmap[i][j] = value < 0 ? 0 : value;
            }
        }
    }

    private void normalizeAndCountRMSE(int[][] bitmap, int iteration) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] row : bitmap) {
            for (int cell : row) {
                if (min > cell) {
                    min = cell;
                }
                if (max < cell) {
                    max = cell;
                }
            }
        }
        max = max - min;
        int radius = bitmap.length / 2;
        long sumRMSE = 0;
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                int value = (int) (((double) (bitmap[i][j] - min) / (double) max) * (255.0));
                bitmap[i][j] = value < 0 ? 0 : value;
                if (Math.pow((i - radius),2) + Math.pow((j - radius),2) <= radius * radius) {
                    sumRMSE += countSquareSubstraction(inputBitmap[i][j][0], bitmap[i][j]);
                }
            }
        }
        rmseArray[iteration + 1] = Math.sqrt(sumRMSE/(bitmap.length*bitmap[0].length));
    }

    private double countSquareSubstraction(int pixelInputColor, int pixelColor) {
        return Math.pow(pixelColor - pixelInputColor, 2);
    }

    private void filterRamLakSinogram() {
        double[] filterMask = createMaskRamLak();
        convolveSinogram(filterMask);
    }

    private double[] createMaskRamLak() {
        double[] filterMask = new double[sinogramBitmap[0].length / 20];
        double multiply = 255.0;
        filterMask[0] = 1.0 * multiply;
        double numerator = (-4.0) / (PI * PI) * multiply;
        for (int i = 1; i < filterMask.length; i++) {
            if (i % 2 == 0) {
                filterMask[i] = 0;
            } else {
                filterMask[i] = numerator / Math.pow(i, 2);
            }
        }
        return filterMask;
    }

    private void convolveSinogram(double[] mask) {
        int[][] convolveSinogram = new int[sinogramBitmap.length][sinogramBitmap[0].length];
        for (int i = 0; i < sinogramBitmap.length; i++) {
            for (int j = 0; j < sinogramBitmap[0].length; j++) {
                convolveSinogram[i][j] = convolveForCell(mask, j, i);
            }
        }
        sinogramBitmap = convolveSinogram;
    }

    private int convolveForCell(double[] mask, int row, int cellPosition) {
        double value = 0.0;
        int startPosition = cellPosition - (mask.length - 1) <= 0 ? 0 : cellPosition - (mask.length - 1);
        int stopPosition = cellPosition + (mask.length - 1) > (sinogramBitmap.length - 1) ? (sinogramBitmap.length - 1) : cellPosition + (mask.length - 1);
        int iterator = cellPosition - (mask.length - 1) >= 0 ? mask.length-1 : cellPosition;
        for (int i = startPosition; i <= stopPosition; i++) {
            value += sinogramBitmap[i][row] * mask[Math.abs(iterator--)];
        }
        return (int) value;
    }

    private class Normalizer implements Runnable {
        private int numberOfIteration;
        private int[][] bitmap;
        private FileManager fileManager;

        Normalizer(int[][] bitmap, FileManager fileManager, int numberOfIteration) {
            this.bitmap = bitmap;
            this.fileManager = fileManager;
            this.numberOfIteration = numberOfIteration;
        }

        @Override
        public void run() {
            normalizeAndCountRMSE(bitmap, numberOfIteration);
            fileManager.saveIndirectImage(bitmap, name, numberOfIteration);
        }
    }

}
