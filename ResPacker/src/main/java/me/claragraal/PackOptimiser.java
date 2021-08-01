package me.claragraal;

import Catalano.Imaging.FastBitmap;
import org.apache.commons.io.FileUtils;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author LBuke (Teddeh)
 */
public final class PackOptimiser {

    public PackOptimiser(File baseDir, File originalResourcePack) {
        File tempPack = new File(baseDir, String.format("temp%s%s", File.separator, originalResourcePack.getName()));
        if (tempPack.exists()) tempPack.delete();
        tempPack.getParentFile().mkdirs();
        try {
            FileUtils.copyDirectory(originalResourcePack, tempPack);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        TreeMap<Long, File> allFilesMap = new TreeMap<>(Collections.reverseOrder());
        long totalSize = 0;
        List<File> allFiles = new ArrayList<>();
        getAllFiles(allFiles, originalResourcePack, new String[] {""});
        for (File file : allFiles) {
            long length = file.length();
            totalSize += length;
            allFilesMap.put(length, file);
        }
        System.out.printf("Original pack size: %skb%n", (totalSize / 1024));
        int i = 0;
        for (Map.Entry<Long, File> entry : allFilesMap.entrySet()) {
            if (i == 10) break;
            System.out.printf("  #%s  %s [%skb]%n", (i + 1), entry.getValue().getAbsolutePath(), (entry.getKey() / 1024));
            i++;
        }

        // optimise image files
        List<File> imageFiles = new ArrayList<>();
        long[] totalImagesLength = {0, 0};
        getAllFiles(imageFiles, tempPack, new String[] {"png"});
        for (File file : imageFiles) {
            totalImagesLength[0] += file.length();
//            optimiseImage(file);
        }
        System.out.printf("(%s) Image files: %s [%skb]%n", originalResourcePack.getName(), imageFiles.size(), (totalImagesLength[0] / 1024));

        // optimise json files
        List<File> jsonFiles = new ArrayList<>();
        long totalJsonLength = 0;
        getAllFiles(jsonFiles, tempPack, new String[] {"json", "png.mcmeta"}, "lang", "font");
        for (File file : jsonFiles) {
            totalJsonLength += file.length();
//            optimiseJson(file);
        }
        System.out.printf("(%s) Json files: %s [%skb]%n", originalResourcePack.getName(), jsonFiles.size(), (totalJsonLength / 1024));

        // optimise ogg sounds (somehow..)
        List<File> soundFiles = new ArrayList<>();
        long[] totalSoundLength = {0, 0};
        getAllFiles(soundFiles, tempPack, new String[] {"ogg"});
        for (File file : soundFiles) {
            totalSoundLength[0] += file.length();
//            optimiseOgg(file);
        }
        System.out.printf("(%s) Sound files: %s [%skb]%n", originalResourcePack.getName(), soundFiles.size(), (totalSoundLength[0] / 1024));

        System.out.println();
    }

    private void getAllFiles(List<File> list, File parent, String[] extensions, String... ignore) {
        File[] children = parent.listFiles();
        if (children == null) return;

        for (File child : children) {
            boolean shouldIgnore = false;
            for (String str : ignore) {
                if (child.getName().equals(str)) {
                    shouldIgnore = true;
                    break;
                }
            }
            if (shouldIgnore)
                continue;

            if (child.isDirectory()) {
                this.getAllFiles(list, child, extensions);
                continue;
            }

            boolean isValidExtension = false;
            for (String type : extensions) {
                if (child.getName().endsWith(type)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension)
                continue;

            list.add(child);
        }
    }

    private void optimiseImage(File file) {
        File opt = new File(file.getParentFile(), String.format("opt-%s", file.getName()));
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
            try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(opt)) {
                ImageReader imageReader = ImageIO.getImageReaders(imageInputStream).next();
                imageReader.setInput(imageInputStream);

                ImageWriter imageWriter = ImageIO.getImageWriter(imageReader);
                imageWriter.setOutput(imageOutputStream);
                ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
                imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParam.setCompressionQuality(0.05f);

                IIOImage iioImage = imageReader.readAll(0, null);

                BufferedImage bufferedImage = (BufferedImage) iioImage.getRenderedImage();
                FastBitmap fastBitmap = new FastBitmap(bufferedImage);

                Graphics2D graphics = bufferedImage.createGraphics();
                graphics.drawImage(fastBitmap.toBufferedImage(), 0, 0, null);
                graphics.dispose();

                imageWriter.write(null, iioImage, imageWriteParam);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
