package com.demo.decompile.cfr;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: huangzh
 * @Date: 2024/3/5 19:43
 **/
public class CFRDemo {
    public static void main(String[] args) throws IOException {

        String sourceJar = "D:/ownerdata/feishu/zenzefi.jar";
        Path sourceJarPath = Paths.get(sourceJar);
        String sourceJarFileName = sourceJarPath.getFileName().toString().replaceFirst("[.][^.]+$", "");

        File file = ResourceUtils.getFile("classpath:");
        String relativePath = file.getPath();
        String path = relativePath.substring(0, relativePath.lastIndexOf(File.separatorChar));
        String outputPath = path + File.separator + "cfr" + File.separator + sourceJarFileName;

        Long time = cfr(sourceJar, outputPath);
        System.out.println(String.format("decompiler time: %dms, outputPath: %s", time, outputPath));
    }

    public static Long cfr(String source, String targetPath) throws IOException {
        Long start = System.currentTimeMillis();
        // source jar
        List<String> files = new ArrayList<>();
        files.add(source);

        // target dir
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("outputdir", targetPath);

        OptionsImpl options = new OptionsImpl(outputMap);
        CfrDriver cfrDriver = new CfrDriver.Builder().withBuiltOptions(options).build();
        cfrDriver.analyse(files);
        Long end = System.currentTimeMillis();
        return (end - start);
    }
}
