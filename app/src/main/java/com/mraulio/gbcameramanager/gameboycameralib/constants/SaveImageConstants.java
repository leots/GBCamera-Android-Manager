package com.mraulio.gbcameramanager.gameboycameralib.constants;

/**
 * Modified from https://github.com/KodeMunkie/gameboycameralib
 */
public final class SaveImageConstants {

    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 112;
    public static final int SMALL_IMAGE_WIDTH = 32;
    public static final int SMALL_IMAGE_HEIGHT = 32;
    public static final int IMAGE_START_LOCATION = 0x2000;
    public static final int NEXT_IMAGE_START_OFFSET = 0x1000;
    public static final int SMALL_IMAGE_START_OFFSET = 0xE00;
    public static final int SMALL_IMAGE_LENGTH = 0x100;
    public static final int IMAGE_LENGTH = 0xE00;
    public static final int MAX_SUPPORTED_IMAGES = 30;
    public static final int FACES_OFFSET = 0x11FC;


    public static final int PHOTO_METADATA_START_OFFSET= 0xC0;



    private SaveImageConstants(){}
}
