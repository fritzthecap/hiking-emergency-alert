package fri.servers.hiking.emergencyalert.ui.swing.form;

public class FormLayout
{
    public static int calculateColumns(
            int componentCount,
            int formPixelWidth,
            int maximumColumns,
            int aimedRowsPerColumn,
            int averageFieldPixelWidth)
    {
        final int possibleColumns = Math.round(formPixelWidth / averageFieldPixelWidth);
        final int maximumPossibleColumns = Math.min(maximumColumns, possibleColumns);
        
        final int columns = (componentCount + aimedRowsPerColumn - 1) / aimedRowsPerColumn;
        
        return (columns <= 0) ? 1 : (columns > maximumPossibleColumns) ? maximumPossibleColumns : columns;
    }
    
    private static int calculateRows(int columns, int componentCount) {
        return (componentCount + columns - 1) / columns;
    }

    public static void main(String [] args) {
        final int WINDOW_WIDTH = 2800; //1900;
        final int AVERAGE_FIELD_PIXEL_WIDTH = 480;
        final int MAXIMUM_COLUMNS = 4;
        final int AIMED_ROWS_PER_COLUMN = 7;
        
        int previousColumns = 1;
        java.io.PrintStream out = System.err;
        
        out.println("AIMED_ROWS_PER_COLUMN = "+AIMED_ROWS_PER_COLUMN);
        out.println("=================================================");
        
        final int MAX_FIELD_COUNT = 70;
        
        for (int fieldCount = 1; fieldCount <= MAX_FIELD_COUNT; fieldCount++)   {
            final int columns = calculateColumns(
                    fieldCount, 
                    WINDOW_WIDTH, 
                    MAXIMUM_COLUMNS, 
                    AIMED_ROWS_PER_COLUMN, 
                    AVERAGE_FIELD_PIXEL_WIDTH);
            final int rows = calculateRows(columns, fieldCount);

            // output layout-sample just before and after a column count change
            if (columns != previousColumns ||
                    columns != calculateColumns(
                            fieldCount + 1, 
                            WINDOW_WIDTH, 
                            MAXIMUM_COLUMNS, 
                            AIMED_ROWS_PER_COLUMN, 
                            AVERAGE_FIELD_PIXEL_WIDTH) ||
                    fieldCount == MAX_FIELD_COUNT)
            {
                previousColumns = columns;
                
                out.println("fields = "+fieldCount+", columns = "+columns+", rows = "+rows);
                out.println("-------------------------------------------------");
                
                int outputCount = 0;
                for (int row = 0; row < rows; row++)    {
                    for (int column = 0; column < columns; column++)    {
                        if (outputCount < fieldCount)
                            out.print("MMMMMMMMMMM ");
                        
                        outputCount++;
                    }
                    out.println();
                }
                out.println("-------------------------------------------------");
            }
        }
    }
}