package Constant;

public class DopplerEffectConstant {
    public static final int CheckAreaStart = Constant.SymbolAreaStartFreq + Constant.SymbolAreaWidth*Constant.SymbolNum;
    public static final int CheckWave = DopplerEffectConstant.CheckAreaStart + Constant.SymbolAreaWidth/2; // 22050
    public static final int CheckAreaEnd = DopplerEffectConstant.CheckAreaStart + Constant.SymbolAreaWidth;
    public static final int AllowDifferenceABS= 25;
}
