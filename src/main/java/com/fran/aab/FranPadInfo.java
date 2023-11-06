package com.fran.aab;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import brut.directory.FileDirectory;


/**
 * @author 程良明
 * @date 2023/11/6
 * * * 说明:
 **/
public class FranPadInfo {
    private static final String FRAN_PAD = "franPad.yml";
    private Map<String, String> padInfo = new LinkedHashMap();

    public FranPadInfo() {
    }

    public Map<String, String> getPadInfo() {
        return padInfo;
    }

    public void setSdkInfoField(String key, String value) {
        this.padInfo.put(key, value);
    }

    private static Yaml getYaml() {
        return new Yaml();
    }


    public static FranPadInfo load(InputStream is) {
        return (FranPadInfo) getYaml().loadAs(is, FranPadInfo.class);
    }

    public static FranPadInfo load(File franPadFile) {

        FranPadInfo var2 = null;
        if (!FRAN_PAD.equals(franPadFile.getName())) {
            franPadFile = new File(franPadFile, FRAN_PAD);
        }
        try (InputStream in = Files.newInputStream(franPadFile.toPath())) {
            var2 = load(in);
        } catch (Exception ignored) {
            System.out.println("clm franPad error: " + ignored.getMessage());
        }
        return var2;
    }

}
