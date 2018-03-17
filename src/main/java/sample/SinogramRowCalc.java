package sample;

public class SinogramRowCalc implements Runnable {

    private static final int NUMBER_OF_MEASURE_ON_LINE = 1000 + 1;//todo to change to optimize better one measure on one x step

    private int[][][] referenceToImage;
    private int[][] referenceToSinogram;
    private int[][] positions;
    private int myRowNumber;
    private double[][] linearFunctionParameters;

    public SinogramRowCalc(int[][][] referenceToImage, int[][] referenceToSinogram, int[][] positions, int myRowNumber, double[][] linearFunctionParameters) {
        this.referenceToImage = referenceToImage;
        this.referenceToSinogram = referenceToSinogram;
        this.positions = positions;
        this.myRowNumber = myRowNumber;
        this.linearFunctionParameters = linearFunctionParameters;
    }

    @Override
    public void run() {
        for (int i = 1; i < positions.length; i++) {
            referenceToSinogram[i - 1][myRowNumber] = countAverageValueOnLineFromEmitterToDetector(i);
        }
    }

    private int countAverageValueOnLineFromEmitterToDetector(int numberOfDetector) {
        int numberOfMeasure = Math.abs(positions[0][0] - positions[numberOfDetector][0]);
        if (numberOfMeasure == 0) numberOfMeasure = 1;
        int sum = 0;
        for (int step = 1; step < numberOfMeasure; step++) {
            double currentX = positions[0][0] < positions[numberOfDetector][0] ? (positions[0][0] + step) : (positions[0][0] - step);
            double currentY = linearFunctionParameters[numberOfDetector][0] * currentX + linearFunctionParameters[numberOfDetector][1];
            sum += getColorFromPixel((int) currentX, (int) currentY);
        }
        return sum / numberOfMeasure;
    }

    private int getColorFromPixel(int x, int y) {
        return referenceToImage[x][y][0];
    }
}
