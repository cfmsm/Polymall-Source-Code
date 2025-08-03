import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Scanner;

public class L {

    public static String convertGitHubToRaw(String url) {
        if (url == null || !url.contains("github.com") || !url.contains("/blob/")) {
            return "❌ Invalid GitHub URL";
        }
        String raw = url.replace("https://github.com/", "raw.githubusercontent.com/")
                .replace("/blob/", "/");
        return raw.replaceFirst("https://", "");  // Remove https://
    }

    public static String convertDriveToDirectDownload(String url) {
        if (url == null || !url.contains("drive.google.com")) {
            return "❌ Invalid Google Drive URL";
        }

        String fileId = null;
        if (url.contains("/file/d/")) {
            int start = url.indexOf("/file/d/") + 8;
            int end = url.indexOf('/', start);
            if (end > start) {
                fileId = url.substring(start, end);
            }
        }

        if (fileId == null || fileId.isEmpty()) {
            return "❌ Invalid Drive link format";
        }

        return "drive.google.com/uc?export=download&id=" + fileId; // No https://
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("🔗 Paste your GitHub or Google Drive link below:");
        String inputUrl = scanner.nextLine().trim();

        if (inputUrl.contains("github.com")) {
            String result = convertGitHubToRaw(inputUrl);
            System.out.println("✅ Raw GitHub Link:");
            System.out.println(result);
            copyToClipboard(result);
            System.out.println("📋 Copied to clipboard!");

        } else if (inputUrl.contains("drive.google.com")) {
            String result = convertDriveToDirectDownload(inputUrl);
            System.out.println("✅ Direct Drive Download Link:");
            System.out.println(result);
            copyToClipboard(result);
            System.out.println("📋 Copied to clipboard!");

        } else {
            System.out.println("❌ Unsupported or invalid URL.");
        }

        scanner.close();
    }

    public static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        System.exit(0);
    }
}
