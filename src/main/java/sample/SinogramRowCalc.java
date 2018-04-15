package sample;

public class SinogramRowCalc implements Runnable {

    private int[][][] referenceToImage;
    private int[][] referenceToSinogram;
    private int[][] positions;
    private int myRowNumber;

    public SinogramRowCalc(int[][][] referenceToImage, int[][] referenceToSinogram, int[][] positions, int myRowNumber) {
        this.referenceToImage = referenceToImage;
        this.referenceToSinogram = referenceToSinogram;
        this.positions = positions;
        this.myRowNumber = myRowNumber;
    }

    @Override
    public void run() {
        for (int i = 1; i < positions.length; i++) {
            referenceToSinogram[i - 1][myRowNumber] = bresenham(i);
        }
    }


    private int bresenham(int numberOfDetector) {
        int sum = 0;
        int measures = 0;

        int coefficientD = 0;

        int dx = Math.abs(positions[numberOfDetector][0] - positions[0][0]);//lenght on axis X
        int dy = Math.abs(positions[numberOfDetector][1] - positions[0][1]);//lenght on axis Y

        int stepX = positions[0][0] < positions[numberOfDetector][0] ? 1 : -1;//direction RIGHT/LEFT
        int stepY = positions[0][1] < positions[numberOfDetector][1] ? 1 : -1;//direction DOWN/UP

        //start position
        int actualX = positions[0][0];
        int actualY = positions[0][1];

        if (dx >= dy) {//a <= 45 degrees
            while (true) {
                if (isInImage(actualX, actualY)) {
                    sum += getColorFromPixel(actualX, actualY);
                    measures++;
                }
                if (actualX == positions[numberOfDetector][0]) {
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
                if (isInImage(actualX, actualY)) {
                    sum += getColorFromPixel(actualX, actualY);
                    measures++;
                }
                if (actualY == positions[numberOfDetector][1]) {
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
        return sum / measures;
    }

    private boolean isInImage(int x, int y) {
        return x < referenceToImage.length && y < referenceToImage[0].length;
    }

    private int getColorFromPixel(int x, int y) {
        return referenceToImage[x][y][0];
    }
}
