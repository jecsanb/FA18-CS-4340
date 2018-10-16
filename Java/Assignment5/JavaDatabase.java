import java.sql.*;

public class JavaDatabase {
    /**
     * Jecsan Blanco
     * 2018FA DATABASE SYSTEMS (CS-4340-01)
     * Assignment #5
     * 10/09/2018
     * Requires mysql.jbcd.driver which should be configured as a global library
     */
    private static Statement statement;
    private static final String EXP_MSG = "Somewhere there’s a village missing its idiot.";

    public static void main(String[] args) {
        new JavaDatabase();
        String result[][];
        System.out.println("1.Display a list of all instructors, showing their ID, name,\n and the number of sections that they have taught");

        result = submitQuery(
                "SELECT i.id,i.name,count(t.course_id)  " +
                        "FROM instructor i, teaches t " +
                        "WHERE t.id=i.id " +
                        "GROUP BY t.id");
        String labels[] = new String[]{"", "", "#ofCoursesTaught"};
        printTable(result, labels);

        System.out.println("2.Display the names of instructors who have not taught any section.");
        labels = new String[]{"Teach No Sections:"};
        result = submitQuery(
                "SELECT i.name FROM instructor i " +
                        "WHERE NOT EXISTS " +
                        "(SELECT 1 FROM teaches t WHERE t.id = i.id)");
        printTable(result, labels);


        System.out.println("3.Display the list of all course sections offered in Spring 2010,\n"+
        " along with the name of the instructors teaching the section.\n"+
        " If a course has more than one section, it should appear as many times\n" +
        "in the result as it has instructors.");
        result = submitQuery(
                "SELECT c.course_id, i.name " +
                        "FROM course c,instructor i,teaches t " +
                        "WHERE t.id=i.id and t.course_id = c.course_id " +
                        "AND t.semester=\"Spring\" AND t.year=\"2010\"");
        printTable(result);


        System.out.println("4.Display the list of all departments with the total number of instructors in each department");
        result = submitQuery("select d.dept_name, count(i.id) " +
                "FROM department d, instructor  i " +
                "WHERE i.dept_name=d.dept_name " +
                "GROUP BY i.dept_name ");
        labels = new String[]{"","Instructor Per Dept."};
        printTable(result,labels);

        System.out.println("5.Display the name, department and salary of the highest paid instructor.");
        result = submitQuery(
                "SELECT i.name, i.dept_name, max(i.salary)" +
                        "FROM instructor i " +
                        "WHERE i.salary >= (SELECT Max(i.salary) FROM instructor i) " +
                        "GROUP BY  i.id "
        );
        printTable(result);

    }

    /**
     * Prints a table
     *
     * @param table the table to display
     */
    private static void printTable(String[][] table) {
        printTable(table, null);
    }

    /**
     * Prints a table  using the labels as the first row
     *
     * @param table  the table to display
     * @param labels the labels to use for each column 0-n
     *               labels length must equal the number of columns otherwise
     *               the default labels provided within the table are used.
     *               If some labels are omitted only the non empty will be used
     *               and the default labels used for the rest.
     */
    private static void printTable( String[][] table, String[] labels) {
        if (table != null) {
            int t = 0;
            // add custom labels?
            if (labels != null && labels.length == table[0].length) {
                //put the label provided if any or put default table label
                for (String label : labels) {
                    assert label != null;
                    System.out.printf("%-15s", (label.equals("")) ? table[0][t++] : label);
                }
                System.out.println();
                t = 1;
            }

            //default table labels added or skipped
            for (int i = t; i < table.length; ++i) {
                for (int j = 0; j < table[0].length; j++) {
                    System.out.printf("%-15s", table[i][j]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    private static String[][] submitQuery(String sql) {
        String[][] table = null;
        try {
            ResultSet result = statement.executeQuery(sql);
            ResultSetMetaData md = result.getMetaData();
            result.last();
            //correction by 1 for the extra column name;
            int rows = result.getRow() + 1;
            result.first();

            //get more info to create table
            int columns = md.getColumnCount();
            table = new String[rows][columns];

            //gets the names of the columns on the first row
            for (int column = 0; column < columns; ++column) {
                table[0][column] = md.getColumnLabel(column + 1);
            }
            //populate the table with the data
            // skipping the first row as it was filled by above.
            for (int row = 1; row < rows; ++row) {
                for (int column = 0; column < columns; ++column) {
                    table[row][column] = result.getString(column + 1);
                }
                //next row
                result.next();
            }
        } catch (SQLException E) {
            System.err.println(EXP_MSG);
            E.printStackTrace();
        }
        return table;
    }

    private JavaDatabase() {
        // This is a driver class used to establish a connection a submit query for this lab.
        try {
            // load the JDBC driver
            // this command will register the driver with the driver manager and make it available to the program
            // setup the connection to the db , this class better be in our library!
            Class.forName("com.mysql.jdbc.Driver");

            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/University_username?autoReconnect=true&useSSL=false", "username", "password");
            statement = connection.createStatement();  // Creates a Statement object for sending SQL statements to the database.
        } catch (SQLException | ClassNotFoundException E) {
            System.err.println(EXP_MSG);
            E.printStackTrace();
        }
    }


}
