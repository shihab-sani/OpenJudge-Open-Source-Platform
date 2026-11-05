package open.judge.Entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskRules {
    private String language;                 // "python", "java"
    private String entryPoint = "main.py";   // file to execute
    private List<TestCaseRule> testCases = new ArrayList<>();
    private Integer timeoutSeconds = 5;
    private Integer memoryLimitMb = 128;
}
