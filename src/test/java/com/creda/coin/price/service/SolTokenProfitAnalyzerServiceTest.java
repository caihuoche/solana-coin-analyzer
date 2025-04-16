/*
package com.creda.coin.price.service;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.creda.coin.price.entity.es.Instruction;
import com.creda.coin.price.service.data.elasticsearch.TransactionService;
import com.creda.coin.price.service.data.jdbc.impl.ProfitCalRecordServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SolTokenProfitAnalyzerServiceTest {
    @Test
    public void testGroupSwapInstructionsGiven6Instructions() {
        try {
            ProfitCalRecordServiceImpl profitCalRecordService = new ProfitCalRecordServiceImpl();
            TransactionService transactionService = new TransactionService();
            SolTokenProfitAnalyzerService service = new SolTokenProfitAnalyzerService(profitCalRecordService, transactionService);

            File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/6Instructions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 解析为 Instruction 对象的列表
            List<Instruction> instructions = objectMapper.readValue(jsonFile, new TypeReference<List<Instruction>>() {});
            List<List<Instruction>> groupedInstructions = service.groupSwapInstructions(instructions);

            assertEquals(2, groupedInstructions.size(), "The length of grouped instructions should be 2.");
            assertEquals(3, groupedInstructions.get(0).size(), "The first grouped instructions should contains 3 elements");
            
            String transferAmount11 = service.getTransferAmount(groupedInstructions.get(0).get(1));
            String transferAmount12 = service.getTransferAmount(groupedInstructions.get(0).get(2));
            assertEquals("55252920", transferAmount11, "The first transfer amount of first grouped instructions should be 55252920");
            assertEquals("37848701", transferAmount12, "The second transfer amount of first grouped instructions should be 37848701");

            assertEquals(3, groupedInstructions.get(1).size(), "The second grouped instructions should contains 3 elements");

            String transferAmount21 = service.getTransferAmount(groupedInstructions.get(1).get(1));
            String transferAmount22 = service.getTransferAmount(groupedInstructions.get(1).get(2));
            assertEquals("37848701", transferAmount21, "The first transfer amount of second grouped instructions should be 37848701");
            assertEquals("55291161", transferAmount22, "The second transfer amount of second grouped instructions should be 55291161");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGroupSwapInstructionsGiven2Instructions() {
        try {
            ProfitCalRecordServiceImpl profitCalRecordService = new ProfitCalRecordServiceImpl();
            TransactionService transactionService = new TransactionService();
            SolTokenProfitAnalyzerService service = new SolTokenProfitAnalyzerService(profitCalRecordService, transactionService);

            File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/2Instructions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 解析为 Instruction 对象的列表
            List<Instruction> instructions = objectMapper.readValue(jsonFile, new TypeReference<List<Instruction>>() {});
            List<List<Instruction>> groupedInstructions = service.groupSwapInstructions(instructions);

            assertEquals(1, groupedInstructions.size(), "The length of grouped instructions should be 1.");
            assertEquals(2, groupedInstructions.get(0).size(), "The first grouped instructions should contains 2 elements");
            
            String transferAmount1 = service.getTransferAmount(groupedInstructions.get(0).get(0));
            String transferAmount2 = service.getTransferAmount(groupedInstructions.get(0).get(1));
            assertEquals("273125121137", transferAmount1, "The first transfer amount should be 273125121137");
            assertEquals("32301894", transferAmount2, "The second transfer amount should be 32301894");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGroupSwapInstructionsGiven1Instructions() {
        try {
            ProfitCalRecordServiceImpl profitCalRecordService = new ProfitCalRecordServiceImpl();
            TransactionService transactionService = new TransactionService();
            SolTokenProfitAnalyzerService service = new SolTokenProfitAnalyzerService(profitCalRecordService, transactionService);

            File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/1Instructions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 解析为 Instruction 对象的列表
            List<Instruction> instructions = objectMapper.readValue(jsonFile, new TypeReference<List<Instruction>>() {});
            List<List<Instruction>> groupedInstructions = service.groupSwapInstructions(instructions);

            assertEquals(1, groupedInstructions.size(), "The length of grouped instructions should be 1.");
            assertEquals(3, groupedInstructions.get(0).size(), "The first grouped instructions should contains 2 elements");
            
            String transferAmount1 = service.getTransferAmount(groupedInstructions.get(0).get(1));
            String transferAmount2 = service.getTransferAmount(groupedInstructions.get(0).get(2));
            assertEquals("5000000000000", transferAmount1, "The first transfer amount should be 273125121137");
            assertEquals("159039553", transferAmount2, "The second transfer amount should be 32301894");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testGroupSwapInstructionsGiven8Instructions() {
        try {
            ProfitCalRecordServiceImpl profitCalRecordService = new ProfitCalRecordServiceImpl();
            TransactionService transactionService = new TransactionService();
            SolTokenProfitAnalyzerService service = new SolTokenProfitAnalyzerService(profitCalRecordService, transactionService);

            File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/8Instructions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 解析为 Instruction 对象的列表
            List<Instruction> instructions = objectMapper.readValue(jsonFile, new TypeReference<List<Instruction>>() {});
            List<List<Instruction>> groupedInstructions = service.groupSwapInstructions(instructions);

            assertEquals(2, groupedInstructions.size(), "The length of grouped instructions should be 2.");
            assertEquals(3, groupedInstructions.get(0).size(), "The first grouped instructions should contains 3 elements");

            assertEquals(3, groupedInstructions.get(1).size(), "The second grouped instructions should contains 3 elements");
            
            String transferAmount11 = service.getTransferAmount(groupedInstructions.get(0).get(1));
            String transferAmount12 = service.getTransferAmount(groupedInstructions.get(0).get(2));
            assertEquals("3000000", transferAmount11, "The first transfer amount should be 3000000");
            assertEquals("19756598", transferAmount12, "The second transfer amount should be 19756598");

            String transferAmount21 = service.getTransferAmount(groupedInstructions.get(1).get(1));
            String transferAmount22 = service.getTransferAmount(groupedInstructions.get(1).get(2));
            assertEquals("19756598", transferAmount21, "The first transfer amount should be 273125121137");
            assertEquals("34384667609016", transferAmount22, "The second transfer amount should be 32301894");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

*/
