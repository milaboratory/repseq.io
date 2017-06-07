package io.repseq.gen.dist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.milaboratory.util.GlobalObjectMappers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class GModels {
    private GModels() {
    }

    private static Map<String, GCloneModel> knownGModels;

    private static void ensureInitialized() {
        if (knownGModels == null)
            synchronized (GModels.class) {
                if (knownGModels == null) {
                    Map<String, GCloneModel> map;
                    try {
                        InputStream is = GModels.class.getClassLoader().getResourceAsStream("GCloneModels.json");
                        TypeReference<HashMap<String, GCloneModel>> typeRef = new TypeReference<HashMap<String, GCloneModel>>() {
                        };
                        map = GlobalObjectMappers.ONE_LINE.readValue(is, typeRef);
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                    knownGModels = map;
                }
            }
    }

    public static Set<String> getAvailableParameterNames() {
        ensureInitialized();
        return knownGModels.keySet();
    }

    public static GCloneModel getGCloneModelByName(String name) {
        try {
            ensureInitialized();
            if (Files.exists(Paths.get(name)))
                return GlobalObjectMappers.ONE_LINE.readValue(new File(name), GCloneModel.class);
            if (Files.exists(Paths.get(name + ".json")))
                return GlobalObjectMappers.ONE_LINE.readValue(new File(name + ".json"), GCloneModel.class);
            GCloneModel model = knownGModels.get(name);
            if (model == null)
                throw new IllegalArgumentException("Can't find model with name " + name);
            return model;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
