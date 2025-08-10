import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;



public class R {
    static String d = "==> DOWNLOAD";
    public static void main(String[] args) {
        p("");
        p("");
        p("");
        p("Polymall successfully booted");
        p("Note: If it shows where the file is saved and its an archive, you should extract it");
        p("Type 'polymall help' if you don't know what to do. Its simple :)");
        Path flagPath = Path.of(System.getProperty("user.home"), "polymall-terminal-flag.txt");

        if (!Files.exists(flagPath)) {
            try {
                Files.createFile(flagPath);
                exec();
            } catch (IOException e) {
            }
            return;
        } else {
            try {
                Files.deleteIfExists(flagPath);
            } catch (IOException e) {
            }
        }

        command();
    }
    public static void install(String cfgUrl) {
        try {
            Path tempDir = Paths.get("temp");
            Files.createDirectories(tempDir);

            String fileName = cfgUrl.substring(cfgUrl.lastIndexOf('/') + 1);
            Path cfgPath = tempDir.resolve(fileName);

            l(cfgUrl);
            downloadFile(cfgUrl, cfgPath.toString());

            Map<String, List<String>> config = parseCfg(cfgPath);

            for (Map.Entry<String, List<String>> entry : config.entrySet()) {
                List<String> fixed = new ArrayList<>();
                for (String val : entry.getValue()) {
                    if (val.contains(".") && !val.startsWith("http://") && !val.startsWith("https://")) {
                        fixed.add("https://" + val);
                    } else {
                        fixed.add(val);
                    }
                }
                entry.setValue(fixed);
            }

            String dirKey = config.getOrDefault("dir", List.of("downloads")).get(0);
            Path saveDir = resolveUserDirectory(dirKey);
            Files.createDirectories(saveDir);

            String osKey = getPlatformKey();
            List<String> platformLinks = config.get(osKey);

            if (platformLinks == null || platformLinks.isEmpty()) {
                platformLinks = config.get("all");
                if (platformLinks == null || platformLinks.isEmpty()) {
                    p("No '" + osKey + " =' or 'all =' entry found in " + cfgPath.getFileName());
                    f(2);
                    return;
                }
            }

            for (String url : platformLinks) {
                String file = url.substring(url.lastIndexOf('/') + 1);
                Path savePath = saveDir.resolve(file);
                l(url);
                downloadFile(url, savePath.toString());
            }
            Files.deleteIfExists(cfgPath);
            try {
                if (Files.isDirectory(tempDir) && Files.list(tempDir).findAny().isEmpty()) {
                    Files.delete(tempDir);
                }
            } catch (IOException e) {
                p("Warning: Could not delete temp folder.");
                e.printStackTrace();
            }

            if (config.containsKey("open")) {
                openDirectory(saveDir);
            }

            f(0);

        } catch (Exception e) {
            p("Error during install: " + e.getMessage());
            f(2);
        }
    }



