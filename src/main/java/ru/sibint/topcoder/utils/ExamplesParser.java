package ru.sibint.topcoder.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExamplesParser {

    public String parseExamples(String input) throws Exception {
        StringBuilder sb = new StringBuilder();
        XmlMapper xmlMapper = new XmlMapper();
        Map example = xmlMapper.readValue(input, Map.class);
        List<Map> tests = (List<Map>)(((Map<String, Object>)example.get("ol")).get("li"));
        HashMap<String, HashMap<String, String>> testsToReturn = new HashMap<>();
        int id = 0;
        for(Map test: tests) {
            id++;
            List<String> singleTest = (List<String>)(((Map<String, Object>) test.get("div")).get("p"));
            HashMap<String, String> singleTestToReturn = new HashMap<>();
            StringBuilder inputString = new StringBuilder();
            StringBuilder outputString = new StringBuilder();
            for(String line: singleTest) {
                if(line.startsWith("Returns: ")) {
                    outputString.append(line.substring(9));
                    break;
                }
                inputString.append(line).append("\n");
            }
            singleTestToReturn.put("input", inputString.toString().trim());
            singleTestToReturn.put("expectedOutput", outputString.toString().trim());
            testsToReturn.put("test" + id, singleTestToReturn);
            if(id == 20) {
                break;
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(testsToReturn);
    }

}
