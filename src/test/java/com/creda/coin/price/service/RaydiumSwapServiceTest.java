package com.creda.coin.price.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.dto.ProfitAnalyzerDTO;
import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.VaultToken;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;
import com.creda.coin.price.third.SolanaApiService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class RaydiumSwapServiceTest {
    @InjectMocks
    private RaydiumSwapService raydiumSwapService;

    @Mock
    private SolanaApiService solanaApiService;
    @Mock
    private IVaultTokenService vaultTokenService;
    @Mock
    private IAssetInfoService assetInfoService;
    @Mock
    private AnalyzerDataHandler analyzerDataHandler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // ProfitAnalyzerContext initialization
        ProfitAnalyzerDTO profitAnalyzerDTO = new ProfitAnalyzerDTO();
        ProfitAnalyzerContext.setProfitAnalyzerDTO(profitAnalyzerDTO);
    }

    @Test
    public void testNto1Transfer() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File("/Users/jadenzhang/Documents/creda/solana-coin-analyzer/src/test/java/com/creda/coin/price/service/raydiumSwapInstructions.json");
        ObjectMapper objectMapper = new ObjectMapper();
        SolanaTransaction mockTransaction = objectMapper.readValue(jsonFile, new TypeReference<SolanaTransaction>() {});
    
        List<String> vaultTokens = Arrays.asList("A7LUs7FL1oySmxTmkfjk6VxAmAEXyPYtJ8bNGKwtN5qt", "FyJXCDjyVpDRhdZ1d1Y2DFRmnuC6ed9LEm12yqHJwdgD");
        Map<String, VaultToken> vaultToMintTokenMap = new HashMap<>();
        VaultToken a = new VaultToken();
        a.setVaultTokenAddress("FyJXCDjyVpDRhdZ1d1Y2DFRmnuC6ed9LEm12yqHJwdgD");
        a.setMintTokenAddress("Eyhjeq8EA8feDGp8FkBJv5iGr5YsgjYBnvmYkX6uDLPh");
        a.setMintTokenDecimals(9);
        vaultToMintTokenMap.put("FyJXCDjyVpDRhdZ1d1Y2DFRmnuC6ed9LEm12yqHJwdgD", a);

        VaultToken b = new VaultToken();
        b.setVaultTokenAddress("A7LUs7FL1oySmxTmkfjk6VxAmAEXyPYtJ8bNGKwtN5qt");
        b.setMintTokenAddress("So11111111111111111111111111111111111111112");
        b.setMintTokenDecimals(9);
        vaultToMintTokenMap.put("A7LUs7FL1oySmxTmkfjk6VxAmAEXyPYtJ8bNGKwtN5qt", b);

        when(vaultTokenService.findByVaultAddresses(vaultTokens)).thenReturn(vaultToMintTokenMap);

        AtomicInteger atomicInteger = new AtomicInteger();
        raydiumSwapService.processRaydiumSwap(mockTransaction, atomicInteger);

        String swapper = "67PBXNgWKjozNFL3xWJsAXSEtT6vr19ctuW3Ud6BBwHc";
        String sellToken = "So11111111111111111111111111111111111111112";
        String buyToken = "Eyhjeq8EA8feDGp8FkBJv5iGr5YsgjYBnvmYkX6uDLPh";

        ArgumentCaptor<SwapInstructionExtraInfo> captor = ArgumentCaptor.forClass(SwapInstructionExtraInfo.class);

        verify(analyzerDataHandler, times(1)).sellErc20(eq(mockTransaction), eq(swapper), eq(sellToken), captor.capture());
        verify(analyzerDataHandler, times(1)).buyErc20(eq(mockTransaction), eq(swapper), eq(buyToken), captor.capture());

        List<SwapInstructionExtraInfo> capturedSwapInstructions = captor.getAllValues();
        SwapInstructionExtraInfo capturedSwapInstruction1 = capturedSwapInstructions.get(0);
        assertEquals(capturedSwapInstruction1.getToken0(), sellToken);
        assertEquals(capturedSwapInstruction1.getToken1(), buyToken);
        assertEquals(capturedSwapInstruction1.getToken0Amount(), new BigDecimal("15.233630832000000000"));
        assertEquals(capturedSwapInstruction1.getToken1Amount(), new BigDecimal("404.246081578000000000"));
        assertEquals(capturedSwapInstruction1.getPostAmount(), new BigDecimal("0"));

        SwapInstructionExtraInfo capturedSwapInstruction2 = capturedSwapInstructions.get(1);
        assertEquals(capturedSwapInstruction2.getToken0(), sellToken);
        assertEquals(capturedSwapInstruction2.getToken1(), buyToken);
        assertEquals(capturedSwapInstruction2.getToken0Amount(), new BigDecimal("15.233630832000000000"));
        assertEquals(capturedSwapInstruction2.getToken1Amount(), new BigDecimal("404.246081578000000000"));
        assertEquals(capturedSwapInstruction2.getPostAmount(), new BigDecimal("404.246081578"));
    }

}
