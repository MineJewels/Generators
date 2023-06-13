package org.minejewels.jewelsgens.gen.storage;

import net.abyssdev.abysslib.storage.json.JsonStorage;
import net.abyssdev.abysslib.utils.file.Files;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.gen.data.GeneratorData;

import java.util.UUID;

public class GeneratorStorage extends JsonStorage<UUID, GeneratorData> {

    public GeneratorStorage(final JewelsGens plugin) {
        super(Files.file("data.json", plugin), GeneratorData.class);
    }
}
