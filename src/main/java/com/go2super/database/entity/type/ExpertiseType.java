package com.go2super.database.entity.type;

public enum ExpertiseType {
    S,
    A,
    B,
    C,
    D
    ;

    public static ExpertiseType getLiteral(char c) {
        for(ExpertiseType type : ExpertiseType.values())
            if(String.valueOf(c).equalsIgnoreCase(type.name()))
                return type;
        return ExpertiseType.D;
    }

}
