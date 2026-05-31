package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.CorpIncidentType;

import java.util.Date;

public class CorpIncident {

    private CorpIncidentType type;
    private Date date;

    private String sourceName;
    private String objectName;

    private Long sourceUserId;
    private Long sourceObjectId;

    private Integer guid;
    private Integer extend;

}
