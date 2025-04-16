package com.creda.coin.price.service;

import cn.hutool.core.codec.Base58;
import cn.hutool.core.collection.CollectionUtil;
import com.creda.coin.price.ProfitAnalyzerContext;
import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.AssetInfo;
import com.creda.coin.price.entity.RaydiumSwapPool;
import com.creda.coin.price.entity.VaultToken;
import com.creda.coin.price.entity.es.Instruction;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.jdbc.IAssetInfoService;
import com.creda.coin.price.service.data.jdbc.IRaydiumSwapPoolService;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;
import com.creda.coin.price.service.data.onchaininstruction.DiscriminatorOnlyInstruction;
import com.creda.coin.price.third.GetAccountInfoResponse;
import com.creda.coin.price.third.SolanaApiService;
import com.syntifi.near.borshj.Borsh;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author gavin
 * @date 2024/10/15
 **/
@Service
@Slf4j
public class RaydiumSwapService {
    @Autowired
    private SolanaApiService solanaApiService;
    @Resource
    private IVaultTokenService vaultTokenService;
    @Resource
    private IAssetInfoService assetInfoService;
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;

    @Resource
    private IRaydiumSwapPoolService RaydiumSwapPoolService;
    String RAYDIUM_AMM_ROUTING = "routeUGWgWzqBWFcrCfv8tritsqukccJPu3q5GPP3xS";
    String RAYDIUM_LIQUIDITY_POOL_V4 = "675kPX9MHTjS2zt1qfr1NYHuzeLXfQM9H24wFSUt1Mp8";
    String RAYDIUM_LIQUIDITY_POOL_AMM_STABLE = "5quBtoiQqxF9Jv6KYKctB59NT3gtJD2Y65kdnB1Uev3h";
    String RAYDIUM_CPMM = "CPMMoo8L3F4NbTegBCKVNunggL7H1ZpdTHKxQB5qKP1C";
    String RAYDIUM_CLMM = "CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK";

