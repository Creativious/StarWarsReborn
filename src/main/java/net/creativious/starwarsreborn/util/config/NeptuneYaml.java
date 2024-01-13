package net.creativious.starwarsreborn.util.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeptuneYaml {

    private HashMap<String, String> yaml_values = new HashMap<>();
    private File file;

    public NeptuneYaml(File file) {
        this.file = file;
    }

    public void set(String key, String value) {
        yaml_values.put(key, value);
    }

    public void save() {
        StringBuilder yaml_builder = new StringBuilder();
        StringBuilder category_builder = new StringBuilder();
        StringBuilder non_category_builder = new StringBuilder();
        List<String> completed_entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : yaml_values.entrySet()) {
            if (!completed_entries.contains(entry.getKey())) {
                if (!entry.getKey().contains(".")) {
                    non_category_builder.append(getYamlLine(entry.getKey(), entry.getValue()));
                    completed_entries.add(entry.getKey());
                    continue;
                }
                String category = entry.getKey().substring(0, entry.getKey().indexOf("."));
                category_builder.append(getYamlLinesForCategory(category, yaml_values));
                completed_entries.addAll(getEntriesForCategory(category, yaml_values));
            }
        }
        yaml_builder.append(non_category_builder);
        yaml_builder.append(category_builder);
        String yaml_string = yaml_builder.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(yaml_string);
        } catch (IOException e) {
            throw new RuntimeException(e);
            // handle the exception
        }
    }

    public Map<String, String> load() {
        Map<String, String> yaml_values = new HashMap<>();
        String yaml_string;
        try {
            yaml_string = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> lines = Arrays.asList(yaml_string.split("\n"));
        String last_category = "";
        String last_category_path = "";
        for (String line : lines) {
            if (Objects.equals(line, "\n")) continue;
            String simple_key_regex = "^[\\w\\s]*";
            String simple_value_regex = "'[\\w\\s.]*'";
            Pattern pattern = Pattern.compile(simple_key_regex);
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) continue;
            String simple_key = matcher.group();
            pattern = Pattern.compile(simple_value_regex);
            matcher = pattern.matcher(line);
            if (!matcher.find()) {
                // then this is a category
                if (last_category.isEmpty()) {
                    last_category = simple_key;
                    last_category_path = simple_key.substring(simple_key.lastIndexOf("\t") + 1);
                    continue;
                }
                int last_category_tab_count = last_category.split("\t").length;
                int simple_key_tab_count = simple_key.split("\t").length;
                if (simple_key_tab_count > last_category_tab_count) {
                    last_category = simple_key;
                    last_category_path = last_category_path + "." + simple_key.substring(simple_key.lastIndexOf("\t") + 1);;
                }
                else if (simple_key_tab_count == last_category_tab_count) {
                    last_category = simple_key;
                    if (!last_category_path.contains(".")) {
                        last_category = simple_key;
                        last_category_path = simple_key.substring(simple_key.lastIndexOf("\t") + 1);
                        continue;
                    }
                    last_category_path = last_category_path.substring(0, last_category_path.indexOf(".")); // steps back a path
                    last_category_path = last_category_path + "." + simple_key.substring(simple_key.lastIndexOf("\t") + 1);;
                }
                else {
                    last_category = simple_key;
                    int difference_in_tab_count = last_category_tab_count - simple_key_tab_count;
                    for (int i = 0; i < difference_in_tab_count; i++) {
                        last_category_path = last_category_path.substring(0, last_category_path.indexOf("."));
                    }
                }

            } else {
                String simple_value = matcher.group(0);
                if (!simple_key.contains("\t")) {
                    yaml_values.put(simple_key, simple_value.substring(1, simple_value.length() - 1));
                    continue;
                }
                int last_category_tab_count = last_category.split("\t").length;
                int simple_key_tab_count = simple_key.split("\t").length;
                if (simple_key_tab_count > last_category_tab_count) {
                    String key_without_tabs = simple_key.substring(simple_key.lastIndexOf("\t") + 1);
                    String key = last_category_path + "." + key_without_tabs;
                    yaml_values.put(key, simple_value.substring(1, simple_value.length() - 1));
                }
                else if (simple_key_tab_count == last_category_tab_count) {
                    String key_without_tabs = simple_key.substring(simple_key.lastIndexOf("\t") + 1);
                    last_category_path = last_category_path.substring(0, last_category_path.indexOf(".")); // steps back a path
                    String key = last_category_path + "." + key_without_tabs;
                    yaml_values.put(key, simple_value.substring(1, simple_value.length() - 1));
                }
                else {
                    String key_without_tabs = simple_key.substring(simple_key.lastIndexOf("\t") + 1);
                    int difference_in_tab_count = last_category_tab_count - simple_key_tab_count;
                    for (int i = 0; i < difference_in_tab_count + 1; i++) {
                        last_category_path = last_category_path.substring(0, last_category_path.indexOf("."));
                    }
                    String key = last_category_path + "." + key_without_tabs;
                    yaml_values.put(key, simple_value.substring(1, simple_value.length() - 1));
                }

            }
        }
        return yaml_values;
    }

    private StringBuilder getYamlLinesForCategory(String key, Map<String, String> yaml_values) {
        StringBuilder yaml_builder = new StringBuilder();
        yaml_builder.append(getYamlCategoryLine(key));
        Map<String, String> valid_entries = new HashMap<>();
        List<String> completed_entries = new ArrayList<>();
        List<String> valid_entry_keys = getEntriesForCategory(key, yaml_values);
        yaml_values.forEach((t_key, t_value) -> {
            if (valid_entry_keys.contains(t_key)) {
                valid_entries.put(t_key, t_value);
            }
        });

        for (Map.Entry<String, String> entry : valid_entries.entrySet()) {
            if (completed_entries.contains(entry.getKey())) continue;
            int parent_key_count = key.split("\\.").length;
            int key_count = entry.getKey().split("\\.").length - 1;
            if (parent_key_count == key_count) {
                yaml_builder.append(getYamlLine(entry.getKey(), entry.getValue()));
                completed_entries.add(entry.getKey());
            }
            else {
                String regex = key + "\\.[\\w\\s]*";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(entry.getKey());
                String category = "";
                if (!matcher.find()) {
                    category = "error";
                } else {
                    category = matcher.group(0);
                }
                yaml_builder.append(getYamlLinesForCategory(category, valid_entries));
                completed_entries.addAll(getEntriesForCategory(category, valid_entries));
            }

        }
        return yaml_builder;
    }

    private List<String> getEntriesForCategory(String category, Map<String, String> yaml_values) {
        return yaml_values.keySet().stream().filter(entry -> entry.startsWith(category + ".")).toList();
    }

    private String getYamlLine(String key, String value) {
        int group_counts = key.split("\\.").length;
        if (group_counts >= 1) group_counts -= 1;
        if (!value.contains("'")) value = getYamlReadyString(value);
        String key_string = key.substring(key.lastIndexOf(".") + 1);
        return "\t".repeat(group_counts) + key_string + ": " + value + "\n";
    }

    private String getYamlCategoryLine(String key) {
        int group_counts = key.split("\\.").length;
        if (group_counts >= 1) group_counts -= 1;
        String category = (key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : key);
        return "\t".repeat(group_counts) + category + ":" + "\n";
    }

    public static String getYamlReadyString(String value, String comment) {
        return (comment.isEmpty() ? "'" + value + "'" : "'" + value + "' # " + comment);
    }

    public static String getYamlReadyString(String value) {
        return getYamlReadyString(value, "");
    }
}
