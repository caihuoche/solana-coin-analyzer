package com.creda.coin.price.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.creda.coin.price.dto.SwapInstructionExtraInfo;
import com.creda.coin.price.entity.es.Instruction;
import com.creda.coin.price.entity.es.SolanaTransaction;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransferService {
    @Resource
    private IVaultTokenService vaultTokenService;
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;

    String TRANSFER_TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
    String TRANSFER_TOKEN_PROGRAM_ID_2022 = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb";
    String ASSOCIATED_TOKEN_ACCOUNT_PROGRAM = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL";

    List<String> TRANSFER_PROGRAMS = Arrays.asList(TRANSFER_TOKEN_PROGRAM_ID, TRANSFER_TOKEN_PROGRAM_ID_2022);

    String GRASS_AIRDROP_CLAIMING = "Eohp5jrnGQgP74oD7ij9EuCSYnQDLLHgsuAmtSTuxABk";
    String JUPITER_AGGREGATOR_V6 = "JUP6LkbZbjS1jKKwapdHNy74zcZ3tLUZoi5QNyVTaV4";
    String RAYDIUM_AMM_ROUTING = "routeUGWgWzqBWFcrCfv8tritsqukccJPu3q5GPP3xS";
    String RAYDIUM_LIQUIDITY_POOL_V4 = "675kPX9MHTjS2zt1qfr1NYHuzeLXfQM9H24wFSUt1Mp8";
    String RAYDIUM_LIQUIDITY_POOL_AMM_STABLE = "5quBtoiQqxF9Jv6KYKctB59NT3gtJD2Y65kdnB1Uev3h";
    String RAYDIUM_CPMM = "CPMMoo8L3F4NbTegBCKVNunggL7H1ZpdTHKxQB5qKP1C";
    String RAYDIUM_CLMM = "CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK";

    String WSOL_ADDRESS = "So11111111111111111111111111111111111111112";

    List<String> FILTERED_PROGRAMS = Arrays.asList(JUPITER_AGGREGATOR_V6, GRASS_AIRDROP_CLAIMING, RAYDIUM_AMM_ROUTING, RAYDIUM_LIQUIDITY_POOL_V4, RAYDIUM_CPMM, RAYDIUM_LIQUIDITY_POOL_AMM_STABLE, RAYDIUM_CLMM);

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

    private String getTransferTokenAddress(Instruction instruction) {
        Object parsed = instruction.getParsed();
        String tokenAddress = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("mint") instanceof String) {
                    tokenAddress = info.get("mint").toString();
                }
            }
        }
        return tokenAddress;
    }

    private String getTransferFromAccount(Instruction instruction) {
        Object parsed = instruction.getParsed();
        String fromAccount = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("multisigAuthority") instanceof String) {
                    fromAccount = info.get("multisigAuthority").toString();
                } else if(info.get("authority") instanceof String) {
                    fromAccount = info.get("authority").toString();
                }
            }
        }
        return fromAccount;
    }

    private String getTransferToAccountOwner(Instruction instruction) {
        Object parsed = instruction.getParsed();
        String toAccount = null;
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("info") instanceof LinkedHashMap) {
                LinkedHashMap info = (LinkedHashMap) parsedMap.get("info");
                if (info.get("destination") instanceof String) {
                    toAccount = info.get("destination").toString();
                }
            }
        }
        return toAccount;
    }

    private boolean isTransferInstruction(Instruction instruction) {
        if (!(instruction.getProgramId().equalsIgnoreCase(TRANSFER_TOKEN_PROGRAM_ID) || instruction.getProgramId().equalsIgnoreCase(TRANSFER_TOKEN_PROGRAM_ID_2022))) {
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

    private String getInstructionType(Instruction instruction) {
        String instructionType = null;
        Object parsed = instruction.getParsed();
        if (parsed instanceof LinkedHashMap) {
            LinkedHashMap parsedMap = (LinkedHashMap) parsed;
            if (parsedMap.get("type") instanceof String) {
                instructionType = parsedMap.get("type").toString();
            }
        }
        return instructionType;
    }

    private void processRealTransfer(
            SolanaTransaction transaction,
            String tokenAddress,
            String fromAccount,
            String toAccountOwner
    ) {
        long startTime = System.currentTimeMillis();
        try {
            Optional<SolanaTransaction.Meta.TokenBalance> transferOutPostTokenBalanceOpt = transaction.getMeta().getPostTokenBalances().stream()
                    .filter(item -> item.getOwner().equalsIgnoreCase(fromAccount) && item.getMint().equalsIgnoreCase(tokenAddress))
                    .findFirst();
            BigDecimal transferOutPostAmount = transferOutPostTokenBalanceOpt.isPresent()
                    ? new BigDecimal(transferOutPostTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);
            Optional<SolanaTransaction.Meta.TokenBalance> transferOutPreTokenBalanceOpt = transaction.getMeta().getPreTokenBalances().stream()
                    .filter(item -> item.getOwner().equalsIgnoreCase(fromAccount) && item.getMint().equalsIgnoreCase(tokenAddress))
                    .findFirst();
            BigDecimal transferOutPreAmount = transferOutPreTokenBalanceOpt.isPresent()
                    ? new BigDecimal(transferOutPreTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);

            SwapInstructionExtraInfo transferOutExtraInfo = new SwapInstructionExtraInfo();
            transferOutExtraInfo.setPostAmount(transferOutPostAmount);

            BigDecimal transferOutAmountDecimal = transferOutPostAmount.subtract(transferOutPreAmount).abs();
            analyzerDataHandler.transferOutErc20(transaction, fromAccount, tokenAddress, transferOutAmountDecimal, transferOutExtraInfo);

            Optional<SolanaTransaction.Meta.TokenBalance> transferInPostTokenBalanceOpt = transaction.getMeta().getPostTokenBalances().stream()
                    .filter(item -> !item.getOwner().equalsIgnoreCase(fromAccount) && item.getMint().equalsIgnoreCase(tokenAddress))
                    .findFirst();
            BigDecimal transferInPostAmount = transferInPostTokenBalanceOpt.isPresent()
                    ? new BigDecimal(transferInPostTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);
            Optional<SolanaTransaction.Meta.TokenBalance> transferInPreTokenBalanceOpt = transaction.getMeta().getPreTokenBalances().stream()
                    .filter(item -> !item.getOwner().equalsIgnoreCase(fromAccount) && item.getMint().equalsIgnoreCase(tokenAddress))
                    .findFirst();
            BigDecimal transferInPreAmount = transferInPreTokenBalanceOpt.isPresent()
                    ? new BigDecimal(transferInPreTokenBalanceOpt.get().getUiTokenAmount().getUiAmountString())
                    : new BigDecimal(0);

            String finalToAddress = transferInPostTokenBalanceOpt.isPresent() ? transferInPostTokenBalanceOpt.get().getOwner() : toAccountOwner;
            SwapInstructionExtraInfo transferInExtraInfo = new SwapInstructionExtraInfo();
            transferInExtraInfo.setPostAmount(transferInPostAmount);

            BigDecimal transferInAmountDecimal = transferInPostAmount.subtract(transferInPreAmount).abs();
            analyzerDataHandler.transferInErc20(transaction, finalToAddress, tokenAddress, transferInAmountDecimal, transferInExtraInfo);
        } catch (Exception e) {
            log.error("processRealTransfer error: {}", e);
        } finally {
            if (System.currentTimeMillis() - startTime > 25) {
                log.info("processRealTransfer time: {}", System.currentTimeMillis() - startTime);
            }
        }
    }

    public void processOnlyOneTransfer(List<Instruction> instructions, int instructionIndex, String toAccountOwner, SolanaTransaction transaction) {
        long start = System.currentTimeMillis();
        Instruction currentTransferInstruction = instructions.get(instructionIndex);

        // Get transfer details
        String tokenAddress = getTransferTokenAddress(currentTransferInstruction);
        String transferAmount = getTransferAmount(currentTransferInstruction);
        String fromAccount = getTransferFromAccount(currentTransferInstruction);

        // If transfer amount is 0, nothing to process
        if ("0".equals(transferAmount)) {
            logProcessTime(start);
            return;
        }

        List<SolanaTransaction.Meta.TokenBalance> postTokenBalances = transaction.getMeta().getPostTokenBalances();

        // If token address is null, try to infer it from post-transfer balances
        if (tokenAddress == null) {
            if (postTokenBalances.size() == 1 || postTokenBalances.size() == 2) {
                tokenAddress = postTokenBalances.get(0).getMint(); // Use inferred token address
                processRealTransfer(transaction, tokenAddress, fromAccount, toAccountOwner);
            } else {
                log.warn("Transaction {} has more than 2 transfer post token balances, unable to handle.", transaction.getHash());
            }
            logProcessTime(start);
            return;
        }

        // Handle special case for WSOL_ADDRESS
        if (WSOL_ADDRESS.equals(tokenAddress)) {
            // If the previous instruction involves an initializeAccount or initializeAccount3, skip processing
            // Get the previous instruction for further processing
            if (instructionIndex >= 1) {
                Instruction preInstruction = instructions.get(instructionIndex - 1);
                String preInstructionType = getInstructionType(preInstruction);
                String preInstructionProgramId = preInstruction.getProgramId();

                if (TRANSFER_PROGRAMS.contains(preInstructionProgramId)) {
                    if ("initializeAccount".equals(preInstructionType) || "initializeAccount3".equals(preInstructionType)) {
                        logProcessTime(start);
                        return;
                    }
                }

                // Check if the program ID matches the "create" or "createIdempotent" case for the associated token program
                if (ASSOCIATED_TOKEN_ACCOUNT_PROGRAM.equals(preInstructionProgramId)) {
                    if ("create".equals(preInstructionType) || "createIdempotent".equals(preInstructionType)) {
                        logProcessTime(start);
                        return;
                    }
                }
            }
        }

        // Process transfer if conditions are met
        if (postTokenBalances.size() == 1 || postTokenBalances.size() == 2) {
            processRealTransfer(transaction, tokenAddress, fromAccount, toAccountOwner);
        } else {
            log.warn("Transaction {} has more than 2 transfer post token balances, unable to process.", transaction.getHash());
        }

        logProcessTime(start);
    }

    private void logProcessTime(long start) {
        long duration = System.currentTimeMillis() - start;
        if (duration > 30) {
            log.info("Transfer time: {} ms", duration);
        }
    }

    public static Map<String, BigDecimal> calculateBalanceDiff(
            Map<String, BigDecimal> preTokenBalanceMap,
            Map<String, BigDecimal> postTokenBalancesMap
    ) {
        Map<String, BigDecimal> diffBalanceMap = new HashMap<>();

        postTokenBalancesMap.forEach((owner, postBalance) -> {
            BigDecimal preBalance = preTokenBalanceMap.getOrDefault(owner, BigDecimal.ZERO);
            BigDecimal diff = postBalance.subtract(preBalance);
            diffBalanceMap.put(owner, diff);
        });

        return diffBalanceMap;
    }

    public boolean areAllMintsSame(List<SolanaTransaction.Meta.TokenBalance> tokenBalances) {
        if (tokenBalances == null || tokenBalances.isEmpty()) {
            return true;  // 空列表视为所有 mint 相同
        }

        String firstMint = tokenBalances.get(0).getMint();  // 获取第一个 mint 值

        // 使用 Stream API 检查所有的 mint 值是否都与第一个 mint 值相同
        return tokenBalances.stream()
            .allMatch(balance -> balance.getMint().equals(firstMint));
    }

    public void processTransfer(SolanaTransaction transaction) {
        try {
            List<Instruction> instructions = transaction.getInstructions();

            List<Integer> potentialTransferInstructions = IntStream.range(0, instructions.size())
                .filter(index -> {
                    Instruction currentTransferInstruction = instructions.get(index);
                    return isTransferInstruction(currentTransferInstruction);
                })
                .boxed()
                .collect(Collectors.toList());

            if (CollectionUtil.isEmpty(potentialTransferInstructions)) {
                // log.warn("Transaction {} has no transfer instructions", transaction.getHash());
                return;
            }

            // 如果包含以下合约，那么直接filter掉
            boolean containsNonTransferPrograms = transaction.getAccountKeys().stream().anyMatch(item -> FILTERED_PROGRAMS.contains(item));
            if (containsNonTransferPrograms) {
                log.warn("Transaction {} has some non-transfer instructions", transaction.getHash());
                return;
            }

            Instruction lastInstruction = instructions.get(potentialTransferInstructions.get(potentialTransferInstructions.size() - 1));
            String toAccountOwner = getTransferToAccountOwner(lastInstruction);
            if (potentialTransferInstructions.size() == 1) {
                processOnlyOneTransfer(instructions, potentialTransferInstructions.get(0), toAccountOwner, transaction);
            }

            if (potentialTransferInstructions.size() > 1) {
                // 如果全部都是transfer 并且 transfer的length + 1 = postTokenBalances, 并且postTokenBalances = pretokenbalances.
                long start = System.currentTimeMillis();
                if (instructions.size() == potentialTransferInstructions.size()) {
                    List<SolanaTransaction.Meta.TokenBalance> preTokenBalances = transaction.getMeta().getPreTokenBalances();
                    List<SolanaTransaction.Meta.TokenBalance> postTokenBalances = transaction.getMeta().getPostTokenBalances();

                    // preTokenBalances,postTokenBalances 都是一个tokenaddress
                    if (
                        potentialTransferInstructions.size() + 1 == preTokenBalances.size() &&
                        preTokenBalances.size() == postTokenBalances.size() &&
                        areAllMintsSame(postTokenBalances) &&
                        areAllMintsSame(preTokenBalances)
                    ) {
                        // from所有的transfer是一样的。
                        // 部分一样，有这种吗？
                        // from全部不一样 5W66jb3HHA5TNXMCs4sTkj8m55s2E31WemYyJCRVS7X1cZ6UiqfsEWfRQZXpysXREbDtFf97XrMs9tMgvkAV4AMY
                        String tokenAddress = postTokenBalances.get(0).getMint();
                        Map<String, BigDecimal> preTokenBalanceMap = preTokenBalances.stream()
                            .collect(Collectors.toMap(
                                item -> item.getOwner(),
                                item -> new BigDecimal(item.getUiTokenAmount().getUiAmountString())
                            ));
                        Map<String, BigDecimal> postTokenBalanceMap = postTokenBalances.stream()
                            .collect(Collectors.toMap(
                                item -> item.getOwner(),
                                item -> new BigDecimal(item.getUiTokenAmount().getUiAmountString())
                            ));
                        Map<String, BigDecimal> tokenBalancesDiff = calculateBalanceDiff(preTokenBalanceMap, postTokenBalanceMap);

                        tokenBalancesDiff.forEach((owner, diffBalance) -> {
                            BigDecimal postBalance = postTokenBalanceMap.get(owner);
                            if (diffBalance.compareTo(BigDecimal.ZERO) < 0) {
                                SwapInstructionExtraInfo transferOutExtraInfo = new SwapInstructionExtraInfo();
                                transferOutExtraInfo.setPostAmount(postBalance);
                                analyzerDataHandler.transferOutErc20(transaction, owner, tokenAddress, diffBalance.abs(), transferOutExtraInfo);
                            } else if (diffBalance.compareTo(BigDecimal.ZERO) > 0) {
                                SwapInstructionExtraInfo transferInExtraInfo = new SwapInstructionExtraInfo();
                                transferInExtraInfo.setPostAmount(postBalance);
                                analyzerDataHandler.transferInErc20(transaction, owner, tokenAddress, diffBalance, transferInExtraInfo);
                            }
                        });
                    }
                    logProcessTime(start);
                } else {
                    log.warn("Transaction {} has more than 1 transfers, don't know how to handle", transaction.getHash());
                }
            }
        }catch (Exception e) {
            log.error("processTransfer error: {}", e);
        }

    }
}
