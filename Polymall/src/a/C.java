package a;

public class C {
    static String d = "==> DOWNLOAD";
    public static void p(String m) {
        System.out.println(m);
    }

    public static void f(int r) {
        if (r == 0) {
            String green = "\u001B[32m";
            String reset = "\u001B[0m";
            p(green + d + " SUCCESSFUL!" + reset);
        }
        else {
            System.err.println(d + " FAILED!");
        }
    }
    public static void l(String m) {
        String purple = "\u001B[35m";
        String reset = "\u001B[0m";
        p(purple + d + "ING FROM:" + reset + m);
    }
}