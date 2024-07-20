package com.diplom.faces_recognition.entity.netmodel;

import java.io.Serializable;

public class NetFrameModel implements INetFrame, Serializable {
    private String windowName;
    private byte[] bytesImage;

    public NetFrameModel(String windowName, byte[] bytesImage) {
        this.windowName = windowName;
        this.bytesImage = bytesImage;
    }

    @Override
    public String getWindowName() {
        return windowName;
    }

    @Override
    public byte[] getBytesImage() {
        return bytesImage;
    }
}
