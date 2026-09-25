import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public final class BuildMappings {
    private static byte[] read(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        boolean toSrg = args[0].equals("srg");
        Map<String, String> names = new HashMap<>();
        try (ZipFile zip = new ZipFile(args[2])) {
            for (String file : new String[]{"fields.csv", "methods.csv"}) {
                String csv = new String(read(zip.getInputStream(zip.getEntry(file))), StandardCharsets.UTF_8);
                for (String line : csv.split("\n")) {
                    String[] columns = line.split(",", 3);
                    if (columns.length >= 2) names.put(columns[0], columns[1]);
                }
            }
        }
        Map<String, String> classes = new HashMap<>();
        Map<String, String> members = new HashMap<>();
        try (ZipFile zip = new ZipFile(args[1])) {
            String srg = new String(read(zip.getInputStream(zip.getEntry("joined.srg"))), StandardCharsets.UTF_8);
            for (String line : srg.split("\r?\n")) {
                String[] p = line.split(" ");
                if (p[0].equals("CL:") && !toSrg) classes.put(p[1], p[2]);
                if (p[0].equals("MD:") || p[0].equals("FD:")) {
                    boolean method = p[0].equals("MD:");
                    String target = p[method ? 3 : 2];
                    int slash = target.lastIndexOf('/');
                    String srgName = target.substring(slash + 1);
                    String mcpName = names.getOrDefault(srgName, srgName);
                    if (toSrg) members.put(target.substring(0, slash + 1) + mcpName + (method ? p[4] : ""), srgName);
                    else members.put(p[1] + (method ? p[2] : ""), mcpName);
                }
            }
        }
        Map<String, List<String>> parents = new HashMap<>();
        for (int i = 3; i < args.length; i++) {
            if (i == 4 || !Files.exists(Paths.get(args[i]))) continue;
            try (ZipFile zip = new ZipFile(args[i])) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) continue;
                    ClassReader reader = new ClassReader(read(zip.getInputStream(entry)));
                    List<String> inherited = new ArrayList<>();
                    if (reader.getSuperName() != null) inherited.add(reader.getSuperName());
                    inherited.addAll(Arrays.asList(reader.getInterfaces()));
                    parents.put(reader.getClassName(), inherited);
                }
            }
        }
        Remapper remapper = new Remapper() {
            private String member(String owner, String name, String descriptor, Set<String> visited) {
                if (!visited.add(owner)) return null;
                String match = members.get(owner + "/" + name + descriptor);
                if (match != null) return match;
                for (String parent : parents.getOrDefault(owner, Collections.emptyList())) {
                    match = member(parent, name, descriptor, visited);
                    if (match != null) return match;
                }
                return null;
            }

            @Override public String map(String name) { return classes.getOrDefault(name, name); }
            @Override public String mapMethodName(String owner, String name, String descriptor) {
                String mapped = member(owner, name, descriptor, new HashSet<>());
                return mapped != null ? mapped : !toSrg ? names.getOrDefault(name, name) : name;
            }
            @Override public String mapFieldName(String owner, String name, String descriptor) {
                String mapped = member(owner, name, "", new HashSet<>());
                return mapped != null ? mapped : !toSrg ? names.getOrDefault(name, name) : name;
            }
        };
        try (ZipFile input = new ZipFile(args[3]); ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(Paths.get(args[4])))) {
            Enumeration<? extends ZipEntry> entries = input.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                ClassReader reader = new ClassReader(read(input.getInputStream(entry)));
                ClassWriter writer = new ClassWriter(0);
                reader.accept(new ClassRemapper(writer, remapper), 0);
                output.putNextEntry(new ZipEntry(remapper.map(reader.getClassName()) + ".class"));
                output.write(writer.toByteArray());
                output.closeEntry();
            }
        }
    }
}
