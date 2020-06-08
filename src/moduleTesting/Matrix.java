package moduleTesting;

import java.util.ArrayList;

/**
 * @author Fernando Munoz
 *
 * @param <T> Data type of the matrix
 */
public class Matrix<T> {

	private final int DIMENSIONS;
	
	/**
	 * If data gets large enough, we can convert this over to a custom
	 * container, with required functionality and non-wrapper types. 
	 * Java states that for scientific computing, primitives are best.
	 * 
	 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/language/autoboxing.html">Autoboxing</a>
	 */
	private ArrayList<ArrayList<T>> data;

	// TODO: Figure out what concurrency safe collections there are
	// TODO: Initialize values in each row
	/**
	 * @param dimensions desired size of new square matrix
	 */
	public Matrix(int dimensions) {
		if(dimensions < 1)
			throw new IllegalArgumentException();
		
		this.DIMENSIONS = dimensions;
		this.data = new ArrayList<ArrayList<T>>(dimensions);
		
		while(dimensions-- > 0) 
			data.add(new ArrayList<T>());
		
		//initMatrixData();
	}
	
	// Since we are using reference values, this is a no-go
	private void initMatrixData(T initialValue) {
		data.forEach(e -> {
			int i = 0;
			while(i < this.DIMENSIONS) {
				e.add(initialValue);
				i++;
			}
		});
	}
	
	public ArrayList<ArrayList<T>> getData() {
		return data;
	}

	public void setData(ArrayList<ArrayList<T>> matrix) {
		this.data = matrix;
	}

	public int getDimensions() {
		return DIMENSIONS;
	}

	public void setCell(int rowNdx, int colNdx, T data) {
		this.data.get(rowNdx).set(colNdx, data);
	}

	public T getCell(int rowNdx, int colNdx) {
		return this.data.get(rowNdx).get(colNdx);
	}

	public ArrayList<T> getRow(int rowNdx) {
		return data.get(rowNdx);
	}

	public void setRow(int rowNdx, ArrayList<T> sourceRow) {
		this.data.set(rowNdx, sourceRow);
	}

	// TODO: Set to a valid exception
	/**
	 * @param colNdx index of column to replace
	 * @param sourceColumn an ArrayList to use as source
	 * @throws Exception
	 */
	public void setColumn(int colNdx, ArrayList<T> sourceColumn) throws Exception {
		if (sourceColumn.size() != this.data.size())
			throw new Exception();
		for (int i = 0; i < this.data.size(); i++) {
			ArrayList<T> current = this.data.get(i);
			current.set(colNdx, sourceColumn.get(i));
		}
	}

	/**
	 * @param colNdx index of the column to return
	 * @return an ArrayList consisting of the elements at the provided index's column
	 */
	public ArrayList<T> getColumn(int colNdx) {
		ArrayList<T> columnList = new ArrayList<>();
		data.forEach(row -> columnList.add(row.get(colNdx)));
		return columnList;
	}

	/**
	 * @param that a matrix to transpose to
	 * @return the transposed matrix
	 * @throws Exception if dimensions do not match
	 */
	public Matrix<T> transposeTo(Matrix<T> that) throws Exception {

		if (that.getDimensions() != this.DIMENSIONS)
			throw new Exception();

		for (int i = 0; i < this.data.size(); i++) {
			for (int j = i; j < this.data.size(); j++) {
				T tempRowData = this.getCell(i, j);
				T tempColData = this.getCell(j, i);
				that.setCell(j, i, tempRowData);
				that.setCell(i, j, tempColData);
			}
		}
		return that;
	}

	/**
	 * @throws Exception
	 */
	public void transpose() throws Exception {
		this.transposeTo(this);
	}
	
	@Override
	public String toString() {
		StringBuilder matrixString = new StringBuilder();
		this.getData().forEach(list -> {
			matrixString.append(list.toString());
			matrixString.append("\n");
		});
		return matrixString.toString();
	}

}
