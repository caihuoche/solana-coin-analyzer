package com.creda.coin.price.test;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// 定义 ParsedInfo 类
class ParsedInfo {
    @JsonProperty("type")
    private String type;

    @JsonProperty("info")
    private Object info;

    // 构造函数
    public ParsedInfo(String type, Info info) {
        this.type = type;
        this.info = info;
    }

    public ParsedInfo(String type, Object info) {
        this.type = type;
        this.info =info;
    }

    // Getter 和 Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }
}

// 定义 Info 类
class Info {
    @JsonProperty("mint")
    private String mint;

    @JsonProperty("lamports")
    private Long lamports;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("newAccount")
    private String newAccount;

    @JsonProperty("source")
    private String source;

    @JsonProperty("space")
    private Integer space;

    @JsonProperty("account")
    private String account;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("authority")
    private String authority;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("instructionType")
    private String instructionType; // 为了兼容不同类型的指令

    // 构造函数

    public Info(Long lamports, String owner, String newAccount, String source, Integer space) {
        this.lamports = lamports;
        this.owner = owner;
        this.newAccount = newAccount;
        this.source = source;
        this.space = space;
    }


    public Info(String amount, String authority, String destination) {
        this.amount = amount;
        this.authority = authority;
        this.destination = destination;
    }

    // Getter 和 Setter
    public String getMint() {
        return mint;
    }

    public void setMint(String mint) {
        this.mint = mint;
    }

    public Long getLamports() {
        return lamports;
    }

    public void setLamports(Long lamports) {
        this.lamports = lamports;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getNewAccount() {
        return newAccount;
    }

    public void setNewAccount(String newAccount) {
        this.newAccount = newAccount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getSpace() {
        return space;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}

// 定义 Instruction 类
class Instruction {
    @JsonProperty("parsed")
    private ParsedInfo parsed;

    @JsonProperty("program")
    private String program;

    @JsonProperty("programId")
    private String programId;

    // 构造函数
    public Instruction(ParsedInfo parsed, String program, String programId) {
        this.parsed = parsed;
        this.program = program;
        this.programId = programId;
    }

    // Getter 和 Setter
    public ParsedInfo getParsed() {
        return parsed;
    }

    public void setParsed(ParsedInfo parsed) {
        this.parsed = parsed;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }
}

// 定义 InnerInstruction 类
class InnerInstruction {
    @JsonProperty("instructions")
    private List<Instruction> instructions;

    @JsonProperty("index")
    private int index;

    // 构造函数
    public InnerInstruction(List<Instruction> instructions, int index) {
        this.instructions = instructions;
        this.index = index;
    }

    // Getter 和 Setter
    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
