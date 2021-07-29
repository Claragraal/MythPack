package me.claragraal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author LBuke (Teddeh)
 */
public final class ResPacker {

    public static void main(String[] args) throws InterruptedException {

        // Get base directory.
        File baseDir = new File(Paths.get("").toAbsolutePath().toString());
        if (!baseDir.exists()) {
            System.out.println("Cannot find base directory");
            System.exit(-1);
            return;
        }

        List<File> resourcePacks = new ArrayList<>();
        File[] baseDirChildren = baseDir.listFiles();
        if (baseDirChildren == null) {
            System.out.println("Something went wrong finding resource packs.");
            System.exit(-1);
            return;
        }

        for (File file : baseDirChildren) {
            if (isResourcePackFolder(file)) {
                resourcePacks.add(file);
            }
        }

        if (resourcePacks.isEmpty()) {
            System.out.println("Could not find any valid resource packs.");
            System.exit(-1);
            return;
        }

        File textFile = new File("output/config.txt");
        try (FileWriter fileWriter = new FileWriter(textFile)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (File pack : resourcePacks) {
                    createZip(pack);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static String[] createZip(File pack) {
        String[] array = new String[4];

        File output = new File(pack.getParent(), String.format("output%s%s.zip", File.separator, pack.getName()));
        output.getParentFile().mkdirs();
        if (output.exists()) output.delete();

        try (FileOutputStream fileOutputStream = new FileOutputStream(output)) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                List<File> childrenFiles = new ArrayList<>();
                getChildrenFiles(pack, childrenFiles);

                // Loop through all inner directories
                for (File file : childrenFiles) {
                    String filePath = file.getCanonicalPath();
                    int packPathLength = pack.getCanonicalPath().length();
                    int filePathLength = filePath.length();

                    String zipFilePath = filePath.substring(packPathLength + 1, filePathLength);
                    ZipEntry zipEntry = new ZipEntry(zipFilePath);
                    zipOutputStream.putNextEntry(zipEntry);

                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(bytes)) >= 0) {
                            zipOutputStream.write(bytes, 0, length);
                        }
                        zipOutputStream.closeEntry();
                        fileInputStream.close();
                    }
                }

                zipOutputStream.finish();
                zipOutputStream.close();
                fileOutputStream.close();

                System.out.printf("Finished zipping: %s%n", output.getName());
                System.out.printf("Hash: %s%n", createSha1(output));
                System.out.println();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return array;
    }

    private static String createSha1(File file) {
        byte[] bytes = new byte[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream inputStream = new FileInputStream(file)) {
                int n = 0;
                byte[] buffer = new byte[8192];
                while (n != -1) {
                    n = inputStream.read(buffer);
                    if (n > 0) {
                        digest.update(buffer, 0, n);
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            bytes = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return byteArrayToHexString(bytes);
    }

    private static String byteArrayToHexString(byte[] array) {
        StringBuilder result = new StringBuilder();
        for (byte b : array) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private static void getChildrenFiles(File file, List<File> list) {
        File[] children = file.listFiles();
        if (children == null) return;

        for (File child : children) {
            if (child.isDirectory()) {
                getChildrenFiles(child, list);
                continue;
            }

            list.add(child);
        }
    }

    private static boolean isResourcePackFolder(File file) {
        if (!file.isDirectory()) return false;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && child.getName().equals("assets")) {
                    return true;
                }
            }
        }
        return false;
    }
}
