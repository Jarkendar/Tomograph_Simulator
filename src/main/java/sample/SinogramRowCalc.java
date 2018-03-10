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
        double step = ((double) Math.abs(positions[0][0] - positions[numberOfDetector][0])) / ((double) NUMBER_OF_MEASURE_ON_LINE);
        int sum = 0;
        for (int i = 1; i < NUMBER_OF_MEASURE_ON_LINE; i++) {
            double currentX = positions[0][0] < positions[numberOfDetector][0] ? (positions[0][0] + step * i) : (positions[0][0] - step * i);
            double currentY = linearFunctionParameters[numberOfDetector][0] * currentX + linearFunctionParameters[numberOfDetector][1];
            sum += getColorFromPixel((int) currentX, (int) currentY);
        }
        return sum / NUMBER_OF_MEASURE_ON_LINE;
    }

    private int getColorFromPixel(int x, int y) {
        return referenceToImage[x][y][0];
    }
}
