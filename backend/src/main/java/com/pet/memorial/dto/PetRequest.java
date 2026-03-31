package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PetRequest {

    @NotBlank(message = "宠物名称不能为空")
    @Size(max = 100, message = "宠物名称长度不能超过100")
    private String name;

    @Size(max = 100, message = "物种长度不能超过100")
    private String species;

    @Size(max = 100, message = "品种长度不能超过100")
    private String breed;

    @Size(max = 20, message = "性别长度不能超过20")
    private String gender;

    private LocalDate birthDate;

    private LocalDate memorialDate;

    @Size(max = 500, message = "头像地址长度不能超过500")
    private String avatarUrl;

    @Size(max = 2000, message = "简介长度不能超过2000")
    private String description;

    private Boolean isPublic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getMemorialDate() {
        return memorialDate;
    }

    public void setMemorialDate(LocalDate memorialDate) {
        this.memorialDate = memorialDate;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}
