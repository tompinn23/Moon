package org.yonside.moon.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.yonside.moon.coremod.MoonCore;

import com.electronwill.nightconfig.toml.TomlFormat;

public final class CoreConfig {

    private static final String FILE_NAME = "mooncore.toml";

    private static List<Rule> rules = Collections.emptyList();
    private static Map<String, List<Rule>> byClass = Collections.emptyMap();
    private static Set<Feature> features = Collections.emptySet();
    private static boolean verbose = true;
    private static boolean failFast = true;

    private static boolean loaded = false;

    private CoreConfig() {}

    public static List<Rule> rules() {
        return rules;
    }

    public static Map<String, List<Rule>> rulesByClass() {
        return byClass;
    }

    public static boolean verbose() {
        return verbose;
    }

    public static boolean failFast() {
        return failFast;
    }

    public static boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }

    // ---- model --------------------------------------------------------

    public static final class Rule {

        public final String name;
        public final String targetClass;
        public Set<String> methods = new HashSet<>(); // empty = every method
        public Set<String> sites = new HashSet<>(); // empty = whole method is one region
        public Set<String> requireAny = new HashSet<>(); // empty = no marker required
        public final Map<String, String> stringSwaps = new LinkedHashMap<>();
        public final Map<FieldRef, FieldRef> fieldSwaps = new LinkedHashMap<>();

        /** -1 when no exact count was configured. */
        public int expectExact = -1;
        public int expectMin = 1;

        public Rule(String name, String targetClass) {
            this.name = name;
            this.targetClass = targetClass;
        }

        public boolean hasSwaps() {
            return !stringSwaps.isEmpty() || !fieldSwaps.isEmpty();
        }

        /** null when the count is acceptable, otherwise the complaint. */
        public String checkCount(int actual) {
            if (expectExact >= 0) {
                return actual == expectExact ? null : "matched " + actual + ", expected exactly " + expectExact;
            }
            return actual >= expectMin ? null : "matched " + actual + ", expected at least " + expectMin;
        }

        @Override
        public String toString() {
            return "'" + name + "' (" + targetClass + ")";
        }
    }

    public static final class FieldRef {

        public final String owner; // internal name, slashes
        public final String name;

        public FieldRef(String owner, String name) {
            this.owner = owner;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FieldRef)) return false;
            FieldRef other = (FieldRef) o;
            return owner.equals(other.owner) && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return owner.hashCode() * 31 + name.hashCode();
        }

        @Override
        public String toString() {
            return owner + "." + name;
        }
    }

    /** Called from FMLPlugin.injectData with the "configDir" value. */
    public static void loadFrom(File configDir) {
        if (loaded) return;
        File config = new File(configDir, FILE_NAME);

        if (!config.exists()) {
            writeTemplate(config);
            MoonCore.LOG.info("wrote default template {}", config.getPath());
        }

        com.electronwill.nightconfig.core.Config toml;
        try (Reader in = Files.newBufferedReader(config.toPath(), StandardCharsets.UTF_8)) {
            toml = TomlFormat.instance()
                .createParser()
                .parse(in);
        } catch (IOException e) {
            throw new RuntimeException("[patch] could not read " + config.getPath(), e);
        }

        verbose = toml.getOrElse("options.verbose", true);
        failFast = toml.getOrElse("options.fail-fast", true);

        features = readFeatures(toml);

        Map<String, Map<String, String>> presets = readPresets(toml);
        byClass = groupByClass(readRules(toml, presets));

        int total = 0;
        for (List<Rule> group : byClass.values()) total += group.size();
        MoonCore.LOG.info(
            "loaded {} rule(s) over {} class(es), {} preset(s) from {}",
            total,
            byClass.size(),
            presets.size(),
            config.getPath());
    }

    private static Set<Feature> readFeatures(com.electronwill.nightconfig.core.Config toml) {
        Set<Feature> features = new HashSet<>();

        List<String> enabledFeatures = toml.getOrElse("features.enabled", Collections.emptyList());
        for (String feature : enabledFeatures) {
            try {
                Feature val = Feature.valueOf(
                    feature.replace('-', '_')
                        .toUpperCase(Locale.ROOT));
                features.add(val);
            } catch (IllegalArgumentException e) {
                MoonCore.LOG.warn("unrecognized feature {}", feature);
            }
        }
        return Collections.unmodifiableSet(features);
    }

    private static Map<String, Map<String, String>> readPresets(com.electronwill.nightconfig.core.Config toml) {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        com.electronwill.nightconfig.core.Config table = toml.get("presets");
        if (table == null) return out;

        for (Map.Entry<String, Object> entry : table.valueMap()
            .entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!(value instanceof List)) {
                throw new IllegalStateException("[patch] preset '" + key + "' must be a list of pairs");
            }
            Map<String, String> swaps = new LinkedHashMap<>();
            for (Object pair : (List<?>) value) {
                String[] fromTo = pair(pair, "preset '" + key + "'");
                swaps.put(fromTo[0], fromTo[1]);
            }
            out.put(key, swaps);
        }
        return out;
    }

    private static List<Rule> readRules(com.electronwill.nightconfig.core.Config toml,
        Map<String, Map<String, String>> presets) {
        List<com.electronwill.nightconfig.core.Config> entries = toml
            .getOrElse("rule", Collections.<com.electronwill.nightconfig.core.Config>emptyList());
        List<Rule> parsed = new ArrayList<>(entries.size());

        for (int i = 0; i < entries.size(); i++) {
            com.electronwill.nightconfig.core.Config rc = entries.get(i);
            String where = "rule #" + (i + 1);

            if (!rc.getOrElse("enabled", true)) continue;

            String target = rc.get("class");
            require(target != null, where + " has no 'class'");
            String name = rc.getOrElse("name", target);
            where = "rule '" + name + "'";

            Rule rule = new Rule(name, target);
            rule.methods = new HashSet<>(rc.getOrElse("methods", Collections.<String>emptyList()));
            rule.sites = new HashSet<>(rc.getOrElse("sites", Collections.<String>emptyList()));
            rule.requireAny = new HashSet<>(rc.getOrElse("require", Collections.<String>emptyList()));

            for (String presetName : rc.getOrElse("use", Collections.<String>emptyList())) {
                Map<String, String> preset = presets.get(presetName);
                require(preset != null, where + " uses unknown preset '" + presetName + "'");
                rule.stringSwaps.putAll(preset);
            }

            for (Object entry : rc.getOrElse("swap", Collections.<Object>emptyList())) {
                String[] fromTo = pair(entry, where + " swap");
                rule.stringSwaps.put(fromTo[0], fromTo[1]);
            }

            for (Object entry : rc.getOrElse("field", Collections.<Object>emptyList())) {
                String[] fromTo = pair(entry, where + " field");
                rule.fieldSwaps.put(fieldRef(fromTo[0], where), fieldRef(fromTo[1], where));
            }

            rule.expectExact = rc.getOrElse("expect", -1);
            rule.expectMin = rc.getOrElse("expect-min", 1);

            require(rule.hasSwaps(), where + " has no swap, field or use entries");
            parsed.add(rule);
        }
        return parsed;
    }

    // ---- helpers ------------------------------------------------------

    private static String[] pair(Object value, String where) {
        require(value instanceof List, where + ": expected a [from, to] pair");
        List<?> list = (List<?>) value;
        require(list.size() == 2, where + ": pair has " + list.size() + " entries, expected 2");
        require(
            list.get(0) instanceof String && list.get(1) instanceof String,
            where + ": pair entries must be strings");
        return new String[] { (String) list.get(0), (String) list.get(1) };
    }

    private static FieldRef fieldRef(String token, String where) {
        int dot = token.lastIndexOf('.');
        require(dot > 0 && dot < token.length() - 1, where + ": '" + token + "' is not <owner/Class.Field>");
        String owner = token.substring(0, dot);
        require(owner.indexOf('.') < 0, where + ": field owner must use slashes, not dots: '" + owner + "'");
        return new FieldRef(owner, token.substring(dot + 1));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("[patch] " + message);
    }

    private static Map<String, List<Rule>> groupByClass(List<Rule> parsed) {
        Map<String, List<Rule>> out = new HashMap<>();
        for (Rule rule : parsed) {
            List<Rule> group = out.get(rule.targetClass);
            if (group == null) {
                group = new ArrayList<>(2);
                out.put(rule.targetClass, group);
            }
            group.add(rule);
        }
        // freeze: read concurrently from the class-loading thread
        for (Map.Entry<String, List<Rule>> e : out.entrySet()) {
            e.setValue(Collections.unmodifiableList(e.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    // ---- template -----------------------------------------------------

    private static void writeTemplate(File config) {
        File dir = config.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            MoonCore.LOG.warn("could not create {}", dir.getPath());
            return;
        }
        try (InputStream in = CoreConfig.class.getResourceAsStream("/coremod/mooncore.toml")) {
            if (in == null) {
                throw new IllegalStateException(
                    "[patch] missing bundled resource /coremod/mooncore.toml - check src/main/resources");
            }
            Files.copy(in, config.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            MoonCore.LOG.warn("could not write template {}: {}", config.getPath(), e.toString());
        }
    }
}
