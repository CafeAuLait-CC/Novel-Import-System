package assignment;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;


public class NovelImportSystem {
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException {

		Connection conn = connect2DB();		// Create a connection to database 'mydb'
		
		// Read text file and create table 'BOOKS' and 'WRITERS'
		DataLoader data = new DataLoader();
		if (!data.loadBooks(conn) || !data.loadWriters(conn)) return;
		
		DBOperations ops = new DBOperations();
		
		Scanner sc = new Scanner(System.in);	// Input command from user 
		String str = "";
		System.out.println("");
		while(!"exit".equals(str)) {
			System.out.print("$ Please enter (a)dd/(d)elete/(u)pdate/(l)ist/(c)ustom/exit to continue...\n$ ");
			str = sc.nextLine();
			
			switch (str) {
			case "l":
			case "list":		// List mode: list the expected info from database
				ops.list(conn);
				break;
				
			case "u":
			case "update":		// Update mode: update a record in the database
				ops.update(conn);
				break;
				
			case "d":
			case "delete":		// Delete mode: delete a record from database
				ops.remove(conn);
				break;
				
			case "a":
			case "add":			// Add mode: insert a record to the database
				ops.add(conn);
				break;
				
			case "c":
			case "custom":		// Custom mode: access the database with custom SQL queries
				ops.custom(conn);
				break;
				
			case "":
			case "exit":		// Exit the program
				break;
				
			default:
				System.out.println("$ Invalid command, please try again!");
			}
		}
		sc.close();
		System.out.println("\n*** Program terminated. Bye bye! ****");
	}
	
	
	private static Connection connect2DB() throws ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection conn = DriverManager.getConnection("jdbc:derby:mydb;create=true");
		return conn;
	}

}
