package com.creda.coin.price.third;

import org.bouncycastle.util.test.FixedSecureRandom.BigInteger;

import lombok.Data;

@Data
public class GetAccountInfoResponse {
    private String jsonrpc;
    private Result result;
    private Integer id;

    @Data
    public static class Result {
        private Context context;
        private Value value;

        @Data
        public static class Context {
            private String apiVersion;
            private String slot;
        }

        @Data
        public static class Value {
            private DataDetail data;
            private boolean executable;
            private BigInteger lamports;
            private String owner;
            private BigInteger rentEpoch;
            private Integer space;

            @Data
            public static class DataDetail {
                private ParsedData parsed;
                private String program;
                private Integer space;

                @Data
                public static class ParsedData {
                    private ParsedDataInfo info;
                    private String type;

                    @Data
                    public static class ParsedDataInfo {
                        private boolean isNative;
                        private String mint;
                        private String owner;
                        private String state;
                        private TokenAmount tokenAmount;

                        @Data
                        public static class TokenAmount {
                            private String amount;
                            private Integer decimals;
                            private Float uiAmount;
                            private String uiAmountString;
                        }
                    }
                }
            }
        }
    }
}
