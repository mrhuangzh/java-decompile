package com.demo.decompile.jdcore;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Author: huangzh
 * @Date: 2024/3/5 10:36
 **/
public class JDCoreDemo {

    public static void main(String[] args) throws Exception {

        File file = ResourceUtils.getFile("classpath:");
        String relativePath = file.getPath();
        String path = JDCoreDecompiler.substringBeforeLast(relativePath, File.separator);
        System.out.println(path);

        JDCoreDecompiler jdCoreDecompiler = new JDCoreDecompiler();
        Long time = jdCoreDecompiler.decompiler("D:\\ownerdata\\feishu\\zenzefi.jar", path + File.separator + "JDCore");
        System.out.println(String.format("decompiler time: %dms", time));
    }
}

class JDCoreDecompiler{

    private ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
    // 存放字节码
    private HashMap<String,byte[]> classByteMap = new HashMap<>();

    /**
     * 注意：没有考虑一个 Java 类编译出多个 Class 文件的情况。
     *
     * @param source
     * @param target
     * @return
     * @throws Exception
     */
    public Long decompiler(String source,String target) throws Exception {
        long start = System.currentTimeMillis();
        String sourceFileName = substringAfterLast(source, File.separator);
        String sourcePathName = substringBeforeLast(sourceFileName, ".");
        target = target + File.separator + sourcePathName;

        // 解压
        archive(source);
        for (String className : classByteMap.keySet()) {
            String path = substringBeforeLast(className, "/");
            String name = substringAfterLast(className, "/");
            if (contains(name, "$")) {
                name = substringAfterLast(name, "$");
            }
            name = StringUtils.replace(name, ".class", ".java");
            decompiler.decompile(loader, printer, className);
            String context = printer.toString();
            Path targetPath = Paths.get(target + "/" + path + "/" + name);
            if (!Files.exists(Paths.get(target + "/" + path))) {
                Files.createDirectories(Paths.get(target + "/" + path));
            }
            Files.deleteIfExists(targetPath);
            Files.createFile(targetPath);
            Files.write(targetPath, context.getBytes());
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * 解压
     * @param path
     * @throws IOException
     */
    private void archive(String path) throws IOException {
        try (ZipFile archive = new JarFile(new File(path))) {
            Enumeration<? extends ZipEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        byte[] bytes = null;
                        try (InputStream stream = archive.getInputStream(entry)) {
                            bytes = toByteArray(stream);
                        }
                        classByteMap.put(name, bytes);
                    }
                }
            }
        }
    }

    private Loader loader = new Loader() {
        @Override
        public byte[] load(String internalName) {
            return classByteMap.get(internalName);
        }
        @Override
        public boolean canLoad(String internalName) {
            return classByteMap.containsKey(internalName);
        }
    };

    private Printer printer = new Printer() {
        protected static final String TAB = "  ";
        protected static final String NEWLINE = "\n";
        protected int indentationCount = 0;
        protected StringBuilder sb = new StringBuilder();
        @Override public String toString() {
            String toString = sb.toString();
            sb = new StringBuilder();
            return toString;
        }
        @Override public void start(int maxLineNumber, int majorVersion, int minorVersion) {}
        @Override public void end() {}
        @Override public void printText(String text) { sb.append(text); }
        @Override public void printNumericConstant(String constant) { sb.append(constant); }
        @Override public void printStringConstant(String constant, String ownerInternalName) { sb.append(constant); }
        @Override public void printKeyword(String keyword) { sb.append(keyword); }
        @Override public void printDeclaration(int type, String internalTypeName, String name, String descriptor) { sb.append(name); }
        @Override public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) { sb.append(name); }
        @Override public void indent() { this.indentationCount++; }
        @Override public void unindent() { this.indentationCount--; }
        @Override public void startLine(int lineNumber) { for (int i=0; i<indentationCount; i++) sb.append(TAB); }
        @Override public void endLine() { sb.append(NEWLINE); }
        @Override public void extraLine(int count) { while (count-- > 0) sb.append(NEWLINE); }
        @Override public void startMarker(int type) {}
        @Override public void endMarker(int type) {}
    };


    /**
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * The empty String {@code ""}.
     */
    public static final String EMPTY = "";

    public static String substringBeforeLast(final String str, final String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Checks if a CharSequence is empty ("") or null.
     * @param cs
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }


    /**
     * Checks if CharSequence contains a search CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String)} if possible.
     * @param seq
     * @param searchSeq
     * @return
     */
    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return false;
        }
        return indexOf(seq, searchSeq, 0) >= 0;
    }

    /**
     * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.
     * @param cs
     * @param searchChar
     * @param start
     * @return
     */
    static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar.toString(), start);
        }
        if (cs instanceof StringBuilder) {
            return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
        }
        if (cs instanceof StringBuffer) {
            return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
        }
        return cs.toString().indexOf(searchChar.toString(), start);
    }


    /**
     * Gets the substring after the last occurrence of a separator.
     * The separator is not returned.
     * @param str
     * @param separator
     * @return
     */
    public static String substringAfterLast(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }


    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}
