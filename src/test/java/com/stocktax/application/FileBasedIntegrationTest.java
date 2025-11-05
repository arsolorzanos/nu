package com.stocktax.application;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktax.infrastructure.adapters.JsonInputAdapter;
import com.stocktax.infrastructure.adapters.JsonOutputAdapter;
import com.stocktax.infrastructure.config.ApplicationConfig;
import com.stocktax.infrastructure.dto.TaxCalculationDto;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileBasedIntegrationTest {

  private TaxCalculationService taxCalculationService;
  private JsonInputAdapter inputAdapter;
  private JsonOutputAdapter outputAdapter;
  private Application application;
  private ObjectMapper objectMapper;

  private InputStream originalIn;
  private PrintStream originalOut;
  private ByteArrayOutputStream capturedOut;

  @BeforeEach
  void setUp() {
    taxCalculationService = ApplicationConfig.createTaxCalculationService();
    inputAdapter = new JsonInputAdapter();
    outputAdapter = new JsonOutputAdapter();
    application =
      new Application(taxCalculationService, inputAdapter, outputAdapter);
    objectMapper = new ObjectMapper();

    originalIn = System.in;
    originalOut = System.out;
  }

  @AfterEach
  void tearDown() {
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  @Test
  void testCase1() throws IOException {
    runTestCase("case1.txt", "case1-expected.txt");
  }

  @Test
  void testCase2() throws IOException {
    runTestCase("case2.txt", "case2-expected.txt");
  }

  @Test
  void testCase3() throws IOException {
    runTestCase("case3.txt", "case3-expected.txt");
  }

  @Test
  void testCase4() throws IOException {
    runTestCase("case4.txt", "case4-expected.txt");
  }

  @Test
  void testCase5() throws IOException {
    runTestCase("case5.txt", "case5-expected.txt");
  }

  @Test
  void testCase6() throws IOException {
    runTestCase("case6.txt", "case6-expected.txt");
  }

  @Test
  void testCase7() throws IOException {
    runTestCase("case7.txt", "case7-expected.txt");
  }

  @Test
  void testCase8() throws IOException {
    runTestCase("case8.txt", "case8-expected.txt");
  }

  @Test
  void testCase9() throws IOException {
    runTestCase("case9.txt", "case9-expected.txt");
  }

  @Test
  void testCase1And2() throws IOException {
    runTestCase("case1-2.txt", "case1-2-expected.txt");
  }

  private void runTestCase(String inputFileName, String expectedFileName)
    throws IOException {
    Path inputFile = getInputPath(inputFileName);
    Path expectedFile = getExpectedPath(expectedFileName);

    assertTrue(
      Files.exists(inputFile),
      String.format("Input file %s should exist", inputFileName)
    );
    assertTrue(
      Files.exists(expectedFile),
      String.format("Expected file %s should exist", expectedFileName)
    );

    System.setIn(new FileInputStream(inputFile.toFile()));

    capturedOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(capturedOut));

    application.run();

    String output = capturedOut.toString();
    List<String> actualLines = extractJsonLines(output);

    List<String> expectedLines = readExpectedOutput(expectedFile);

    assertFalse(
      actualLines.isEmpty(),
      String.format("Expected output for %s, but got none", inputFileName)
    );

    assertEquals(
      expectedLines.size(),
      actualLines.size(),
      String.format(
        "Number of output lines should match for %s. Expected: %d, Actual: %d",
        inputFileName,
        expectedLines.size(),
        actualLines.size()
      )
    );

    for (int i = 0; i < expectedLines.size(); i++) {
      verifyTaxCalculations(
        expectedLines.get(i),
        actualLines.get(i),
        i,
        inputFileName
      );
    }
  }

  private Path getInputPath(String fileName) {
    return Paths.get("src", "test", "resources", "inputs", fileName);
  }

  private Path getExpectedPath(String fileName) {
    return Paths.get("src", "test", "resources", "expected", fileName);
  }

  private List<String> readExpectedOutput(Path expectedFile)
    throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(expectedFile)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty()) {
          lines.add(trimmed);
        }
      }
    }
    return lines;
  }

  private List<String> extractJsonLines(String output) {
    List<String> lines = new ArrayList<>();
    String[] split = output.split("\n");
    for (String line : split) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty() && trimmed.startsWith("[")) {
        lines.add(trimmed);
      }
    }
    return lines;
  }

  private void verifyTaxCalculations(
    String expectedJson,
    String actualJson,
    int lineIndex,
    String testCase
  ) {
    try {
      List<TaxCalculationDto> expected = objectMapper.readValue(
        expectedJson,
        new TypeReference<List<TaxCalculationDto>>() {}
      );
      List<TaxCalculationDto> actual = objectMapper.readValue(
        actualJson,
        new TypeReference<List<TaxCalculationDto>>() {}
      );

      assertEquals(
        expected.size(),
        actual.size(),
        String.format(
          "Number of tax calculations should match at line %d for %s. Expected: %d, Actual: %d",
          lineIndex,
          testCase,
          expected.size(),
          actual.size()
        )
      );

      for (int i = 0; i < expected.size(); i++) {
        BigDecimal expectedTax = expected.get(i).getTax();
        BigDecimal actualTax = actual.get(i).getTax();

        assertEquals(
          0,
          expectedTax.compareTo(actualTax),
          String.format(
            "Tax should match at line %d, index %d for %s. Expected: %s, Actual: %s",
            lineIndex,
            i,
            testCase,
            expectedTax,
            actualTax
          )
        );
      }
    } catch (Exception e) {
      fail(
        String.format(
          "Failed to parse or compare JSON at line %d for %s: %s. Expected: %s, Actual: %s",
          lineIndex,
          testCase,
          e.getMessage(),
          expectedJson,
          actualJson
        ),
        e
      );
    }
  }
}
