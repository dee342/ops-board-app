package gov.nyc.dsny.smart.opsboard;

import gov.nyc.dsny.smart.opsboard.domain.reference.Category;

import java.io.IOException;
import java.lang.reflect.Field;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CategorySerializer extends JsonSerializer<Category> {
    @Override
    public void serialize(Category value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        Field[] fields = value.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                jgen.writeObjectField(field.getName(), field.get(value));
                field.setAccessible(false);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("className"+value.getClass().getCanonicalName().toString());
        jgen.writeStringField("_class", value.getClass().getCanonicalName().toString());
        jgen.writeEndObject();
    }
}
