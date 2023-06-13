package org.minejewels.jewelsgens.gen.data;

import lombok.Data;
import net.abyssdev.abysslib.storage.id.Id;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;

import java.util.UUID;

@Data
public class GeneratorData {

    @BsonIgnore private transient GeneratorTask task;
    @Id
    private final UUID uuid;
    private final String location;
    private String generator;
}
