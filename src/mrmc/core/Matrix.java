/**
 * Matrix.java
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
 */

package mrmc.core;

import java.text.DecimalFormat;

/**
 * Contains matrix math equations
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class Matrix {

	/**
	 * copy vector
	 * 
	 * @param m input vector
	 * @return copy of m
	 */
	public static double[] copy(double[] m) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++) {
			result[i] = m[i];
		}
		return result;
	}

	/**
	 * copy matrix
	 * 
	 * @param m input matrix
	 * @return copy of m
	 */
	public static double[][] copy(double[][] m) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				result[i][j] = m[i][j];
			}
		}
		return result;
	}

	/**
	 * Scales a double vector by a double
	 * 
	 * @param m1 The vector
	 * @param s The scalar
	 * @return m1 scaled by s
	 */
	public static double[] scale(double[] m1, double s) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++) {
			result[i] = m1[i] * s;
		}
		return result;
	}

	/**
	 * Scales a double matrix by a double
	 * 
	 * @param m The matrix
	 * @param s The scalar
	 * @return m scaled by s
	 */
	public static double[][] scale(double[][] m, double s) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				result[i][j] = m[i][j] * s;
			}
		}
		return result;
	}

	/**
	 * Adds the corresponding elements of two double vectors
	 * 
	 * @param m1 First vector
	 * @param m2 Second vector
	 * @return Vector of m1 + m2
	 */
	public static double[] add(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] + m2[i];
		return result;
	}

	/**
	 * Adds the corresponding elements of two double matrices
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return Matrix of m1 + m2
	 */
	public static double[][] add(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] + m2[i][j];
		return result;
	}

	/**
	 * Square elements of a double vector
	 * 
	 * @param m1 vector
	 * @return vector with elements squared
	 */
	public static double[] squareTerms(double[] m1) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++) {
			result[i] = m1[i] * m1[i];
		}
		return result;
	}

	/**
	 * Square elements of a double matrix
	 * 
	 * @param m matrix
	 * @return matrix with elements squared
	 */
	public static double[][] squareTerms(double[][] m) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				result[i][j] = m[i][j] * m[i][j];
			}
		}
		return result;
	}

	/**
	 * Calculates the total of all values in an int matrix
	 * 
	 * @param m The matrix
	 * @return Total value of all entries in m
	 */
	public static int total(int[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		int T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	/**
	 * Calculates the total of all values in a double matrix
	 * 
	 * @param m The matrix
	 * @return Total value of all entries in m
	 */
	public static double total(double[][] m) {
		int rows = m.length;
		int cols = m[0].length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				T = T + m[i][j];
		return T;
	}

	/**
	 * Calculates the total of all values in a double vector
	 * 
	 * @param m The vector
	 * @return Total value of all entries in m
	 */
	public static double total(double[] m) {
		int rows = m.length;
		double T = 0;
		for (int i = 0; i < rows; i++)
			T = T + m[i];
		return T;
	}

	/**
	 * Multiplies two double matrices
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return m1 multiplied by m2
	 */
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

	/**
	 * Multiplies a double matrix by a double vector
	 * 
	 * @param m1 The matrix
	 * @param m2 The vectory
	 * @return m1 multiplied by m2
	 */
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

	/**
	 * Multiplies two int matrices
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return m1 multiplied by m2
	 */
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

	/**
	 * Calculates dot product of two double vectors
	 * 
	 * @param m1 First vector
	 * @param m2 Second vector
	 * @return dot product of m1, m2
	 */
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

	/**
	 * Transposes a double matrix
	 * 
	 * @param m The matrix
	 * @return Transposition of m
	 */
	public static double[][] matrixTranspose(double[][] m) {
		double[][] result = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	/**
	 * Transposes an int matrix
	 * 
	 * @param m The matrix
	 * @return Transposition of m
	 */
	public static int[][] matrixTranspose(int[][] m) {
		int[][] result = new int[m[0].length][m.length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++)
				result[j][i] = m[i][j];
		}
		return result;
	}

	/**
	 * Prints a double vector in an easy to read format
	 * 
	 * @param m The vector
	 */
	public static void printVector(double[] m) {
		DecimalFormat df = new DecimalFormat("0.###E0");
		String temp = "";
		for (int i = 0; i < m.length; i++) {
			int totalWidth = 14;
			int numWidth = df.format(m[i]).length();
			int numSpaces = totalWidth - numWidth;
			temp = temp + df.format(m[i]);
			for (int n = 0; n < numSpaces; n++) {
				temp = temp + " ";
			}
		}
		temp = temp + "\n";
		System.out.print(temp);
	}

	/**
	 * Prints a double matrix in an easy to read format
	 * 
	 * @param m The matrix
	 */
	public static void printMatrix(double[][] m) {
		int col = m[0].length;
		int row = m.length;
		DecimalFormat df = new DecimalFormat("0.###E0");
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				int totalWidth = 14;
				int numWidth = df.format(m[i][j]).length();
				int numSpaces = totalWidth - numWidth;
				temp = temp + df.format(m[i][j]);
				for (int n = 0; n < numSpaces; n++) {
					temp = temp + " ";
				}
			}
			temp = temp + "\n";
			System.out.print(temp);
		}
	}

	/**
	 * Prints an int matrix in an easy to read format
	 * 
	 * @param m The matrix
	 */
	public static void printMatrix(int[][] m) {
		int col = m[0].length;
		int row = m.length;
		for (int i = 0; i < row; i++) {
			String temp = "";
			for (int j = 0; j < col; j++) {
				temp = temp + m[i][j] + " ";
			}
			temp = temp + "\n";
			System.out.print(temp);
		}
	}

	// extracts elements from 3d matrix like IDL syntax m[*, d2, d3]
	/**
	 * Places one dimension of a 3-D matrix into a vector using a syntax similar
	 * to IDL of m[*,d2,d3], etc.
	 * 
	 * @param start How far along the dimension to iterate
	 * @param m The matrix
	 * @param d1 First dimension parameter
	 * @param d2 Second dimension parameter
	 * @param d3 Third dimension parameter
	 * @return Vector of all values in the specified dimension
	 */
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

	/**
	 * Gets two dimensions of a 3-d int matrix
	 * 
	 * @param m The matrix
	 * @param d2 Second dimension
	 * @param d3 Third dimension
	 * @return Matrix of two specified dimensions
	 */
	public static int[][] extractFirstDimension(int[][][] m, int d2, int d3) {
		int[][] result = new int[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	/**
	 * Gets two dimensions of a 3-d double matrix
	 * 
	 * @param m The matrix
	 * @param d2 Second dimension
	 * @param d3 Third dimension
	 * @return Matrix of two specified dimensions
	 */
	public static double[][] extractFirstDimension(double[][][] m, int d2,
			int d3) {
		double[][] result = new double[m.length][1];
		for (int i = 0; i < m.length; i++) {
			result[i][0] = m[i][d2][d3];
		}
		return result;
	}

	/**
	 * Performs linear transformation on a double matrix
	 * 
	 * @param m The matrix
	 * @param k Scalar
	 * @param d Add value
	 * @return Transformed matrix
	 */
	public static double[][] linearTrans(double[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	/**
	 * Performs linear transformation on a double vector
	 * 
	 * @param m The vector
	 * @param k Scalar
	 * @param d Add value
	 * @return Transformed matrix
	 */
	public static double[] linearTrans(double[] m, double k, double d) {
		double[] result = new double[m.length];
		for (int i = 0; i < m.length; i++)
			result[i] = m[i] * k + d;
		return result;
	}

	/**
	 * Performs linear transformation on an int matrix
	 * 
	 * @param m The matrix
	 * @param k Scalar
	 * @param d Add value
	 * @return Transformed matrix
	 */
	public static double[][] linearTrans(int[][] m, double k, double d) {
		double[][] result = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				result[i][j] = m[i][j] * k + d;
		return result;
	}

	/**
	 * Subtracts one double matrix from another
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return result of m1 - m2
	 */
	public static double[][] subtract(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] - m2[i][j];
		return result;
	}

	/**
	 * Multiplies the corresponding elements of a double matrix
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return Multiplied matrix
	 */
	public static double[][] elementMultiply(double[][] m1, double[][] m2) {
		double[][] result = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}

	/**
	 * Multiplies the corresponding elements of an int matrix
	 * 
	 * @param m1 First matrix
	 * @param m2 Second matrix
	 * @return Multiplied matrix
	 */
	public static int[][] elementMultiply(int[][] m1, int[][] m2) {
		int[][] result = new int[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				result[i][j] = m1[i][j] * m2[i][j];
		return result;
	}





	/**
	 * Same as double dot product, but doesn't check bounds
	 * 
	 * @param m1 First vector
	 * @param m2 Second vector
	 * @return Multiplied vector
	 * @see #dotProduct(double[], double[])
	 */
	public static double[] elementMultiply(double[] m1, double[] m2) {
		double[] result = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			result[i] = m1[i] * m2[i];
		return result;
	}

	/**
	 * Sums across all rows in the matrix
	 * 
	 * @param m The matrix
	 * @return Array of sums for each row
	 */
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

	/**
	 * Sums across all columns in the matrix
	 * 
	 * @param m The matrix
	 * @return Array of sums for each column
	 */
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

	/**
	 * Creates a double vector of size X with all elements initialized to zero.
	 * Note that this method is not necessary since declaring a double array of
	 * any dimensions automatically initializes its contents to zero.
	 * 
	 * @param X size of vector
	 * @return zero'd vector
	 */
	public static double[] setZero(int X) {
		double[] result = new double[X];
		for (int i = 0; i < X; i++)
			result[i] = 0.0;
		return result;
	}

	/**
	 * Creates a double matrix of dimensions X, Y with all elements initialized
	 * to zero. Note that this method is not necessary since declaring a double
	 * array of any dimensions automatically initializes its contents to zero.
	 * 
	 * @param X First dimension of matrix
	 * @param Y Second dimension of matrix
	 * @return zero'd matrix
	 */
	public static double[][] setZero(int X, int Y) {
		double[][] result = new double[X][Y];
		for (int i = 0; i < X; i++)
			for (int j = 0; j < Y; j++)
				result[i][j] = 0.0;
		return result;
	}

	/**
	 * Gets the minimum value in a double matrix
	 * 
	 * @param m The matrix
	 * @return The minimum value
	 */
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
