package view;

import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.CellStyle.HorizontalAlign;

public class UiTest {
    public static void main(String[] var0) {
        System.out.println("Testing library features...");

        try {
            new CellStyle(HorizontalAlign.center);
            System.out.println("CellStyle found.");
        } catch (Exception | NoClassDefFoundError var2) {
            System.out.println("CellStyle NOT found: " + String.valueOf(var2));
        }

    }
}