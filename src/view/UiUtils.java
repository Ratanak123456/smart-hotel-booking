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

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printBanner() {
        System.out.println(ANSI_CYAN +
                "██╗  ██╗ ██████╗ ████████╗███████╗██╗     \n" +
                "██║  ██║██╔═══██╗╚══██╔══╝██╔════╝██║     \n" +
                "███████║██║   ██║   ██║   █████╗  ██║     \n" +
                "██╔══██║██║   ██║   ██║   ██╔══╝  ██║     \n" +
                "██║  ██║╚██████╔╝   ██║   ███████╗███████╗\n" +
                "╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝" + ANSI_RESET);
    }

    public static void printHeader(String title) {
        int width = Math.max(35, title.length() + 4);
        String line = "━".repeat(width);
        int padding = (width - title.length()) / 2;
        String leftPad = " ".repeat(padding);
        String rightPad = " ".repeat(width - title.length() - padding);

        System.out.println(ANSI_CYAN + "┏" + line + "┓" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "┃" + ANSI_RESET + leftPad + title + rightPad + ANSI_CYAN + "┃" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "┗" + line + "┛" + ANSI_RESET);
    }

    public static void printMenu(String title, String... options) {
        if (title != null) {
            System.out.println(ANSI_YELLOW + "───────── " + title + " ─────────" + ANSI_RESET);
        }
        for (String option : options) {
            System.out.println(ANSI_PURPLE + "❯ " + ANSI_RESET + option);
        }
        System.out.println(ANSI_YELLOW + "─────────────────────────────" + ANSI_RESET);
    }

    public static String renderTable(Table table) {
        return ANSI_CYAN + table.render() + ANSI_RESET;
    }

    public static void printMessage(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(" " + message + " ");
        System.out.println(renderTable(table));
    }

    public static void printSuccess(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(ANSI_GREEN + " ✓ " + message + " " + ANSI_RESET);
        System.out.println(renderTable(table));
    }

    public static void printError(String message) {
        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX);
        table.addCell(ANSI_RED + " ✗ " + message + " " + ANSI_RESET);
        System.out.println(renderTable(table));
    }

    public static Table createTable(int columns) {
        return new Table(columns, BorderStyle.UNICODE_BOX_HEAVY_BORDER);
    }

    public static Table createDataTable(int columns) {
        return new Table(columns, BorderStyle.UNICODE_BOX_DOUBLE_BORDER);
    }
}
