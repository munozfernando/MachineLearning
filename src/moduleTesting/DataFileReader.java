package moduleTesting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DataFileReader {
	public DataFileReader() {
		//System.out.println(this.getDataFolderPath().toAbsolutePath().toString());
		
		Metrics m = new Metrics();
		
		
		Matrix<Integer> matrix = new Matrix<Integer>(5);
		
		
		for(int i = 0; i < matrix.getDimensions(); i++)
		{
			ArrayList<Integer> list = matrix.getRow(i);
			for(int j = 0; j < matrix.getDimensions(); j++) {
				list.add(j+(i*matrix.getDimensions()));
			}
		}
		
		System.out.println(matrix);
		
		try {
			matrix.transpose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(matrix);
		//System.out.println(m.getStdDeviation(Arrays.asList(1.0,2.0,3.0,4.0)));
		
		System.out.println(m.getCovariance(Arrays.asList(1.0,2.0,3.0,4.0), Arrays.asList(6.0,7.0,8.0,9.0)));
	}

	public Path getDataFolderPath() {
		Path dataFilePath = null;
		Scanner userInputScanner = new Scanner(System.in);
		do {
			try {
				dataFilePath = Paths.get("src/moduleTesting/data");

				File datafolder = dataFilePath.toFile();
				
				System.out.println("Files found. Select a file from below: ");
				List<File> files = getDirectoryFiles(datafolder);
				
				for(File f : files) {
					int ndx = f.getPath().lastIndexOf("\\");
					System.out.println(f.getPath().substring(ndx+1));
				}
				
				
			} catch (InvalidPathException | UnsupportedOperationException e) {
				System.out.println("Place data files in the data folder with .data extension. Enter the file name:");
			}

		} while (!Files.exists(dataFilePath));

		return dataFilePath;
	}

	private List<File> getDirectoryFiles(File dataFolder) {
		final String extension = ".data";
		
		List<File> fileList = new ArrayList<File>();

		for (File currentFile : dataFolder.listFiles()) {
			if (currentFile.getName().endsWith(extension)) {
				fileList.add(currentFile);
			}
		}
		return fileList;
	}
}
