package view;

import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

public class UiUtils {
    // ANSI Colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // Styles
    private static final CellStyle CENTER_ALIGN = new CellStyle(CellStyle.HorizontalAlign.center);
    private static final CellStyle RIGHT_ALIGN = new CellStyle(CellStyle.HorizontalAlign.right);

    public static void printHeader(String title) {
        Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE);
        table.addCell(ANSI_CYAN + " " + title + " " + ANSI_RESET, CENTER_ALIGN);
        System.out.println(table.render());
    }

    public static void printMenu(String title, String... options) {
        if (title != null) {
            Table titleTable = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER);
            titleTable.addCell(ANSI_CYAN + title + ANSI_RESET, CENTER_ALIGN);
            System.out.println(titleTable.render());
        }

        Table menuTable = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        for (String option : options) {
            menuTable.addCell(" " + option + " ");
        }
        System.out.println(menuTable.render());
    }

    public static void printMessage(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(" " + message + " ");
        System.out.println(table.render());
    }

    public static void printSuccess(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(ANSI_GREEN + " ✓ " + message + " " + ANSI_RESET);
        System.out.println(table.render());
    }

    public static void printError(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(ANSI_RED + " ✗ " + message + " " + ANSI_RESET);
        System.out.println(table.render());
    }

    public static Table createTable(int columns) {
        return new Table(columns, BorderStyle.UNICODE_BOX_HEAVY_BORDER);
    }

    public static Table createDataTable(int columns) {
        return new Table(columns, BorderStyle.UNICODE_BOX_DOUBLE_BORDER);
    }
}
