package io.komune.c2.chaincode.api.fabric.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonUtilsTest {

    @Test
    public void toJson_withSimpleObject_shouldReturnJsonString() throws JsonProcessingException {
        TestObject obj = new TestObject("test", 42);

        String json = JsonUtils.toJson(obj);

        assertThat(json).isEqualTo("{\"name\":\"test\",\"value\":42}");
    }

    @Test
    public void toJson_withNull_shouldReturnNullString() throws JsonProcessingException {
        String json = JsonUtils.toJson(null);

        assertThat(json).isEqualTo("null");
    }

    @Test
    public void toJson_withNestedObject_shouldReturnJsonString() throws JsonProcessingException {
        TestObjectWithNested obj = new TestObjectWithNested("parent", new TestObject("child", 10));

        String json = JsonUtils.toJson(obj);

        assertThat(json).isEqualTo("{\"label\":\"parent\",\"nested\":{\"name\":\"child\",\"value\":10}}");
    }

    @Test
    public void toJson_withList_shouldReturnJsonArray() throws JsonProcessingException {
        List<String> list = List.of("a", "b", "c");

        String json = JsonUtils.toJson(list);

        assertThat(json).isEqualTo("[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void toJson_withMap_shouldReturnJsonObject() throws JsonProcessingException {
        Map<String, Integer> map = Map.of("key1", 1, "key2", 2);

        String json = JsonUtils.toJson(map);

        assertThat(json).contains("\"key1\":1");
        assertThat(json).contains("\"key2\":2");
    }

    @Test
    public void toObject_withValidUrl_shouldDeserialize() throws IOException {
        URL url = getClass().getClassLoader().getResource("utils/test-object.json");

        TestObject result = JsonUtils.toObject(url, TestObject.class);

        assertThat(result.name).isEqualTo("fromFile");
        assertThat(result.value).isEqualTo(123);
    }

    @Test
    public void toObject_withNullUrl_shouldThrowException() {
        assertThatThrownBy(() -> JsonUtils.toObject(null, TestObject.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void toJson_withPrivateFieldsNoAccessors_shouldSerializeFields() throws JsonProcessingException {
        PrivateFieldsOnlyObject obj = new PrivateFieldsOnlyObject();
        obj.setFieldsViaReflectionStyle("secretName", 999, true);

        String json = JsonUtils.toJson(obj);

        assertThat(json).contains("\"privateName\":\"secretName\"");
        assertThat(json).contains("\"privateValue\":999");
        assertThat(json).contains("\"privateFlag\":true");
    }

    @Test
    public void toObject_withPrivateFieldsNoAccessors_shouldDeserializeFields() throws IOException {
        URL url = getClass().getClassLoader().getResource("utils/private-fields-object.json");

        PrivateFieldsOnlyObject result = JsonUtils.toObject(url, PrivateFieldsOnlyObject.class);

        assertThat(result.getPrivateName()).isEqualTo("deserializedName");
        assertThat(result.getPrivateValue()).isEqualTo(456);
        assertThat(result.isPrivateFlag()).isTrue();
    }

    @Test
    public void toJson_withInheritedPrivateFields_shouldSerializeAllFields() throws JsonProcessingException {
        ChildObject child = new ChildObject();
        child.setParentField("parentValue");
        child.setChildField("childValue");

        String json = JsonUtils.toJson(child);

        assertThat(json).contains("\"parentField\":\"parentValue\"");
        assertThat(json).contains("\"childField\":\"childValue\"");
    }

    private static class TestObject {
        private String name;
        private int value;

        TestObject() {}

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class TestObjectWithNested {
        private String label;
        private TestObject nested;

        TestObjectWithNested() {}

        TestObjectWithNested(String label, TestObject nested) {
            this.label = label;
            this.nested = nested;
        }
    }

    /**
     * Test class with only private fields and NO public getters/setters.
     * This verifies that the FIELD visibility configuration works correctly.
     */
    private static class PrivateFieldsOnlyObject {
        private String privateName;
        private int privateValue;
        private boolean privateFlag;

        PrivateFieldsOnlyObject() {}

        void setFieldsViaReflectionStyle(String name, int value, boolean flag) {
            this.privateName = name;
            this.privateValue = value;
            this.privateFlag = flag;
        }

        String getPrivateName() { return privateName; }
        int getPrivateValue() { return privateValue; }
        boolean isPrivateFlag() { return privateFlag; }
    }

    private static class ParentObject {
        private String parentField;

        ParentObject() {}

        void setParentField(String value) { this.parentField = value; }
    }

    private static class ChildObject extends ParentObject {
        private String childField;

        ChildObject() {}

        void setChildField(String value) { this.childField = value; }
    }
}