package com.creda.coin.price.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.dto.ProfitAnalyzerDTO;
import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {
    @InjectMocks
    private TransferService transferService;

    @Mock
    private AnalyzerDataHandler analyzerDataHandler;
    @Mock
    private IVaultTokenService vaultTokenService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // ProfitAnalyzerContext initialization
        ProfitAnalyzerDTO profitAnalyzerDTO = new ProfitAnalyzerDTO();
        ProfitAnalyzerContext.setProfitAnalyzerDTO(profitAnalyzerDTO);
    }

    @Test
    public void testNto1Transfer() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/transferNTo1.json");
        ObjectMapper objectMapper = new ObjectMapper();
        SolanaTransaction mockTransaction = objectMapper.readValue(jsonFile, new TypeReference<SolanaTransaction>() {});
    
        transferService.processTransfer(mockTransaction);

        String inAccount = "23bKThH2mBsDqtKrW9bW4fn4tyP7bzRS6MEiAddogpBU";
        String tokenAddress = "EMPGw4dLkMrT6jdGQb3BEuPZzF1SqkYQYU3ZuU5vQ8NT";
        BigDecimal inAmount = new BigDecimal("1279475.87620918");

        verify(analyzerDataHandler, times(7)).transferOutErc20(eq(mockTransaction), anyString(), eq(tokenAddress), any(BigDecimal.class), any(SwapInstructionExtraInfo.class));
        verify(analyzerDataHandler, times(1)).transferInErc20(eq(mockTransaction), eq(inAccount), eq(tokenAddress), eq(inAmount), any(SwapInstructionExtraInfo.class));
    }

    @Test
    public void test1ToNTransfer() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/transfer1ToN.json");
        ObjectMapper objectMapper = new ObjectMapper();
        SolanaTransaction mockTransaction = objectMapper.readValue(jsonFile, new TypeReference<SolanaTransaction>() {});
    
        transferService.processTransfer(mockTransaction);

        String outAccount = "4pbxZHRear4z2R7wfEGL2ziGLrPoVKUYqdpKwWwHKH4X";
        String tokenAddress = "6cnDfXAG9EsGn5BB1QF3i6Xx1SNJ88Qcc1nckJp6pump";
        BigDecimal outAmount = new BigDecimal("11.665000");

        verify(analyzerDataHandler, times(1)).transferOutErc20(eq(mockTransaction), eq(outAccount), eq(tokenAddress), eq(outAmount), any(SwapInstructionExtraInfo.class));
        verify(analyzerDataHandler, times(5)).transferInErc20(eq(mockTransaction), anyString(), eq(tokenAddress), any(BigDecimal.class), any(SwapInstructionExtraInfo.class));
    }
}
