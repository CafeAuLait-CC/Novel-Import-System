package assignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataLoader {
	public boolean loadWriters(Connection conn) throws SQLException, FileNotFoundException {
		Statement stmt = conn.createStatement();
		
		FileInputStream fis = null;
		BufferedReader br = null;
		
		try {
			fis = new FileInputStream("text_data/writers.txt");
			br = new BufferedReader(new InputStreamReader(fis));
		} catch (Exception e) {
			System.out.println("\n!! File not found! - text_data/writers.txt");
			System.out.println("!! Failed to load books info from file!");
			return false;
		}
		
		System.out.println("\n- Reading writer info from text file...");
		try {
			String str = null;
			int numOfCols = -1;
			int lineNum = 1;
			while ((str = br.readLine()) != null) {
				String[] line = str.split("\t");
				if (lineNum == 1) {
					// First line in text file, including column names (headers)
					// create table "WRITERS"
					numOfCols = line.length;
					String sql = "create table writers (";
					for (int i = 0; i < numOfCols; i++) {
						sql += line[i];
						if (i != numOfCols-1) sql += " varchar(100), ";
					}
					sql += " varchar(100))";
					System.out.println("- Executing SQL: " + sql);
					try {
						stmt.execute(sql);
						stmt.execute("alter table writers alter column Name not null");
						stmt.execute("alter table writers add primary key(Name)");
						System.out.println("- Table 'WRITERS' created!");
					} catch (Exception e) {
						System.out.println("-- Table 'WRITERS' already exists!");
					}
					lineNum++;
				} else {
					// Read lines from text file and insert records into table 'WRITERS'
					if (line.length != numOfCols) {
						System.out.println("-- Column size incorrect: " + line.length + " line will be skipped!");
						for (int i = 0; i < line.length; i++) System.out.println(line[i]);
						continue;
					}
					PreparedStatement pstmt = conn.prepareStatement("insert into writers values (?,?,?,?,?)");
					for (int i = 0; i < line.length; i++) {
						pstmt.setString(i+1, line[i]);
					}
					try {
						pstmt.execute();
					} catch (Exception e) {
						System.out.println("-- " + line[0] + " already exists!");
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				br.close();
				fis.close();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		return true;
	}
	
	public boolean loadBooks(Connection conn) throws SQLException, FileNotFoundException {

		Statement stmt = conn.createStatement();
		
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream("text_data/books.txt");
			br = new BufferedReader(new InputStreamReader(fis));
		} catch (Exception e) {
			System.out.println("!! File not found! - text_data/books.txt");
			System.out.println("!! Failed to load books info from file!");
			return false;
		}
		
		System.out.println("- Reading book info from text file...");
		try {
			String str = null;
			int numOfCols = -1;
			int lineNum = 1;
			
			
			while ((str = br.readLine()) != null) {
				String[] line = str.split("\t");
				if (lineNum == 1) {
					// First line in text file, including column names (headers)
					// create table "BOOKS"
					numOfCols = line.length;
					String sql = "create table books (";
					for (int i = 0; i < numOfCols; i++) {
						sql += line[i];
						if (i != numOfCols-1) sql += " varchar(100), ";
					}
					sql += " varchar(50))";
					System.out.println("- Executing SQL: " + sql);
					try {
						stmt.execute(sql);
						stmt.execute("alter table books alter column ISBN13 not null");
						stmt.execute("alter table books add primary key(ISBN13)");
						System.out.println("- Table 'BOOKS' created!");
					} catch (Exception e) {
						System.out.println("-- Table 'BOOKS' already exists!");
					}
					lineNum++;
				} else {
					// Read lines from text file and insert records into table 'BOOKS'
					if (line.length != numOfCols) {
						System.out.println("-- Column size incorrect: " + line.length + " line will be skipped!");
						for (int i = 0; i < line.length; i++) System.out.println(line[i]);
						continue;
					}
					PreparedStatement pstmt = conn.prepareStatement("insert into books values (?,?,?,?,?,?,?,?,?)");
					for (int i = 0; i < line.length; i++) {
						pstmt.setString(i+1, line[i]);
					}
					try {
						pstmt.execute();
					} catch (Exception e) {
						System.out.println("-- " + line[0] + " already exists!");
					}
					
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				br.close();
				fis.close();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		return true;
	}
}
