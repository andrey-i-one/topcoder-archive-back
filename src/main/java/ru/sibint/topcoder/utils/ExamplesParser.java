package ru.sibint.topcoder.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sibint.topcoder.generated.dto.TestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamplesParser {

    public List<TestDto> parseExamples(String input) throws Exception {
        input = input.replace("&nbsp;", "");
        XmlMapper xmlMapper = new XmlMapper();
        Map example = xmlMapper.readValue(input, Map.class);
        if(example.get("ol").getClass().getName().equals("java.lang.String")) {
            return new ArrayList<>();
        }
        List<Map> tests = (List<Map>)(((Map<String, Object>)example.get("ol")).get("li"));
        List<TestDto> testsToReturn = new ArrayList<>();
        int id = 0;
        for(Map test: tests) {
            id++;
            List<String> singleTest = (List<String>)(((Map<String, Object>) test.get("div")).get("p"));
            StringBuilder inputString = new StringBuilder();
            StringBuilder outputString = new StringBuilder();
            for(String line: singleTest) {
                if(line.startsWith("Returns: ")) {
                    outputString.append(line.substring(9));
                    break;
                }
                inputString.append(line).append("\n");
            }
            testsToReturn.add(TestDto.builder()
                            .id(String.valueOf(id))
                            .input(inputString.toString().trim())
                            .expectedOutput(outputString.toString().trim())
                    .build());
        }
        return testsToReturn;
    }

}
