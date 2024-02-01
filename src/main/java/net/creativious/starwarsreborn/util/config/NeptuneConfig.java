package net.creativious.starwarsreborn.util.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeptuneConfig {

    // Ported from NeptuneLib(experimental staging) for FabricMC by Creativious

    @NotConfigField
    private String config_location;
    @NotConfigField
    private boolean init_ran = false;
    @NotConfigField
    private NeptuneYaml loaded_config_yaml;

    public NeptuneConfig() {
        // @TODO: Add a field analyzer
    }

    public void init(String plugin_id) {
        this.init_ran = true;
        String folder_loc = "plugins/" + plugin_id;
        new File(folder_loc).mkdir();
        File config_file = new File(folder_loc + "/config.yaml");
        if (config_file.exists()) {
            this.loadConfig(config_file);
        }
        else {
            this.saveConfig(config_file);
        }
    }

    public void saveConfig(File config_file) {
        try {
            HashMap<String, String> objects_to_save = processFieldsForSave(this, getFields());
            if (this.loaded_config_yaml == null) {
                this.loaded_config_yaml = new NeptuneYaml(config_file);
            }
            for (Map.Entry<String, String> entry : objects_to_save.entrySet()) {
                if (entry == null) {
                    continue;
                }
                this.loaded_config_yaml.set(entry.getKey(), entry.getValue());
            }
            this.loaded_config_yaml.save();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private HashMap<String, String> processFieldsForSave(Object parent_object, Field[] fields) throws IllegalAccessException {
        HashMap<String, String> save_values = new HashMap<>();
        for (Field field : fields) {
            String name = field.getAnnotation(ConfigCustomName.class) != null ? field.getAnnotation(ConfigCustomName.class).value() : getNameFromField(field);
            if (Arrays.stream(field.getType().getInterfaces()).toList().contains(NeptuneSubConfig.class)) {
                HashMap<String, String> sub_config_values = processFieldsForSave(field.get(parent_object), getFieldsFromClass(field.getType()));
                for (Map.Entry<String, String> sub_config_entry : sub_config_values.entrySet()) {
                    save_values.put(name + "." + sub_config_entry.getKey(), sub_config_entry.getValue());
                }
                continue;
            }
            if (!NeptuneSerializationUtil.isSupportedClass(field.getType())) continue;
            String comment = field.getAnnotation(ConfigComment.class) != null ? field.getAnnotation(ConfigComment.class).value() : "";
            String value = NeptuneYaml.getYamlReadyString(NeptuneSerializationUtil.getSerializedValue(field.get(parent_object)), comment);
            if (value.isEmpty()) continue;
            save_values.put(name, value);
        }
        return save_values;
    }
    public void loadConfig(File config_file) {
        if (this.loaded_config_yaml == null) {
            this.loaded_config_yaml = new NeptuneYaml(config_file);
        }
        Map<String, String> yaml_values = this.loaded_config_yaml.load();
        for (Map.Entry<String, String> entry : yaml_values.entrySet()) {
            try {
                findMatchingFieldAndSetValue(entry);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private void findMatchingFieldAndSetValue(Map.Entry<String, String> entry) throws IllegalAccessException {
        Field[] fields = getFields();
        for (Field other_field : fields) {
            String key = other_field.isAnnotationPresent(ConfigCustomName.class) ? other_field.getAnnotation(ConfigCustomName.class).value() : getNameFromField(other_field);
            if (key.equals(entry.getKey())) {
                other_field.setAccessible(true);
                if (!NeptuneSerializationUtil.isSupportedClass(other_field.getType())) continue;
                other_field.set(this, NeptuneSerializationUtil.getDeserializedValue(entry.getValue(), other_field.getType()));
            }
            else if (entry.getKey().contains(".")) {
                String first_category_from_entry = entry.getKey().substring(0, entry.getKey().indexOf("."));
                if (!first_category_from_entry.equals(key)) continue;
                findMatchingFieldAndSetValue(entry, other_field.get(this), getFieldsFromClass(other_field.get(this).getClass()), first_category_from_entry);
            }
        }
    }

    private void findMatchingFieldAndSetValue(Map.Entry<String, String> entry, Object parent_object, Field[] fields, String last_category_path) throws IllegalAccessException {
        for (Field other_field : fields) {
            String key = last_category_path + "." + (other_field.isAnnotationPresent(ConfigCustomName.class) ? other_field.getAnnotation(ConfigCustomName.class).value() : getNameFromField(other_field));
            if (key.equals(entry.getKey())) {
                other_field.setAccessible(true);
                if (!NeptuneSerializationUtil.isSupportedClass(other_field.getType())) continue;
                other_field.set(parent_object, NeptuneSerializationUtil.getDeserializedValue(entry.getValue(), other_field.getType()));
            }
            else if (entry.getKey().contains(".")) {
                String regex = "^" + last_category_path + "\\.[\\w\\s]*";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(key);
                if (!matcher.find()) continue;
                String t_category = matcher.group(0);
                matcher = pattern.matcher(entry.getKey());
                if (!matcher.find()) continue;
                String new_last_category = matcher.group(0);
                if (!t_category.equals(new_last_category)) continue;
                findMatchingFieldAndSetValue(entry, other_field.get(parent_object), getFieldsFromClass(other_field.get(parent_object).getClass()), new_last_category);
            }
        }
    }

    private String getNameFromField(Field field) {
        return field.getName().toLowerCase(Locale.ROOT).replace("_", " ");
    }

    private Field[] getFields() {
        return getFieldsFromClass(this.getClass());
    }

    private Field[] getFieldsFromClass(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> !field.isAnnotationPresent(NotConfigField.class)).toArray(Field[]::new);
    }
}
