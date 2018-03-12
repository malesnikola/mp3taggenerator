package main.java.domain;

import javafx.beans.property.SimpleStringProperty;

public class StateImage {
    private final SimpleStringProperty favoriteImage = new SimpleStringProperty();

    public StateImage(String imagePath) {
        setFavoriteImage(imagePath);
    }

    public void setFavoriteImage(String favoriteImageFile) {
        favoriteImage.set(favoriteImageFile);
    }

    public String getFavoriteImage() {
        return favoriteImage.get();
    }
}
