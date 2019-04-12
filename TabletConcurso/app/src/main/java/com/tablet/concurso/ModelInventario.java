package com.tablet.concurso;

public class ModelInventario {

    String versionName;
    String versionNumber;
    int image; // drawable reference id
    int colorText;

    public ModelInventario(String vName, String vNumber, int image, int colorText)
    {
        this.versionName = vName;
        this.versionNumber = vNumber;
        this.image = image;
        this.colorText = colorText;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getColorText() {
        return colorText;
    }

    public void setColorText(int colorText) {
        this.colorText = colorText;
    }
}
