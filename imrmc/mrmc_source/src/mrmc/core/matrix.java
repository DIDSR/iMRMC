package mrmc.core;

import java.util.*;

public class matrix {
	public matrix() {
		;
	}

	public static double[][] multiply(double[][] m1, double[][] m2) {
		int m1rows = m1.length;
		int m1cols = m1[0].length;
		int m2rows = m2.length;
		int m2cols = m2[0].length;
		if (m1cols != m2rows)
			throw new IllegalArgumentException("matrices don't match: "
					+ m1cols + " != " + m2rows);
		double[][] result = new double[m1rows][m2cols];
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				result[i][j] = 0.0;
		// multiply
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				for (int k = 0; k < m1cols; k++)
					result[i][j] += m1[i][k] * m2[k][j];

		return result;
	}

	public static double[] multiply(double[][] m1, double[] m2) {
		int m1rows = m1.length;
		int m1cols = m1[0].length;
		int m2rows = m2.length;
		if (m1cols != m2rows)
			throw new IllegalArgumentException("matrices don't match: "
					+ m1cols + " != " + m2rows);
		double[] result = new double[m1rows];
		for (int i = 0; i < m1rows; i++)
			result[i] = 0.0;
		// multiply
		for (int i = 0; i < m1rows; i++)
			for (int k = 0; k < m1cols; k++)
				result[i] += m1[i][k] * m2[k];

		return result;
	}

	public static int[][] multiply(int[][] m1, int[][] m2) {
		int m1rows = m1.length;
		int m1cols = m1[0].length;
		int m2rows = m2.length;
		int m2cols = m2[0].length;
		if (m1cols != m2rows)
			throw new IllegalArgumentException("matrices don't match: "
					+ m1cols + " != " + m2rows);
		int[][] result = new int[m1rows][m2cols];
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				result[i][j] = 0;
		// multiply
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				for (int k = 0; k < m1cols; k++)
					result[i][j] += m1[i][k] * m2[k][j];

		return result;
	}

	public double[] dotProduct(double[] m1, double[] m2) {
		double result[] = new double[m1.length];
		if (m1.length != m2.length)
			throw new IllegalArgumentException("vector length don't match : "
					+ m1.length + " != " + m2.length);
		// multiply
		for (int i = 0; i < m1.length; i++) {
			result[i] = m1[i] * m2[i];
		}
		return result;
	}

	public double[] scaleVector(double[] m1, double s) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++) {
			result[i] = m1[i] * s;
		}
		return result;
	}

	public int total(int[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		int T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	public double total(double[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	public double total(double[] m) {
		int rows = m.length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			T = T + m[i];
		return T;
	}

	public double[][] matrixTranspose(double[][] m) {
		double[][] result = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	public int[][] matrixTranspose(int[][] m) {
		int[][] result = new int[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	public void printMatrix(double[][] m) {
		int col = m[0].length;
		int row = m.length;
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				temp = temp + m[i][j] + "\t";
			}
			temp = temp + "\n";
			System.out.println(temp);
		}
	}

	public void printMatrix(int[][] m) {
		int col = m[0].length;
		int row = m.length;
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				temp = temp + m[i][j] + "\t";
			}
			temp = temp + "\n";
			System.out.println(temp);
		}
	}

	public int[][] extractFirstDimention(int[][][] m, int d2, int d3) {
		int[][] result = new int[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	public double[][] extractFirstDimention(double[][][] m, int d2, int d3) {
		double[][] result = new double[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	public double[][] linearTrans(double[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	public double[] linearTrans(double[] m, double k, double d) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++)
			result[i] = m[i] * k + d;
		return result;
	}

	public double[][] linearTrans(int[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	public double[][] subtract(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] - m2[i][j];
		return result;
	}

	public double[][] elementMultiply(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}

	public int[][] elementMultiply(int[][] m1, int[][] m2) {
		int[][] result = new int[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}

	public double[] matrixAdd(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] + m2[i];
		return result;
	}

	public double[][] matrixAdd(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] + m2[i][j];
		return result;
	}

	public double[] elementMultiply(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] * m2[i];
		return result;
	}

	public double[] rowSum(double[][] m) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++) {
			result[i] = 0;
			for (int j = 0; j < m[0].length; j++) {
				result[i] = result[i] + m[i][j];
			}
		}
		return result;
	}

	public double[] colSum(double[][] m) {
		double[] result = new double[m[0].length];
		for (int j = 0; j < m[0].length; j++) {
			result[j] = 0;
			for (int i = 0; i < m.length; i++) {
				result[j] = result[j] + m[i][j];
			}
		}
		return result;
	}

	public double[] setZero(int X) {
		double[] result = new double[X];
		for (int i = 0; i < X; i++)
			result[i] = 0.0;
		return result;
	}

	public double[][] setZero(int X, int Y) {
		double[][] result = new double[X][Y];
		for (int i = 0; i < X; i++)
			for (int j = 0; j < Y; j++)
				result[i][j] = 0.0;
		return result;
	}

	public double min(double[][] m) {
		double x = m[0][0];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++) {
				if (m[i][j] < x)
					x = m[i][j];
			}
		return x;

	}
}
