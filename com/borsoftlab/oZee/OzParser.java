package com.borsoftlab.oZee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [12];
    int pc = 0;

    public OzParser(final OzScanner scanner){
        this.scanner = scanner;
        // nextLexeme();
    }
    
    public void compile(){
        pc = 0;
        nextLexeme();
        while(aheadLexeme != 0 ){
            nextLexeme();
        }
    }

    private void nextLexeme(){
        aheadLexeme = scanner.nextLexeme();
    }

    public byte[] getExecMemModule(){
        int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntValue(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }
}