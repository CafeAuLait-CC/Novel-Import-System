package assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class DBOperations {
	
	public void add(Connection conn) throws SQLException {
		
		boolean TABLE_BOOKS_NOT_EXIST = false;
		boolean TABLE_WRITERS_NOT_EXIST = false;
		int numColsBooks = 0;
		int numColsWriters = 0;
		int numCols = 0;
		
		if ((numColsBooks = ifTableExists("books", conn)) == -1) {
			TABLE_BOOKS_NOT_EXIST = true;
		}
		if ((numColsWriters = ifTableExists("writers", conn)) == -1) {
			TABLE_WRITERS_NOT_EXIST = true;
		}
		
		Scanner sc = new Scanner(System.in);
		System.out.print("(Add) Add a writer or a book? (\"quit\" to quit add mode)\n(Add) >> ");
		String cmd = sc.nextLine();
		
		PreparedStatement pstmt;
		Statement stmt = conn.createStatement();
		ResultSet rs;
		ResultSetMetaData rsmd;
		while (!"quit".equals(cmd)) {
			String tableName = "";
			switch (cmd) {
			case "writer":
				if (TABLE_WRITERS_NOT_EXIST) {
					System.out.println("Table 'WRITERS' does not exist!");
					break;
				}
				tableName = "writers";
				numCols = numColsWriters;

			case "book":
				if (TABLE_BOOKS_NOT_EXIST) {
					System.out.println("Table 'BOOKS' does not exist!");
					break;
				}
				if ("book".equals(cmd)) {
					tableName = "books";
					numCols = numColsBooks;
				}
				
				// Assemble the SQL query
				String sql = "insert into " + tableName + " values (";
				for (int i = 0; i < numCols-1; i++) {
					sql += "?,";
				}
				sql += "?)";
				pstmt = conn.prepareStatement(sql);
				System.out.println("(Add) Please enter the following information:");
				sql = "select * from " + tableName;
				rs = stmt.executeQuery(sql);
				rsmd = rs.getMetaData();
				
				// Ask user to enter the information for each column of this record
				for (int i = 0; i < numCols; i++) {
					System.out.print("(Add) >> " + rsmd.getColumnName(i+1) + ": ");
					pstmt.setString(i+1, sc.nextLine());
				}
				try {
					pstmt.execute();
					System.out.println("Record added successfully!");
				} catch (Exception e) {
					System.out.println("Record of this book already exists!");
				}
				break;
			case "":
				System.out.print("(Add) >> ");
				break;
			default:
				System.out.println("(Add) >> Invalid input, please try again!");
			}
			
			System.out.print("(Add) Add a writer or a book? (\"quit\" to quit add mode)\n(Add) >> ");
			cmd = sc.nextLine();
		}
		System.out.println("");
		
	}
	
	public void remove(Connection conn) throws SQLException {
		
		boolean TABLE_BOOKS_NOT_EXIST = false;
		boolean TABLE_WRITERS_NOT_EXIST = false;
		
		if (ifTableExists("books", conn) == -1) {
			TABLE_BOOKS_NOT_EXIST = true;
		}
		if (ifTableExists("writers", conn) == -1) {
			TABLE_WRITERS_NOT_EXIST = true;
		}
		
		Scanner sc = new Scanner(System.in);
		System.out.print("(Delete) Delete a writer or a book? (\"quit\" to quit delete mode)\n(Delete) >> ");
		String cmd = sc.nextLine();
		PreparedStatement pstmt;
		while (!"quit".equals(cmd)) {
			
			switch (cmd) {
			case "writer":
				if (TABLE_WRITERS_NOT_EXIST) {
					System.out.println("Table 'WRITERS' does not exist!");
				} else {
					System.out.print("(Delete) >> Please enter the name of the writer you want to delete:\n(Delete) >> ");
					String name = sc.nextLine();
					pstmt = conn.prepareStatement("select * from writers where name=?");
					pstmt.setString(1, name);
					ResultSet rs = pstmt.executeQuery();
					if (!rs.next()) {
						// Requested record doesn't exist
						System.out.println(name + " not found in records!");
					} else {
						// Confirm with user for deletion
						System.out.println("(Delete) Are you sure to delete the following record? (y/N)");
						rs = pstmt.executeQuery();
						listSQLResults(rs);
						System.out.print("(Delete) >> ");
						String str = sc.nextLine();
						if ("yes".equals(str.toLowerCase())) {
							pstmt = conn.prepareStatement("delete from writers where name=?");
							pstmt.setString(1, name);
							int lineChanged = pstmt.executeUpdate();
							System.out.println("Record deleted! " + lineChanged + " lines changed.");
						} else {
							System.out.println("Delete operation aborted.");
						}
					}
				}
				break;
			case "book":
				if (TABLE_BOOKS_NOT_EXIST) {
					System.out.println("Table 'BOOKS' does not exist!");
				} else {
					System.out.print("(Delete) >> Please enter the title of the book you want to delete:\n(Delete) >> ");
					String title = sc.nextLine();
					pstmt = conn.prepareStatement("select * from books where title=?");
					pstmt.setString(1, title);
					ResultSet rs = pstmt.executeQuery();
					if (!rs.next()) {
						// Did not find the requested book title!
						System.out.println(title + " not found in records!");
					} else {
						String isbn = "";
						if (rs.next()) {
							// The requested book title links to more than one records,
							// they have different ISBN-13 code.
							// Thus, show the conflict results and ask the user to enter an expected ISBN-13 code
							System.out.println("(Delete) There are more then one record with this title: ");
							rs = pstmt.executeQuery();
							listSQLResults(rs);
							System.out.print("(Delete) Enter the ISBN-13 code of the book you want to delete:\n(Delete) >> ");
							
							boolean checkISBN = true;
							while (checkISBN) {
								// Validate the input ISBN-13 code
								isbn = sc.nextLine();
								pstmt = conn.prepareStatement("select * from books where title=? and ISBN13=?");
								pstmt.setString(1, title);
								pstmt.setString(2, isbn);
								rs = pstmt.executeQuery();
								if (!rs.next()) {
									System.out.print("ISBN-13 incorrect, please try again!\n(Delete) >> ");
								} else {
									checkISBN = false;
								}
							}
							
							// Expected book with correct ISBN-13 found,
							// prepare a statement to show and confirm the result with user.
							pstmt = conn.prepareStatement("select * from books where ISBN13=?");
							pstmt.setString(1, isbn);
						}
						
						System.out.println("(Delete) Are you sure to delete the following record? (y/N)");
						rs = pstmt.executeQuery();	
						listSQLResults(rs);
						
						if ("".equals(isbn)) {
							// There is no conflict on book title, previous check was skipped
							// Use the book title to acquire the ISBN-13 code
							pstmt = conn.prepareStatement("select title,ISBN13 from books where title=?");
							pstmt.setString(1, title);
							rs = pstmt.executeQuery();
							rs.next();
							isbn = rs.getString(2);
						}
						
						System.out.print("(Delete) >> ");
						String str = sc.nextLine();
						if ("yes".equals(str.toLowerCase()) || "y".equals(str.toLowerCase())) {
							// Final confirmation.
							// Delete the record using the ISBN-13 code.
							pstmt = conn.prepareStatement("delete from books where ISBN13=?");
							pstmt.setString(1, isbn);
							int lineChanged = pstmt.executeUpdate();
							System.out.println("Record deleted! " + lineChanged + " lines changed.");
						} else {
							System.out.println("Delete operation aborted.");
						}
					}
				}
				break;
			case "":
				System.out.print("(Delete) >> ");
				break;
			default:
				System.out.println("(Delete) >> Invalid input, please try again!");
			}
			
			System.out.print("(Delete) Delete a writer or a book? (\"quit\" to quit delete mode)\n(Delete) >> ");
			cmd = sc.nextLine();
		}
		System.out.println("");
		
	}
	
	public void update(Connection conn) throws SQLException {
		
		boolean TABLE_BOOKS_NOT_EXIST = false;
		boolean TABLE_WRITERS_NOT_EXIST = false;
		int numColsBooks = 0;
		int numColsWriters = 0;
		int numCols = 0;
		
		if ((numColsBooks = ifTableExists("books", conn)) == -1) {
			TABLE_BOOKS_NOT_EXIST = true;
		}
		if ((numColsWriters = ifTableExists("writers", conn)) == -1) {
			TABLE_WRITERS_NOT_EXIST = true;
		}
		
		Scanner sc = new Scanner(System.in);
		System.out.print("(Update) Update writers or books? (\"quit\" to quit update mode)\n(Update) >> ");
		String cmd = sc.nextLine();
		
		Statement stmt = conn.createStatement();
		ResultSet rs;
		ResultSetMetaData rsmd;
		
		while (!"quit".equals(cmd)) {
			
			String tableName = "";
			switch (cmd) {
			case "writer":
			case "writers":
				if (TABLE_WRITERS_NOT_EXIST) {
					System.out.println("Table 'WRITERS' does not exist!");
					break;
				}
				tableName = "writers";
				numCols = numColsWriters;
				
			case "book":
			case "books":
				if (TABLE_BOOKS_NOT_EXIST) {
					System.out.println("Table 'BOOKS' does not exist!");
					break;
				}
				if ("book".equals(cmd) || "books".equals(cmd)) {
					tableName = "books";
					numCols = numColsBooks;
				}
				
				// First, show the header of the table to the user
				String sql = "select * from " + tableName;
				rs = stmt.executeQuery(sql);
				rsmd = rs.getMetaData();
				ArrayList<String> header = new ArrayList<String>();
				System.out.println("(Update) This table have the following columns:");
				System.out.println("tableName" + tableName);
				System.out.println("numCols" + numCols);
				
				for (int i = 0; i < numCols; i++) {
					System.out.print(rsmd.getColumnName(i+1) + "\t");
					header.add(rsmd.getColumnName(i+1));
				}
				System.out.println("\n");
				
				// Ask the user to select columns to update
				System.out.println("(Update) Which columns do you want to update? (Separate by \",\")");
				String colsUpdateStr = "";
				while ("".equals(colsUpdateStr)) {
					System.out.print("(Update) >> ");
					colsUpdateStr = sc.nextLine();
				}
				String[] colsUpdate = colsUpdateStr.split(",");
				
				// Validate the input column_names
				boolean hasInvalidKeys = false;
				for (int i = 0; i < colsUpdate.length; i++) {
					if (!header.contains(colsUpdate[i].toUpperCase().trim())) {
						System.out.println("Input contains invalid column name \"" + colsUpdate[i].trim().toUpperCase() + "\"! Operation aborted!");
						hasInvalidKeys = true;
						break;
					}
				}
				if (hasInvalidKeys) break;
				
				// Get new values for columns from user
				// And assemble the SQL query
				sql = "update " + tableName + " set ";
				System.out.println("(Update) Enter new values:");
				for (int i = 0; i < colsUpdate.length; i++) {
					System.out.print("(Update) >> " + colsUpdate[i].toUpperCase().trim() + ": ");
					sql = sql + colsUpdate[i].trim() + "=";
					String val = sc.nextLine();
					sql = sql + "'" + val + "'";
					if (i != colsUpdate.length-1) {
						sql += ", ";
					}
				}
				
				// Get conditions (rows) for the update query
				System.out.println("(Update) Which rows do you want to update?"
						+ "(Enter \"column name, value\", or \"No\" to skip) (Separate by \",\")");
				String condStr = "";
				while ("".equals(condStr)) {
					System.out.print("(List) >> ");
					condStr = sc.nextLine();
				}
				
				// If user input != "No", parse the input string and assemble the SQL query
				if (!"N".equals(condStr.toUpperCase()) && !"NO".equals(condStr.toUpperCase())) {
					sql += " where ";
					String[] conds = condStr.split(",");
					if (conds.length % 2 == 1) {	
						// number of input parameters must be an even number, i.e. (key, value) pairs
						hasInvalidKeys = true;
						System.out.println("Number of input params incorrect! Operation aborted!");
						break;
					}
					for (int i = 0; i < conds.length-1; i+=2) {
						if (header.contains(conds[i].toUpperCase().trim())) {
							sql = sql + conds[i].trim() + "=" + conds[i+1].trim();
							if (i != conds.length-2) {
								sql += " and ";
							}
						} else {
							// column_name entered by user does not exist in the table
							hasInvalidKeys = true;
							System.out.println("Input contains invalid column name \"" + conds[i].trim().toUpperCase() + "\"! Operation aborted!");
							break;
						}
					}
					if (hasInvalidKeys) break;
				} else {
					// If user input == "No", confirm with user to change the entire table
					System.out.print("(Update) Are you sure to update all records in this table? (y/N)\n(Update) >> ");
					String confirmationStr = sc.nextLine();
					if (!"y".equals(confirmationStr.toLowerCase()) && !"yes".equals(confirmationStr.toLowerCase())) {
						// User does not confirm, cancel the update operation!
						System.out.println("Update operation aborted!");
						break;
					}
				}
				
				// Execute SQL query
				System.out.println("executing: \n" + sql);
				try {
					int lineChanged = stmt.executeUpdate(sql);
					System.out.println("Record updated! " + lineChanged + " lines changed.");
				} catch (Exception e) {
					System.out.println(e);
				}
				
				break;
			case "":
				System.out.print("(Update) >> ");
				break;
			default:
				System.out.println("(Update) >> Invalid input, please try again!");
			}
			
			System.out.print("(Update) Update writers or books? (\"quit\" to quit update mode)\n(Update) >> ");
			cmd = sc.nextLine();
		}
		System.out.println("");
		
	}
	
	public void list(Connection conn) throws SQLException {
		
		boolean TABLE_BOOKS_NOT_EXIST = false;
		boolean TABLE_WRITERS_NOT_EXIST = false;
		int numColsBooks = 0;
		int numColsWriters = 0;
		int numCols = 0;
		
		if ((numColsBooks = ifTableExists("books", conn)) == -1) {
			TABLE_BOOKS_NOT_EXIST = true;
		}
		if ((numColsWriters = ifTableExists("writers", conn)) == -1) {
			TABLE_WRITERS_NOT_EXIST = true;
		}
		
		Scanner sc = new Scanner(System.in);
		
		String cmd = "";
		
		Statement stmt = conn.createStatement();
		ResultSet rs;
		ResultSetMetaData rsmd;
		
		while (!"quit".equals(cmd)) {
			
			System.out.println("(List) What do you want to check? Please select a number from below. (\"quit\" to quit list mode)");
			System.out.println("1. List the writers records.");
			System.out.println("2. List the books records.");
			System.out.print("3. More options, jump to custom mode!\n(List) >> ");
			cmd = sc.nextLine();
			
			String tableName = "";
			switch (cmd) {
			case "1":
				if (TABLE_WRITERS_NOT_EXIST) {
					System.out.println("Table 'WRITERS' does not exist!");
					break;
				}
				tableName = "writers";
				numCols = numColsWriters;
				
			case "2":
				if (TABLE_BOOKS_NOT_EXIST) {
					System.out.println("Table 'BOOKS' does not exist!");
					break;
				}
				if ("2".equals(cmd)) {
					tableName = "books";
					numCols = numColsBooks;
				}
				
				// First, show the header of the table to the user
				String sql = "select * from " + tableName;
				rs = stmt.executeQuery(sql);
				rsmd = rs.getMetaData();
				ArrayList<String> header = new ArrayList<String>();
				System.out.println("(List) This table have the following columns:");
				for (int i = 0; i < numCols; i++) {
					System.out.print(rsmd.getColumnName(i+1) + "\t");
					header.add(rsmd.getColumnName(i+1));
				}
				System.out.println("\n");
				
				// Ask the user to select columns to print
				System.out.println("(List) Which columns do you want to see in this table? Enter \"all\" to show all columns. (Separate by \",\")");
				String filteredColsStr = "";
				while ("".equals(filteredColsStr)) {
					System.out.print("(List) >> ");
					filteredColsStr = sc.nextLine();
				}
				String[] filteredCols = filteredColsStr.split(",");
				
				// Assembling the SQL query
				// First part: "select ... from ..."
				sql = "select ";
				boolean hasInvalidWord = false;
				if ("all".equals(filteredCols[0])) {	// "all" --> "select * from table_name"
					sql = sql + "* from " + tableName;
				} else {
					// user input "col1, col2, ..."
					// SQL query --> "select col1, col2,... from table_name"
					for (int i = 0; i < filteredCols.length; i++) {
						if (!header.contains(filteredCols[i].trim().toUpperCase())) {
							System.out.println("Input contains invalid column name \"" + filteredCols[i].trim().toUpperCase() + "\"! Operation aborted!");
							hasInvalidWord = true;
							break;
						} else {
							sql += filteredCols[i].trim();
							if (i != filteredCols.length-1) {
								sql += ", ";
							}
						}
					}
					if (hasInvalidWord) break;
					sql = sql + " from " + tableName;
				}
				
				// Second part: "select ... from ... where ..."
				System.out.println("(List) Are you looking for specific column values? "
						+ "(Enter \"column name, value\", or \"No\" to skip) (Separate by \",\")");
				String condStr = "";
				while ("".equals(condStr)) {
					System.out.print("(List) >> ");
					condStr = sc.nextLine();
				}
				
				// If user input != "No", parse the input string and assemble the SQL query
				if (!"N".equals(condStr.toUpperCase()) && !"NO".equals(condStr.toUpperCase())) {
					sql += " where ";
					String[] conds = condStr.split(",");
					if (conds.length % 2 == 1) {	
						// number of input parameters must be an even number, i.e. (key, value) pairs
						hasInvalidWord = true;
						System.out.println("Number of input params incorrect! Operation aborted!");
						break;
					}
					for (int i = 0; i < conds.length-1; i+=2) {
						if (header.contains(conds[i].toUpperCase().trim())) {
							sql = sql + conds[i].trim() + "=" + conds[i+1].trim();
							if (i != conds.length-2) {
								sql += " and ";
							}
						} else {
							// column_name entered by user does not exist in the table
							hasInvalidWord = true;
							System.out.println("Input contains invalid column name \"" + conds[i].trim().toUpperCase() + "\"! Operation aborted!");
							break;
						}
					}
					if (hasInvalidWord) break;
				}
				
				// Execute SQL query
				System.out.println("executing: \n" + sql);
				try {
					rs = stmt.executeQuery(sql);
					listSQLResults(rs);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				break;
				
			case "3":
				custom(conn);
				break;
			case "":
				System.out.print("(List) >> ");
				break;
			case "quit":
				break;
			default:
				System.out.println("(List) >> Invalid input, please try again!");
			}
		}
		System.out.println("");
		
	}

	public void custom(Connection conn) {
		Scanner sc = new Scanner(System.in);
		System.out.print("(Custom) Enter your query: (\"quit\" to quit custom mode)\n(Custom) >> ");
		String sql = sc.nextLine();
		while (!"quit".equals(sql)) {
			if (!"".equals(sql)) {
				Statement stmt;
				try {
					stmt = conn.createStatement();
					if (stmt.execute(sql)) {
						listSQLResults(stmt.getResultSet());	// print result to screen
					} else {
						System.out.println("Done. " + stmt.getUpdateCount() + " records updated.");
					}
					
				} catch (SQLException e) {
					System.out.println(e);
				}
			}
			System.out.print("(Custom) >> ");
			sql = sc.nextLine();
		}
		System.out.println("");
	}
	
	// Print the result
	private static void listSQLResults(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCols = rsmd.getColumnCount();
		for (int i = 0; i < numCols; i++) {
			System.out.printf("%-25s", rsmd.getColumnName(i+1));
		}
		
		System.out.println("");
		String printLine = "";
		while (rs.next()) {
			for (int i = 0; i < numCols; i++) {
				printLine = rs.getString(i+1);
				if (printLine.length() > 20) {
					printLine = printLine.substring(0, 20);
					printLine += "...";
				}
				System.out.printf("%-25s", printLine);
			}
			System.out.println("\n");
		}
	}
	
	// Check if the table exists,
	// if yes, return the number of columns;
	// if no, return -1.
	private static int ifTableExists(String tableName, Connection conn) throws SQLException {
		
		Statement stmt = conn.createStatement();
		ResultSet rs;
		ResultSetMetaData rsmd;
		int numCols = 0;
		ArrayList<String> header = new ArrayList<String>();
		try {
			String sql = "select * from " + tableName;
			rs = stmt.executeQuery(sql);
			rsmd = rs.getMetaData();
			numCols = rsmd.getColumnCount();
			for (int i = 0; i < numCols; i++) {
				header.add(rsmd.getColumnName(i+1));
			}
		} catch (Exception e) {
			numCols = -1;
		}
		
		return numCols;
	}
	
}
