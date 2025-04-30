package io.github.spencerpark.ijava;

import io.github.spencerpark.ijava.utils.FileUtils;
import io.github.spencerpark.ijava.utils.RuntimeCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {
    @Test
    public void testReadXml() {
        try {
            Path filePath = Path.of("D:\\Maven\\apache-maven-3.6.3\\conf\\settings.xml");
            Set<String> elementNames = Collections.singleton("localRepository");
            Map<String, String> elementTextData = FileUtils.readXmlElementText(filePath, elementNames);
            Assertions.assertNotNull(elementTextData);
            for (String elementName : elementNames) {
                Assertions.assertEquals("D:\\Maven\\repository", elementTextData.get(elementName));
            }
        } catch (Exception e) {
            // pass
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testCompile() {
        String name = "vo.Cat";
        String clzDef = """
                package vo;
                
                //import lombok.Data;
                
                //@Data
                public class Cat {
                    private String name;
                    private Integer age;
                }
                """;
        Class<?> clz = RuntimeCompiler.compile(name, clzDef, true);
        List<String> methods = Arrays.stream(clz.getDeclaredMethods())
                .map(method -> method.getName() + "(" + Arrays.stream(method.getGenericParameterTypes())
                        .map(type -> type.getTypeName().substring(type.getTypeName().lastIndexOf('.') + 1))
                        .collect(Collectors.joining(",")) + ")").toList();

        System.out.printf("compile done, clz: %s, clz's declared methods: %s%n", clz, methods);
    }
}
