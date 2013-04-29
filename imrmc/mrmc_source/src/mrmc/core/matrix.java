/*
 * matrix.java
 * 
 * v2.0b
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
 * 
 * This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) 
 * by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 
 * 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the 
 * Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the 
 * Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or 
 * implied, about its quality, reliability, or any other characteristic.   Further, use of this code in no way 
 * implies endorsement by the FDA or confers any advantage in regulatory decisions.  Although this software 
 * can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they 
 * are derived from it, and any modified versions bear some notice that they have been modified.
 *     
 *     Contains matrix math equations
 */

package mrmc.core;

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

	public static double[] dotProduct(double[] m1, double[] m2) {
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

	public static double[] scaleVector(double[] m1, double s) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++) {
			result[i] = m1[i] * s;
		}
		return result;
	}

	public static int total(int[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		int T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	public static double total(double[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	public static double total(double[] m) {
		int rows = m.length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			T = T + m[i];
		return T;
	}

	public static double[][] matrixTranspose(double[][] m) {
		double[][] result = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	public static int[][] matrixTranspose(int[][] m) {
		int[][] result = new int[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	public static void printMatrix(double[][] m) {
		int col = m[0].length;
		int row = m.length;
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				temp = temp + m[i][j] + "\t";
			}
			temp = temp + "\n";
			System.out.print(temp);
		}
	}

	public static void printMatrix(int[][] m) {
		int col = m[0].length;
		int row = m.length;
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				temp = temp + m[i][j] + "\t";
			}
			temp = temp + "\n";
			System.out.print(temp);
		}
	}

	// extracts elements from 3d matrix like IDL syntax m[*, d2, d3]
	public static double[] get1Dimension(int start, double[][][] m, String d1,
			String d2, String d3) {
		double[] result;
		if (d1.equals("*")) {
			result = new double[m.length - start];
			int dim2 = Integer.parseInt(d2);
			int dim3 = Integer.parseInt(d3);
			for (int i = 0; i < m.length - start; i++) {
				result[i] = m[i + start][dim2][dim3];
			}

		} else if (d2.equals("*")) {
			result = new double[m[0].length - start];
			int dim1 = Integer.parseInt(d1);
			int dim3 = Integer.parseInt(d3);
			for (int i = 0; i < m[0].length - start; i++) {
				result[i] = m[dim1][i + start][dim3];
			}
		} else if (d3.equals("*")) {
			result = new double[m[0][0].length - start];
			int dim1 = Integer.parseInt(d1);
			int dim2 = Integer.parseInt(d2);
			for (int i = 0; i < m[0][0].length - start; i++) {
				result[i] = m[dim1][dim2][i + start];
			}
		} else {
			throw new NumberFormatException(
					"Must indicate a dimension to iterate");
		}

		return result;
	}

	public static int[][] extractFirstDimension(int[][][] m, int d2, int d3) {
		int[][] result = new int[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	public static double[][] extractFirstDimension(double[][][] m, int d2,
			int d3) {
		double[][] result = new double[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	public static double[][] linearTrans(double[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	public static double[] linearTrans(double[] m, double k, double d) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++)
			result[i] = m[i] * k + d;
		return result;
	}

	public static double[][] linearTrans(int[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	public static double[][] subtract(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] - m2[i][j];
		return result;
	}

	public static double[][] elementMultiply(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}

	public static int[][] elementMultiply(int[][] m1, int[][] m2) {
		int[][] result = new int[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}

	public static double[] matrixAdd(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] + m2[i];
		return result;
	}

	public static double[][] matrixAdd(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] + m2[i][j];
		return result;
	}

	public static double[] elementMultiply(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] * m2[i];
		return result;
	}

	public static double[] rowSum(double[][] m) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++) {
			result[i] = 0;
			for (int j = 0; j < m[0].length; j++) {
				result[i] = result[i] + m[i][j];
			}
		}
		return result;
	}

	public static double[] colSum(double[][] m) {
		double[] result = new double[m[0].length];
		for (int j = 0; j < m[0].length; j++) {
			result[j] = 0;
			for (int i = 0; i < m.length; i++) {
				result[j] = result[j] + m[i][j];
			}
		}
		return result;
	}

	public static double[] setZero(int X) {
		double[] result = new double[X];
		for (int i = 0; i < X; i++)
			result[i] = 0.0;
		return result;
	}

	public static double[][] setZero(int X, int Y) {
		double[][] result = new double[X][Y];
		for (int i = 0; i < X; i++)
			for (int j = 0; j < Y; j++)
				result[i][j] = 0.0;
		return result;
	}

	public static double min(double[][] m) {
		double x = m[0][0];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++) {
				if (m[i][j] < x)
					x = m[i][j];
			}
		return x;

	}
}
