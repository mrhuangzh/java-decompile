package com.demo.decompile.procyon;

import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author: huangzh
 * @Date: 2024/3/7 11:46
 **/
public class ProcyonJarDemo {
    public static void main(String[] args) throws FileNotFoundException {
        String sourceJar = "D:/ownerdata/feishu/zenzefi.jar";
        Path sourceJarPath = Paths.get(sourceJar);
        String sourceJarFileName = sourceJarPath.getFileName().toString().replaceFirst("[.][^.]+$", "");

        File file = ResourceUtils.getFile("classpath:");
        String relativePath = file.getPath();
        String path = relativePath.substring(0, relativePath.lastIndexOf(File.separatorChar));
        String outputPath = path + File.separator + "procyon" + File.separator + sourceJarFileName;

        String[] command = {
                "java",
                "-jar",
                "src/main/resources/procyon/procyon-decompiler-0.6.0.jar",
                "-jar",
                sourceJar,
                "-o",
                outputPath
        };

        try {
            Process process = new ProcessBuilder(command).start();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("info: " + line);
            }

            InputStream inputStreamErr = process.getErrorStream();
            BufferedReader readerErr = new BufferedReader(new InputStreamReader(inputStreamErr));
            String lineErr;
            while ((lineErr = readerErr.readLine()) != null) {
                System.out.println("error: " + lineErr);
            }

            // waiting for command execution to complete
            int exitCode = process.waitFor();
            System.out.println("decompile completed,exit code: " + exitCode);
            System.out.println("output dir: " + outputPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
