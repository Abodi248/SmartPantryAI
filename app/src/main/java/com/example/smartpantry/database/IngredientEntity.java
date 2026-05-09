package com.example.smartpantry.database;

// do later: add @Entity(tableName = "ingredients") and @PrimaryKey(autoGenerate = true)
public class IngredientEntity {

    public long id;
    public String name;
    public String quantity;
    public String unit;
}
