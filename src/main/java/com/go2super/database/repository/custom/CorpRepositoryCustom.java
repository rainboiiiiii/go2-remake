package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;

import java.util.List;

public interface CorpRepositoryCustom {

    List<Corp> findByPower(int page, int max);

}