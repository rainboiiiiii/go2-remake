package com.go2super.database.entity;

import com.go2super.database.entity.sub.CorpHistory;
import com.go2super.database.entity.sub.CorpMembers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document(collection = "game_corps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Corp {

    @Id
    private ObjectId id;

    private int corpId;
    private int contribution;
    private int exp;
    private int icon;

    private String name;
    private String philosophy;
    private String bulletin;

    private int planets;
    private int mall;
    private int merging;
    private int warehouse;

    private int fees;
    private Date lastPirates;

    private CorpHistory history;
    private CorpMembers members;

}
