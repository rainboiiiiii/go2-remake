package com.go2super.database.entity.sub;

import java.util.ArrayList;
import java.util.List;

public class CorpMembers {

    private List<CorpMember> members = new ArrayList<>();

    public List<CorpMember> getMembers() {
        return members;
    }

    public void addMember(CorpMember member) {

        if(members == null)
            members = new ArrayList<>();

        members.add(member);

    }

}
