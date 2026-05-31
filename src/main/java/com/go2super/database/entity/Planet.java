package com.go2super.database.entity;

import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;

@Document(collection = "game_planets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planet {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private long userId;

    private PlanetType type;
    private GalaxyTile position;

}
