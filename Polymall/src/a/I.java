package a;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import static a.C.*;
import static a.R.*;

public class I {

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
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim().replace("\uFEFF", "");
                String value = parts[1].trim();
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
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

            if (input.startsWith("polymall install ")) {
                String name = input.substring("polymall install ".length()).trim();
                String url = "raw.githubusercontent.com/cfmsm/Polymall/main/downloads/recipes/"
                        + name + "/temp.cfg";
                install(url);
            } else if (input.equalsIgnoreCase("exit")) {
                break;
            } else {
                p("Unknown command.");
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

}