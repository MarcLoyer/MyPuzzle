package com.oduratereptile.game;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by Marc on 10/3/2017.
 */

public class MyPacker {
    public static void main (String[] args) throws Exception {
        //TODO: delete existing atlas files first
        TexturePacker.process("../../AssetDevelopment/MyPuzzle/PackImages", "android/assets", "atlas");
    }
}
