package moduleTesting;

import java.util.ArrayList;

public class Matrix<T> {

	private final int dimensions;
	private ArrayList<ArrayList<T>> matrix;

	// Figure out what concurrency safe collections are
	public Matrix(int dimensions) {
		if(dimensions < 1)
			throw new IllegalArgumentException();
		
		this.dimensions = dimensions;
		this.matrix = new ArrayList<ArrayList<T>>();
		
		while(dimensions-- > 0) 
			matrix.add(new ArrayList<T>());
	}
	
	public ArrayList<ArrayList<T>> getMatrix() {
		return matrix;
	}

	public void setMatrix(ArrayList<ArrayList<T>> matrix) {
		this.matrix = matrix;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setCell(int rowNdx, int colNdx, T data) {
		this.matrix.get(rowNdx).set(colNdx, data);
	}

	public T getCell(int rowNdx, int colNdx) {
		return this.matrix.get(rowNdx).get(colNdx);
	}

	public ArrayList<T> getRow(int rowNdx) {
		return matrix.get(rowNdx);
	}

	public void setRow(int rowNdx, ArrayList<T> sourceRow) {
		this.matrix.set(rowNdx, sourceRow);
	}

	// TODO: Set to a valid exception
	// May need optimization
	public void setColumn(int colNdx, ArrayList<T> sourceColumn) throws Exception {
		if (sourceColumn.size() != this.matrix.size())
			throw new Exception();
		for (int i = 0; i < this.matrix.size(); i++) {
			ArrayList<T> current = this.matrix.get(i);
			current.set(colNdx, sourceColumn.get(i));
		}
	}

	// This mehtod requires iterating through each row...
	// Should this become a time constraint issue, consider
	// storing as single long array, and abstracting operations...
	public ArrayList<T> getColumn(int colNdx) {
		ArrayList<T> columnList = new ArrayList<T>();
		matrix.forEach(row -> columnList.add(row.get(colNdx)));
		return columnList;
	}

	// Needs tested
	public Matrix<T> transposeTo(Matrix<T> that) throws Exception {

		if (that.getDimensions() != this.dimensions)
			throw new Exception();

		for (int i = 0; i < this.matrix.size(); i++) {
			for (int j = i; j < this.matrix.size(); j++) {
				T tempRowData = getCell(i, j);
				T tempColData = getCell(j, i);
				that.setCell(j, i, tempRowData);
				that.setCell(i, j, tempColData);
			}
		}
		return that;
	}

	// I like this :D
	public void transpose() throws Exception {
		this.transposeTo(this);
	}

}
