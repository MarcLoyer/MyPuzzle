package com.oduratereptile.game;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by Marc on 10/3/2017.
 */

// TODO: add code to generate the skin
//    - need a font
//    - need one 9-patch for text buttons
//    - need a nice round button
//      - is there a way to add various icons onto a single round button?
//    - everything should be gray, so that everything can be tinted.

public class MyPacker {
    public static void main (String[] args) throws Exception {
        //TODO: delete existing atlas files first
        TexturePacker.process("../../AssetDevelopment/MyPuzzle/PackImages", "android/assets", "atlas");
    }
}