    List<String> RAYDIUM_SWAP_PROGRAMS = Arrays.asList(RAYDIUM_AMM_ROUTING, RAYDIUM_LIQUIDITY_POOL_V4, RAYDIUM_CPMM, RAYDIUM_LIQUIDITY_POOL_AMM_STABLE, RAYDIUM_CLMM);
    private String getTransferAuthority(Instruction instruction) {
        Object parsed = instruction.getParsed();

        String authority = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("authority") instanceof String) {
                    authority = info.get("authority").toString();
                }
            }
        }
        return authority;
    }

    private String getTransferDestination(Instruction instruction) {
        Object parsed = instruction.getParsed();

        String destination = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("destination") instanceof String) {
                    destination = info.get("destination").toString();
                }
            }
        }
        return destination;
    }

    private String getTransferSource(Instruction instruction) {
        Object parsed = instruction.getParsed();

        String source = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("source") instanceof String) {
                    source = info.get("source").toString();
                }
            }
        }
        return source;
    }

    public String getTransferAmount(Instruction instruction) {
        Object parsed = instruction.getParsed();

        String amount = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("amount") instanceof String) {
                    amount = info.get("amount").toString();
                } else {
                    if (info.get("tokenAmount") instanceof LinkedHashMap) {
                        LinkedHashMap tokenAmount = (LinkedHashMap) info.get("tokenAmount");
                        if (tokenAmount.get("amount") instanceof String) {
                            amount = tokenAmount.get("amount").toString();
                        }
                    }
                }
            }
        }
        return amount;
    }

    private boolean isTransferInstruction(Instruction instruction) {
        String transferTokenProgramId = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
        String transferTokenProgramId2022 = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb";
        if (!(instruction.getProgramId().equalsIgnoreCase(transferTokenProgramId) || instruction.getProgramId().equalsIgnoreCase(transferTokenProgramId2022))) {
            return false;
        }
        Object parsed = instruction.getParsed();
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("type") instanceof String) {
                return parsedMap.get("type").equals("transfer") || parsedMap.get("type").equals("transferChecked");
            }
        }
        return false;
    }

    // > 2才会group
    public List<List<Instruction>> groupSwapInstructions(List<Instruction> instructions) {
        List<List<Instruction>> groupedInstructions = new ArrayList<>();
        int i = 0;
        while (i < instructions.size()) {
            Instruction currentInstruction = instructions.get(i);
            if (!isTransferInstruction(currentInstruction)) {
                if (i + 2 < instructions.size() && isTransferInstruction(instructions.get(i + 1)) && isTransferInstruction(instructions.get(i + 2))) {
                    List<Instruction> group = new ArrayList<>();
                    group.add(currentInstruction);
                    group.add(instructions.get(i + 1));
                    group.add(instructions.get(i + 2));
                    groupedInstructions.add(group);
                    i += 3;
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        return groupedInstructions;
    }

    private VaultToken getVaultTokenFrom3rdParty(String tokenVault) {
        GetAccountInfoResponse response = solanaApiService.getAccountInfo(tokenVault);
        String mintToken = response.getResult().getValue().getData().getParsed().getInfo().getMint();
        int mintDecimals = response.getResult().getValue().getData().getParsed().getInfo().getTokenAmount().getDecimals();

        VaultToken vaultToken = new VaultToken();
        vaultToken.setVaultTokenAddress(tokenVault);
        vaultToken.setMintTokenAddress(mintToken);
        vaultToken.setMintTokenDecimals(mintDecimals);
        return vaultToken;
    }

    private void saveAssetInfos(String inputMintToken, Integer inputMintDecimals, String outputMintToken, Integer outputMintDecimals) {
        AssetInfo inputAssetInfo = new AssetInfo();
        inputAssetInfo.setAddress(inputMintToken);
        inputAssetInfo.setDecimals(inputMintDecimals);

        AssetInfo outputAssetInfo = new AssetInfo();
        outputAssetInfo.setAddress(outputMintToken);
        outputAssetInfo.setDecimals(outputMintDecimals);

        ProfitAnalyzerContext.getProfitAnalyzerDTO().getAssetInfos().add(inputAssetInfo);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getAssetInfos().add(outputAssetInfo);
    }

    private void processSwap(
            Instruction inputTransferInstruction,
            Instruction outputTransferInstruction,
            SolanaTransaction transaction,
            Integer parentInstructionIndex,
            Integer innerInstructionIndex
    ) {
        long start = System.currentTimeMillis();
        try {
            String swapper = getTransferAuthority(inputTransferInstruction);

            String inputVault = getTransferDestination(inputTransferInstruction);
            String outputVault = getTransferSource(outputTransferInstruction);
            boolean vaultTokensPreFinish = ProfitAnalyzerContext.getProfitAnalyzerDTO().isVaultTokensPreFinish();
           // 预处理未完成
            if (!vaultTokensPreFinish){
                processSwapPre(inputTransferInstruction, outputTransferInstruction, transaction, parentInstructionIndex, innerInstructionIndex);
                return;
            }
            long l = System.currentTimeMillis();
            Map<String, VaultToken> vaultToMintTokenMap = vaultTokenService.findByVaultAddresses(Arrays.asList(inputVault, outputVault));
            if (System.currentTimeMillis() - l > 30){
                log.info("findByVaultAddresses time: {} ms", System.currentTimeMillis() - l);
            }
            String inputMintToken;
            Integer inputMintDecimals;
            String outputMintToken;
            Integer outputMintDecimals;
            List<VaultToken> vaultTokens = new ArrayList<VaultToken>();
            if (vaultToMintTokenMap.containsKey(inputVault) && vaultToMintTokenMap.containsKey(outputVault)) {
                inputMintToken = vaultToMintTokenMap.get(inputVault).getMintTokenAddress();
                inputMintDecimals = vaultToMintTokenMap.get(inputVault).getMintTokenDecimals();

                outputMintToken = vaultToMintTokenMap.get(outputVault).getMintTokenAddress();
                outputMintDecimals = vaultToMintTokenMap.get(outputVault).getMintTokenDecimals();
            } else if (vaultToMintTokenMap.containsKey(inputVault) && !vaultToMintTokenMap.containsKey(outputVault)) {
                inputMintToken = vaultToMintTokenMap.get(inputVault).getMintTokenAddress();
                inputMintDecimals = vaultToMintTokenMap.get(inputVault).getMintTokenDecimals();

                VaultToken outputVaultToken = getVaultTokenFrom3rdParty(outputVault);
                outputMintToken = outputVaultToken.getMintTokenAddress();
                outputMintDecimals = outputVaultToken.getMintTokenDecimals();
                vaultTokens.add(outputVaultToken);
            } else if (!vaultToMintTokenMap.containsKey(inputVault) && vaultToMintTokenMap.containsKey(outputVault)) {
                VaultToken inputVaultToken = getVaultTokenFrom3rdParty(inputVault);
                inputMintToken = inputVaultToken.getMintTokenAddress();
                inputMintDecimals = inputVaultToken.getMintTokenDecimals();
                vaultTokens.add(inputVaultToken);

                outputMintToken = vaultToMintTokenMap.get(outputVault).getMintTokenAddress();
                outputMintDecimals = vaultToMintTokenMap.get(outputVault).getMintTokenDecimals();
            } else {
                CompletableFuture<VaultToken> inputFuture = CompletableFuture.supplyAsync(() -> getVaultTokenFrom3rdParty(inputVault));
                CompletableFuture<VaultToken> outputFuture = CompletableFuture.supplyAsync(() -> getVaultTokenFrom3rdParty(outputVault));
                CompletableFuture<Void> allOf = CompletableFuture.allOf(inputFuture, outputFuture);
                allOf.join();
                VaultToken inputVaultToken = inputFuture.get();
                VaultToken outputVaultToken = outputFuture.get();

                inputMintToken = inputVaultToken.getMintTokenAddress();
                inputMintDecimals = inputVaultToken.getMintTokenDecimals();

                outputMintToken = outputVaultToken.getMintTokenAddress();
                outputMintDecimals = outputVaultToken.getMintTokenDecimals();

                vaultTokens.add(inputVaultToken);
                vaultTokens.add(outputVaultToken);
            }

            if (vaultTokens.size() > 0) {
                long currentTimeMillis = System.currentTimeMillis();
                this.vaultTokenService.saveBatchStreamLoad(vaultTokens);
                if (System.currentTimeMillis() - currentTimeMillis > 30) {
                    log.info("saveBatch time:{}", System.currentTimeMillis() - currentTimeMillis);
                }
            }
            saveAssetInfos(inputMintToken, inputMintDecimals, outputMintToken, outputMintDecimals);

            if (inputMintToken != null && inputMintDecimals != null && outputMintToken != null && outputMintDecimals != null) {
                String inputAmountString = getTransferAmount(inputTransferInstruction);
                BigDecimal inputAmount = new BigDecimal(inputAmountString).divide(new BigDecimal(10).pow(inputMintDecimals), 18, RoundingMode.HALF_UP);

                String outputAmountString = getTransferAmount(outputTransferInstruction);
                BigDecimal outputAmount = new BigDecimal(outputAmountString).divide(new BigDecimal(10).pow(outputMintDecimals), 18, RoundingMode.HALF_UP);

                Optional<SolanaTransaction.Meta.TokenBalance> inputTokenPostTokenBalanceOpt = transaction.getMeta().getPostTokenBalances().stream()
                    .filter(item -> item.getOwner().equalsIgnoreCase(swapper) && item.getMint().equalsIgnoreCase(inputMintToken))
                    .findFirst();
                BigDecimal inputTokenPostAmount = inputTokenPostTokenBalanceOpt.isPresent()
                    ? new BigDecimal(inputTokenPostTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);

                Optional<SolanaTransaction.Meta.TokenBalance> outputTokenPostTokenBalanceOpt = transaction.getMeta().getPostTokenBalances().stream()
                    .filter(item -> item.getOwner().equalsIgnoreCase(swapper) && item.getMint().equalsIgnoreCase(outputMintToken))
                    .findFirst();
                BigDecimal outputTokenPostAmount = outputTokenPostTokenBalanceOpt.isPresent()
                    ? new BigDecimal(outputTokenPostTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);

                long startTime = System.currentTimeMillis();

                SwapInstructionExtraInfo inputExtraInfo = new SwapInstructionExtraInfo();
                inputExtraInfo.setToken0(inputMintToken);
                inputExtraInfo.setToken1(outputMintToken);
                inputExtraInfo.setToken0Amount(inputAmount);
                inputExtraInfo.setToken1Amount(outputAmount);
                inputExtraInfo.setParentInstructionIndex(parentInstructionIndex);
                inputExtraInfo.setInnerInstructionIndex(innerInstructionIndex);
                inputExtraInfo.setPostAmount(inputTokenPostAmount);
                analyzerDataHandler.sellErc20(transaction, swapper, inputMintToken, inputExtraInfo);
                
                SwapInstructionExtraInfo outputExtraInfo = new SwapInstructionExtraInfo();
                outputExtraInfo.setToken0(inputMintToken);
                outputExtraInfo.setToken1(outputMintToken);
                outputExtraInfo.setToken0Amount(inputAmount);
                outputExtraInfo.setToken1Amount(outputAmount);
                outputExtraInfo.setParentInstructionIndex(parentInstructionIndex);
                outputExtraInfo.setInnerInstructionIndex(innerInstructionIndex);
                outputExtraInfo.setPostAmount(outputTokenPostAmount);
                analyzerDataHandler.buyErc20(transaction, swapper, outputMintToken, outputExtraInfo);

                if (System.currentTimeMillis() - startTime>25) {
                    log.info("analyzerDataHandler buyErc20 time:{}", System.currentTimeMillis() - startTime);
                }

            } else {
                log.warn("Transaction {} is not handled cause not getting mint tokens by vaults", transaction.getHash());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Process swap get account info error {}", e);
        }finally {
            if ( System.currentTimeMillis() - start>25) {

                log.info("processsellErc20 buyErc20 time:{}", System.currentTimeMillis() - start);
            }
        }
    }
    private void processSwapPre(
            Instruction inputTransferInstruction,
            Instruction outputTransferInstruction,
            SolanaTransaction transaction,
            Integer parentInstructionIndex,
            Integer innerInstructionIndex
    ) {

        String inputVault = getTransferDestination(inputTransferInstruction);
        String outputVault = getTransferSource(outputTransferInstruction);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getVaultTokens().add(inputVault);
        ProfitAnalyzerContext.getProfitAnalyzerDTO().getVaultTokens().add(outputVault);

    }


    private void processInnerCall(
            SolanaTransaction transaction,
            Integer parentInstructionIndex
    ) {
        Optional<SolanaTransaction.Meta.InnerInstruction> innerInstructionOpt = transaction.getMeta().getInnerInstructions().stream()
                .filter(item -> Objects.equals(item.getIndex(), parentInstructionIndex))
                .findFirst();

        if (innerInstructionOpt.isPresent()) {
            List<Instruction> currentInstructions = innerInstructionOpt.get().getInstructions();
            List<List<Instruction>> groupedInstructions = groupSwapInstructions(currentInstructions);
            IntStream.range(0, groupedInstructions.size()).forEach(index -> {
				List<Instruction> groupedInstruction = groupedInstructions.get(index);
				if (groupedInstruction.size() == 3) {
					Instruction firstInstruction = groupedInstruction.get(0);
					String programId = firstInstruction.getProgramId();
					if (programId.equalsIgnoreCase(RAYDIUM_LIQUIDITY_POOL_V4) || programId.equalsIgnoreCase(RAYDIUM_LIQUIDITY_POOL_AMM_STABLE)) {
						RaydiumSwapPool entity = new RaydiumSwapPool();
						entity.setPairAddress(firstInstruction.getAccounts().get(1));
						entity.setProgramId(programId);
						entity.setType("Standard");
//						this.RaydiumSwapPoolService.saveOrUpdate(entity);
                        ProfitAnalyzerContext.getProfitAnalyzerDTO().getRaydiumSwapPools().add(entity);
					}
					processSwap(groupedInstruction.get(1), groupedInstruction.get(2), transaction, parentInstructionIndex, index);
				} else {
					log.warn("Transaction {} is not handled cause grouped inner instruction size is not 3", transaction.getHash());
				}
			});
        }
    }

    private void processRaydiumCLMMOuterCall(
            SolanaTransaction transaction,
            Integer instructionIndex,
            byte[] base58Bytes
    ) {
        String anchorDiscriminator = Base58.encode(Arrays.copyOfRange(base58Bytes, 0, 8));
        if (Arrays.asList("8CLkZYyRkFB", "icSNZP7U1uh", "Cd99ifFPnYT").contains(anchorDiscriminator)) {
            Optional<SolanaTransaction.Meta.InnerInstruction> innerInstructionOpt = transaction.getMeta().getInnerInstructions().stream()
                    .filter(item -> item.getIndex() == instructionIndex)
                    .findFirst();

            if (innerInstructionOpt.isPresent()) {
                List<Instruction> currentInnerInstructions = innerInstructionOpt.get().getInstructions();
                if (currentInnerInstructions.size() == 2) {
                    if (isTransferInstruction(currentInnerInstructions.get(0)) && isTransferInstruction(currentInnerInstructions.get(1))) {
                        processSwap(currentInnerInstructions.get(0), currentInnerInstructions.get(1), transaction, instructionIndex, 0);
                    } else {
                        log.warn("Transaction {} is not handled by RaydiumCLMM cause inner instructions are not transfer program", transaction.getHash());
                    }
                } else {
                    log.warn("Transaction {} is not handled by RaydiumCLMM cause inner instruction size is not 2", transaction.getHash());
                }
            }
        }
    }

    private void processRaydiumCPMMOuterCall(SolanaTransaction transaction, Integer instructionIndex, byte[] base58Bytes) {
        String anchorDiscriminator = Base58.encode(Arrays.copyOfRange(base58Bytes, 0, 8));
        if (Arrays.asList("R3VaXtR71em", "ALoo1W6nyL8").contains(anchorDiscriminator)) {
            Optional<SolanaTransaction.Meta.InnerInstruction> innerInstructionOpt = transaction.getMeta().getInnerInstructions().stream()
                    .filter(item -> item.getIndex() == instructionIndex)
                    .findFirst();

            if (innerInstructionOpt.isPresent()) {
                List<Instruction> currentInnerInstructions = innerInstructionOpt.get().getInstructions();
                if (currentInnerInstructions.size() == 2) {
                    if (isTransferInstruction(currentInnerInstructions.get(0)) && isTransferInstruction(currentInnerInstructions.get(1))) {
                        processSwap(currentInnerInstructions.get(0), currentInnerInstructions.get(1), transaction, instructionIndex, 0);
                    } else {
                        log.warn("Transaction {} is not handled by RaydiumCPMM cause inner instructions are not transfer program", transaction.getHash());
                    }
                } else {
                    log.warn("Transaction {} is not handled by RaydiumCPMM cause inner instruction size is not 2", transaction.getHash());
                }
            }
        }
    }

    private void processRaydiumAMMOuterCall(SolanaTransaction transaction, Integer instructionIndex, byte[] base58Bytes) {
        DiscriminatorOnlyInstruction decoded = Borsh.deserialize(base58Bytes, DiscriminatorOnlyInstruction.class);
        if (decoded.discriminator == 9 || decoded.discriminator == 11) {
            // save pool id to db
            // RaydiumSwapPool entity = new RaydiumSwapPool();
            // entity.setPairAddress();
            // this.RaydiumSwapPoolService.save(null);
            Optional<SolanaTransaction.Meta.InnerInstruction> innerInstructionOpt = transaction.getMeta().getInnerInstructions().stream()
                    .filter(item -> item.getIndex() == instructionIndex)
                    .findFirst();

            if (innerInstructionOpt.isPresent()) {
                List<Instruction> currentInnerInstructions = innerInstructionOpt.get().getInstructions();
                if (currentInnerInstructions.size() == 2) {
                    if (isTransferInstruction(currentInnerInstructions.get(0)) && isTransferInstruction(currentInnerInstructions.get(1))) {
                        processSwap(currentInnerInstructions.get(0), currentInnerInstructions.get(1), transaction, instructionIndex, 0);
                    } else {
                        log.warn("Transaction {} is not handled by RaydiumAMM cause inner instructions(2) are not transfer program", transaction.getHash());
                    }
                } else if (currentInnerInstructions.size() == 3 ) {
                    if (isTransferInstruction(currentInnerInstructions.get(1)) && isTransferInstruction(currentInnerInstructions.get(2))) {
                        processSwap(currentInnerInstructions.get(1), currentInnerInstructions.get(2), transaction, instructionIndex, 0);
                    } else {
                        log.warn("Transaction {} is not handled by RaydiumAMM cause inner instructions(3) are not transfer program", transaction.getHash());
                    }
                } else {
                    log.warn("Transaction {} is not handled by RaydiumAMM cause inner instruction size is not 2 or 3", transaction.getHash());
                }
            }
        }
    }

    public void processRaydiumSwap(SolanaTransaction transaction, AtomicInteger atomicInteger) {
        List<Instruction> instructions = transaction.getInstructions();
        List<Integer> potentialRaydiumSwapInstructions = IntStream.range(0, instructions.size())
                .filter(index -> {
                    Instruction item = instructions.get(index);
                    return RAYDIUM_SWAP_PROGRAMS.contains(item.getProgramId()) && CollectionUtil.isNotEmpty(item.getAccounts());
                })
                .boxed()
                .collect(Collectors.toList());

        long start = System.currentTimeMillis();
        // 过滤了instruction without accounts
        if (CollectionUtil.isNotEmpty(potentialRaydiumSwapInstructions)) {
            potentialRaydiumSwapInstructions.forEach(instructionIndex -> {
                try {
                    Instruction instruction = instructions.get(instructionIndex);
                    String programId = instruction.getProgramId();
                    long innerStart = System.currentTimeMillis();

                    byte[] base58Bytes = Base58.decode(instruction.getData());
                    // 检查是否属于Raydium CLMM
                    if (programId.equalsIgnoreCase(RAYDIUM_CLMM)) {
                        long time = System.currentTimeMillis() - innerStart;
                        if (time>30){
                            log.info("RAYDIUM_CLMM time: {}");
                        }
                        processRaydiumCLMMOuterCall(transaction, instructionIndex, base58Bytes);
                        return;
                    }

                    // 检查是否属于Raydium CPMM
                    if (programId.equalsIgnoreCase(RAYDIUM_CPMM)) {
                        processRaydiumCPMMOuterCall(transaction, instructionIndex, base58Bytes);
                        long time = System.currentTimeMillis() - innerStart;
                        if (time>30){
                            log.info("RAYDIUM_CPMM time: {}", System.currentTimeMillis() - innerStart);

                        }
                        return;
                    }

                    // 检查是否属于 RaydiumLiquidityPoolV4 或者RaydiumLiquidityPoolStable
                    if (programId.equalsIgnoreCase(RAYDIUM_LIQUIDITY_POOL_V4) || programId.equalsIgnoreCase(RAYDIUM_LIQUIDITY_POOL_AMM_STABLE)) {
                        atomicInteger.incrementAndGet();
                        RaydiumSwapPool entity = new RaydiumSwapPool();
                        entity.setPairAddress(instruction.getAccounts().get(1));
                        entity.setProgramId(programId);
                        entity.setType("Standard");
//                        this.RaydiumSwapPoolService.saveOrUpdate(entity);
                        ProfitAnalyzerContext.getProfitAnalyzerDTO().getRaydiumSwapPools().add(entity);
                        processRaydiumAMMOuterCall(transaction, instructionIndex, base58Bytes);

                        return;
                    }

                    // 如果是RaydiumAMMRouting, 那就直接往下处理, 他的inner instruction是大于2的
                    if (programId.equalsIgnoreCase(RAYDIUM_AMM_ROUTING)) {
                        processInnerCall(transaction, instructionIndex);
                        long time = System.currentTimeMillis() - innerStart;
                        if (time>30){
                            log.info("RAYDIUM_AMM_ROUTING time: {}", System.currentTimeMillis() - innerStart);
                        }
                        return;
                    }

                } catch (Exception e) {
                    log.error("Transaction {} is unhandled by raydium swap programs caused by error ", transaction.getHash(), e);
                    return;
                }
            });
            long time = System.currentTimeMillis() - start;
            if (time > 30) {
//                log.info("processRaydiumSwap time {} ", System.currentTimeMillis() - start);
            }
        }
    }

    public void batchSaveOrUpdate(List<RaydiumSwapPool> raydiumSwapPools) {

        if (CollectionUtil.isNotEmpty(raydiumSwapPools)) {
            List<RaydiumSwapPool> result = removeDuplicatesAndKeepLatest(raydiumSwapPools);
            this.RaydiumSwapPoolService.saveOrUpdateBatchStreamLoad(result);
        }
    }


    public List<RaydiumSwapPool> removeDuplicatesAndKeepLatest(List<RaydiumSwapPool> raydiumSwapPools) {
        // 使用 Collectors.toMap 按照 pairAddress 去重，保留后面的元素
        Map<String, RaydiumSwapPool> uniquePoolMap = raydiumSwapPools.stream()
                .collect(Collectors.toMap(
                        RaydiumSwapPool::getPairAddress, // 使用 pairAddress 作为 key
                        pool -> pool,                   // 直接使用池子对象作为值
                        (oldPool, newPool) -> newPool   // 如果有重复的 key，保留后面的值
                ));

        // 将去重后的结果转换回 List
        return uniquePoolMap.values().stream().collect(Collectors.toList());
    }
}