    public static void downloadFile(String urlString, String outputPath) throws IOException {
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "https://" + urlString;
        }
        try (InputStream in = new URL(urlString).openStream()) {
            Files.copy(in, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private static Map<String, List<String>> parseCfg(Path path) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.replace("\r", "").trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim().replace("\uFEFF", "");
                    String value = parts[1].trim();
                    map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                }
            } else {
                String key = line.trim().replace("\uFEFF", "");
                map.putIfAbsent(key, new ArrayList<>());
            }
        }

        return map;
    }



    private static Path resolveUserDirectory(String dir) {
        String home = System.getProperty("user.home");

        if (dir.startsWith("user.home")) {
            String subPath = dir.replace("user.home", "").replace("+", "").trim();
            return Paths.get(home + File.separator + subPath);
        }

        return switch (dir.toLowerCase()) {
            case "desktop" -> Paths.get(home, "Desktop");
            case "downloads" -> Paths.get(home, "Downloads");
            case "documents" -> Paths.get(home, "Documents");
            default -> Paths.get(dir);
        };
    }


    public static void command() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine().trim();
            String p = "polymall ";

            if (input.startsWith(p + "install ")) {
                String name = input.substring((p + "install ").length()).trim();
                String url = "raw.githubusercontent.com/cfmsm/Polymall/main/downloads/recipes/"
                        + name + "/temp.cfg";
                install(url);

            } else if (input.startsWith(p + "list ")) {
                install("raw.githubusercontent.com/cfmsm/Polymall/main/downloads/recipes/list/temp.cfg");
                String search = input.substring((p + "list ").length()).trim();
                list(search);

            } else if (input.equalsIgnoreCase(p + "help")) {
                p("Use 'polymall install' to install a package");
                p("Type the name of the package at the end of 'polymall install' command");
                p("Example: polymall install quellwrap");
                p("This will install QuellWrap");
                p("");
                p("Use 'polymall list' to search for packages");
                p("Type a keyword at the end, or just letters");
                p("Example: polymall list wr");
                p("It will tell all the packages that include 'wr' in their name, end, middle or start");
                p("");
                p("Use 'polymall issue' followed by a keyword to get solutions to common problems");
                p("");
                p("Use 'polymall exit' to exit the software");

            } else if (input.startsWith(p + "issue ")) {
                String issueKeyword = input.substring((p + "issue ").length()).trim();
                install("https://raw.githubusercontent.com/cfmsm/Polymall/main/downloads/recipes/issue/temp.cfg");
                issue(issueKeyword);

            } else if (input.equalsIgnoreCase(p + "exit")) {
                break;

            } else {
                p("Unknown command.");
                p("Did you mean the following?(if there is none means nothing found):");

                String inputCommand = input.startsWith(p) ? input.substring(p.length()) : input;
                String[] commands = {"install", "list", "issue", "exit"};
                for (String str : commands) {

                    if (str.contains(inputCommand)) {
                        p(p + str);
                    }
                    else if (str.startsWith(inputCommand)) {
                        p(p + str);
                    }
                    else if (str.endsWith(inputCommand)) {
                        p(p + str);
                    }
                }
                p("");
                p("Otherwise, type 'polymall help' for instructions.");
            }
        }
    }


    private static String getPlatformKey() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "win";
        if (os.contains("mac")) return "mac";
        return "nux";
    }
    private static void openDirectory(Path p) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("explorer", p.toAbsolutePath().toString()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", p.toAbsolutePath().toString()).start();
            } else {
                new ProcessBuilder("xdg-open", p.toAbsolutePath().toString()).start();
            }
        } catch (IOException e) {
            p("Failed to open folder: " + e.getMessage());
        }
    }
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
            System.err.println("Try making sure you spelled the name of the package correctly");
        }
    }
    public static void l(String m) {
        String purple = "\u001B[35m";
        String reset = "\u001B[0m";
        p(purple + d + "ING FROM:" + reset + m);
    }
    public static void list(String keyword) {
        String filePath = System.getProperty("user.home") + "/Downloads/git-download-polymall-github-recipes-list.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            p("Here are a list of packages that have '" + keyword + "' in their name:");
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(keyword)) {

                    p(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            p("Warning: Could not delete recipe list file.");
            e.printStackTrace();
        }
    }
    public static void issue(String issue) {
        String filePath = System.getProperty("user.home") + "/Downloads/git-download-polymall-github-issue-list.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            p("Here are some solutions and similar problems for '" + issue + "':");
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(issue)) {

                    p(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            p("Warning: Could not delete recipe list file.");
            e.printStackTrace();
        }
    }
    public static void exec() {
        try {
            p("");
            p("");
            p("");
            p("Polymall successfully booted");
            p("Note: If it shows where the file is saved and its an archive, you should extract it");
            p("Type 'polymall help' if you don't know what to do. Its simple :)");
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            ProcessBuilder builder;

            String dir = getRunningDirectory();
            String jarFileName = getJarFileName();

            if (dir == null || jarFileName == null) {
                throw new RuntimeException("Could not determine running directory or JAR file name.");
            }

            if (os.contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", "start", "\"Polymall\"", "cmd.exe", "/k", "cd /d \"" + dir + "\" && java -jar " + jarFileName
                );

            } else if (os.contains("mac")) {
                builder = new ProcessBuilder("osascript", "-e",
                        "tell application \"Terminal\" to do script \"cd '" + dir + "' && java -jar " + jarFileName + "\"",
                        "-e", "tell application \"Terminal\" to activate"
                );


            } else if (os.contains("nux") || os.contains("nix")) {
                builder = new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-c",
                        "cd \"" + dir + "\" && java -jar \"" + jarFileName + "\"; exec bash");

            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }

            builder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getRunningDirectory() {
        try {
            File jarFile = new File(R.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return jarFile.getParentFile().getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getJarFileName() {
        try {
            File jarFile = new File(R.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return jarFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Polymall.jar";
        }
    }
}