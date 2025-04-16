package com.creda.coin.price;

import com.creda.coin.price.dto.ProfitAnalyzerDTO;

/**
 * @author gavin
 * @date 2024/10/15
 **/
public class ProfitAnalyzerContext {
    public static ThreadLocal<ProfitAnalyzerDTO> profitAnalyzerDTOThreadLocal = new ThreadLocal<>();

    public static ProfitAnalyzerDTO getProfitAnalyzerDTO() {
        return profitAnalyzerDTOThreadLocal.get();
    }

    public static void setProfitAnalyzerDTO(ProfitAnalyzerDTO profitAnalyzerDTO) {
        profitAnalyzerDTOThreadLocal.set(profitAnalyzerDTO);
    }

    public static void remove() {
        profitAnalyzerDTOThreadLocal.remove();
    }

}
