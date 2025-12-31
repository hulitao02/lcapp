package com.cloud.backend.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CountryDict implements Serializable {

    private static final long serialVersionUID = 6321033274552469125L;
    private Long id;
    private String name;
    private String code;
}
