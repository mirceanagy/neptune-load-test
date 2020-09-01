import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final int PLACE_HIERARCHIES = 50;
    private static final int PLACE_HIERARCHY_DEPTH = 100;
    private static final int CONTENT_PER_PLACE = 30;
    private static final int GROUPS = 100;
    private static final int USERS_PER_GROUP = 100;

    public static void main(String[] args) {
        List<List<String>> vertices = new ArrayList<>();
        List<List<String>> edges = new ArrayList<>();
        vertices.add(Vertex.toCsvLineHeader());
        edges.add(Edge.toCsvLineHeader());
        for (int hierarchy = 0; hierarchy < PLACE_HIERARCHIES; hierarchy++) {
            for (int hierarchyDepth = 0; hierarchyDepth < PLACE_HIERARCHY_DEPTH; hierarchyDepth++) {
                String placeId = "place_" + hierarchy + "_" + hierarchyDepth;
                String placeParentId = "place_" + hierarchy + "_" + (hierarchyDepth - 1);
                vertices.add(new Vertex(placeId, "place", placeId, placeId).toCsvLine());
                if (hierarchyDepth != 0) {
                    edges.add(new Edge("parent_" + placeId, placeId, placeParentId, "parent").toCsvLine());
                }
                for (int content = 0; content < CONTENT_PER_PLACE; content++) {
                    String contentId = "content_" + hierarchy + "_" + hierarchyDepth + "_" + content;
                    vertices.add(new Vertex(contentId, "content", contentId, contentId).toCsvLine());
                    edges.add(new Edge("content_place_" + contentId, contentId, placeId, "placed").toCsvLine());
                }
            }
        }
        for (int group = 0; group < GROUPS; group++) {
            String groupId = "group_" + group;
            vertices.add(new Vertex(groupId, "group", groupId, groupId).toCsvLine());
            edges.add(new Edge("group_place" + group, groupId, "place_" + Math.round(Math.random() * (PLACE_HIERARCHIES-1) )+ "_0", "read").toCsvLine());
            if (Math.random() < 0.5) {
                edges.add(new Edge("group_place" + groupId, groupId, "place_" + Math.round(Math.random() * (PLACE_HIERARCHIES-1) )+ "_0", "write").toCsvLine());
            }
            for (int user = 0; user < USERS_PER_GROUP; user++) {
                String userId = "user_" + group + "_" + user;
                vertices.add(new Vertex(userId, "user", userId, userId).toCsvLine());
                edges.add(new Edge("user_group_" + userId, userId, groupId, "memberOf").toCsvLine());
            }
        }
        exportCsv(vertices, "c:/dev/vertex.csv");
        exportCsv(edges, "c:/dev/edges.csv");

    }

    private static void exportCsv(List<List<String>> data, String fileName) {
        File vertexFile = new File(fileName);
        try (PrintWriter pw = new PrintWriter(vertexFile)) {
            data.stream()
                    .map(Main::convertToCSV)
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertToCSV(List<String> data) {
        return data.stream()
//                .map(Main::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static class Vertex {
        String id;
        String label;
        String name;
        String description;

        public Vertex(String id, String label, String name, String description) {
            this.id = id;
            this.label = label;
            this.name = name;
            this.description = description;
        }

        List<String> toCsvLine() {
            return Arrays.asList(id, "\"" + name + "\"", "\"" + description + "\"", label);
        }

        static List<String> toCsvLineHeader() {
            return Arrays.asList("~id", "name:String", "description:String", "~label");
        }
    }

    public static class Edge {
        String id;
        String from;
        String to;
        String label;

        public Edge(String id, String from, String to, String label) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.label = label;
        }

        List<String> toCsvLine() {
            return Arrays.asList(id, from, to, label);
        }

        static List<String> toCsvLineHeader() {
            return Arrays.asList("~id", "~from", "~to", "~label");
        }
    }
}
